// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        mavenCentral()
        maven("https://jitpack.io").content {
            //includeGroup("com.github.aasitnikov")
        }
        google()
    }
    dependencies {
        //classpath("com.github.aasitnikov:fat-aar-android:ce932b38ef")
    }
}
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.android.library") version "8.1.1" apply false
    id("com.b.m.default") version "0.0.1" apply false
    id("com.o.m.fataar") version "0.0.1" apply false
}