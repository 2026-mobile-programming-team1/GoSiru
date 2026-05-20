package com.example.app_admin.data

import kotlinx.serialization.Serializable

@Serializable
data class WelfareRequest(
    val title: String,
    val content: String,
    val min_age: Int? = null,
    val max_age: Int? = null,
    val target_job: String? = null,
    val income_limit: Int? = null,
    val gender: String? = null,
    val is_disabled_only: Boolean = false,
    val is_foreigner_only: Boolean = false
)