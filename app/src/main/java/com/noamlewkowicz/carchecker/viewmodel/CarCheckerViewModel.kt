package com.noamlewkowicz.carchecker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noamlewkowicz.carchecker.data.network.RetrofitClient
import com.noamlewkowicz.carchecker.data.repository.CarRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Manages the input and UI state for the Car Checker screen.
 *
 * The ViewModel observes license number changes, waits until the user pauses
 * typing, and requests the combined vehicle information from the repository.
 */
@OptIn(FlowPreview::class)
class CarCheckerViewModel(
    private val repository: CarRepository =
        CarRepository(RetrofitClient.apiService)
) : ViewModel() {

    private val _licenseNumber = MutableStateFlow("")

    /**
     * Exposes the current license number as read-only state.
     */
    val licenseNumber: StateFlow<String> =
        _licenseNumber.asStateFlow()

    private val _uiState =
        MutableStateFlow<CarCheckerUiState>(
            CarCheckerUiState.Idle
        )

    /**
     * Exposes the current search state as read-only state.
     */
    val uiState: StateFlow<CarCheckerUiState> =
        _uiState.asStateFlow()

    init {
        observeLicenseNumber()
    }

    /**
     * Updates the license number while allowing only digits and separators.
     */
    fun onLicenseNumberChanged(value: String) {
        val sanitizedValue = buildString {
            var digitCount = 0

            value.forEach { character ->
                when {
                    character.isDigit() && digitCount < MAX_LICENSE_DIGITS -> {
                        append(character)
                        digitCount++
                    }

                    character == '-' -> {
                        append(character)
                    }
                }
            }
        }

        _licenseNumber.value = sanitizedValue
    }

    /**
     * Observes input changes and starts a search after the user pauses typing.
     */
    private fun observeLicenseNumber() {
        viewModelScope.launch {
            _licenseNumber
                .map { value ->
                    value.filter(Char::isDigit)
                }
                // Avoid sending a network request after every typed character.
                .debounce(SEARCH_DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                // Cancel the previous search when a newer input value arrives.
                .collectLatest { normalizedLicenseNumber ->
                    if (normalizedLicenseNumber.isValidLicenseNumber()) {
                        searchCar(normalizedLicenseNumber)
                    } else {
                        _uiState.value = CarCheckerUiState.Idle
                    }
                }
        }
    }

    /**
     * Retrieves the vehicle details and converts the result into a UI state.
     */
    private suspend fun searchCar(
        normalizedLicenseNumber: String
    ) {
        _uiState.value = CarCheckerUiState.Loading

        _uiState.value = try {
            val carDetails =
                repository.getCarDetails(normalizedLicenseNumber)

            CarCheckerUiState.Success(
                carDetails = carDetails
            )
        } catch (_: NoSuchElementException) {
            CarCheckerUiState.NotFound
        } catch (_: IOException) {
            CarCheckerUiState.Error(
                message = "Unable to connect to the server."
            )
        } catch (_: HttpException) {
            CarCheckerUiState.Error(
                message = "The server returned an unexpected response."
            )
        } catch (exception: CancellationException) {
            // Cancellation is expected when the user enters a newer number.
            throw exception
        } catch (_: Exception) {
            CarCheckerUiState.Error(
                message = "Something went wrong. Please try again."
            )
        }
    }

    /**
     * Checks whether the normalized value contains seven or eight digits.
     */
    private fun String.isValidLicenseNumber(): Boolean {
        return length == 7 || length == 8
    }


    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 500L
        const val MAX_LICENSE_DIGITS = 8
    }
}