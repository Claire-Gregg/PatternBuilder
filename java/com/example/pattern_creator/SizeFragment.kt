package com.example.pattern_creator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pattern_creator.databinding.FragmentSizeBinding
import com.example.pattern_creator.model.PatternViewModelKotlin
import kotlin.math.roundToInt

/**
 * [SizeFragment] allows a user to choose a cupcake flavor for the order.
 */
class SizeFragment : Fragment() {

    // Binding object instance corresponding to the fragment_size.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment.
    private var binding: FragmentSizeBinding? = null

    private val sharedViewModel: PatternViewModelKotlin by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentSizeBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        binding?.sizeChooser?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val photoWidth: Double = sharedViewModel.photo.value!!.width.toDouble()
                val photoHeight: Double = sharedViewModel.photo.value!!.height.toDouble()
                when {
                    progress > 0 -> {
                        sharedViewModel.setPatternWidth(progress)
                    }
                    photoHeight > photoWidth -> {
                        sharedViewModel.setPatternWidth(1)
                    }
                    else -> {
                        sharedViewModel.setPatternWidth(((1 / photoHeight) * photoWidth).roundToInt())
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?)  {
                binding?.imageView?.setImageBitmap(sharedViewModel.photoPixelated.value)
            }
        })



        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            sizeFragment = this@SizeFragment
            imageView.setImageBitmap(sharedViewModel.photoPixelated.value)
        }
    }

    fun sizeChooserMoreInfo(){
        Toast.makeText(requireContext(), resources.getString(R.string.size_chooser_more_info_toast_text), Toast.LENGTH_LONG).show()
    }

    fun decreaseWidth() {
        sharedViewModel.setPatternWidth(sharedViewModel.patternWidth.value!! - 1)
    }

    fun increaseWidth() {
        sharedViewModel.setPatternWidth(sharedViewModel.patternWidth.value!! + 1)
    }

    /**
     * Navigate to the next screen to choose pickup date.
     */
    fun goToNextScreen() {
        findNavController().navigate(R.id.action_flavorFragment_to_pickupFragment)
    }

    /**
     * This fragment lifecycle method is called when the view hierarchy associated with the fragment
     * is being removed. As a result, clear out the binding object.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}