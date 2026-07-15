package com.noamlewkowicz.carchecker.data.repository

import android.content.Context
import com.google.gson.Gson
import com.noamlewkowicz.carchecker.data.local.CarDao
import com.noamlewkowicz.carchecker.data.local.CarDatabase
import com.noamlewkowicz.carchecker.data.local.CarDetailsEntity
import com.noamlewkowicz.carchecker.data.model.CarDetails
import com.noamlewkowicz.carchecker.data.network.DataGovApiService
import com.noamlewkowicz.carchecker.data.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Retrieves and combines vehicle information from the DataGov APIs.
 *
 * When [carDao] is provided, the repository is offline-first: a vehicle
 * that was already searched before is returned instantly from the local
 * cache, without a network call. Vehicles that were never searched are
 * still fetched from the network as before, and the result is cached for
 * next time. When [carDao] is null, the repository behaves exactly like a
 * network-only repository, which keeps existing tests and usages working
 * unchanged.
 */
class CarRepository(
    private val apiService: DataGovApiService,
    private val carDao: CarDao? = null
) {

    suspend fun getCarDetails(licenseNumber: String): CarDetails {
        val normalizedLicenseNumber =
            licenseNumber.filter(Char::isDigit)

        val cachedVehicle =
            carDao?.getByLicenseNumber(normalizedLicenseNumber)

        if (cachedVehicle != null) {
            return cachedVehicle.toCarDetails()
        }

        val carDetails = fetchFromNetwork(normalizedLicenseNumber)

        carDao?.upsert(
            carDetails.toEntity(
                licenseNumber = normalizedLicenseNumber
            )
        )

        return carDetails
    }

    /**
     * Re-fetches every vehicle already stored locally and refreshes the
     * cache with the latest data from the network. Requires [carDao] to
     * have been provided; used by the daily background refresh.
     */
    suspend fun refreshCachedVehicles() {
        val dao = carDao ?: return

        dao.getAll().forEach { cachedVehicle ->
            val refreshed = fetchFromNetwork(cachedVehicle.licenseNumber)

            dao.upsert(
                refreshed.toEntity(
                    licenseNumber = cachedVehicle.licenseNumber
                )
            )
        }
    }

    private suspend fun fetchFromNetwork(
        normalizedLicenseNumber: String
    ): CarDetails = coroutineScope {
        val numericLicenseNumber =
            normalizedLicenseNumber.toInt()

        val vehicleFilters = Gson().toJson(
            mapOf(
                "mispar_rechev" to numericLicenseNumber
            )
        )

        val disabledBadgeFilters = Gson().toJson(
            mapOf(
                "MISPAR RECHEV" to numericLicenseNumber
            )
        )

        // Both requests are independent, so they start concurrently.
        val vehicleRequest = async {
            apiService.getVehicle(
                resourceId = VEHICLE_RESOURCE_ID,
                filters = vehicleFilters
            )
        }

        val disabledBadgeRequest = async {
            apiService.getDisabledBadge(
                resourceId = DISABLED_BADGE_RESOURCE_ID,
                filters = disabledBadgeFilters
            )
        }

        val vehicleResponse = vehicleRequest.await()
        val disabledBadgeResponse = disabledBadgeRequest.await()

        val vehicle =
            vehicleResponse.result.records.firstOrNull()
                ?: throw NoSuchElementException(
                    "Vehicle not found"
                )

        CarDetails(
            manufacturer = vehicle.manufacturer.orEmpty(),
            color = vehicle.color.orEmpty(),
            vehicleType = vehicle.vehicleType.orEmpty(),
            hasDisabledBadge =
                disabledBadgeResponse.result.records.isNotEmpty()
        )
    }

    private fun CarDetailsEntity.toCarDetails() = CarDetails(
        manufacturer = manufacturer,
        color = color,
        vehicleType = vehicleType,
        hasDisabledBadge = hasDisabledBadge
    )

    private fun CarDetails.toEntity(licenseNumber: String) =
        CarDetailsEntity(
            licenseNumber = licenseNumber,
            manufacturer = manufacturer,
            color = color,
            vehicleType = vehicleType,
            hasDisabledBadge = hasDisabledBadge,
            lastUpdatedEpochMillis = System.currentTimeMillis()
        )

    companion object {
        private const val VEHICLE_RESOURCE_ID =
            "053cea08-09bc-40ec-8f7a-156f0677aff3"

        private const val DISABLED_BADGE_RESOURCE_ID =
            "c8b9f9c8-4612-4068-934f-d4acd2e3c06e"

        /**
         * Builds a repository wired to the real network client and local
         * database. Used by both the ViewModel factory and the background
         * sync worker, so this wiring only needs to be written once.
         */
        fun createOfflineFirst(context: Context): CarRepository {
            return CarRepository(
                apiService = RetrofitClient.apiService,
                carDao = CarDatabase.getInstance(context).carDao()
            )
        }
    }
}
