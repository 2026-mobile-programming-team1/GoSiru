import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.app_admin"
    compileSdk = 36


    defaultConfig {
        applicationId = "com.example.app_admin"
        minSdk = 35
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties에서 키 값 가져오기
        val properties = Properties().apply {
            val propertiesFile = project.rootProject.file("local.properties")
            if (propertiesFile.exists()) {
                load(propertiesFile.inputStream())
            }
        }
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${properties.getProperty("SUPABASE_KEY")}\"")
    }

    buildTypes {
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

    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("io.github.jan-tennert.supabase:compose-auth-ui:2.5.0")
    // --- 슈파베이스 (Supabase) 추가 ---
    implementation(platform("io.github.jan-tennert.supabase:bom:2.5.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt") // DB용
    implementation("io.github.jan-tennert.supabase:gotrue-kt")    // 인증용

    // HTTP 통신을 위한 Ktor 엔진 (슈파베이스 필수)
    implementation("io.ktor:ktor-client-android:2.3.10")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    // 슈파베이스 Auth UI
    implementation("io.github.jan-tennert.supabase:compose-auth-ui:2.5.0")
}