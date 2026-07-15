package com.noamlewkowicz.carchecker

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
 */
private class FakeDataGovApiService(
    private val vehicleRecords: List<VehicleRecordDto>,
    private val disabledBadgeRecords: List<DisabledBadgeRecordDto>
) : DataGovApiService {

    override suspend fun getVehicle(
        resourceId: String,
        filters: String,
        limit: Int
    ): DataGovResponse<VehicleRecordDto> {
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