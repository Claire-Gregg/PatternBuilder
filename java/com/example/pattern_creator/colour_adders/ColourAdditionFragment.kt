package com.example.pattern_creator.colour_adders

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pattern_creator.databinding.FragmentColourAdditionBinding
import com.example.pattern_creator.model.Colour
import com.example.pattern_creator.model.PatternViewModelKotlin
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode


/**
 * A simple [Fragment] subclass.
 */
class ColourAdditionFragment : Fragment() {

    private var binding: FragmentColourAdditionBinding? = null

    private val sharedViewModel: PatternViewModelKotlin by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentColourAdditionBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            colourAdditionFragment = this@ColourAdditionFragment
            val bubbleFlag = BubbleFlag(requireContext())
            bubbleFlag.flagMode = FlagMode.FADE
            colorPickerView.flagView = bubbleFlag
        }
    }

    fun selectColour(){
        if (binding != null && binding?.colorPickerView != null) {
            Log.d(TAG, "ColourAdditionFragment.selectColour() - colour is ${binding!!.colorPickerView.color}")
            val newColour = Colour(binding?.colorPickerView!!.color)
            if (binding?.editableTextColourName!!.text != null) {
                val name = binding?.editableTextColourName!!.text.toString()
                Log.d(TAG, "Colour name is $name")
                Log.d(TAG, "Colour is " + String.format("#%06X", (0xFFFFFF and binding!!.colorPickerView.color)))
                if (name != "") {
                    newColour.setName(name)
                }
                else {
                    newColour.setName(String.format("#%06X", (0xFFFFFF and binding!!.colorPickerView.color)))
                }
            }
            sharedViewModel.addColourOption(newColour)
            val prefs = activity?.getPreferences(Context.MODE_PRIVATE)
            if (prefs != null) {
                val prefsEditor = prefs.edit()
                sharedViewModel.storeColours(prefsEditor)
            }
            findNavController().navigateUp()
        }
    }

    companion object {
        const val TAG = "ColourAdditionFragment"
    }

}