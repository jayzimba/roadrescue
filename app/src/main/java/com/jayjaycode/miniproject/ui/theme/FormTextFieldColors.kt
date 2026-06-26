package com.jayjaycode.miniproject.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val InputTextBlack = Color(0xFF1A1F36)
private val LabelMuted = Color(0xFF424242)
private val PlaceholderGray = Color(0xFF616161)
private val BorderLight = Color(0xFFBDBDBD)
private val ErrorRed = Color(0xFFB00020)

/**
 * Form field colors tuned for readability on light or dark surfaces.
 *
 * @param onLightSurface Pass `true` for fields on a white/light card (e.g. auth screens).
 * Defaults to light styling in light theme and dark-surface styling in dark theme.
 */
@Composable
fun formOutlinedTextFieldColors(
    onLightSurface: Boolean = !isSystemInDarkTheme(),
) = if (onLightSurface) {
    lightSurfaceFormColors()
} else {
    darkSurfaceFormColors()
}

@Composable
private fun lightSurfaceFormColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = InputTextBlack,
    unfocusedTextColor = InputTextBlack,
    disabledTextColor = InputTextBlack.copy(alpha = 0.38f),
    errorTextColor = InputTextBlack,
    focusedLabelColor = OrangePrimary,
    unfocusedLabelColor = LabelMuted,
    disabledLabelColor = LabelMuted.copy(alpha = 0.6f),
    errorLabelColor = ErrorRed,
    focusedPlaceholderColor = PlaceholderGray,
    unfocusedPlaceholderColor = PlaceholderGray,
    disabledPlaceholderColor = PlaceholderGray.copy(alpha = 0.5f),
    cursorColor = OrangePrimary,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = BorderLight,
    disabledBorderColor = BorderLight.copy(alpha = 0.6f),
    errorBorderColor = ErrorRed,
    focusedLeadingIconColor = OrangePrimary,
    unfocusedLeadingIconColor = OrangePrimary.copy(alpha = 0.85f),
    disabledLeadingIconColor = OrangePrimary.copy(alpha = 0.45f),
    focusedTrailingIconColor = LabelMuted,
    unfocusedTrailingIconColor = LabelMuted,
    disabledTrailingIconColor = LabelMuted.copy(alpha = 0.5f),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color(0xFFF0F1F5),
    errorContainerColor = Color.White,
)

@Composable
private fun darkSurfaceFormColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = SurfaceLight,
    unfocusedTextColor = SurfaceLight,
    disabledTextColor = SurfaceLight.copy(alpha = 0.45f),
    errorTextColor = SurfaceLight,
    focusedLabelColor = OrangePrimary,
    unfocusedLabelColor = Color(0xFFCDD3E3),
    disabledLabelColor = TextSecondary.copy(alpha = 0.55f),
    errorLabelColor = Color(0xFFFF8A80),
    focusedPlaceholderColor = TextSecondary,
    unfocusedPlaceholderColor = Color(0xFFA8AFC4),
    disabledPlaceholderColor = TextSecondary.copy(alpha = 0.45f),
    cursorColor = OrangePrimary,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = Color(0xFF5C6585),
    disabledBorderColor = Color(0xFF454D68),
    errorBorderColor = Color(0xFFFF8A80),
    focusedLeadingIconColor = OrangePrimary,
    unfocusedLeadingIconColor = OrangePrimary.copy(alpha = 0.9f),
    disabledLeadingIconColor = OrangePrimary.copy(alpha = 0.45f),
    focusedTrailingIconColor = TextSecondary,
    unfocusedTrailingIconColor = TextSecondary,
    disabledTrailingIconColor = TextSecondary.copy(alpha = 0.45f),
    focusedContainerColor = Color(0xFF2E3454),
    unfocusedContainerColor = Color(0xFF2A304C),
    disabledContainerColor = Color(0xFF232840),
    errorContainerColor = Color(0xFF2E3454),
)
