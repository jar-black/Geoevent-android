package com.geoevent.ui.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.geoevent.data.model.ChatMessage
import com.geoevent.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesAdapter(
    private val currentUserId: String
) : ListAdapter<ChatMessage, MessagesAdapter.MessageViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding, currentUserId)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(
        private val binding: ItemMessageBinding,
        private val currentUserId: String
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.textContent.text = message.content

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = Date(message.timestamp)
            binding.textTimestamp.text = timeFormat.format(time)

            // Align message based on sender
            val layoutParams = binding.cardMessage.layoutParams as ConstraintLayout.LayoutParams
            if (message.userId == currentUserId) {
                // My message - align right
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                layoutParams.marginStart = 48
                layoutParams.marginEnd = 8
            } else {
                // Other's message - align left
                layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
                layoutParams.marginStart = 8
                layoutParams.marginEnd = 48
            }
            binding.cardMessage.layoutParams = layoutParams
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
