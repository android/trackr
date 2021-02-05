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

import android.graphics.Rect
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/**
 * Configures the app content to be displayed edge-to-edge.
 *
 * @param root The root view.
 * @param scrollingContent The scrolling view holding the main content. Typically a Recyclerview or
 * a ScrollView.
 * @param topBar The top bar.
 * @param bottomBar The bottom app bar if it exists.
 * @param fab The floating action button if it needs adjusting to the insets. If it's attached to
 * the bottom bar, it doesn't need adjustment.
 */
fun configureEdgeToEdge(
    root: View,
    scrollingContent: View,
    topBar: View,
    bottomBar: View? = null,
    fab: View? = null
) {
    val fabMargin = Rect(0, 0, 0, 0)
    if (fab != null) {
        fabMargin.left = fab.marginLeft
        fabMargin.top = fab.marginTop
        fabMargin.right = fab.marginRight
        fabMargin.bottom = fab.marginBottom
    }
    val topBarPadding = Rect(
        topBar.paddingLeft,
        topBar.paddingTop,
        topBar.paddingRight,
        topBar.paddingBottom
    )
    ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
        v.updatePadding(
            top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        )
        val navigationInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        fun updateScrollingContentPadding(bottomBarHeight: Int) {
            scrollingContent.updatePadding(
                left = navigationInsets.left,
                right = navigationInsets.right,
                bottom = navigationInsets.bottom + bottomBarHeight
            )
        }
        topBar.updatePadding(
            left = topBarPadding.left + navigationInsets.left,
            right = topBarPadding.right + navigationInsets.right
        )
        if (bottomBar != null) {
            bottomBar.doOnLayout { bar -> updateScrollingContentPadding(bar.height) }
        } else {
            updateScrollingContentPadding(0)
        }
        fab?.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            leftMargin = fabMargin.left + navigationInsets.left
            rightMargin = fabMargin.right + navigationInsets.right
            bottomMargin = fabMargin.bottom + navigationInsets.bottom
        }
        insets
    }
}
