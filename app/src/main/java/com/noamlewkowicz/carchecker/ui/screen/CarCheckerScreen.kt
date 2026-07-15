package com.noamlewkowicz.carchecker.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noamlewkowicz.carchecker.R
import com.noamlewkowicz.carchecker.data.model.CarDetails
import com.noamlewkowicz.carchecker.ui.components.CarCheckerHeader
import com.noamlewkowicz.carchecker.ui.components.LicensePlateTextField
import com.noamlewkowicz.carchecker.viewmodel.CarCheckerUiState
import com.noamlewkowicz.carchecker.viewmodel.CarCheckerViewModel
import com.noamlewkowicz.carchecker.viewmodel.carCheckerViewModelFactory

/**
 * Connects the screen to its ViewModel and collects lifecycle-aware state.
 */
@Composable
fun CarCheckerRoute(
    viewModel: CarCheckerViewModel = viewModel(
        factory = carCheckerViewModelFactory(
            context = LocalContext.current.applicationContext
        )
    )
) {
    val licenseNumber by
    viewModel.licenseNumber.collectAsStateWithLifecycle()

    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    CarCheckerScreen(
        licenseNumber = licenseNumber,
        uiState = uiState,
        onLicenseNumberChange = viewModel::onLicenseNumberChanged,
        onRetry = viewModel::retrySearch
    )
}

/**
 * Displays the complete Car Checker screen.
 *
 * The composable receives its state and callbacks as parameters, keeping it
 * independent of the ViewModel and easier to preview and test.
 */
@Composable
fun CarCheckerScreen(
    licenseNumber: String,
    uiState: CarCheckerUiState,
    onLicenseNumberChange: (String) -> Unit,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FB))
    ) {
        CarCheckerHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 20.dp,
                    vertical = 24.dp
                )
        ) {
            LicenseNumberSection(
                licenseNumber = licenseNumber,
                onLicenseNumberChange = onLicenseNumberChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            SearchResultContent(
                licenseNumber = licenseNumber,
                uiState = uiState,
                onRetry = onRetry
            )
        }
    }
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
        text = stringResource(R.string.license_number_label),
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
    licenseNumber: String,
    uiState: CarCheckerUiState,
    onRetry: () -> Unit
) {
    // Crossfades between states instead of popping in abruptly.
    AnimatedContent(
        targetState = uiState,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith
                fadeOut(animationSpec = tween(220))
        },
        label = "search_result_content"
    ) { targetUiState ->
        when (targetUiState) {
            CarCheckerUiState.Idle -> {
                val typedDigitCount =
                    licenseNumber.count(Char::isDigit)

                if (typedDigitCount in 1 until MIN_LICENSE_DIGITS) {
                    StatusCard(
                        title = stringResource(R.string.status_typing_title),
                        message = stringResource(
                            R.string.status_typing_message,
                            MIN_LICENSE_DIGITS - typedDigitCount
                        ),
                        emphasis = StatusEmphasis.Neutral
                    )
                } else {
                    StatusCard(
                        title = stringResource(R.string.status_idle_title),
                        message = stringResource(R.string.status_idle_message),
                        emphasis = StatusEmphasis.Neutral
                    )
                }
            }

            CarCheckerUiState.Loading -> {
                LoadingContent()
            }

            is CarCheckerUiState.Success -> {
                VehicleResultCard(
                    carDetails = targetUiState.carDetails
                )
            }

            CarCheckerUiState.NotFound -> {
                StatusCard(
                    title = stringResource(R.string.status_not_found_title),
                    message = stringResource(R.string.status_not_found_message),
                    emphasis = StatusEmphasis.Warning
                )
            }

            is CarCheckerUiState.Error -> {
                StatusCard(
                    title = stringResource(R.string.status_error_title),
                    message = targetUiState.message,
                    emphasis = StatusEmphasis.Error,
                    onRetry = onRetry
                )
            }
        }
    }
}

