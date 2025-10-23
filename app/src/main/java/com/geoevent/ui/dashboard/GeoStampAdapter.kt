package com.geoevent.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.geoevent.data.model.GeoStamp
import com.geoevent.databinding.ItemGeostampBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeoStampAdapter : ListAdapter<GeoStamp, GeoStampAdapter.GeoStampViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeoStampViewHolder {
        val binding = ItemGeostampBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GeoStampViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GeoStampViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GeoStampViewHolder(private val binding: ItemGeostampBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(geoStamp: GeoStamp) {
            binding.textStampId.text = "ID: ${geoStamp.id.take(8)}..."

            val locationText = if (geoStamp.latitude != null && geoStamp.longitude != null) {
                "Location: ${String.format("%.4f", geoStamp.latitude)}, ${String.format("%.4f", geoStamp.longitude)}"
            } else {
                "Location: Not available"
            }
            binding.textLocation.text = locationText

            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = Date(geoStamp.timestamp)
            binding.textTimestamp.text = "Created: ${dateFormat.format(date)}"

            binding.textEventStatus.text = if (geoStamp.geoEventId != null) {
                "Linked to event: ${geoStamp.geoEventId.take(8)}..."
            } else {
                "No event linked"
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<GeoStamp>() {
        override fun areItemsTheSame(oldItem: GeoStamp, newItem: GeoStamp): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GeoStamp, newItem: GeoStamp): Boolean {
            return oldItem == newItem
        }
    }
}
