package com.noamlewkowicz.carchecker.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noamlewkowicz.carchecker.data.model.CarDetails
import com.noamlewkowicz.carchecker.viewmodel.CarCheckerUiState
import com.noamlewkowicz.carchecker.viewmodel.CarCheckerViewModel

/**
 * Connects the screen to its ViewModel and collects lifecycle-aware state.
 */
@Composable
fun CarCheckerRoute(
    viewModel: CarCheckerViewModel = viewModel()
) {
    val licenseNumber by
    viewModel.licenseNumber.collectAsStateWithLifecycle()

    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    CarCheckerScreen(
        licenseNumber = licenseNumber,
        uiState = uiState,
        onLicenseNumberChange = viewModel::onLicenseNumberChanged
    )
}

/**
 * Displays the complete Car Checker screen.
 *
 * This composable receives state and user actions as parameters, which keeps
 * it independent from the ViewModel and easier to test.
 */
@Composable
fun CarCheckerScreen(
    licenseNumber: String,
    uiState: CarCheckerUiState,
    onLicenseNumberChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 24.dp,
                vertical = 32.dp
            ),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Car Checker",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter a license number to view vehicle information",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "License number",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = licenseNumber,
            onValueChange = onLicenseNumberChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "17-512-78",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        SearchResultContent(uiState = uiState)
    }
}

/**
 * Displays content that matches the current search state.
 */
@Composable
private fun SearchResultContent(
    uiState: CarCheckerUiState
) {
    when (uiState) {
        CarCheckerUiState.Idle -> {
            StatusCard(
                title = "Ready to search",
                message = "Enter a valid 7 or 8 digit license number."
            )
        }

        CarCheckerUiState.Loading -> {
            LoadingContent()
        }

        is CarCheckerUiState.Success -> {
            VehicleResultCard(
                carDetails = uiState.carDetails
            )
        }

        CarCheckerUiState.NotFound -> {
            StatusCard(
                title = "Vehicle not found",
                message = "No vehicle was found for this license number."
            )
        }

        is CarCheckerUiState.Error -> {
            StatusCard(
                title = "Unable to complete the search",
                message = uiState.message
            )
        }
    }
}

/**
 * Displays an indeterminate loading indicator while both requests are running.
 */
@Composable
private fun LoadingContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Checking vehicle details...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Displays the vehicle information combined from both DataGov resources.
 */
@Composable
private fun VehicleResultCard(
    carDetails: CarDetails
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Vehicle information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            VehicleDetailRow(
                label = "Manufacturer",
                value = carDetails.manufacturer.ifBlank {
                    "Unknown"
                }
            )

            VehicleDetailRow(
                label = "Color",
                value = carDetails.color.ifBlank {
                    "Unknown"
                }
            )

            VehicleDetailRow(
                label = "Vehicle type",
                value = carDetails.vehicleType.ifBlank {
                    "Unknown"
                }
            )

            VehicleDetailRow(
                label = "Disabled badge",
                value = if (carDetails.hasDisabledBadge) {
                    "Yes"
                } else {
                    "No"
                }
            )
        }
    }
}

/**
 * Displays one label-value pair inside the result card.
 */
@Composable
private fun VehicleDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

/**
 * Displays an informational, not-found, or error message.
 */
@Composable
private fun StatusCard(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}