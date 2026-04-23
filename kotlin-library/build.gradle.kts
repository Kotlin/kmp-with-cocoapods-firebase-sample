@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

group = "org.jetbrains.kotlin.firebase.sample"
version = "1.0-SNAPSHOT"

val firebaseVersion = libs.versions.firebase.get()

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KotlinLibrary"
            isStatic = true
        }
    }

    swiftPMDependencies {
        iosMinimumDeploymentTarget = "16.6"
        discoverClangModulesImplicitly = false

        swiftPackage(
            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
            version = from(firebaseVersion),
            products = listOf(
                product("FirebaseAI"),
                product("FirebaseAnalytics"),
                product("FirebaseAppCheck"),
                product("FirebaseAppDistribution-Beta"),
                product("FirebaseAuth"),
                product("FirebaseCore"),
                product("FirebaseCrashlytics"),
                product("FirebaseDatabase"),
                product("FirebaseFirestore"),
                product("FirebaseFunctions"),
                product("FirebaseInAppMessaging-Beta"),
                product("FirebaseInstallations"),
                product("FirebaseMessaging"),
                product("FirebaseMLModelDownloader"),
                product("FirebasePerformance"),
                product("FirebaseRemoteConfig"),
                product("FirebaseStorage"),
            ),
            importedClangModules = listOf(
                "FirebaseABTesting",
                "FirebaseAnalytics",
                "FirebaseAppCheck",
                "FirebaseAppDistribution",
                "FirebaseAuth",
                "FirebaseCore",
                "FirebaseCrashlytics",
                "FirebaseDatabaseInternal",
                "FirebaseFirestoreInternal",
                "FirebaseInAppMessagingInternal",
                "FirebaseInstallations",
                "FirebaseMessaging",
                "FirebasePerformance",
                "FirebaseRemoteConfigInternal",
                "FirebaseStorage",
            ),
        )
    }


    sourceSets.configureEach {
        languageSettings {
            optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }
}
