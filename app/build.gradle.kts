import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.hilt)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

// Load secrets from secrets.properties
val secretsFile = rootProject.file("secrets.properties")
val secretsProperties = Properties().apply {
    if (secretsFile.exists()) {
        load(FileInputStream(secretsFile))
    }
}

val localProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}


android {
    namespace = "org.classapp.bookmark"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.classapp.bookmark"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        debug {
            buildConfigField(
                "String",
                "GOOGLE_BOOKS_API_KEY",
                "\"${secretsProperties["GOOGLE_BOOKS_API_KEY"]}\""
            )
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField(
                "String",
                "GOOGLE_BOOKS_API_KEY",
                "\"${secretsProperties["GOOGLE_BOOKS_API_KEY"]}\""
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // 1. Android / UI Basics
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)

    // 2. Dependency Injection (Hilt)
    implementation(libs.javax.inject)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // 3. Firebase (Cleaned Up)
    // ONLY ONE BoM call. We will use the modern non-ktx versions (they include Kotlin support now)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-safetynet:18.0.1")

    // 4. Networking / Utils
    implementation(libs.adapter.guava)
    implementation(libs.converter.gson)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // 5. Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}


