package com.noamlewkowicz.carchecker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
     * Updates the license number, keeping only digits and automatically
     * inserting dashes in the same positions as a real Israeli license
     * plate (2 digits, 3 digits, then the rest), so the field looks like an
     * actual plate as the user types.
     */
    fun onLicenseNumberChanged(value: String) {
        val digitsOnly = value
            .filter(Char::isDigit)
            .take(MAX_LICENSE_DIGITS)

        _licenseNumber.value = digitsOnly.formatAsLicensePlate()
    }

    /**
     * Repeats the last search for the current license number. Used when the
     * user taps "Try again" after a failed search, so they do not have to
     * retype the number just to trigger a new attempt.
     */
    fun retrySearch() {
        val normalizedLicenseNumber =
            _licenseNumber.value.filter(Char::isDigit)

        if (normalizedLicenseNumber.isValidLicenseNumber()) {
            viewModelScope.launch {
                searchCar(normalizedLicenseNumber)
            }
        }
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

    /**
     * Inserts dashes after the second and fifth digit, matching the layout
     * of a real Israeli license plate (for example "28-367-902").
     */
    private fun String.formatAsLicensePlate(): String {
        val digits = this

        return buildString {
            digits.forEachIndexed { index, character ->
                if (index == 2 || index == 5) {
                    append('-')
                }

                append(character)
            }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 500L
        const val MAX_LICENSE_DIGITS = 8
    }
}

/**
 * Builds a [CarCheckerViewModel] backed by the local cache, so that
 * previously searched vehicles are available offline. Kept outside the
 * class so the class itself still has a simple, context-free default
 * constructor for tests and previews.
 */
fun carCheckerViewModelFactory(context: Context): ViewModelProvider.Factory =
    viewModelFactory {
        initializer {
            CarCheckerViewModel(
                repository = CarRepository.createOfflineFirst(context)
            )
        }
    }