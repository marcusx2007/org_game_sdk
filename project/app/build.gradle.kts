import com.gaming.marcusx.Maven
import com.gaming.marcusx.AndroidConfig
import org.gradle.internal.Cast.uncheckedCast

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.org.marcus.x"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.org.marcus.x"
        minSdk = 24
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.maybeCreate("Release").apply {
                keyAlias = "marcus"
                keyPassword = "marcus2023"
                storeFile = file("../app.jks")
                storePassword = "marcus2023"
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

fun DependencyHandler.core(): ProjectDependency {
    return project(":lib-core")
}

dependencies {
    //noinspection GradleDependency
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    //noinspection GradleDependency
    implementation("androidx.annotation:annotation:1.1.0")

    if (AndroidConfig.usingRemoteLib) {
        implementation("io.github.marcusx2007.origi:game-sdk:${Maven.version}")
    } else {
        implementation(core())
    }
}