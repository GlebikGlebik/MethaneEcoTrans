buildscript{
    dependencies{
        classpath(libs.google.services)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("com.android.library") version "7.4.2" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}