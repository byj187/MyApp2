plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    sourceSets {
        val iosMain by getting {
            dependencies {
                implementation(project(":composeApp"))
            }
        }
    }
}