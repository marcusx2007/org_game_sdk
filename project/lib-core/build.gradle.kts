import com.gaming.marcusx.AndroidConfig
import com.gaming.marcusx.AES
import org.json.JSONObject
import com.gaming.marcusx.Maven

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
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

/*************************** 自定义Gradle Task ******************************/

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

/*************************** Maven Publish ******************************/
tasks.register("makeAAR", Jar::class) {
    group = "sdk"
    val variant = "Release"
    dependsOn("assemble$variant")
    doLast {
        val fileName = project.name.plus("-").plus(variant.lowercase()).plus(".aar")
        val source = File(buildDir, "outputs/aar/$fileName")
        if (!source.exists()) {
            throw IllegalStateException("$fileName not found")
        }
        val targetFile = File("../output", "game-sdk${Maven.version}.aar")
        //source.copyTo(targetFile)
        destinationDirectory.set(file("../output"))
        outputs.file(targetFile.parentFile)
        copy {
            from(source)
            into(targetFile)
        }
    }
}

publishing {
    publications {
        val config = Maven.config(project)
        create<MavenPublication>(Maven.name) {
            groupId = config.getString("gid")
            artifactId = config.getString("aid")
            version = Maven.version

            //project object model. 用来定义项目元数据和依赖关系的xml文件.
            //项目元数据包含 groupId,artifactId,version.
            pom {
                name.set(config.getString("pmn"))
                description.set(config.getString("pmd"))
                url.set(config.getString("pmu"))
                inceptionYear.set(config.getString("pmy"))
            }
            artifact(tasks.findByName("makeAAR"))
        }

        repositories {
            maven {
                setUrl(config.getString("central"))
                credentials {
                    username = config.getString("user")
                    password = config.getString("pass")
                }
            }
        }
    }
}


signing {
    sign(publishing.publications.maybeCreate(Maven.name))
}