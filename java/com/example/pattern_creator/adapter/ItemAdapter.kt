package com.example.pattern_creator.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.pattern_creator.R
import com.example.pattern_creator.model.Colour
import com.example.pattern_creator.model.PatternViewModelKotlin

/**
 * Adapter for the [RecyclerView] in [ColoursFragment]. Displays [Colour] data object.
 */
class ItemAdapter(
    private val context: Context,
    private val dataset: MutableList<Colour>,
    private val sharedViewModel: PatternViewModelKotlin
    ) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.itemCheckBox)
        val colourSample: Button = view.findViewById(R.id.colourSample)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset.elementAt(position)
        holder.checkBox.text = item.name
        holder.checkBox.isChecked = item.checked
        holder.colourSample.setBackgroundColor(item.colour)
        holder.checkBox.setOnClickListener {
            sharedViewModel.checkItem(position)
        }

    }

    override fun getItemCount() =  dataset.size
}