package com.noamlewkowicz.carchecker.data.network

import com.noamlewkowicz.carchecker.data.model.DataGovResponse
import com.noamlewkowicz.carchecker.data.model.DisabledBadgeRecordDto
import com.noamlewkowicz.carchecker.data.model.VehicleRecordDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Defines the DataGov network requests used by the application.
 *
 * Retrofit generates the implementation of this interface at runtime.
 */
interface DataGovApiService {

    @GET("api/3/action/datastore_search")
    suspend fun getVehicle(
        @Query("resource_id") resourceId: String,
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): DataGovResponse<VehicleRecordDto>

    @GET("api/3/action/datastore_search")
    suspend fun getDisabledBadge(
        @Query("resource_id") resourceId: String,
        @Query("filters") filters: String,
        @Query("limit") limit: Int = 1
    ): DataGovResponse<DisabledBadgeRecordDto>
}
