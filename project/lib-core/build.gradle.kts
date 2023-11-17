import com.gaming.marcusx.AndroidConfig
import com.gaming.marcusx.AES
import org.json.JSONObject

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.orgi.game.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    //noinspection GradleDependency
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    //noinspection GradleDependency
    implementation("com.google.android.material:material:1.5.0")

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


    //compose相关依赖
    val composeVersion = "1.2.0"
    //系统ui控制器,用来修改状态栏颜色等等
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")
    //noinspection GradleDependency
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    //noinspection GradleDependency
    implementation("androidx.activity:activity-compose:1.7.2")
    //noinspection GradleDependency
    implementation("androidx.compose.ui:ui:${composeVersion}")
    //noinspection GradleDependency
    implementation("androidx.compose.ui:ui-tooling-preview:${composeVersion}")
    //noinspection GradleDependency
    implementation("androidx.compose.material:material:${composeVersion}")
    //noinspection GradleDependency
    implementation("androidx.compose.foundation:foundation:${composeVersion}")
}


//加密
tasks.register("aesEncrypted") {
    //./gradlew :lib-core:aesEncrypted
    group = "sdk"
    version = "0.0.1"
    doLast {
        println(">>>开始进行AES加密<<<\n${AndroidConfig.line}")
        val config = String(
            project.rootProject.file("config.json").readBytes()
        )
        val json = JSONObject(config)
        println("原始数据: \n$config")
        val encData = AES.encrypt(config.toByteArray(charset("UTF-8")))
        println("加密数据: \n${String(encData ?: byteArrayOf())}")

        val id = json.getString("id")
        val ver = json.getString("version")
        val file = project.file("../outputs/sdk-$id-$ver.enc")
        encData?.let(file::writeBytes)
        println("SDK配置文件生成: $file, 文件长度: ${file.length()}\n${AndroidConfig.line}")
    }
}


//解密
tasks.register("aesDecrypted") {
    //./gradlew :lib-core:aesDecrypted
    group = "sdk"
    version = "0.0.1"
    doLast {
        val config = String(
            project.rootProject.file("config.json").readBytes()
        )
        val json = JSONObject(config)
        val id = json.getString("id")
        val ver = json.getString("version")
        val file = project.file("../outputs/sdk-$id-$ver.enc")
        if (!file.exists()) {
            println("SDK配置文件不存在.")
            return@doLast
        }

        println(">>>开始进行AES解密<<<\n${AndroidConfig.line}")
        val data = file.readBytes()
        println("加密数据: \n${String(data)}")
        val encData = AES.decrypt(data)
        println("解密数据: \n${encData}")
    }
}