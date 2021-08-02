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

package com.example.android.trackr.utils

import android.app.Application
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.trackr.shared.R
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@RunWith(AndroidJUnit4::class)
class DateTimeUtilsTest {
    private val dateInEpochSecond = 1584310694L // March 15, 2020
    private val fakeClock =
        Clock.fixed(Instant.ofEpochSecond(dateInEpochSecond), ZoneId.of("US/Central"))
    private lateinit var resources: Resources

    @Before
    fun setup() {
        val application: Application = ApplicationProvider.getApplicationContext()
        resources = application.resources
    }

    @Test
    fun formattedGenericDate() {
        val text = DateTimeUtils.formattedDate(resources, Instant.ofEpochSecond(dateInEpochSecond), fakeClock)
        assertThat(text).isEqualTo("Due Mar 15, 2020")
    }

    @Test
    fun formattedDueMessage_overdue1Day() {
        val timeYesterday = Instant.now(fakeClock) - Duration.ofDays(1)
        val message = DateTimeUtils.durationMessage(resources, timeYesterday, fakeClock)
        assertThat(message).isEqualTo(
            resources.getQuantityString(R.plurals.due_date_overdue_x_days, 1, 1)
        )
    }

    @Test
    fun formattedDueMessage_dueToday() {
        val message = DateTimeUtils.durationMessage(resources, Instant.now(fakeClock), fakeClock)

        assertThat(message).isEqualTo(
            resources.getString(R.string.due_date_today)
        )
    }

    @Test
    fun formattedDueMessage_dueTomorrow() {
        val timeTomorrow = Instant.now(fakeClock) + Duration.ofDays(1)
        val message = DateTimeUtils.durationMessage(resources, timeTomorrow, fakeClock)

        assertThat(message).isEqualTo(
            resources.getQuantityString(R.plurals.due_date_days, 1)
        )
    }

    @Test
    fun formattedDueMessage_dueLongAhead() {
        val timeWayAhead = Instant.now(fakeClock) + Duration.ofDays(
            DateTimeUtils.MAX_NUM_DAYS_FOR_CUSTOM_MESSAGE + 1L)
        val message = DateTimeUtils.durationMessage(resources, timeWayAhead, fakeClock)
        assertThat(message).isEmpty()
    }
}