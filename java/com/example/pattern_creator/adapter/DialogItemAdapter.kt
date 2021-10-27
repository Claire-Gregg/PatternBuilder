package com.example.pattern_creator.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.pattern_creator.R
import com.example.pattern_creator.model.Colour

class DialogItemAdapter (
    private val context: Context,
    val dataset: MutableList<Colour>
        ) : RecyclerView.Adapter<DialogItemAdapter.DialogItemViewHolder>() {
    val deletedItemIndex = mutableListOf<Int>()

    class DialogItemViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val checkBox: CheckBox = view.findViewById(R.id.itemCheckBox)
        val colourSample: Button = view.findViewById(R.id.colourSample)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return DialogItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: DialogItemViewHolder, position: Int) {
        val item = dataset.elementAt(position)
        holder.checkBox.text = item.name
        holder.checkBox.isChecked = false
        holder.colourSample.setBackgroundColor(item.colour)
        holder.checkBox.setOnClickListener {
            Log.d(TAG, "Item $position has been clicked")
            if (holder.checkBox.isChecked) {
                Log.d(TAG, "Item $position has been checked")
                deletedItemIndex.add(position)
            } else {
                Log.d(TAG, "Item $position has been unchecked")
                if (deletedItemIndex.contains(position)) {
                    deletedItemIndex.remove(position)
                }
            }
        }
    }

    override fun getItemCount() = dataset.size

    companion object {
        const val TAG = "DialogItemAdapter"
    }
}