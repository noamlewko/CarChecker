package com.noamlewkowicz.carchecker.viewmodel

import com.noamlewkowicz.carchecker.data.model.CarDetails

/**
 * Represents the possible states of the vehicle search.
 */
sealed interface CarCheckerUiState {

    data object Idle : CarCheckerUiState

    data object Loading : CarCheckerUiState

    data class Success(
        val carDetails: CarDetails
    ) : CarCheckerUiState

    data object NotFound : CarCheckerUiState

    data class Error(
        val message: String
    ) : CarCheckerUiState
}