package com.example.sendsms.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sendsms.R
import com.example.sendsms.models.Message

// Adapter to display sent messages
class SentMessagesAdapter(
    private var messages: MutableList<Message>
) : RecyclerView.Adapter<SentMessagesAdapter.MessageViewHolder>() {

    // ViewHolder to manage items in the list
    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val phoneNumbers: TextView = view.findViewById(R.id.textViewPhoneNumbers)
        val messageContent: TextView = view.findViewById(R.id.textViewMessageContent)
        val timestamp: TextView = view.findViewById(R.id.textViewTimestamp)
    }

    // Create ViewHolder for each item in RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sent_message, parent, false)
        return MessageViewHolder(view)
    }

    // Bind data to ViewHolder
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.phoneNumbers.text = message.phoneNumbers
        holder.messageContent.text = message.content
        holder.timestamp.text = message.timestamp
    }

    // Update messages list and notify adapter
    fun updateMessages(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }

    // Return number of items in RecyclerView
    override fun getItemCount(): Int = messages.size
}