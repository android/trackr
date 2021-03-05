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

package com.example.android.trackr.ui.widget

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.android.trackr.R
import com.example.android.trackr.TestApplication
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class StarButtonTest {
    private val application: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun when_starred() {
        val starButton = StarButton(application)
        starButton.isChecked = true
        assertThat(starButton.drawableResId).isEqualTo(R.drawable.ic_star)
        assertThat(starButton.contentDescription).isEqualTo(application.resources.getString(R.string.starred))
    }

    @Test
    fun when_unStarred() {
        val starButton = StarButton(application)
        starButton.isChecked = false
        assertThat(starButton.drawableResId).isEqualTo(R.drawable.ic_star_border)
        assertThat(starButton.contentDescription).isEqualTo(application.resources.getString(R.string.unstarred))
    }
}