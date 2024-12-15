package com.example.sendsms.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.sendsms.models.Message
import android.util.Log


class MessageDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        // Tạo bảng chặn số điện thoại
        db.execSQL(
            "CREATE TABLE $TABLE_BLOCKED_NUMBERS (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_PHONE_NUMBER TEXT UNIQUE)"
        )

        // Tạo bảng lưu tin nhắn
        db.execSQL(
            "CREATE TABLE $TABLE_SENT_MESSAGES (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_PHONE_NUMBER TEXT, " +
                    "$COLUMN_MESSAGE TEXT, " +
                    "$COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BLOCKED_NUMBERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SENT_MESSAGES")
        onCreate(db)
    }

    fun isNumberBlocked(phoneNumber: String): Boolean {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_BLOCKED_NUMBERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_PHONE_NUMBER = ?",
            arrayOf(phoneNumber),
            null,
            null,
            null
        )
        val isBlocked = cursor.moveToFirst()
        cursor.close()
        return isBlocked
    }

    // Phương thức chèn tin nhắn vào cơ sở dữ liệu
    fun insertSentMessage(phoneNumber: String, message: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PHONE_NUMBER, phoneNumber)
            put(COLUMN_MESSAGE, message)
        }
        db.insert(TABLE_SENT_MESSAGES, null, values)
        db.close()
    }

    // Phương thức lấy tất cả tin nhắn đã gửi từ cơ sở dữ liệu
    fun getSentMessages(): List<Message> {
        val messagesList = mutableListOf<Message>()
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_SENT_MESSAGES,
            arrayOf(COLUMN_PHONE_NUMBER, COLUMN_MESSAGE, COLUMN_TIMESTAMP),
            null, null, null, null, null
        )

        val columnNames = cursor.columnNames
        Log.d("MessageDatabaseHelper", "Columns: ${columnNames.joinToString(", ")}")

        if (cursor.moveToFirst()) {
            do {
                val phoneNumberIndex = cursor.getColumnIndex(COLUMN_PHONE_NUMBER)
                val messageContentIndex = cursor.getColumnIndex(COLUMN_MESSAGE)
                val timestampIndex = cursor.getColumnIndex(COLUMN_TIMESTAMP)

                if (phoneNumberIndex >= 0 && messageContentIndex >= 0 && timestampIndex >= 0) {
                    val phoneNumber = cursor.getString(phoneNumberIndex)
                    val messageContent = cursor.getString(messageContentIndex)
                    val timestamp = cursor.getString(timestampIndex)
                    // Add message to the list
                    messagesList.add(Message(phoneNumbers = phoneNumber, content = messageContent, timestamp = timestamp, fileUri = null))
                } else {
                    Log.e("MessageDatabaseHelper", "Một hoặc nhiều cột không hợp lệ..")
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return messagesList
    }


    // Thêm số điện thoại vào danh sách bị chặn
    fun blockNumber(phoneNumber: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PHONE_NUMBER, phoneNumber)
        }
        db.insert(TABLE_BLOCKED_NUMBERS, null, values)
        db.close()
    }

    // Xóa số điện thoại khỏi danh sách bị chặn
    fun unblockNumber(phoneNumber: String) {
        val db = writableDatabase
        db.delete(TABLE_BLOCKED_NUMBERS, "$COLUMN_PHONE_NUMBER = ?", arrayOf(phoneNumber))
        db.close()
    }


    companion object {
        private const val DATABASE_NAME = "sms_app.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_BLOCKED_NUMBERS = "blocked_numbers"
        const val COLUMN_ID = "id"
        const val COLUMN_PHONE_NUMBER = "phone_number"

        const val TABLE_SENT_MESSAGES = "sent_messages"
        const val COLUMN_MESSAGE = "message"
        const val COLUMN_TIMESTAMP = "timestamp"
    }
}
