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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.example.android.trackr.NavTaskEditGraphArgs
import com.example.android.trackr.R
import com.example.android.trackr.data.TaskState
import com.example.android.trackr.databinding.FragmentTaskEditBinding
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.Instant

private const val FRAGMENT_DATE_PICKER = "DatePicker"

@AndroidEntryPoint
class TaskEditFragment : Fragment(R.layout.fragment_task_edit) {

    private val args: NavTaskEditGraphArgs by navArgs()
    private val viewModel: TaskEditViewModel by navGraphViewModels(R.id.nav_task_edit_graph) {
        defaultViewModelProviderFactory
    }
    private lateinit var binding: FragmentTaskEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.taskId = args.taskId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentTaskEditBinding.bind(view)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save -> {
                    viewModel.save()
                    true
                }
                else -> false
            }
        }
        val menuItemSave = binding.toolbar.menu.findItem(R.id.action_save)
        viewModel.modified.observe(viewLifecycleOwner) { modified ->
            menuItemSave.isEnabled = modified
        }
        binding.status.adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_status_spinner,
            R.id.status_text,
            TaskState.values().map { getString(it.stringResId) }
        )
        binding.status.doOnItemSelected { position ->
            viewModel.updateState(TaskState.values()[position])
        }
        viewModel.status.observe(viewLifecycleOwner) { status ->
            binding.status.setSelection(status.ordinal)
        }
        binding.owner.setOnClickListener {
            findNavController().navigate(R.id.nav_user_selection)
        }
        binding.dueAt.setOnClickListener {
            MaterialDatePicker.Builder.datePicker().build().apply {
                addOnPositiveButtonClickListener { time ->
                    viewModel.updateDueAt(Instant.ofEpochMilli(time))
                }
            }.show(childFragmentManager, FRAGMENT_DATE_PICKER)
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
