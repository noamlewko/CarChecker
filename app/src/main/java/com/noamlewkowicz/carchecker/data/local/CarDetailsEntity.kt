package com.noamlewkowicz.carchecker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores the vehicle details for a previously looked-up license number,
 * so the app can show a result instantly even without a network connection.
 */
@Entity(tableName = "car_details")
data class CarDetailsEntity(
    @PrimaryKey
    val licenseNumber: String,
    val manufacturer: String,
    val color: String,
    val vehicleType: String,
    val hasDisabledBadge: Boolean,
    val lastUpdatedEpochMillis: Long
)
