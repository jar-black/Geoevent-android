package com.geoevent.ui.geostamps

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.geoevent.data.model.GeoStamp
import com.geoevent.databinding.ItemGeostampActionsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GeoStampsAdapter(
    private val onDeleteClick: (GeoStamp) -> Unit
) : ListAdapter<GeoStamp, GeoStampsAdapter.GeoStampViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeoStampViewHolder {
        val binding = ItemGeostampActionsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GeoStampViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: GeoStampViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GeoStampViewHolder(
        private val binding: ItemGeostampActionsBinding,
        private val onDeleteClick: (GeoStamp) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

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

            binding.buttonDelete.setOnClickListener {
                onDeleteClick(geoStamp)
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
