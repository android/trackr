/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.example.android.trackr.ui.edit

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.android.trackr.NavTaskEditGraphArgs
import com.example.android.trackr.R
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.databinding.TaskEditFragmentBinding
import com.example.android.trackr.utils.DateTimeUtils
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

private const val FRAGMENT_DATE_PICKER = "DatePicker"

@AndroidEntryPoint
class TaskEditFragment : DialogFragment(R.layout.task_edit_fragment) {

    private val args: NavTaskEditGraphArgs by navArgs()
    private val viewModel: TaskEditViewModel by hiltNavGraphViewModels(R.id.nav_task_edit_graph)

    @Inject
    lateinit var clock: Clock

    // DialogFragment does not have a viewLifecycleOwner when it returns a Dialog from this method,
    // so we use the Fragment as the lifecycle owner instead. This makes `repeatWithLifecycle` raise
    // an "UnsafeRepeatOnLifecycleDetector" warning, but there isn't an alternative.
    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val taskId = args.taskId
        viewModel.taskId = taskId

        val themedContext = ContextThemeWrapper(
            requireContext(),
            R.style.ThemeOverlay_Trackr_TaskEdit
        )
        val dialog = MaterialAlertDialogBuilder(themedContext)
            .setCancelable(false)
            .setOnKeyListener { _, keyCode, event ->
                // This is the only way to intercept the back button press in DialogFragment.
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    close()
                    true
                } else false
            }
            .create()

        val binding = TaskEditFragmentBinding.inflate(dialog.layoutInflater)
        binding.viewModel = viewModel
        binding.clock = clock
        binding.lifecycleOwner = this

        binding.toolbar.setTitle(if (taskId == 0L) R.string.new_task else R.string.edit_task)
        binding.toolbar.setNavigationOnClickListener {
            close()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save -> {
                    if (binding.content.title.text.toString().isEmpty()) {
                        binding.content.title.error = resources.getString(R.string.missing_title_error)
                    } else {
                        viewModel.save { success ->
                            if (success) {
                                findNavController().popBackStack()
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }

        val menuItemSave = binding.toolbar.menu.findItem(R.id.action_save)
        binding.content.status.adapter = ArrayAdapter(
            requireContext(),
            R.layout.status_spinner_item,
            R.id.status_text,
            TaskStatus.values().map {
                getString(it.stringResId)
            }
        )
        binding.content.status.doOnItemSelected { position ->
            viewModel.updateState(TaskStatus.values()[position])
        }
        binding.content.tagContainer.setOnClickListener {
            findNavController().navigate(R.id.nav_tag_selection)
        }
        binding.content.owner.setOnClickListener {
            findNavController().navigate(R.id.nav_user_selection)
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.modified.collect { modified ->
                        menuItemSave.isVisible = modified
                    }
                }
                launch {
                    viewModel.status.collect { status ->
                        binding.content.status.setSelection(status.ordinal)
                    }
                }
                launch {
                    viewModel.owner.collect {
                        binding.content.owner.contentDescription =
                            resources.getString(R.string.owner_with_value, it.username)
                    }
                }
                launch {
                    viewModel.dueAt.collect {
                        binding.content.dueAt.contentDescription = resources.getString(
                            R.string.due_date_with_value,
                            DateTimeUtils.formattedDate(resources, it, clock)
                        )

                        binding.content.dueAt.setOnClickListener {
                            MaterialDatePicker.Builder.datePicker().build().apply {
                                addOnPositiveButtonClickListener { time ->
                                    viewModel.updateDueAt(Instant.ofEpochMilli(time))
                                }
                            }.show(childFragmentManager, FRAGMENT_DATE_PICKER)
                        }
                    }
                }
                launch {
                    // TODO: Add a fragment test to verify changes result in navigation.
                    viewModel.discarded.collect {
                        findNavController().popBackStack(R.id.nav_task_edit_graph, true)
                    }
                }
            }
        }

        return dialog.apply {
            setView(binding.root)
            WindowCompat.setDecorFitsSystemWindows(requireNotNull(window), false)
        }
    }

    private fun close() {
        if (viewModel.modified.value) {
            findNavController().navigate(R.id.nav_discard_confirmation)
        } else {
            findNavController().popBackStack(R.id.nav_task_edit_graph, true)
        }
    }
}

fun Spinner.doOnItemSelected(onItemSelected: (position: Int) -> Unit) {
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onItemSelected(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // Ignore
        }
    }
}
