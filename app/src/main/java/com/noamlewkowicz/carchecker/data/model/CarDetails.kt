package com.noamlewkowicz.carchecker.data.model

/**
 * Represents the combined vehicle information displayed by the application.
 */
data class CarDetails(
    val manufacturer: String,
    val color: String,
    val vehicleType: String,
    val hasDisabledBadge: Boolean
)