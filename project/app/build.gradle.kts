import com.gaming.marcusx.Maven

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

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")

    implementation("androidx.annotation:annotation:1.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")


    //基础库
    implementation("androidx.security:security-crypto:1.0.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.geyifeng.immersionbar:immersionbar:3.2.2")
    //noinspection GradleDependency
    implementation("com.squareup.okhttp3:okhttp:3.12.2")

    //业务核心库
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
    implementation("com.google.zxing:core:3.4.0")
    implementation("cn.thinkingdata.android:ThinkingAnalyticsSDK:2.8.3")
    implementation("com.adjust.sdk:adjust-android:4.33.0")

    val usingRemote = false
    if (usingRemote) {
        implementation("io.github.marcusx2007.origi:game-sdk:${Maven.version}")
    }else {
        val file = project.rootProject.file("output/${Maven.aar}")
        if (file.exists()) {
            implementation(files(file))
        }else {
            implementation(project(":lib-core"))
        }
    }

}