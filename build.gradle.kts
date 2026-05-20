// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    kotlin("plugin.serialization") version "2.0.0" apply false // 2.0.0으로 수정
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false // 추가
    id("com.google.gms.google-services") version "4.4.1" apply false
}