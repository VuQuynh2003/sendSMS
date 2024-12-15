package com.example.sendsms.models

data class Message(
    val phoneNumbers: String,
    val content: String,
    val timestamp: String,
    val fileUri: String? = null
)
