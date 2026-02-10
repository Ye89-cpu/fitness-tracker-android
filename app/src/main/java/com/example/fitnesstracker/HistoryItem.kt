package com.example.fitnesstracker

data class HistoryItem(
    val id: Int,
    val type: String, // "activity" or "workout"
    val title: String,
    val subtitle: String,
    val startTime: String = "",
    val endTime: String = "",
    val durationMin: Int = 0,
    val intensity: Int = 0,
    val reps: Int = 0,
    val weightLifted: String = "",
    val notes: String = ""
)
