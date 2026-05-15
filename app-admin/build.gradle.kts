import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    kotlin("plugin.serialization")
}

android {
    namespace = "com.example.app_admin"
    compileSdk = 35

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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- 슈파베이스 (Supabase) 추가 ---
    implementation(platform("io.github.jan-tennert.supabase:bom:2.5.0"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt") // DB용
    implementation("io.github.jan-tennert.supabase:gotrue-kt")    // 인증용

    // HTTP 통신을 위한 Ktor 엔진 (슈파베이스 필수)
    implementation("io.ktor:ktor-client-android:2.3.10")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}