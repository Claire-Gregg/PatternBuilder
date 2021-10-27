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
package com.example.pattern_creator

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.pattern_creator.adapter.ItemAdapter
import com.example.pattern_creator.databinding.FragmentColoursBinding
import com.example.pattern_creator.model.PatternViewModelKotlin

/**
 * [ColoursFragment] allows the user to choose a pickup date for the cupcake order.
 */
class ColoursFragment : Fragment(){

    // Binding object instance corresponding to the fragment_colours.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment.
    private var binding: FragmentColoursBinding? = null

    private val sharedViewModel: PatternViewModelKotlin by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        restoreColours()
        Log.d(TAG, "onCreateView")
        val fragmentBinding = FragmentColoursBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        val colourChangeObserver = Observer<Boolean> {
            changed ->
            if (changed) {
                renewRecyclerViewDataset()
                sharedViewModel.resetChangedColours()
            }
        }

        sharedViewModel.coloursChanged.observe(viewLifecycleOwner, colourChangeObserver)
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            coloursFragment = this@ColoursFragment
        }
        renewRecyclerViewDataset()
        closeKeyboard()
    }

    fun addColour() {
        saveColours()
        findNavController().navigate(R.id.action_coloursFragment_to_colourAdditionFragment)
    }

    fun colourListMoreInfo() {
        Toast.makeText(requireContext(), resources.getString(R.string.colour_list_more_info_toast_text), Toast.LENGTH_LONG).show()
    }

    fun addColourRGB() {
        saveColours()
        findNavController().navigate(R.id.action_coloursFragment_to_rgbColourAdditionFragment)
    }

    fun addColourPhoto() {
        saveColours()
        findNavController().navigate(R.id.action_coloursFragment_to_photoColourAdderFragment)
    }

    fun addColourDmc() {
        saveColours()
        findNavController().navigate(R.id.action_coloursFragment_to_dmcColourAdderFragment)
    }

    private fun closeKeyboard() {
        val view = requireActivity().currentFocus

        if (view != null){
            val manager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun deleteColours(){
        val dialog = DeleteColourDialogFragment(sharedViewModel)
        dialog.show(childFragmentManager, DeleteColourDialogFragment.TAG)
    }
    /**
     * Navigate to the next screen to see the order summary.
     */
    fun goToNextScreen() {
        saveColours()
        Log.d(TAG, "${sharedViewModel.numberOfCheckedColours} checked colours")
        if(sharedViewModel.numberOfCheckedColours > 0) {
            sharedViewModel.colourPattern()
            findNavController().navigate(R.id.action_pickupFragment_to_summaryFragment)
        } else {
            val colours = sharedViewModel.colourOptions.value
            if (colours != null){
                var checked = false
                var index = 0
                while (!checked && index < colours.size) {
                    if (colours[index].checked) {
                        checked = true
                    }
                    index++
                }
                if (checked) {
                    sharedViewModel.colourPattern()
                    findNavController().navigate(R.id.action_pickupFragment_to_summaryFragment)
                } else {
                    Toast.makeText(requireContext(), resources.getString(R.string.colour_list_must_have_at_least_one_colour), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        saveColours()
    }


    private fun renewRecyclerViewDataset() {
        Log.d(TAG, "ColoursFragment.renewRecyclerViewDataset() - ${sharedViewModel.colourOptions.value.toString()}")
        val dataset = sharedViewModel.colourOptions.value!!
        binding?.recyclerView?.adapter = ItemAdapter(requireContext(), dataset, sharedViewModel)
        binding?.recyclerView?.setHasFixedSize(true)
    }

    private fun saveColours(){
        val prefs = activity?.getPreferences(MODE_PRIVATE)
        if (prefs != null) {
            val prefsEditor = prefs.edit()
            sharedViewModel.storeColours(prefsEditor)
        }
    }

    private fun restoreColours(){
        val prefs = activity?.getPreferences(MODE_PRIVATE)
        sharedViewModel.restoreColours(prefs)
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Resumed")
        restoreColours()
    }

    companion object {
        const val TAG = "ColoursFragment"
    }



}