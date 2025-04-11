buildscript {
    //Use this block to define project level buildConfig properties
    extra["HOST"] = "192.168.0.102"
    extra["PORT"] = "3000"
    extra["BASE_URL"] = "http://${extra["HOST"]}:${extra["PORT"]}/"
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.android.library) apply false
}