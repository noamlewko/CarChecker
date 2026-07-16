package com.noamlewkowicz.carchecker

import com.noamlewkowicz.carchecker.data.model.DataGovResponse
import com.noamlewkowicz.carchecker.data.model.DataGovResult
import com.noamlewkowicz.carchecker.data.model.DisabledBadgeRecordDto
import com.noamlewkowicz.carchecker.data.model.VehicleRecordDto
import com.noamlewkowicz.carchecker.data.network.DataGovApiService
import com.noamlewkowicz.carchecker.data.repository.CarRepository
import com.noamlewkowicz.carchecker.viewmodel.CarCheckerUiState
import com.noamlewkowicz.carchecker.viewmodel.CarCheckerViewModel
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verifies license number input sanitization, and the debounced search
 * flow (including cancellation of a stale in-flight search) in
 * CarCheckerViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CarCheckerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        // viewModelScope requires a Main dispatcher; the test dispatcher's
        // virtual clock is what advanceTimeBy/advanceUntilIdle control below.
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `unsupported characters are removed`() {
        val viewModel = CarCheckerViewModel()

        viewModel.onLicenseNumberChanged("28A-36B7-902")

        assertEquals(
            "28-367-902",
            viewModel.licenseNumber.value
        )
    }

    @Test
    fun `input is limited to eight digits`() {
        val viewModel = CarCheckerViewModel()

        viewModel.onLicenseNumberChanged("123456789")

        assertEquals(
            "12-345-678",
            viewModel.licenseNumber.value
        )
    }

    @Test
    fun `dashes are formatted like a real license plate`() {
        val viewModel = CarCheckerViewModel()

        viewModel.onLicenseNumberChanged("28367902")

        assertEquals(
            "28-367-902",
            viewModel.licenseNumber.value
        )
    }

    @Test
    fun `dashes appear progressively while typing`() {
        val viewModel = CarCheckerViewModel()

        viewModel.onLicenseNumberChanged("28367")

        assertEquals(
            "28-367",
            viewModel.licenseNumber.value
        )
    }

    @Test
    fun `rapid typing only triggers one network search after the user pauses`() =
        runTest(testDispatcher) {
            val apiService = SequencedFakeApiService()

            val viewModel = CarCheckerViewModel(
                repository = CarRepository(apiService = apiService)
            )

            // Each keystroke arrives before the previous debounce window
            // (500ms) elapses, so none of them should trigger a search on
            // their own.
            viewModel.onLicenseNumberChanged("2")
            testDispatcher.scheduler.advanceTimeBy(100)
            viewModel.onLicenseNumberChanged("28")
            testDispatcher.scheduler.advanceTimeBy(100)
            viewModel.onLicenseNumberChanged("283679")
            testDispatcher.scheduler.advanceTimeBy(100)
            viewModel.onLicenseNumberChanged("28367902")

            advanceUntilIdle()

            assertEquals(1, apiService.callCount)
            assertTrue(viewModel.uiState.value is CarCheckerUiState.Success)
        }

    @Test
    fun `typing a new number cancels the still-loading search for the previous one`() =
        runTest(testDispatcher) {
            // The first search is deliberately slow; the second is instant.
            // If the first search were not cancelled, its late response
            // would eventually overwrite the second search's correct result.
            val apiService = SequencedFakeApiService(
                delaysMillis = listOf(10_000L, 0L)
            )

            val viewModel = CarCheckerViewModel(
                repository = CarRepository(apiService = apiService)
            )

            viewModel.onLicenseNumberChanged("28367902")
            testDispatcher.scheduler.advanceTimeBy(600)

            // The first search is now in flight, suspended inside the fake
            // network call's 10-second delay.
            viewModel.onLicenseNumberChanged("11122233")
            advanceUntilIdle()

            val finalState = viewModel.uiState.value
            assertTrue(finalState is CarCheckerUiState.Success)
            assertEquals(
                "Second search manufacturer",
                (finalState as CarCheckerUiState.Success).carDetails.manufacturer
            )
        }
}

/**
 * Returns a different, delayed response on each successive call to
 * [getVehicle], so tests can simulate a slow first search followed by a
 * fast second search and verify that only the latest search's result is
 * ever applied.
 */
private class SequencedFakeApiService(
    private val delaysMillis: List<Long> = emptyList()
) : DataGovApiService {

    var callCount = 0
        private set

    override suspend fun getVehicle(
        resourceId: String,
        filters: String,
        limit: Int
    ): DataGovResponse<VehicleRecordDto> {
        val callIndex = callCount
        callCount++

        val delayMillis = delaysMillis.getOrElse(callIndex) { 0L }
        if (delayMillis > 0) {
            delay(delayMillis.milliseconds)
        }

        return DataGovResponse(
            success = true,
            result = DataGovResult(
                records = listOf(
                    VehicleRecordDto(
                        licenseNumber = 0,
                        manufacturer = if (callIndex == 0) {
                            "First search manufacturer"
                        } else {
                            "Second search manufacturer"
                        },
                        color = "Black",
                        vehicleType = "P"
                    )
                )
            )
        )
    }

    override suspend fun getDisabledBadge(
        resourceId: String,
        filters: String,
        limit: Int
    ): DataGovResponse<DisabledBadgeRecordDto> {
        return DataGovResponse(
            success = true,
            result = DataGovResult(records = emptyList())
        )
    }
}