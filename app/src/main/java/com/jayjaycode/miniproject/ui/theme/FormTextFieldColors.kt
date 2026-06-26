package com.jayjaycode.miniproject.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val InputTextBlack = Color(0xFF000000)
private val LabelMuted = Color(0xFF424242)
private val PlaceholderGray = Color(0xFF757575)

/**
 * Consistent form styling: typed text is always black for readability.
 */
@Composable
fun formOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = InputTextBlack,
    unfocusedTextColor = InputTextBlack,
    disabledTextColor = InputTextBlack.copy(alpha = 0.38f),
    errorTextColor = InputTextBlack,
    focusedLabelColor = OrangePrimary,
    unfocusedLabelColor = LabelMuted,
    disabledLabelColor = LabelMuted.copy(alpha = 0.6f),
    errorLabelColor = Color(0xFFB00020),
    focusedPlaceholderColor = PlaceholderGray,
    unfocusedPlaceholderColor = PlaceholderGray,
    disabledPlaceholderColor = PlaceholderGray.copy(alpha = 0.5f),
    cursorColor = OrangePrimary,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = Color(0xFFBDBDBD),
    disabledBorderColor = Color(0xFFE0E0E0),
    errorBorderColor = Color(0xFFB00020),
    focusedLeadingIconColor = OrangePrimary,
    unfocusedLeadingIconColor = OrangePrimary.copy(alpha = 0.8f),
    focusedTrailingIconColor = LabelMuted,
    unfocusedTrailingIconColor = LabelMuted,
)
