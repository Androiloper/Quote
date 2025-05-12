package com.example.quotex

data class Promise(
    val id: Long = System.currentTimeMillis(), // Unique identifier
    val title: String,
    val verse: String,
    val reference: String
)
