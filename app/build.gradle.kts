plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.batterypet.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.batterypet.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
