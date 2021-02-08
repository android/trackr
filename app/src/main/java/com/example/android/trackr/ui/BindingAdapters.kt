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

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.databinding.BindingAdapter
import com.example.android.trackr.R
import com.example.android.trackr.data.Tag
import com.example.android.trackr.ui.utils.DateTimeUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.threeten.bp.Clock
import org.threeten.bp.Instant

/**
 * Sets tags to be shown in this [ChipGroup].
 *
 * @param tags The list of tags to show.
 * @param showAllTags Whether all the tags should be shown or they should be truncated to the number
 * of available [Chip] in this [ChipGroup]. This should be `false` when the method should not
 * inflate new views, .
 */
@BindingAdapter("tags", "showAllTags")
fun ChipGroup.tags(
    tags: List<Tag>?,
    showAllTags: Boolean
) {
    bind(tags ?: emptyList(), showAllTags)
}

private fun ChipGroup.bind(tags: List<Tag>, showAllTags: Boolean) {
    var index = 0

    for (i in 0 until childCount) {
        (getChildAt(i) as? Chip)?.let { chip ->
            if (index >= tags.size) {
                chip.visibility = View.GONE
            } else {
                chip.bind(tags[index])
                chip.visibility = View.VISIBLE
            }
            index++
        }
    }

    if (showAllTags) {
        while (index < tags.size) {
            val chip = LayoutInflater.from(context)
                .inflate(R.layout.tag, this, false) as Chip
            chip.bind(tags[index])
            addView(chip, index)
            index++
        }
    } else {
        (getChildAt(childCount - 1) as? TextView)?.let { label ->
            val extraCount = tags.size - index
            if (extraCount > 0) {
                label.visibility = View.VISIBLE
                label.text = resources.getString(R.string.more_tags, extraCount)
            } else {
                label.visibility = View.GONE
            }
        }
    }
}

private fun Chip.bind(tag: Tag) {
    text = tag.label
    val color = tag.color
    val typedValue = TypedValue()

    context.theme.resolveAttribute(color.textColor, typedValue, true)
    setTextColor(typedValue.data)

    context.theme.resolveAttribute(color.backgroundColor, typedValue, true)
    chipBackgroundColor = ColorStateList.valueOf(typedValue.data)
}

@BindingAdapter("dueMessageOrDueDate", "clock")
fun showFormattedDueMessageOrDueDate(view: TextView, instant: Instant?, clock: Clock?) {
    view.text = if (instant == null || clock == null) {
        ""
    } else {
        DateTimeUtils.durationMessageOrDueDate(view.resources, instant, clock)
    }
}

/**
 * Binding adapter to format due date of task to a human-readable format. If the due date is not
 * close, the [view] is hidden. TODO: rephrase.
 */
@BindingAdapter("dueMessageOrHide", "clock")
fun showFormattedDueMessageOrHide(view: TextView, dueDate: Instant?, clock: Clock) {
    val text = if (dueDate == null) {
        ""
    } else {
        DateTimeUtils.durationMessageOrDueDate(view.resources, dueDate, clock)
    }
    if (text.isEmpty()) {
        view.visibility = View.GONE
    }
}

@BindingAdapter("formattedDate", "clock")
fun formattedGenericDate(view: TextView, instant: Instant?, clock: Clock) {
    instant?.let {
        view.text = DateTimeUtils.formattedDate(view.resources, it, clock)
    }
}

/**
 * Replaces the label for the click action associated with [view]. The custom
 * label is then passed on to the user of an accessibility service, which can use [label].
 * For example, this replaces Talkback's generic "double tap to activate" announcement with the more
 * descriptive "double tap to <label>" action label.
 */
@BindingAdapter("clickActionLabel")
fun addClickActionLabel(
    view: View,
   label: String
) {
    ViewCompat.replaceAccessibilityAction(
        view,
        AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK,
        label,
        null
    )
}
