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

object TrackrColors {
    val Blue500 = Color(0xFF364F6B)
    val Blue700 = Color(0xFF1C2A3A)
    val Pink400 = Color(0xFFFF85AA)
    val Pink600 = Color(0xFFE9678E)
}

private val DarkColorPalette = with(TrackrColors) {
    darkColors(
        primary = Blue700,
        primaryVariant = Blue700,
        secondary = Pink400
    )
}

private val LightColorPalette = with(TrackrColors) {
    lightColors(
        primary = Blue500,
        primaryVariant = Blue700,
        secondary = Pink600,
        background = Blue500
    )
}

@Composable
fun TrackrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) {
            DarkColorPalette
        } else {
            LightColorPalette
        },
        shapes = Shapes(
            medium = RoundedCornerShape(8.dp),
        ),
        content = content
    )
}
