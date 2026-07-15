package com.noamlewkowicz.carchecker

import com.noamlewkowicz.carchecker.data.local.CarDao
import com.noamlewkowicz.carchecker.data.local.CarDetailsEntity
import com.noamlewkowicz.carchecker.data.model.DataGovResponse
import com.noamlewkowicz.carchecker.data.model.DataGovResult
import com.noamlewkowicz.carchecker.data.model.DisabledBadgeRecordDto
import com.noamlewkowicz.carchecker.data.model.VehicleRecordDto
import com.noamlewkowicz.carchecker.data.network.DataGovApiService
import com.noamlewkowicz.carchecker.data.repository.CarRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Verifies how CarRepository combines vehicle and disabled badge responses.
 */
class CarRepositoryTest {

    @Test
    fun `getCarDetails combines vehicle and disabled badge information`() =
        runTest {
            val apiService = FakeDataGovApiService(
                vehicleRecords = listOf(
                    createVehicleRecord()
                ),
                disabledBadgeRecords = listOf(
                    DisabledBadgeRecordDto(
                        licenseNumber = TEST_LICENSE_NUMBER
                    )
                )
            )

            val repository = CarRepository(
                apiService = apiService
            )

            val result = repository.getCarDetails(
                licenseNumber = TEST_LICENSE_NUMBER.toString()
            )

            assertEquals(
                "Skoda",
                result.manufacturer
            )
            assertEquals(
                "Black",
                result.color
            )
            assertEquals(
                "P",
                result.vehicleType
            )
            assertTrue(
                result.hasDisabledBadge
            )
        }

    @Test
    fun `getCarDetails returns false when disabled badge is not found`() =
        runTest {
            val apiService = FakeDataGovApiService(
                vehicleRecords = listOf(
                    createVehicleRecord()
                ),
                disabledBadgeRecords = emptyList()
            )

            val repository = CarRepository(
                apiService = apiService
            )

            val result = repository.getCarDetails(
                licenseNumber = TEST_LICENSE_NUMBER.toString()
            )

            assertFalse(
                result.hasDisabledBadge
            )
        }

    @Test
    fun `getCarDetails throws when vehicle is not found`() =
        runTest {
            val apiService = FakeDataGovApiService(
                vehicleRecords = emptyList(),
                disabledBadgeRecords = emptyList()
            )

            val repository = CarRepository(
                apiService = apiService
            )

            try {
                repository.getCarDetails(
                    licenseNumber = TEST_LICENSE_NUMBER.toString()
                )

                fail(
                    "Expected NoSuchElementException to be thrown"
                )
            } catch (exception: NoSuchElementException) {
                assertEquals(
                    "Vehicle not found",
                    exception.message
                )
            }
        }

    @Test
    fun `getCarDetails returns cached vehicle without calling the network`() =
        runTest {
            val apiService = FakeDataGovApiService(
                vehicleRecords = listOf(
                    createVehicleRecord()
                ),
                disabledBadgeRecords = emptyList()
            )

            val carDao = FakeCarDao()
            carDao.upsert(
                CarDetailsEntity(
                    licenseNumber = TEST_LICENSE_NUMBER.toString(),
                    manufacturer = "Cached manufacturer",
                    color = "Cached color",
                    vehicleType = "Cached type",
                    hasDisabledBadge = true,
                    lastUpdatedEpochMillis = 0L
                )
            )

            val repository = CarRepository(
                apiService = apiService,
                carDao = carDao
            )

            val result = repository.getCarDetails(
                licenseNumber = TEST_LICENSE_NUMBER.toString()
            )

            assertEquals(
                "Cached manufacturer",
                result.manufacturer
            )
            assertEquals(
                0,
                apiService.vehicleCallCount
            )
        }

