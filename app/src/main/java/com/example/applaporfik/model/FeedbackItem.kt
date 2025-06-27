package com.example.applaporfik.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class FeedbackStatus {
    REPORTED,
    FINISHED
}

enum class FeedbackCategory {
    ACADEMIC,
    FACILITY
}

@Parcelize // Make it Parcelable if you need to pass it between components
data class FeedbackItem(
    val id: String, // Unique ID for the feedback
    val category: FeedbackCategory,
    val title: String, // "Judul"
    val date: String, // Consider using a Date/Timestamp object in a real app
    val description: String,
    var status: FeedbackStatus = FeedbackStatus.REPORTED, // Default status
    var adminFeedback: String? = null // Optional feedback from admin
) : Parcelable