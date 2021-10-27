package com.example.pattern_creator.colour_adders

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pattern_creator.R
import com.example.pattern_creator.databinding.FragmentRgbColourAdditionBinding
import com.example.pattern_creator.model.Colour
import com.example.pattern_creator.model.PatternViewModelKotlin


/**
 * A simple [Fragment] subclass.
 */
class RgbColourAdditionFragment : Fragment() {
    private var binding: FragmentRgbColourAdditionBinding? = null

    private val sharedViewModel: PatternViewModelKotlin by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val fragmentBinding = FragmentRgbColourAdditionBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        if(binding != null) {
            binding!!.editableTextColourRgb.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s!!.length == 6) {
                        val colour = textRgbToColorInt(s.toString())
                        if (colour != null) {
                            Log.d(TAG, "text changed: colour is valid")
                            binding!!.colourDisplay.setBackgroundColor(colour)
                            binding!!.colourDisplay.text = resources.getString(R.string.rgb_colour_sample_text)
                        } else {
                            Log.d(TAG, "text changed: colour is not valid")
                            binding!!.colourDisplay.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.amber_light))
                            binding!!.colourDisplay.text = resources.getString(R.string.invalid_rgb)
                        }
                    }
                }
            })
        }

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            rgbColourAdditionFragment = this@RgbColourAdditionFragment
        }
    }

    fun selectColour(): Boolean{
        if (binding != null && binding?.editableTextColourRgb != null) {
            val rgb = binding!!.editableTextColourRgb.text.toString()
            if (rgb != "" &&  textRgbToColorInt(rgb) != null && textRgbToColorInt(rgb)!! <= 0xFFFFFF) {
                val newColour = Colour(textRgbToColorInt(rgb)!!)
                newColour.check()
                if (binding?.editTextColourName != null) {
                    val name = binding!!.editableTextColourName.text.toString()
                    if (name != "") {
                        newColour.setName(name)
                    } else {
                        newColour.setName("#$rgb")
                    }
                }
                Log.d(TAG, "navigating up")
                sharedViewModel.addColourOption(newColour)
                val prefs = activity?.getPreferences(Context.MODE_PRIVATE)
                if (prefs != null) {
                    val prefsEditor = prefs.edit()
                    sharedViewModel.storeColours(prefsEditor)
                }
                findNavController().navigateUp()
                Log.d(TAG, "button: colour is valid")
                return true
            }
        }
        Log.d(TAG, "button: colour is not valid")
        Toast.makeText(requireContext(), resources.getString(R.string.invalid_rgb_toast), Toast.LENGTH_SHORT).show()
        return false
    }

    @ColorInt
    fun textRgbToColorInt(rgb: String): Int? {
        return try {
            Color.rgb(Integer.parseInt(rgb.substring(0, 2), 16),
                Integer.parseInt(rgb.substring(2, 4), 16), Integer.parseInt(rgb.substring(4), 16))
        } catch (exception: NumberFormatException) {
            null
        }
    }

    companion object {
        const val TAG = "RgbClrAdditionFragment"
    }
}