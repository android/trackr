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

package com.example.android.trackr.ui.archives

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.android.trackr.R
import com.example.android.trackr.data.User
import com.example.android.trackr.databinding.FragmentArchiveBinding
import com.example.android.trackr.ui.utils.configureEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.threeten.bp.Clock
import javax.inject.Inject

@AndroidEntryPoint
class ArchivesFragment : Fragment(R.layout.fragment_archive) {

    private val viewModel: ArchiveViewModel by viewModels()
    private lateinit var binding: FragmentArchiveBinding

    @Inject
    lateinit var currentUser: User

    @Inject
    lateinit var clock: Clock

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentArchiveBinding.bind(view)
        configureEdgeToEdge(
            root = view,
            scrollingContent = binding.archivedTasks,
            topBar = binding.toolbar
        )

        val adapter = ArchiveAdapter(
            currentUser = currentUser,
            clock = clock,
            onItemSelected = { task -> viewModel.toggleTaskSelection(task.id) },
            onItemStarClicked = { /* TODO */ }
        )
        binding.archivedTasks.adapter = adapter
        viewModel.archivedTasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
        }
    }
}
