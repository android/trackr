/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackr.ui

import android.app.Application
import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.android.trackr.R
import com.example.android.trackr.TestApplication
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class BindingAdaptersTest {

    private val dateInEpochSecond = 1584310694L // March 15, 2020
    private val fakeClock =
        Clock.fixed(Instant.ofEpochSecond(dateInEpochSecond), ZoneId.systemDefault())
    private lateinit var resources: Resources
    private lateinit var textView: TextView

    @Before
    fun setup() {
        val application: Application = ApplicationProvider.getApplicationContext()
        resources = application.resources
        textView = TextView(application)
    }

    @Test
    fun formatDueDate_overdue3Day() {
        val time3DaysAgo = Instant.now(fakeClock) - Duration.ofDays(3)
        formatDueDate(textView, time3DaysAgo, fakeClock)
        assertThat(textView.text).isEqualTo(
            resources.getQuantityString(R.plurals.due_date_overdue_x_days, 3, 3)
        )
    }

    @Test
    fun formatDueDate_overdue1Day() {
        val timeYesterday = Instant.now(fakeClock) - Duration.ofDays(1)
        formatDueDate(textView, timeYesterday, fakeClock)
        assertThat(textView.text).isEqualTo(
            resources.getQuantityString(R.plurals.due_date_overdue_x_days, 1, 1)
        )
    }

    @Test
    fun formatDueDate_dueToday() {
        val timeToday = Instant.now(fakeClock) + Duration.ofHours(3)
        formatDueDate(textView, timeToday, fakeClock)
        assertThat(textView.text).isEqualTo(
            resources.getString(R.string.due_date_today)
        )
    }

    @Test
    fun formatDueDate_dueTomorrow() {
        val timeTomorrow = Instant.now(fakeClock) + Duration.ofDays(1)
        formatDueDate(textView, timeTomorrow, fakeClock)
        assertThat(textView.text).isEqualTo(
            resources.getQuantityString(R.plurals.due_date_days, 1, 1)
        )
    }

    @Test
    fun formatDueDate_dueInXDays() {
        val timeIn3Days = Instant.now(fakeClock) + Duration.ofDays(3) + Duration.ofHours(2)
        formatDueDate(textView, timeIn3Days, fakeClock)
        assertThat(textView.text).isEqualTo(
            resources.getQuantityString(R.plurals.due_date_days, 3, 3)
        )
    }

    @Test
    fun formatDueMessage_overdue1Day() {
        val timeYesterday = Instant.now(fakeClock) - Duration.ofDays(1)
        formatDueMessage(textView, timeYesterday, fakeClock)
        assertThat(textView.text).isEqualTo(
            resources.getQuantityString(R.plurals.due_date_overdue_x_days, 1, 1)
        )
        assertThat(textView.visibility).isEqualTo(View.VISIBLE)
    }

    @Test
    fun formatDueMessage_dueLongAhead() {
        val timeIn30Days = Instant.now(fakeClock) + Duration.ofDays(30)
        formatDueMessage(textView, timeIn30Days, fakeClock)
        assertThat(textView.visibility).isEqualTo(View.GONE)
    }

    @Test
    fun instant_empty() {
        instant(textView, null)
        assertThat(textView.text.toString()).isEmpty()
    }
}
