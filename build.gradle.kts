// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    kotlin("plugin.serialization") version "1.9.24" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}