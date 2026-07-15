package com.noamlewkowicz.carchecker.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the vehicle fields required from the vehicle registry API.
 */
data class VehicleRecordDto(
    @SerializedName("mispar_rechev")
    val licenseNumber: Int,

    @SerializedName("tozeret_nm")
    val manufacturer: String?,

    @SerializedName("tzeva_rechev")
    val color: String?,

    @SerializedName("sug_degem")
    val vehicleType: String?
)