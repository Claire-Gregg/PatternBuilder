package com.example.pattern_creator

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.pattern_creator.adapter.DialogItemAdapter
import com.example.pattern_creator.model.Colour
import com.example.pattern_creator.model.PatternViewModelKotlin

class DeleteColourDialogFragment (private val sharedViewModel: PatternViewModelKotlin) : DialogFragment() {


    lateinit var dialogView: View
    lateinit var recyclerViewAdapter: DialogItemAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        return activity?.let { it ->
            val builder = AlertDialog.Builder(it)
            dialogView = requireActivity().layoutInflater.inflate(R.layout.fragment_delete_colour_dialog, null)

            builder.apply {
                setTitle(getString(R.string.delete_colours_dialog_title))
                setPositiveButton(getString(R.string.delete_colours_dialog_positive_button))
                { _, _ -> deleteColours() }
                setNegativeButton(getString(R.string.delete_colours_dialog_negative_button)) { _, _ -> }
                setView(dialogView)
            }

            val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)
            val sortedColours: MutableList<Colour>
            if (sharedViewModel.colourOptions.value != null) {
                sortedColours = sharedViewModel.colourOptions.value!!
                sortedColours.sortBy { it.name }
            } else {
                sortedColours = mutableListOf()
            }
            recyclerViewAdapter = DialogItemAdapter(requireContext(), sortedColours)
            recyclerView.adapter = recyclerViewAdapter
            recyclerView.setHasFixedSize(true)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
    private fun deleteColours() {
        val colourList: MutableList<Colour> = recyclerViewAdapter.dataset
        val deletedIndices: MutableList<Int> = recyclerViewAdapter.deletedItemIndex
        Log.d(TAG, "Deleting ${deletedIndices.size} colours")
        if (sharedViewModel.colourOptions.value != null) {
            Log.d(TAG, "colour options is not null")
            val sharedViewModelColourList = sharedViewModel.colourOptions.value!!
            for (deletedIndex in deletedIndices) {
                Log.d(TAG, "deleting colour with index $deletedIndex")
                if (sharedViewModelColourList.contains(colourList[deletedIndex])){
                    Log.d(TAG, "deleting colour with name ${colourList[deletedIndex].name}")
                    sharedViewModelColourList.remove(colourList[deletedIndex])
                }
            }
            saveColours()
        }
    }

    private fun saveColours(){
        val prefs = activity?.getPreferences(Context.MODE_PRIVATE)
        if (prefs != null) {
            val prefsEditor = prefs.edit()
            sharedViewModel.storeColours(prefsEditor)
        }
    }

    companion object {
        const val TAG = "DeleteClrDialogFragment"
    }
}