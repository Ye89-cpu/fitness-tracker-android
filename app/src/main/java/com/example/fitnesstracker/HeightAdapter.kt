package com.example.fitnesstracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.databinding.ItemHeightBinding


class HeightAdapter(private val heights: List<Int>) :
    RecyclerView.Adapter<HeightAdapter.HeightViewHolder>() {

    inner class HeightViewHolder(val binding: ItemHeightBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeightViewHolder {
        val binding = ItemHeightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HeightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeightViewHolder, position: Int) {
        holder.binding.tvHeightItem.text = heights[position].toString()
    }

    override fun getItemCount(): Int = heights.size
}
