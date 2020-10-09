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
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.databinding.BindingAdapter
import com.example.android.trackr.R
import com.example.android.trackr.data.Tag
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

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
    setTextColor(tag.color)
    val color = tag.color
    chipBackgroundColor =
        ColorStateList.valueOf(Color.argb(0x17, color.red, color.green, color.blue))
}
