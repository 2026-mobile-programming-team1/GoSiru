package com.example.app_admin.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WelfareRequest(
    val title: String,
    val content: String,

    // 💡 관리자 앱에서 입력한 '주소'가 저장될 핵심 필드입니다.
    // DB의 apply_link 컬럼과 매핑됩니다.
    @SerialName("apply_link") val apply_link: String,

    val min_age: Int? = null,
    val max_age: Int? = null,
    val target_job: String? = null,
    val income_limit: Int? = null,
    val gender: String? = null,
    val is_disabled_only: Boolean = false,
    val is_foreigner_only: Boolean = false
)
//
///**
// * 💡  레포지토리에서 insert 후 생성된 ID를 받아오기 위해
// *  WelfareResponse 클래스도 같은 파일 하단에 선언
// */
//@Serializable
//data class WelfareResponse(
//    val id: Int
//)