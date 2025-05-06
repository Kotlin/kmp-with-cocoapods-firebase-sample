import org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
}

group = "org.jetbrains.kotlin.firebase.sample"
version = "1.0-SNAPSHOT"

val firebaseConfig: CocoapodsExtension.CocoapodsDependency.() -> Unit = {
    version = "11.12.0"
    extraOpts += listOf("-compiler-option", "-fmodules")
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Kotlin sample project with CocoaPods Firebase dependencies"
        homepage = "https://github.com/Kotlin/kotlin-with-cocoapods-firebase-sample"

        podfile = project.file("../iosApp/Podfile")

        ios.deploymentTarget = "16.6"
        osx.deploymentTarget = "13.5"
        tvos.deploymentTarget = "16.6"
        watchos.deploymentTarget = "9.6"

        pod("FirebaseCore", firebaseConfig)
        pod("FirebaseAuth", firebaseConfig)
        pod("FirebaseFirestore", firebaseConfig)
        pod("FirebaseFirestoreInternal", firebaseConfig)
        pod("FirebaseAnalytics", firebaseConfig)

        framework {
            baseName = "KotlinLibrary"
            isStatic = true
        }
    }

    sourceSets {
        all {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }
}