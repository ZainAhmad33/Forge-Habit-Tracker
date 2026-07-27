// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    // alias(libs.plugins.kotlin.android)      // <-- Add this
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)                 // Uses libs alias
    alias(libs.plugins.hilt)                // Uses libs alias
}

android {
    namespace = "com.example.habitz"
    compileSdk = 36 // Cleaned up syntax for standard API levels

    defaultConfig {
        applicationId = "com.example.habitz"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}
dependencies {
    // 1. Compose BOM (Manages Material 3 & Compose versions)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.activity.compose)

    // Remove the hardcoded material3-android alpha line to avoid version conflicts with the BOM:
    implementation("androidx.compose.material3:material3-android:1.5.0-alpha01")

    implementation("androidx.compose.material:material-icons-extended")

    // 2. Compose Emoji Picker
    implementation("com.github.alexdametto:compose-emoji-picker:1.0.0")

    // Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Tooling & Testing
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}