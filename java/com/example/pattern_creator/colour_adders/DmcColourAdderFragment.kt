package com.example.pattern_creator.colour_adders

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pattern_creator.R
import com.example.pattern_creator.databinding.FragmentDmcColourAdderBinding
import com.example.pattern_creator.model.DatabaseDmcRgb
import com.example.pattern_creator.model.PatternViewModelKotlin

/**
 * A simple [Fragment] subclass.
 */
class DmcColourAdderFragment : Fragment() {
    private var binding: FragmentDmcColourAdderBinding? = null
    private lateinit var dmcThreadDatabase: DatabaseDmcRgb

    private val sharedViewModel: PatternViewModelKotlin by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentDmcColourAdderBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        dmcThreadDatabase = DatabaseDmcRgb(requireContext())

        if (binding != null) {
            binding!!.editableTextColourDMC.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val colour = dmcThreadDatabase.checkForDmcId(s.toString())
                    if (colour != null) {
                        binding!!.colourDisplay.setBackgroundColor(colour.colour)
                        binding!!.colourDisplay.text = colour.name
                    } else {
                        binding!!.colourDisplay.text = resources.getString(R.string.not_dmc_id_text)
                        binding!!.colourDisplay.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.amber_light))
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
            dmcColourAdderFragment = this@DmcColourAdderFragment
        }
    }

    fun selectColour(): Boolean{
        if (binding != null) {
            val newColour =
                dmcThreadDatabase.checkForDmcId(binding!!.editableTextColourDMC.text.toString())
            if (newColour != null) {
                sharedViewModel.addColourOption(newColour)
                val prefs = activity?.getPreferences(Context.MODE_PRIVATE)
                if (prefs != null) {
                    val prefsEditor = prefs.edit()
                    sharedViewModel.storeColours(prefsEditor)
                }
                findNavController().navigateUp()
                return true
            }
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.not_dmc_id_text),
                Toast.LENGTH_SHORT
            ).show()
        }
        return false
    }


}