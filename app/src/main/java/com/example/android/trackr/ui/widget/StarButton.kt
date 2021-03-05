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

import android.content.Context
import android.util.AttributeSet
import android.widget.Checkable
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import com.example.android.trackr.R

class StarButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatImageButton(context, attrs, defStyleAttr), Checkable {

    private var _checked = false

    @VisibleForTesting
    var drawableResId = R.drawable.ic_star_border

    override fun setChecked(checked: Boolean) {
        _checked = checked
        if (checked) {
            drawableResId = R.drawable.ic_star
            contentDescription = context.getString(R.string.starred)
        } else {
            drawableResId = R.drawable.ic_star_border
            contentDescription = context.getString(R.string.unstarred)
        }
        background = ContextCompat.getDrawable(context, drawableResId)
    }

    override fun isChecked(): Boolean {
        return _checked
    }

    override fun toggle() {
        _checked = !_checked
    }
}