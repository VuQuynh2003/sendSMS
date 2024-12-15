package com.example.sendsms

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sendsms.adapters.BlockedNumbersAdapter
import com.example.sendsms.database.MessageDatabaseHelper

class BlockedNumbersActivity : AppCompatActivity() {  // ke thua appcompatactivity
    private lateinit var dbHelper: MessageDatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var blockedNumbersAdapter: BlockedNumbersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_numbers)

        dbHelper = MessageDatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerViewBlockedNumbers)
        val editTextBlockNumber: EditText = findViewById(R.id.editTextBlockNumber)
        val btnAddBlockedNumber: Button = findViewById(R.id.btnAddBlockedNumber)

        val blockedNumbers = mutableListOf<String>()

        blockedNumbersAdapter = BlockedNumbersAdapter(blockedNumbers) { number ->
            dbHelper.unblockNumber(number)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = blockedNumbersAdapter

        btnAddBlockedNumber.setOnClickListener {
            val numberToBlock = editTextBlockNumber.text.toString().trim()
            if (numberToBlock.isNotEmpty()) {
                dbHelper.blockNumber(numberToBlock)
                blockedNumbers.add(numberToBlock)
                blockedNumbersAdapter.notifyItemInserted(blockedNumbers.size - 1)
                editTextBlockNumber.text.clear()
            }
        }
    }
}
