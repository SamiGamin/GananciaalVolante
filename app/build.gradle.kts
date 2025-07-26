@file:Suppress("DEPRECATION")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.martinez.gananciaalvolante"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.martinez.gananciaalvolante"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- Arquitectura MVVM ---
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.2")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.2")
    // Navigation Component (para manejar la navegación entre Fragments)
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.2")

    // --- Base de Datos Local (Room) ---

    implementation("androidx.room:room-runtime:2.7.2")
    annotationProcessor("androidx.room:room-compiler:2.7.2")
    // Para usar KSP en lugar de KAPT (más rápido), se configura un poco diferente
    // ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:2.7.2") // Soporte para Coroutines y Flow
    ksp("androidx.room:room-compiler:2.7.2")

    // --- Coroutines para trabajo en segundo plano ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.2")

    implementation("com.google.dagger:hilt-android:2.57")
    ksp("com.google.dagger:hilt-compiler:2.57")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(libs.androidx.preference)
}