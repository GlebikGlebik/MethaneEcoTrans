plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.methane.eco.trans"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.met"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Ktor Client для HTTP запросов
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("io.ktor:ktor-client-logging:2.3.7")

    // DataStore для хранения токенов (вместо SharedPreferences)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material:1.6.8")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-common-ktx")

    // Google Auth
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // базовые
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.0")
    implementation("androidx.compose.ui:ui:1.8.1")
    implementation("androidx.compose.material:material:1.8.1")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.material:material:1.8.1")
    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-auth:23.2.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.2.0")
    implementation("com.google.firebase:firebase-auth:23.2.0")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    //realtimeDatabase
    implementation("com.google.firebase:firebase-database:21.0.0")
    // графики
    implementation("com.github.madrapps:plot:0.1.1")
    implementation("com.patrykandpatrick.vico:compose:1.12.0")
    implementation("com.patrykandpatrick.vico:core:1.12.0")
    // JDBC драйвер
    implementation("org.postgresql:postgresql:42.7.7")
}