/*
 * Copyright 2020 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.goldenraven.padawanwallet.home.tutorials

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.goldenraven.padawanwallet.R
import com.goldenraven.padawanwallet.databinding.FragmentTutorialE4Binding
import com.goldenraven.padawanwallet.home.HomeViewModel

class TutorialE4 : Fragment() {

    private lateinit var binding: FragmentTutorialE4Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentTutorialE4Binding.inflate(inflater, container, false)
        val view: View = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = Navigation.findNavController(view)
        val viewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        binding.buttonMarkDone.setOnClickListener {
            viewModel.markAsDone(tutorialNumber = 4)
            navController.navigate(R.id.action_tutorialE4_to_tutorialsHome)
        }
        binding.buttonBack.setOnClickListener {
            navController.navigate(R.id.action_tutorialE4_to_tutorialsHome)
        }
    }
}