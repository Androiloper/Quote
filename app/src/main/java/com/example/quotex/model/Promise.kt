package com.example.quotex.model

data class Promise(
    val id: Long = System.currentTimeMillis(), // Unique identifier
    val title: String,
    val verse: String,
    val reference: String
)
