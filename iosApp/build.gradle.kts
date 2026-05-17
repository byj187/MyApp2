plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    iosArm64("ios")
    iosX64("iosX64")
    iosSimulatorArm64("iosSimulatorArm64")

    sourceSets {
        val iosMain by getting {
            dependencies {
                implementation(project(":composeApp"))
            }
        }
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}

compose {
    kotlinCompilerPlugin.set(dependencies.composeCompiler.get().toString())
}