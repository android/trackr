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

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.example.android.trackr.NavTaskEditGraphArgs
import com.example.android.trackr.R
import com.example.android.trackr.data.TaskStatus
import com.example.android.trackr.databinding.TaskEditFragmentBinding
import com.example.android.trackr.ui.dataBindings
import com.example.android.trackr.ui.utils.DateTimeUtils
import com.example.android.trackr.ui.utils.configureEdgeToEdge
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import javax.inject.Inject

private const val FRAGMENT_DATE_PICKER = "DatePicker"

@AndroidEntryPoint
class TaskEditFragment : Fragment(R.layout.task_edit_fragment) {

    private val args: NavTaskEditGraphArgs by navArgs()
    private val viewModel: TaskEditViewModel by navGraphViewModels(R.id.nav_task_edit_graph) {
        defaultViewModelProviderFactory
    }
    private val binding by dataBindings(TaskEditFragmentBinding::bind)

    @Inject
    lateinit var clock: Clock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.taskId = args.taskId
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            close()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.clock = clock

        configureEdgeToEdge(
            root = view,
            scrollingContent = binding.scrollingContent,
            topBar = binding.toolbar
        )

        binding.toolbar.setNavigationOnClickListener {
            close()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save -> {
                    if (binding.title.text.toString().isEmpty()) {
                        binding.title.error = resources.getString(R.string.missing_title_error)
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
        viewModel.modified.observe(viewLifecycleOwner) { modified ->
            menuItemSave.isVisible = modified
        }
        binding.status.adapter = ArrayAdapter(
            requireContext(),
            R.layout.status_spinner_item,
            R.id.status_text,
            TaskStatus.values().map { getString(it.stringResId) }
        )
        binding.status.doOnItemSelected { position ->
            viewModel.updateState(TaskStatus.values()[position])
        }
        viewModel.status.observe(viewLifecycleOwner) { status ->
            binding.status.setSelection(status.ordinal)
        }

        viewModel.owner.observe(viewLifecycleOwner) {
            binding.owner.contentDescription =
                resources.getString(R.string.owner_with_value, it.username)
        }

        binding.owner.setOnClickListener {
            findNavController().navigate(R.id.nav_user_selection)
        }

        viewModel.dueAt.observe(viewLifecycleOwner) {
            // Combine the label ("Due date") with the date value. This consolidates the announced text
            // for screenreader users.
            binding.dueAt.contentDescription =
                resources.getString(
                    R.string.due_date_with_value, DateTimeUtils.formattedDate(resources, it, clock)
                )

            binding.dueAt.setOnClickListener {
                MaterialDatePicker.Builder.datePicker().build().apply {
                    addOnPositiveButtonClickListener { time ->
                        viewModel.updateDueAt(Instant.ofEpochMilli(time))
                    }
                }.show(childFragmentManager, FRAGMENT_DATE_PICKER)
            }
        }

        binding.tagContainer.setOnClickListener {
            findNavController().navigate(R.id.nav_tag_selection)
        }

        // TODO: Add a fragment test to verify LiveData changes result in navigation.
        viewModel.discarded.observe(viewLifecycleOwner) { discarded ->
            if (discarded) {
                findNavController().popBackStack(R.id.nav_task_edit_graph, true)
            }
        }
    }

    private fun close() {
        if (viewModel.modified.value == true) {
            findNavController().navigate(R.id.nav_discard_confirmation)
        } else {
            findNavController().popBackStack()
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
