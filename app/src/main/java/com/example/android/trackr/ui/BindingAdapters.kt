package com.example.android.trackr.ui

import android.content.res.ColorStateList
import androidx.databinding.BindingAdapter
import com.example.android.trackr.data.Tag
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

@BindingAdapter("appendTagChips")
fun ChipGroup.appendTagChips(
    tags: List<Tag>
) {
    val states = arrayOf(
        intArrayOf(android.R.attr.state_enabled),
        intArrayOf(android.R.attr.state_enabled),
        intArrayOf(android.R.attr.state_checked),
        intArrayOf(android.R.attr.state_pressed)
    )
    // TODO: consider if there is a more performant way to avoid re-adding the chips.
    removeAllViews()

    for (tag in tags) {
        // TODO: derive different colors for different states
        val colors = intArrayOf(tag.color, tag.color, tag.color, tag.color)
        val colorStateList = ColorStateList(states, colors)

        val chip = Chip(context)
        chip.text = tag.label
        chip.chipBackgroundColor = colorStateList
        addView(chip)
    }
}
