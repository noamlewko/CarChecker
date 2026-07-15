package com.noamlewkowicz.carchecker.data.repository

import com.google.gson.Gson
import com.noamlewkowicz.carchecker.data.model.CarDetails
import com.noamlewkowicz.carchecker.data.network.DataGovApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Retrieves and combines vehicle information from the DataGov APIs.
 */
class CarRepository(
    private val apiService: DataGovApiService
) {

    suspend fun getCarDetails(licenseNumber: String): CarDetails =
        coroutineScope {
            val normalizedLicenseNumber =
                licenseNumber.filter(Char::isDigit)

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

    private companion object {
        const val VEHICLE_RESOURCE_ID =
            "053cea08-09bc-40ec-8f7a-156f0677aff3"

        const val DISABLED_BADGE_RESOURCE_ID =
            "c8b9f9c8-4612-4068-934f-d4acd2e3c06e"
    }
}