package com.example.gosiru.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("birth_date") val birthDate: String,
    val gender: String,
    @SerialName("job_status") val jobStatus: String? = null,
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
    val content: String? = null, // 기본값 추가
    @SerialName("min_age") val minAge: Int? = null, // 기본값 추가
    @SerialName("max_age") val maxAge: Int? = null, // 기본값 추가
    @SerialName("target_job") val targetJob: String? = null, // 기본값 추가
    @SerialName("income_limit") val incomeLimit: Int? = null, // 기본값 추가
    val amount: String? = null, // 기본값 추가
    @SerialName("is_siru") val isSiru: Boolean? = null, // 기본값 추가
    val gender: String? = null, // 기본값 추가
    @SerialName("apply_link") val applyLink: String? = null, // 기본값 추가
    @SerialName("is_disabled_only") val isDisabledOnly: Boolean? = null, // 기본값 추가
    @SerialName("is_foreigner_only") val isForeignerOnly: Boolean? = null // 기본값 추가
)