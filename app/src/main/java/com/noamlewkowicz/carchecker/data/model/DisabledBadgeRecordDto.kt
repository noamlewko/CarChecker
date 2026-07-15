package com.noamlewkowicz.carchecker.data.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a vehicle record found in the disabled parking badge registry.
 */
data class DisabledBadgeRecordDto(
    @SerializedName("MISPAR RECHEV")
    val licenseNumber: Int
)