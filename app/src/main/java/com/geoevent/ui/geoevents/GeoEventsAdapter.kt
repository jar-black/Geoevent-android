package com.geoevent.ui.geoevents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.geoevent.data.model.GeoEvent
import com.geoevent.databinding.ItemGeoeventBinding

class GeoEventsAdapter(
    private val onViewChatClick: (GeoEvent) -> Unit,
    private val onDeleteClick: (GeoEvent) -> Unit
) : ListAdapter<GeoEvent, GeoEventsAdapter.GeoEventViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeoEventViewHolder {
        val binding = ItemGeoeventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GeoEventViewHolder(binding, onViewChatClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: GeoEventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GeoEventViewHolder(
        private val binding: ItemGeoeventBinding,
        private val onViewChatClick: (GeoEvent) -> Unit,
        private val onDeleteClick: (GeoEvent) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(geoEvent: GeoEvent) {
            binding.textEventId.text = "Event: ${geoEvent.id.take(8)}..."
            binding.textUserId.text = "Created by: ${geoEvent.userId.take(8)}..."

            binding.buttonViewChat.setOnClickListener {
                onViewChatClick(geoEvent)
            }

            binding.buttonDelete.setOnClickListener {
                onDeleteClick(geoEvent)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<GeoEvent>() {
        override fun areItemsTheSame(oldItem: GeoEvent, newItem: GeoEvent): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GeoEvent, newItem: GeoEvent): Boolean {
            return oldItem == newItem
        }
    }
}
