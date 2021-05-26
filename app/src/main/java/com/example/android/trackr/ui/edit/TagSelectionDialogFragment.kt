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

package com.example.android.trackr.ui.edit

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import com.example.android.trackr.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TagSelectionDialogFragment : DialogFragment() {

    private val viewModel: TaskEditViewModel by hiltNavGraphViewModels(R.id.nav_task_edit_graph)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val allTags = viewModel.allTags
        val tags = viewModel.tags.value ?: emptyList()
        val checked = allTags.map { tag ->
            tag in tags
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setMultiChoiceItems(
                allTags.map { it.label }.toTypedArray(),
                checked.toBooleanArray()
            ) { _, which, isChecked ->
                if (isChecked) {
                    viewModel.addTag(allTags[which])
                } else {
                    viewModel.removeTag(allTags[which])
                }
            }
            .create()
    }
}
