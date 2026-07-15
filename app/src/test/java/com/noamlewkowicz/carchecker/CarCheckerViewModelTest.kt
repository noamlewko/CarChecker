package com.noamlewkowicz.carchecker

import com.noamlewkowicz.carchecker.viewmodel.CarCheckerViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies license number input sanitization in CarCheckerViewModel.
 */
class CarCheckerViewModelTest {

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
}