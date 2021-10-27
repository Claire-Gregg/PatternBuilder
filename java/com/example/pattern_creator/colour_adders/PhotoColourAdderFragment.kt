package com.example.pattern_creator.colour_adders

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pattern_creator.databinding.FragmentPhotoColourAdderBinding
import com.example.pattern_creator.model.Colour
import com.example.pattern_creator.model.PatternViewModelKotlin
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import java.io.FileNotFoundException
import java.io.InputStream


/**
 * A simple [Fragment] subclass.
 */
class PhotoColourAdderFragment : Fragment() {

    private var binding: FragmentPhotoColourAdderBinding? = null
    private val sharedViewModel: PatternViewModelKotlin by activityViewModels()
    private var resultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentPhotoColourAdderBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                try {
                    val imageUri: Uri = data?.data!!
                    val imageStream: InputStream = requireContext().contentResolver.openInputStream(imageUri)!!
                    val selectedImage = BitmapFactory.decodeStream(imageStream)
                    val drawable: Drawable = BitmapDrawable(resources, selectedImage)
                    binding?.colorPickerView?.setPaletteDrawable(drawable)
                    binding?.colorPickerView?.visibility = View.VISIBLE
                    val bubbleFlag = BubbleFlag(requireContext())
                    bubbleFlag.flagMode = FlagMode.ALWAYS
                    binding?.colorPickerView?.flagView = bubbleFlag
                    binding?.editTextColourName?.visibility = View.VISIBLE
                    binding?.editableTextColourName?.visibility = View.VISIBLE
                    binding?.selectColourButton?.visibility = View.VISIBLE
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = sharedViewModel
            photoColourAdderFragment = this@PhotoColourAdderFragment
        }
    }

    fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .apply { type = "image/*" }
        if (activity?.packageManager?.resolveActivity(intent, 0) != null) {
            resultLauncher?.launch(intent)
        }
    }

    fun selectColour(){
        if (binding != null && binding?.colorPickerView != null) {
            val newColour = Colour(binding!!.colorPickerView.color)
            if (binding!!.editableTextColourName.text != null) {
                val name = binding!!.editableTextColourName.text.toString()
                Log.d(ColourAdditionFragment.TAG, "Colour name is $name")
                Log.d(ColourAdditionFragment.TAG, "Colour is " + String.format("#%06X", (0xFFFFFF and binding!!.colorPickerView.color)))
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
        const val REQUEST_IMAGE_GET = 1
        const val TAG = "PhotoColourAdderFragment"
    }
}