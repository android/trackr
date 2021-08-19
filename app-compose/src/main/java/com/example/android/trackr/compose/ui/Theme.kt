/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackr.compose.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.trackr.data.TagColor

private val Blue100 = Color(0xFFDDE3E9)
private val Blue500 = Color(0xFF364F6B)
private val Blue700 = Color(0xFF1C2A3A)

private val Green100 = Color(0xFFE6F4EA)
private val Green200 = Color(0xFFCEEAD6)
private val Green700 = Color(0xFF137333)
private val Green800 = Color(0xFF37694E)

private val Pink400 = Color(0xFFFF85AA)
private val Pink600 = Color(0xFFE9678E)
private val Pink800 = Color(0xFF843B6F)

private val Purple100 = Color(0xFFF3E8FD)
private val Purple200 = Color(0xFFE9D2FD)
private val Purple700 = Color(0xFF7627BB)
private val Purple800 = Color(0xFF5B3691)

private val Red100 = Color(0xFFFDE7F3)
private val Red700 = Color(0xFFB80672)

private val Teal100 = Color(0xFFCBF0F8)
private val Teal800 = Color(0xFF09536B)

private val White50 = Color(0xFFFFFFFF)

private val Yellow100 = Color(0xFFFFFDCF)
private val Yellow300 = Color(0xFFF3BB59)
private val Yellow500 = Color(0xFF9B6303)
private val Yellow700 = Color(0xFF824A00)

private val DarkColorPalette = darkColors(
    surface = Blue700,
    background = Blue700,
    secondary = Pink400,
)

private val LightColorPalette = lightColors(
    primary = Blue500,
    primaryVariant = Blue700,
    secondary = Pink600,
    background = Blue500
)

private val DarkTrackrColors = TrackrColors(
    star = Yellow300,
    tagTexts = listOf(
        TagColor.BLUE to Blue100,
        TagColor.GREEN to Green200,
        TagColor.PURPLE to Purple200,
        TagColor.RED to White50,
        TagColor.TEAL to Teal100,
        TagColor.YELLOW to Yellow100,
    ),
    tagBackgrounds = listOf(
        TagColor.BLUE to Blue700,
        TagColor.GREEN to Green800,
        TagColor.PURPLE to Purple800,
        TagColor.RED to Pink800,
        TagColor.TEAL to Teal800,
        TagColor.YELLOW to Yellow700,
    ),
)

private val LightTrackrColors = TrackrColors(
    star = Yellow500,
    tagTexts = listOf(
        TagColor.BLUE to Blue700,
        TagColor.GREEN to Green700,
        TagColor.PURPLE to Purple700,
        TagColor.RED to Red700,
        TagColor.TEAL to Teal800,
        TagColor.YELLOW to Yellow700,
    ),
    tagBackgrounds = listOf(
        TagColor.BLUE to Blue100,
        TagColor.GREEN to Green100,
        TagColor.PURPLE to Purple100,
        TagColor.RED to Red100,
        TagColor.TEAL to Teal100,
        TagColor.YELLOW to Yellow100,
    ),
)

object TrackrTheme {

    /**
     * Extra theme colors.
     */
    val colors: TrackrColors
        @Composable
        get() = LocalTrackrColors.current
}

@Composable
fun TrackrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Allow the content to access the `TrackrTheme` object.
    ProvideTrackrColors(if (darkTheme) DarkTrackrColors else LightTrackrColors) {
        MaterialTheme(
            colors = if (darkTheme) DarkColorPalette else LightColorPalette,
            shapes = Shapes(
                medium = RoundedCornerShape(8.dp),
                small = RoundedCornerShape(8.dp),
            ),
            content = content
        )
    }
}
