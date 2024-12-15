package com.example.sendsms

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sendsms.database.MessageDatabaseHelper

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: MessageDatabaseHelper
    private lateinit var phoneEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var selectFileButton: Button
    private lateinit var selectContactsButton: Button
    private lateinit var sendButton: Button
    private var selectedFileUri: Uri? = null

    // Khai báo selectFileLauncher
    private val selectFileLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Xử lý tệp đã chọn (ví dụ: lưu URI tệp vào selectedFileUri)
            if (uri != null) {
                selectedFileUri = uri
                Toast.makeText(this, "Tệp đã được chọn", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize database helper
        dbHelper = MessageDatabaseHelper(this)

        // Reference views
        phoneEditText = findViewById(R.id.editTextPhone)
        messageEditText = findViewById(R.id.editTextMessage)
        selectFileButton = findViewById(R.id.selectFileButton)
        selectContactsButton = findViewById(R.id.selectContactsButton)
        sendButton = findViewById(R.id.btnSend)

        // Request necessary permissions
        requestPermissionsIfNeeded()

        // Handle contact selection
        selectContactsButton.setOnClickListener {
            handleContactSelection()
        }

        // Handle file selection
        selectFileButton.setOnClickListener {
            // Mở bộ chọn tệp
            selectFileLauncher.launch("*/*")  // "*/" cho phép chọn bất kỳ tệp nào
        }

        // Send message button
        sendButton.setOnClickListener {
            sendMessage()
        }

        // Manage sent messages button
        findViewById<Button>(R.id.btnManageMessages).setOnClickListener {
            startActivity(Intent(this, SentMessagesActivity::class.java))
        }

        // Blocked numbers button
        findViewById<Button>(R.id.btnBlockedNumbers).setOnClickListener {
            startActivity(Intent(this, BlockedNumbersActivity::class.java))
        }
    }

    private fun sendMessage() {
        val phoneNumbers = phoneEditText.text.toString().split(",").map { it.trim() }
        val message = messageEditText.text.toString()

        // Check for blocked numbers
        val blockedNumbers = phoneNumbers.filter { dbHelper.isNumberBlocked(it) }
        if (blockedNumbers.isNotEmpty()) {
            Toast.makeText(this, "Một số điện thoại đã bị chặn", Toast.LENGTH_SHORT).show()
            return
        }

        if (phoneNumbers.isNotEmpty() && message.isNotEmpty()) {
            try {
                if (selectedFileUri == null) {
                    sendSMS(phoneNumbers, message)
                } else {
                    sendMMS(phoneNumbers, message, selectedFileUri!!)
                }

                // Save sent message to database
                dbHelper.insertSentMessage(
                    phoneNumbers.joinToString(", "),
                    message
                )

                // Clear message input after sending
                messageEditText.text.clear()
            } catch (e: Exception) {
                Toast.makeText(this, "Lỗi gửi tin nhắn: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Vui lòng nhập số điện thoại và tin nhắn!", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to send SMS
    private fun sendSMS(phoneNumbers: List<String>, message: String) {
        val smsManager = SmsManager.getDefault()
        phoneNumbers.forEach { phoneNumber ->
            try {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Log.d("MainActivity", "SMS sent to $phoneNumber")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to send SMS: ${e.message}")
            }
        }
    }

    // Method to send MMS
    private fun sendMMS(phoneNumbers: List<String>, message: String, fileUri: Uri) {
        val smsManager = SmsManager.getDefault()
        phoneNumbers.forEach { phoneNumber ->
            try {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "image/*" // You can change this type for other media types (e.g., video/*)
                intent.putExtra(Intent.EXTRA_TEXT, message)
                intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                intent.putExtra("address", phoneNumber)  // The recipient's phone number
                intent.putExtra("sms_body", message)

                startActivity(Intent.createChooser(intent, "Send MMS"))
                Log.d("MainActivity", "MMS sent to $phoneNumber")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to send MMS: ${e.message}")
            }
        }
    }

    // Request permissions if needed
    private fun requestPermissionsIfNeeded() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_PERMISSIONS
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_READ_CONTACTS && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val phoneNumber = cursor.getString(numberIndex)

                        // Lấy số điện thoại và thêm vào EditText
                        val currentNumbers = phoneEditText.text.toString()
                        val updatedNumbers = if (currentNumbers.isEmpty())
                            phoneNumber
                        else
                            "$currentNumbers, $phoneNumber"
                        phoneEditText.setText(updatedNumbers)
                    }
                }
            }
        }
    }

    // Handle contact selection
    private fun handleContactSelection() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        startActivityForResult(intent, REQUEST_READ_CONTACTS)
    }

    companion object {
        private const val REQUEST_READ_CONTACTS = 101
        private const val REQUEST_PERMISSIONS = 1
    }
}
