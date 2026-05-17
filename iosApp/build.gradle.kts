plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    sourceSets {
        val iosMain by getting {
            dependencies {
                implementation(project(":composeApp"))
            }
        }
    }
}

android {
    namespace = "com.anxincaiguan.iosapp"
    compileSdk = 34
}