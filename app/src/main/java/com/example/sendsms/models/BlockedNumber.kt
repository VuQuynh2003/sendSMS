package com.example.sendsms.models

data class BlockedNumber(
    val number: String,
    val reason: String = "User blocked"
)