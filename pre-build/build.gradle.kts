
buildscript {
    dependencies {
        //classpath("com.bugsnag:bugsnag-android-gradle-plugin:7.0.0")
    }
}

plugins {
    id("groovy")
    id("java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version ("1.8.21")
}

val bundletool = "com.android.tools.build:bundletool:0.10.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

//添加kotlin源代码路径
sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
            include("**/*.java")
        }
        kotlin {
            srcDirs("src/main/kotlin")
            include("**/*.kt")
        }
        groovy {
            srcDirs("src/main/kotlin")
            include("**/*.groovy")
        }
    }
}

repositories {
    mavenCentral()
    google()
}

gradlePlugin {
    plugins {
        create("DependenciesPlugin") {
            id = "com.b.m.default"
            version = "0.0.1"
            implementationClass = "com.gaming.marcusx.plugins.DefaultPlugin"
        }
        create("FatAarPlugin") {
            id = "com.o.m.fataar"
            version = "0.0.1"
            implementationClass = "com.kezong.fataar.FatAarPlugin"
        }
    }
}

dependencies {
    gradleApi()
    localGroovy()
    //noinspection GradleDependency
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.21")
    //noinspection GradleDependency
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.json:json:20210307") // JSON 库的依赖
    implementation("com.android.tools.build:gradle:7.2.0")
    implementation("org.javassist:javassist:3.27.0-GA")
    implementation("com.squareup:javapoet:1.13.0")
    //noinspection GradleDependency
    implementation("com.android.tools:common:30.2.0")
    //noinspection GradleDependency
    implementation("org.ow2.asm:asm-commons:9.2")
}