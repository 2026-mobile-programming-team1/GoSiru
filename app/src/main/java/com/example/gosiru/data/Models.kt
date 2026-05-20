package com.example.gosiru.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WelfareItem(
    val id: Long,
    val title: String,
    val content: String,
    @SerialName("min_age") val minAge: Int,
    @SerialName("max_age") val maxAge: Int,
    @SerialName("target_job") val targetJob: String,
    @SerialName("income_limit") val incomeLimit: Int,
    val amount: String,
    @SerialName("is_siru") val isSiru: Boolean,
    @SerialName("apply_link") val applyLink: String
)

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("birth_date") val birthDate: String,
    @SerialName("job_status") val jobStatus: String,
    @SerialName("income_level") val incomeLevel: Int,
    @SerialName("is_admin") val isAdmin: Boolean,
    @SerialName("fcm_token") val fcmToken: String? = null
)