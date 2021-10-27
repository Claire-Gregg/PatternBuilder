
package com.example.pattern_creator

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.pattern_creator.databinding.FragmentStartBinding
import com.example.pattern_creator.model.PatternViewModelKotlin

/**
 * This is the first screen of the Cupcake app. The user can choose how many cupcakes to order.
 */
class StartFragment : Fragment() {

    // Binding object instance corresponding to the fragment_start.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment.
    private var binding: FragmentStartBinding? = null

    private val sharedViewModel: PatternViewModelKotlin by activityViewModels()

    private var resultLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentStartBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null && data.data != null) {
                    val uri = data.data!!

                    var photo: Bitmap
                    if (Build.VERSION.SDK_INT >= 28) {
                        val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                        photo = ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        photo = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                    }
                    if (photo.width > MAX_BITMAP_WIDTH){
                        val aspectRatio = photo.height.toDouble() / photo.width.toDouble()
                        val targetHeight = (MAX_BITMAP_WIDTH * aspectRatio).toInt()
                        photo = Bitmap.createScaledBitmap(photo, MAX_BITMAP_WIDTH, targetHeight, false)
                    }
                    sharedViewModel.setPhotoBitmap(photo)
                    findNavController().navigate(R.id.action_startFragment_to_sizeFragment)
                }
            }
        }

        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.startFragment = this
    }

    /**
     * Select the image which will be turned into a pattern.
     */
    fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val mimeTypes = arrayOf("image/png", "image/jpg", "image/jpeg")
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        if (activity?.packageManager?.resolveActivity(intent, 0) != null) {
             Log.d(TAG, "StartFragment.selectImage - about to start activity")
             resultLauncher?.launch(intent)
        }

    }

    /**
     * This fragment lifecycle method is called when the view hierarchy associated with the fragment
     * is being removed. As a result, clear out the binding object.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        const val REQUEST_IMAGE_GET = 1
        const val TAG = "StartFragment"
        const val MAX_BITMAP_WIDTH = 1000
    }


}