private const val MIN_LICENSE_DIGITS = 7

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
                text = stringResource(R.string.loading_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.loading_message),
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
    // A very subtle blue tint hints that this vehicle has a disabled
    // parking badge. The badge itself and its "Yes" pill stay green, since
    // green is what communicates the positive answer.
    val containerColor = if (carDetails.hasDisabledBadge) {
        Color(0xFFF6F9FD)
    } else {
        Color.White
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
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
                        text = stringResource(R.string.result_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.result_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusPill(
                    text = stringResource(R.string.status_pill_found),
                    positive = true
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            val unknownValue = stringResource(R.string.unknown_value)

            ManufacturerRow(
                manufacturerName = carDetails.manufacturer.ifBlank { unknownValue }
            )

            VehicleColorRow(
                colorName = carDetails.color.ifBlank { unknownValue }
            )

            VehicleDetailRow(
                icon = Icons.Rounded.DirectionsCar,
                label = stringResource(R.string.label_vehicle_type),
                value = carDetails.vehicleType.toDisplayVehicleType(
                    privateLabel = stringResource(R.string.vehicle_type_private),
                    commercialLabel = stringResource(R.string.vehicle_type_commercial),
                    unknownLabel = unknownValue
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            DisabledBadgeSection(
                hasDisabledBadge = carDetails.hasDisabledBadge
            )
        }
    }
}

/**
 * Displays one vehicle detail as a badge next to a label and value.
 *
 * Shared by every detail row so the label/value layout is defined once;
 * only the badge itself (an icon, a monogram, or a color swatch) differs
 * between rows.
 */
@Composable
private fun DetailRow(
    label: String,
    value: String,
    badge: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        badge()

        Column(
            modifier = Modifier.padding(start = 14.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * A 42dp rounded badge with a plain background, used to frame an icon or a
 * monogram. The color swatch badge does not use this, since for that row
 * the badge's entire background is the answer.
 */
@Composable
private fun IconBadge(
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = RoundedCornerShape(13.dp),
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * Displays one vehicle detail with an identifying icon.
 */
@Composable
private fun VehicleDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    DetailRow(
        label = label,
        value = value
    ) {
        IconBadge {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Displays the manufacturer with a monogram badge showing its first letter
 * on a color derived from the manufacturer's name, so different brands are
 * visually distinguishable from one another.
 *
 * A stylized monogram is used instead of a specific brand logo, since
 * reproducing real manufacturer trademarks is avoided.
 */
@Composable
private fun ManufacturerRow(
    manufacturerName: String
) {
    DetailRow(
        label = stringResource(R.string.label_manufacturer),
        value = manufacturerName
    ) {
        IconBadge(
            backgroundColor = manufacturerName.toManufacturerColor()
        ) {
            Text(
                text = manufacturerName.trim().firstOrNull()
                    ?.uppercase()
                    .orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

/**
 * Picks a consistent color for a manufacturer name from a small palette, so
 * the same manufacturer always gets the same badge color across searches.
 */
private fun String.toManufacturerColor(): Color {
    if (isBlank()) {
        return Color(0xFF9DA4AE)
    }

    val paletteIndex = (hashCode().mod(MANUFACTURER_BADGE_COLORS.size))

    return MANUFACTURER_BADGE_COLORS[paletteIndex]
}

private val MANUFACTURER_BADGE_COLORS = listOf(
    Color(0xFF3B5BDB),
    Color(0xFFE8590C),
    Color(0xFF2F9E44),
    Color(0xFFAE3EC9),
    Color(0xFF1098AD),
    Color(0xFFE64980),
    Color(0xFF5C940D),
    Color(0xFFD9480F)
)

/**
 * Displays the vehicle color as a swatch: the whole badge is filled with
 * the actual color, the same way the manufacturer badge shows a letter and
 * the vehicle type badge shows an icon, so the badge itself is the answer.
 */
@Composable
private fun VehicleColorRow(
    colorName: String
) {
    DetailRow(
        label = stringResource(R.string.label_color),
        value = colorName
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(colorName.toVehicleDisplayColor())
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(13.dp)
                )
        )
    }
}

/**
 * Maps the API vehicle color description to a representative UI color.
 */
private fun String.toVehicleDisplayColor(): Color {
    val normalizedColor = lowercase()

    return when {
        "שחור" in normalizedColor ||
                "black" in normalizedColor -> Color(0xFF202124)

        "לבן" in normalizedColor ||
                "white" in normalizedColor -> Color.White

        "כסף" in normalizedColor ||
                "כסוף" in normalizedColor ||
                "silver" in normalizedColor -> Color(0xFFB7BDC5)

        "אפור" in normalizedColor ||
                "gray" in normalizedColor ||
                "grey" in normalizedColor -> Color(0xFF747B85)

        "כחול" in normalizedColor ||
                "blue" in normalizedColor -> Color(0xFF3267B2)

        "אדום" in normalizedColor ||
                "red" in normalizedColor -> Color(0xFFC63D42)

        "ירוק" in normalizedColor ||
                "green" in normalizedColor -> Color(0xFF3D7B58)

        "צהוב" in normalizedColor ||
                "yellow" in normalizedColor -> Color(0xFFF4CF45)

        "כתום" in normalizedColor ||
                "orange" in normalizedColor -> Color(0xFFE68238)

        "חום" in normalizedColor ||
                "brown" in normalizedColor -> Color(0xFF795548)

        else -> Color(0xFF9DA4AE)
    }
}

/**
 * Displays the disabled parking badge result as a prominent status row.
 */
@Composable
private fun DisabledBadgeSection(
    hasDisabledBadge: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (hasDisabledBadge) {
                    Color(0xFFE9F9EC)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "♿",
                style = MaterialTheme.typography.headlineSmall
            )

            Column(
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Text(
                    text = stringResource(R.string.disabled_badge_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (hasDisabledBadge) {
                        stringResource(R.string.disabled_badge_present)
                    } else {
                        stringResource(R.string.disabled_badge_absent)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        StatusPill(
            text = if (hasDisabledBadge) {
                stringResource(R.string.answer_yes)
            } else {
                stringResource(R.string.answer_no)
            },
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
            Color(0xFF22C55E)
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
 * Displays an informational, not-found, or error message. When [onRetry] is
 * provided, a "Try again" button is shown below the message.
 */
@Composable
private fun StatusCard(
    title: String,
    message: String,
    emphasis: StatusEmphasis,
    onRetry: (() -> Unit)? = null
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

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onRetry) {
                    Text(text = stringResource(R.string.retry_button_label))
                }
            }
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

/**
 * Converts the API vehicle type code ("P"/"C") into a user-friendly label.
 */
private fun String.toDisplayVehicleType(
    privateLabel: String,
    commercialLabel: String,
    unknownLabel: String
): String {
    return when (uppercase()) {
        "P" -> privateLabel
        "C" -> commercialLabel
        else -> ifBlank { unknownLabel }
    }
}