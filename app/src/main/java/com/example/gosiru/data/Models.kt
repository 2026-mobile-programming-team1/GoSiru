package com.example.gosiru.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String, // Supabase Auth의 유저 UID
    @SerialName("birth_date") val birthDate: String, // DB 타입이 date이므로 "YYYY-MM-DD" 포맷
    val gender: String,
    @SerialName("job_status") val jobStatus: String?,
    @SerialName("income_level") val incomeLevel: Int,
    @SerialName("is_admin") val isAdmin: Boolean? = false,
    @SerialName("household_count") val householdCount: Int? = null,
    @SerialName("is_disabled") val isDisabled: Boolean = false,
    @SerialName("is_foreigner") val isForeigner: Boolean = false
)

@Serializable
data class WelfareItem(
    val id: Long,
    val title: String,
    val content: String?,
    @SerialName("min_age") val minAge: Int?,
    @SerialName("max_age") val maxAge: Int?,
    @SerialName("target_job") val targetJob: String?,
    @SerialName("income_limit") val incomeLimit: Int?,
    val amount: String?,
    @SerialName("is_siru") val isSiru: Boolean?,
    val gender: String?,
    @SerialName("apply_link") val applyLink: String?,
    @SerialName("is_disabled_only") val isDisabledOnly: Boolean?,
    @SerialName("is_foreigner_only") val isForeignerOnly: Boolean?
)