    @Test
    fun `getCarDetails caches the result after fetching from the network`() =
        runTest {
            val apiService = FakeDataGovApiService(
                vehicleRecords = listOf(
                    createVehicleRecord()
                ),
                disabledBadgeRecords = emptyList()
            )

            val carDao = FakeCarDao()

            val repository = CarRepository(
                apiService = apiService,
                carDao = carDao
            )

            val result = repository.getCarDetails(
                licenseNumber = TEST_LICENSE_NUMBER.toString()
            )

            assertEquals(
                "Skoda",
                result.manufacturer
            )
            assertEquals(
                1,
                apiService.vehicleCallCount
            )

            val cachedVehicle = carDao.getByLicenseNumber(
                licenseNumber = TEST_LICENSE_NUMBER.toString()
            )

            assertEquals(
                "Skoda",
                cachedVehicle?.manufacturer
            )
        }

    @Test
    fun `refreshCachedVehicles updates every cached vehicle from the network`() =
        runTest {
            val apiService = FakeDataGovApiService(
                vehicleRecords = listOf(
                    createVehicleRecord()
                ),
                disabledBadgeRecords = emptyList()
            )

            val carDao = FakeCarDao()
            carDao.upsert(
                CarDetailsEntity(
                    licenseNumber = TEST_LICENSE_NUMBER.toString(),
                    manufacturer = "Outdated manufacturer",
                    color = "Outdated color",
                    vehicleType = "Outdated type",
                    hasDisabledBadge = false,
                    lastUpdatedEpochMillis = 0L
                )
            )

            val repository = CarRepository(
                apiService = apiService,
                carDao = carDao
            )

            repository.refreshCachedVehicles()

            val refreshedVehicle = carDao.getByLicenseNumber(
                licenseNumber = TEST_LICENSE_NUMBER.toString()
            )

            assertEquals(
                "Skoda",
                refreshedVehicle?.manufacturer
            )
            assertEquals(
                1,
                apiService.vehicleCallCount
            )
        }

    @Test
    fun `refreshCachedVehicles does nothing without a local cache`() =
        runTest {
            val apiService = FakeDataGovApiService(
                vehicleRecords = listOf(
                    createVehicleRecord()
                ),
                disabledBadgeRecords = emptyList()
            )

            val repository = CarRepository(
                apiService = apiService
            )

            repository.refreshCachedVehicles()

            assertEquals(
                0,
                apiService.vehicleCallCount
            )
        }

    /**
     * Creates a reusable vehicle response for repository tests.
     */
    private fun createVehicleRecord(): VehicleRecordDto {
        return VehicleRecordDto(
            licenseNumber = TEST_LICENSE_NUMBER,
            manufacturer = "Skoda",
            color = "Black",
            vehicleType = "P"
        )
    }

    private companion object {
        const val TEST_LICENSE_NUMBER = 28367902
    }
}

/**
 * Provides predictable API responses without performing real network calls.
 *
 * Tracks how many times [getVehicle] was called, so tests can verify the
 * network was skipped when a cached result should have been used instead.
 */
private class FakeDataGovApiService(
    private val vehicleRecords: List<VehicleRecordDto>,
    private val disabledBadgeRecords: List<DisabledBadgeRecordDto>
) : DataGovApiService {

    var vehicleCallCount = 0
        private set

    override suspend fun getVehicle(
        resourceId: String,
        filters: String,
        limit: Int
    ): DataGovResponse<VehicleRecordDto> {
        vehicleCallCount++

        return DataGovResponse(
            success = true,
            result = DataGovResult(
                records = vehicleRecords
            )
        )
    }

    override suspend fun getDisabledBadge(
        resourceId: String,
        filters: String,
        limit: Int
    ): DataGovResponse<DisabledBadgeRecordDto> {
        return DataGovResponse(
            success = true,
            result = DataGovResult(
                records = disabledBadgeRecords
            )
        )
    }
}

/**
 * An in-memory stand-in for [CarDao], used so repository tests can verify
 * caching behavior without touching a real database.
 */
private class FakeCarDao : CarDao {

    private val storage = mutableMapOf<String, CarDetailsEntity>()

    override suspend fun upsert(carDetails: CarDetailsEntity) {
        storage[carDetails.licenseNumber] = carDetails
    }

    override suspend fun getByLicenseNumber(
        licenseNumber: String
    ): CarDetailsEntity? {
        return storage[licenseNumber]
    }

    override suspend fun getAll(): List<CarDetailsEntity> {
        return storage.values.toList()
    }
}