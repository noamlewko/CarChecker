package com.noamlewkowicz.carchecker.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/**
 * Defines the local database operations for cached vehicle lookups.
 */
@Dao
interface CarDao {

    /**
     * Inserts a new cached vehicle, or replaces the existing one for the
     * same license number.
     */
    @Upsert
    suspend fun upsert(carDetails: CarDetailsEntity)

    /**
     * Returns the cached vehicle for the given license number, or null if
     * it was never searched before.
     */
    @Query("SELECT * FROM car_details WHERE licenseNumber = :licenseNumber")
    suspend fun getByLicenseNumber(licenseNumber: String): CarDetailsEntity?

    /**
     * Returns every vehicle that has been searched and cached so far.
     * Used by the daily background refresh.
     */
    @Query("SELECT * FROM car_details")
    suspend fun getAll(): List<CarDetailsEntity>
}
