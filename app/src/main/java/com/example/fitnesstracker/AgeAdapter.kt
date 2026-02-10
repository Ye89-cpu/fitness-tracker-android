package com.example.fitnesstracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.databinding.ItemAgeBinding

class AgeAdapter(private val ages: List<Int>) :
    RecyclerView.Adapter<AgeAdapter.AgeViewHolder>() {

    inner class AgeViewHolder(val binding: ItemAgeBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AgeViewHolder {
        val binding = ItemAgeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AgeViewHolder, position: Int) {
        holder.binding.tvAgeItem.text = ages[position].toString()
    }

    override fun getItemCount(): Int = ages.size
}