package com.example.gosiru.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: Long,
    val user_id: String,
    val title: String,
    val body: String,
    val url: String? = null,
    val created_at: String? = null,

    @SerialName("apply_link") val applyLink: String? = null,

    @SerialName("is_read") val isRead: Boolean = false

)