package com.noamlewkowicz.carchecker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noamlewkowicz.carchecker.R

/**
 * Width reserved for the search icon slot on the end side of the number
 * area, mirrored by an equal empty slot on the start side so the digits
 * stay visually centered.
 */
private val ICON_SLOT_WIDTH = 34.dp

/**
 * Displays an editable field styled as an Israeli license plate.
 *
 * The field is forced to a left-to-right layout regardless of the app's
 * Hebrew (right-to-left) locale, since a license number always reads left
 * to right, the same way it does on a real plate. Without this, digits
 * would appear to type in reverse order under a right-to-left locale.
 */
@Composable
fun LicensePlateTextField(
    value: String,
    onValueChange: (String) -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr
    ) {
        LicensePlateTextFieldContent(
            value = value,
            onValueChange = onValueChange
        )
    }
}

@Composable
private fun LicensePlateTextFieldContent(
    value: String,
    onValueChange: (String) -> Unit
) {
    // Built fresh on every recomposition with the cursor forced to the end.
    // The plain-String overload of BasicTextField tries to preserve the
    // cursor position by diffing old and new text, which goes wrong once
    // dashes are inserted automatically and misplaces new digits. Always
    // placing the cursor at the end avoids that entirely.
    val textFieldValue = TextFieldValue(
        text = value,
        selection = TextRange(value.length)
    )

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue -> onValueChange(newValue.text) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        textStyle = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            textDirection = TextDirection.Ltr,
            letterSpacing = 1.sp
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFCC00))
                    .border(
                        width = 2.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(12.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .width(60.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF0038A8)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🇮🇱",
                        fontSize = 18.sp
                    )

                    Text(
                        text = stringResource(R.string.license_plate_country_code),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }

                // The search icon sits in its own fixed-width slot on the
                // end side, matched by an equal empty slot on the start
                // side. Without this, the icon's visual weight pulls the
                // centered digits off-center even though they are
                // mathematically centered in the full row.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(ICON_SLOT_WIDTH))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = stringResource(R.string.license_number_placeholder),
                                color = Color.Black.copy(alpha = 0.45f),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp
                            )
                        }

                        innerTextField()
                    }

                    Box(
                        modifier = Modifier
                            .width(ICON_SLOT_WIDTH)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black.copy(alpha = 0.35f)
                        )
                    }
                }
            }
        }
    )
}