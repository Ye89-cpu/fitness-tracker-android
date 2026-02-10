package com.example.fitnesstracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnesstracker.databinding.ItemHistoryRowBinding

class HistoryAdapter(
    private var items: List<HistoryItem>,
    private val onEdit: (HistoryItem) -> Unit,
    private val onDelete: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    inner class VH(val binding: ItemHistoryRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemHistoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.binding.tvTitle.text = item.title
        holder.binding.tvSubtitle.text = item.subtitle

        holder.binding.btnEdit.setOnClickListener { onEdit(item) }
        holder.binding.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<HistoryItem>) {
        items = newList
        notifyDataSetChanged()
    }
}
