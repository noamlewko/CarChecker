package com.noamlewkowicz.carchecker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noamlewkowicz.carchecker.data.model.CarDetails
import com.noamlewkowicz.carchecker.viewmodel.CarCheckerUiState
import com.noamlewkowicz.carchecker.viewmodel.CarCheckerViewModel
import com.noamlewkowicz.carchecker.ui.components.LicensePlateTextField
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
 * The composable receives its state and callbacks as parameters, keeping it
 * independent from the ViewModel and easier to preview and test.
 */
@Composable
fun CarCheckerScreen(
    licenseNumber: String,
    uiState: CarCheckerUiState,
    onLicenseNumberChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surface
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = 28.dp
                ),
            verticalArrangement = Arrangement.Top
        ) {
            ScreenHeader()

            Spacer(modifier = Modifier.height(28.dp))

            LicenseNumberSection(
                licenseNumber = licenseNumber,
                onLicenseNumberChange = onLicenseNumberChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            SearchResultContent(
                uiState = uiState
            )
        }
    }
}

/**
 * Displays the screen title and a short explanation.
 */
@Composable
private fun ScreenHeader() {
    Text(
        text = "Car Checker",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = "Enter a license number to check vehicle information",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Displays the license number input in a license-plate-inspired container.
 */
@Composable
private fun LicenseNumberSection(
    licenseNumber: String,
    onLicenseNumberChange: (String) -> Unit
) {
    Text(
        text = "License number",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(10.dp))

    LicensePlateTextField(
        value = licenseNumber,
        onValueChange = onLicenseNumberChange
    )
}

/**
 * Displays content that matches the current vehicle search state.
 */
@Composable
private fun SearchResultContent(
    uiState: CarCheckerUiState
) {
    when (uiState) {
        CarCheckerUiState.Idle -> {
            StatusCard(
                title = "Ready to search",
                message = "Enter a valid 7 or 8 digit license number.",
                emphasis = StatusEmphasis.Neutral
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
                message = "No vehicle was found for this license number.",
                emphasis = StatusEmphasis.Warning
            )
        }

        is CarCheckerUiState.Error -> {
            StatusCard(
                title = "Unable to complete the search",
                message = uiState.message,
                emphasis = StatusEmphasis.Error
            )
        }
    }
}

/**
 * Displays an indeterminate indicator while both API requests are running.
 */
@Composable
private fun LoadingContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Retrieving vehicle and parking badge information",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Displays vehicle information combined from both DataGov resources.
 */
@Composable
private fun VehicleResultCard(
    carDetails: CarDetails
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 5.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Vehicle information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Data received successfully",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusPill(
                    text = "Found",
                    positive = true
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

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

            HorizontalDivider(
                modifier = Modifier.padding(
                    vertical = 10.dp
                ),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            DisabledBadgeSection(
                hasDisabledBadge =
                    carDetails.hasDisabledBadge
            )
        }
    }
}

/**
 * Displays one vehicle detail as a clearly separated label-value pair.
 */
@Composable
private fun VehicleDetailRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Displays the disabled parking badge result as a highlighted section.
 */
@Composable
private fun DisabledBadgeSection(
    hasDisabledBadge: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (hasDisabledBadge) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Disabled parking badge",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (hasDisabledBadge) {
                "A valid badge was found for this vehicle."
            } else {
                "No badge was found for this vehicle."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatusPill(
            text = if (hasDisabledBadge) "Yes" else "No",
            positive = hasDisabledBadge
        )
    }
}

/**
 * Displays a compact status label.
 */
@Composable
private fun StatusPill(
    text: String,
    positive: Boolean
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (positive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 7.dp
            ),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (positive) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    }
}

/**
 * Displays an informational, not-found, or error message.
 */
@Composable
private fun StatusCard(
    title: String,
    message: String,
    emphasis: StatusEmphasis
) {
    val borderColor = when (emphasis) {
        StatusEmphasis.Neutral ->
            MaterialTheme.colorScheme.outlineVariant

        StatusEmphasis.Warning ->
            MaterialTheme.colorScheme.tertiary

        StatusEmphasis.Error ->
            MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when (emphasis) {
                    StatusEmphasis.Error ->
                        MaterialTheme.colorScheme.error

                    else ->
                        MaterialTheme.colorScheme.onSurface
                }
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

/**
 * Defines the visual emphasis used by informational status cards.
 */
private enum class StatusEmphasis {
    Neutral,
    Warning,
    Error
}