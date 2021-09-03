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

package com.example.android.trackr.ui

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import com.example.android.trackr.NavTaskEditGraphArgs
import com.example.android.trackr.R
import com.example.android.trackr.ui.utils.doOnApplyWindowInsets
import com.example.android.trackr.ui.utils.repeatWithViewLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Base class for fragments which show two panes using [SlidingPaneLayout]. This class handles
 * some common interactions, including sliding the detail pane on back press when only one pane can
 * be shown at a time.
 *
 * Child fragments that want to know whether or not both panes are shown at the same time can use a
 * [TwoPaneViewModel] scoped to the Activity to observe this state.
 */
abstract class BaseTwoPaneFragment : Fragment {

    constructor() : super()
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    private val twoPaneViewModel: TwoPaneViewModel by activityViewModels()
    private lateinit var backPressHandler: SlidingPaneBackPressHandler

    /** Retrieve this fragment's [SlidingPaneLayout]. */
    abstract fun getSlidingPaneLayout(): SlidingPaneLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val slidingPaneLayout = getSlidingPaneLayout()
        slidingPaneLayout.apply {
            lockMode = SlidingPaneLayout.LOCK_MODE_LOCKED
            doOnLayout { // Wait for layout so that isSlideable has the correct value.
                twoPaneViewModel.isTwoPane.value = !isSlideable
                if (!isSlideable) {
                    doOnApplyWindowInsets { v, insets, padding, _ ->
                        // Consume horizontal insets, otherwise the children might apply padding
                        // where they shouldn't, such as the left pane padding its right side.
                        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                        v.updatePadding(
                            left = padding.left + systemBars.left,
                            right = padding.right + systemBars.right
                        )
                        WindowInsetsCompat.Builder(insets).setInsets(
                            WindowInsetsCompat.Type.systemBars(),
                            Insets.of(0, systemBars.top, 0, systemBars.bottom)
                        ).build()
                    }
                }
            }
        }

        repeatWithViewLifecycle {
            launch {
                twoPaneViewModel.detailPaneUpEvents.collect {
                    if (backPressHandler.isEnabled) {
                        backPressHandler.handleOnBackPressed()
                    }
                }
            }
            launch {
                twoPaneViewModel.editTaskEvents.collect { taskId ->
                    findNavController().navigate(
                        R.id.nav_task_edit_graph,
                        NavTaskEditGraphArgs(taskId).toBundle()
                    )
                }
            }
        }

        backPressHandler = SlidingPaneBackPressHandler(slidingPaneLayout)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressHandler)
    }
}

/**
 * [OnBackPressedCallback] that slides the detail pane of a [SlidingPaneLayout] when only one pane
 * can be shown at a time.
 */
internal class SlidingPaneBackPressHandler(
    private val slidingPaneLayout: SlidingPaneLayout
) : OnBackPressedCallback(false), SlidingPaneLayout.PanelSlideListener {

    init {
        slidingPaneLayout.addPanelSlideListener(this)
        slidingPaneLayout.doOnLayout {
            syncState()
        }
    }

    private fun syncState() {
        isEnabled = slidingPaneLayout.isSlideable && slidingPaneLayout.isOpen
    }

    override fun handleOnBackPressed() {
        slidingPaneLayout.closePane()
    }

    override fun onPanelOpened(panel: View) {
        syncState()
    }

    override fun onPanelClosed(panel: View) {
        syncState()
    }

    override fun onPanelSlide(panel: View, slideOffset: Float) {
        // empty
    }
}
