package com.example.gosiru.data // 패키지명 확인!

import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: Long,
    val user_id: String,
    val title: String,
    val body: String,
    val url: String? = null,
    val created_at: String? = null
)