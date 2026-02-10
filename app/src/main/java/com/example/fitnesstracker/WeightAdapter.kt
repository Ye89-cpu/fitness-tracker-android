package com.example.fitnesstracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.databinding.FragmentLRSWeightBinding
import com.example.fitnesstracker.databinding.ItemWeightBinding

class WeightAdapter(private val weights: List<Int>) :
    RecyclerView.Adapter<WeightAdapter.WeightViewHolder>() {


    inner class WeightViewHolder(val binding: ItemWeightBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val binding = ItemWeightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WeightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        holder.binding.tvWeightItem.text = weights[position].toString()
    }

    override fun getItemCount(): Int = weights.size
}
