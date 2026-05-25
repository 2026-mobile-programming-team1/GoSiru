package com.example.gosiru.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: Long,
    val user_id: String? = null, // null 허용 (전체 공지용)
    val title: String,
    val body: String,
    // val url: String? = null, // 이것도 안 쓴다면 삭제 권장
    val created_at: String? = null,

    // 💡 이제 이 녀석이 핵심입니다. welfare_list 테이블의 ID와 매칭됩니다.
    @SerialName("welfare_id") val welfareId: Int? = null,

    @SerialName("is_read") val isRead: Boolean = false
)