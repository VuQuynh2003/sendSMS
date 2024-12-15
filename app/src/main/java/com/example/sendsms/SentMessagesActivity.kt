package com.example.sendsms

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sendsms.adapters.SentMessagesAdapter
import com.example.sendsms.database.MessageDatabaseHelper


class SentMessagesActivity : AppCompatActivity() {

    private lateinit var dbHelper: MessageDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SentMessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_sent_messages)

            // Khởi tạo database helper
            dbHelper = MessageDatabaseHelper(this)

            // Ánh xạ RecyclerView
            recyclerView = findViewById(R.id.recyclerViewMessages)

            // Lấy danh sách tin nhắn đã gửi từ cơ sở dữ liệu và chuyển sang MutableList
            val messages = dbHelper.getSentMessages().toMutableList()

            // Log số lượng tin nhắn để debug
            Log.d("SentMessagesActivity", "Số lượng tin nhắn đã gửi: ${messages.size}")

            // Kiểm tra nếu không có tin nhắn
            if (messages.isEmpty()) {
                Toast.makeText(this, "Không có tin nhắn nào được gửi", Toast.LENGTH_SHORT).show()
            }

            // Thiết lập adapter cho RecyclerView
            adapter = SentMessagesAdapter(messages)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter

        } catch (e: Exception) {
            // Ghi log lỗi chi tiết
            Log.e("SentMessagesActivity", "Lỗi khi tạo activity", e)

            // Hiển thị thông báo lỗi cho người dùng
            Toast.makeText(
                this,
                "Có lỗi xảy ra: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Thêm phương thức refresh để cập nhật lại danh sách tin nhắn nếu cần
    fun refreshMessagesList() {
        val messages = dbHelper.getSentMessages().toMutableList()
        adapter.updateMessages(messages)
    }
}