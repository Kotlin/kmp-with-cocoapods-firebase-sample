plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
}

group = "org.jetbrains.kotlin.firebase.sample"
version = "1.0-SNAPSHOT"

val firebaseVersion = libs.versions.firebase.get()

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

        pod("FirebaseCore", firebaseVersion)
        pod("FirebaseAuth", firebaseVersion)
        pod("FirebaseFirestore", firebaseVersion)
        pod("FirebaseFirestoreInternal", firebaseVersion)
        pod("FirebaseAnalytics", firebaseVersion)

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