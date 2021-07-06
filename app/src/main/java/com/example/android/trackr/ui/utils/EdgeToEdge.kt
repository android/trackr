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

package com.example.android.trackr.ui.utils

import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter

/**
 * Set up a listener to apply window insets. The lambda also receives this View's initial padding
 * and margin values, to aid in properly updating the view based on the insets.
 */
fun View.doOnApplyWindowInsets(
    f: (v: View, insets: WindowInsetsCompat, padding: Insets, margins: Insets) -> WindowInsetsCompat
) {
    // Create a snapshot of the view's padding and margins.
    val padding = recordPadding()
    val margins = recordMargins()
    // Set a listener which proxies to the given lambda, also passing in the recorded state.
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        f(view, insets, padding, margins)
    }

    requestApplyInsetsWhenAttached()
}

fun View.recordPadding() = Insets.of(paddingLeft, paddingTop, paddingRight, paddingBottom)

fun View.recordMargins(): Insets {
    val lp = layoutParams as? MarginLayoutParams ?: return Insets.NONE
    return Insets.of(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin)
}

/**
 * Convenience to request window insets, or ensure that the request is made when attached to the
 * view hierarchy.
 */
fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal.
        requestApplyInsets()
    } else {
        // Add a listener to request when we are attached to the hierarchy.
        addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
}

/**
 * Adjust padding or margin based on the current window insets for system bars like the navigation
 * bar and status bar. This will only ever increase padding or margins using the sum of the View's
 * original padding or margin plus the system insets.
 *
 * Specify which sides to adjust in XML using the binding expression `@{true}`. For example, the
 * following View will have its top margin set to the system's top inset, and it's left and right
 * padding set to the system's left and right insets plus 16dp.
 *
 * ```
 * <View
 *   android:layout_width="match_parent"
 *   android:layout_height="wrap_content"
 *   android:paddingLeft="16dp"
 *   android:paddingRight="16dp"
 *   app:marginTopSystemBars="@{true}"
 *   app:paddingLeftSystemBars="@{true}"
 *   app:paddingRightSystemBars="@{true}" />
 * ```
 *
 * Note that no insets will be consumed. If you need to consume insets, or need to apply some other
 * behavior, use [doOnApplyWindowInsets] directly instead.
 */
@BindingAdapter(
    "paddingLeftSystemBars",
    "paddingTopSystemBars",
    "paddingRightSystemBars",
    "paddingBottomSystemBars",
    "marginLeftSystemBars",
    "marginTopSystemBars",
    "marginRightSystemBars",
    "marginBottomSystemBars",
    requireAll = false
)
fun applySystemBars(
    view: View,
    padLeft: Boolean,
    padTop: Boolean,
    padRight: Boolean,
    padBottom: Boolean,
    marginLeft: Boolean,
    marginTop: Boolean,
    marginRight: Boolean,
    marginBottom: Boolean
) {
    val adjustPadding = padLeft || padTop || padRight || padBottom
    val adjustMargins = marginLeft || marginTop || marginRight || marginBottom
    if (!(adjustPadding || adjustMargins)) {
        return
    }

    view.doOnApplyWindowInsets { v, insets, padding, margins ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        if (adjustPadding) {
            val systemLeft = if (padLeft) systemBars.left else 0
            val systemTop = if (padTop) systemBars.top else 0
            val systemRight = if (padRight) systemBars.right else 0
            val systemBottom = if (padBottom) systemBars.bottom else 0
            v.updatePadding(
                left = padding.left + systemLeft,
                top = padding.top + systemTop,
                right = padding.right + systemRight,
                bottom = padding.bottom + systemBottom,
            )
        }
        if (adjustMargins) {
            val systemLeft = if (marginLeft) systemBars.left else 0
            val systemTop = if (marginTop) systemBars.top else 0
            val systemRight = if (marginRight) systemBars.right else 0
            val systemBottom = if (marginBottom) systemBars.bottom else 0
            v.updateLayoutParams<MarginLayoutParams> {
                leftMargin = margins.left + systemLeft
                topMargin = margins.top + systemTop
                rightMargin = margins.right + systemRight
                bottomMargin = margins.bottom + systemBottom
            }

        }
        insets // Always return the insets, so that children can also use them.
    }
}
