import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    kotlin("plugin.serialization")
//    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.gosiru"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }
    // --- 서명 설정 (팀원 공통 키 사용) ---
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "com.example.gosiru"
        minSdk = 35
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        ndk {
            // 실제 기기(arm)와 에뮬레이터(x86) 모두 대응
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//      local.properties에서 키 값 가져오기
        val properties = Properties().apply {
            val propertiesFile = project.rootProject.file("local.properties")
            if (propertiesFile.exists()) {
                load(propertiesFile.inputStream())
            }
        }
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${properties.getProperty("SUPABASE_KEY")}\"")
        val kakaoKey = properties.getProperty("KAKAO_APP_KEY") ?: ""

        // 2. 읽어온 키를 매니페스트의 ${KAKAO_APP_KEY} 자리에 주입
        manifestPlaceholders["KAKAO_APP_KEY"] = kakaoKey
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }



    // 뷰 바인딩 활성화
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // --- 카카오맵 V2 ---
    implementation("com.kakao.maps.open:android:2.11.0")

    // Supabase (Bom을 쓰면 버전 관리가 편해)
    implementation(platform("io.github.jan-tennert.supabase:bom:2.5.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt") // DB용
    implementation("io.github.jan-tennert.supabase:gotrue-kt")    // 인증용(선택)

    // HTTP 통신을 위한 Ktor 엔진
    implementation("io.ktor:ktor-client-android:2.3.10")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}