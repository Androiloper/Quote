package com.example.quotex.model

data class ProverbChapter(
    val chapter: Int,
    val verses: List<ProverbVerse>
)

data class ProverbVerse(
    val verse: Int,
    val text: String
)