package com.example.sendsms.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sendsms.R

class BlockedNumbersAdapter(
    private val blockedNumbers: MutableList<String>, // Khai báo đúng
    private val onUnblockListener: (String) -> Unit  // Callback để xử lý unblocking
) : RecyclerView.Adapter<BlockedNumbersAdapter.BlockedNumberViewHolder>() {

    class BlockedNumberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val blockedNumber: TextView = view.findViewById(R.id.textViewBlockedNumber)
        val unblockButton: ImageButton = view.findViewById(R.id.btnUnblockNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedNumberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_blocked_number, parent, false)
        return BlockedNumberViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedNumberViewHolder, position: Int) {
        val number = blockedNumbers[position]
        holder.blockedNumber.text = number
        holder.unblockButton.setOnClickListener {
            onUnblockListener(number) // Gọi callback với số bị chặn
            blockedNumbers.removeAt(position) // Xóa số bị chặn khỏi danh sách
            notifyItemRemoved(position) // Thông báo RecyclerView cập nhật
        }
    }

    override fun getItemCount() = blockedNumbers.size
}
