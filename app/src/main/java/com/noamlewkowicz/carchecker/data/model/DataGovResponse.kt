package com.noamlewkowicz.carchecker.data.model

/**
 * Represents the common response structure returned by the DataGov CKAN API.
 */
data class DataGovResponse<T>(
    val success: Boolean,
    val result: DataGovResult<T>
)

/**
 * Contains the records returned for a DataGov resource query.
 */
data class DataGovResult<T>(
    val records: List<T>
)