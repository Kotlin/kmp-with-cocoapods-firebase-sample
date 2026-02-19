# Migrating KMP Firebase from CocoaPods to SwiftPM Import

This guide documents the migration of a Kotlin Multiplatform project from the `kotlin("native.cocoapods")` plugin to the new `swiftPMDependencies` DSL for importing Firebase iOS SDK.

## Prerequisites

The SwiftPM import feature requires a pre-release Kotlin toolchain:

- **Kotlin version**: `2.4.0-titan-217` (or newer dev build with SwiftPM support)
- **JetBrains dev Maven repository**: `https://packages.jetbrains.team/maven/p/kt/dev`
- **Buildscript constraint**: forces the Kotlin Gradle plugin to the exact dev version

## Gradle Changes

### 1. `gradle/libs.versions.toml`

Update the Kotlin version:

```diff
 [versions]
-kotlin = "2.3.10"
+kotlin = "2.4.0-titan-217"
 firebase = "12.9.0"
```

The `kotlinCocoapods` plugin alias can remain in the catalog if Swift-only pods still need it (see [Partial CocoaPods Removal](#8-partial-cocoapods-removal)), but remove it if CocoaPods is fully eliminated.

### 2. `settings.gradle.kts`

Add the JetBrains dev repository to **both** `pluginManagement` and `dependencyResolutionManagement`:

```kotlin
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/kt/dev")
    }
}
```

### 3. Root `build.gradle.kts`

Add a buildscript constraint to pin the Kotlin Gradle plugin version, and remove the CocoaPods plugin:

```diff
+buildscript {
+    dependencies.constraints {
+        "classpath"("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0-titan-217!!")
+    }
+}
+
 plugins {
     alias(libs.plugins.kotlinMultiplatform) apply false
-    alias(libs.plugins.kotlinCocoapods).apply(false)
 }
```

The `!!` suffix on the version forces strict resolution, ensuring no other dependency pulls in a different Kotlin Gradle plugin version.

### 4. Module `build.gradle.kts`

Remove the `cocoapods` plugin and block; replace with `swiftPMDependencies` and explicit `binaries.framework`:

```diff
 plugins {
     alias(libs.plugins.kotlinMultiplatform)
-    alias(libs.plugins.kotlinCocoapods)
 }

 kotlin {
-    iosArm64()
-    iosSimulatorArm64()
-
-    cocoapods {
-        summary = "..."
-        homepage = "..."
-        podfile = project.file("../iosApp/Podfile")
-        ios.deploymentTarget = "16.6"
-
-        pod("FirebaseCore", firebaseVersion)
-        pod("FirebaseAuth", firebaseVersion)
-        // ... other pods ...
-
-        framework {
-            baseName = "KotlinLibrary"
-            isStatic = true
-        }
-    }
+    listOf(
+        iosArm64(),
+        iosSimulatorArm64(),
+    ).forEach { iosTarget ->
+        iosTarget.binaries.framework {
+            baseName = "KotlinLibrary"
+            isStatic = true
+        }
+    }
+
+    swiftPMDependencies {
+        iosDeploymentVersion.set("16.6")
+        discoverModulesImplicitly = false
+
+        `package`(
+            url = url("https://github.com/firebase/firebase-ios-sdk.git"),
+            version = from(firebaseVersion),
+            products = listOf(/* ... */),
+            importedModules = listOf(/* ... */),
+        )
+    }
 }
```

## The `swiftPMDependencies` DSL

### Products vs. Imported Modules

The DSL distinguishes between two concepts:

- **`products`** — SPM product names as declared in the package's `Package.swift`. These are what you'd add in Xcode's "Add Package Dependency" UI. They control what gets linked.
- **`importedModules`** — the internal Clang module names that Kotlin should generate cinterop bindings for. These are the actual module names you'll `import` in Kotlin code.

All Firebase products come from a single `package()` declaration since they share one Git repository.

### Full Firebase declaration

```kotlin
swiftPMDependencies {
    iosDeploymentVersion.set("16.6")
    discoverModulesImplicitly = false

    `package`(
        url = url("https://github.com/firebase/firebase-ios-sdk.git"),
        version = from(firebaseVersion),
        products = listOf(
            product("FirebaseAnalytics"),
            product("FirebaseAppCheck"),
            product("FirebaseAppDistribution-Beta"),
            product("FirebaseAuth"),
            product("FirebaseCore"),
            product("FirebaseCrashlytics"),
            product("FirebaseDatabase"),
            product("FirebaseFirestore"),
            product("FirebaseInAppMessaging-Beta"),
            product("FirebaseInstallations"),
            product("FirebaseMessaging"),
            product("FirebasePerformance"),
            product("FirebaseRemoteConfig"),
            product("FirebaseStorage"),
        ),
        importedModules = listOf(
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
```

## Firebase-Specific Gotchas

### Beta products need a `-Beta` suffix

Some Firebase products are in beta and their SPM product names include a `-Beta` suffix. The product name in Gradle must match exactly:

| CocoaPods pod name | SPM product name |
|---|---|
| `FirebaseAppDistribution` (`-beta` version) | `FirebaseAppDistribution-Beta` |
| `FirebaseInAppMessaging` (`-beta` version) | `FirebaseInAppMessaging-Beta` |

Note: the `-Beta` suffix is only on the **product** name. The actual Clang module (used in `importedModules`) does not have it — use `FirebaseAppDistribution` and `FirebaseInAppMessagingInternal` respectively.

### Internal Clang module names differ from product names

Several Firebase products expose ObjC headers through internal Clang modules whose names don't match the product name. You must use the internal module name in `importedModules`:

| SPM Product | Clang Module (importedModules) |
|---|---|
| `FirebaseDatabase` | `FirebaseDatabaseInternal` |
| `FirebaseFirestore` | `FirebaseFirestoreInternal` |
| `FirebaseInAppMessaging-Beta` | `FirebaseInAppMessagingInternal` |
| `FirebaseRemoteConfig` | `FirebaseRemoteConfigInternal` |

Other products (Analytics, Auth, Core, Crashlytics, etc.) use the same name for both product and module.

### `FirebaseABTesting` — module-only, no SPM product

`FirebaseABTesting` does **not** have its own SPM product. It is pulled in transitively by `FirebaseRemoteConfig`. However, it **does** expose a Clang module, so it must be listed in `importedModules` only (not in `products`).

In practice, A/B Testing functionality is accessed via RemoteConfig APIs, so you import RemoteConfig classes.

### Disable `discoverModulesImplicitly`

```kotlin
discoverModulesImplicitly = false
```

This is **required** for Firebase. When enabled (the default), Kotlin attempts to generate cinterop bindings for every Clang module discovered in the dependency graph. Firebase's transitive C++ dependencies (gRPC, abseil/absl, leveldb, BoringSSL, etc.) contain modules that fail cinterop generation. Disabling implicit discovery and explicitly listing only the Firebase modules you need avoids these failures.

## Import Transformation

All Kotlin imports change from the `cocoapods.*` prefix to the `swiftPMImport.*` prefix. The new prefix includes the full framework qualifier based on your module's group/name.

### Pattern

```
cocoapods.<PodName>.<Class>
    ↓
swiftPMImport.<group>.<module-name-with-dots>.<Class>
```

Where:
- `<group>` is your Kotlin module's `group` (e.g., `org.jetbrains.kotlin.firebase.sample`)
- `<module-name-with-dots>` is your Kotlin module's name with dashes converted to dots (e.g., `kotlin-library` becomes `kotlin.library`)

### Examples

```diff
-import cocoapods.FirebaseCore.FIRApp
+import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRApp

-import cocoapods.FirebaseAuth.FIRAuth
+import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRAuth

-import cocoapods.FirebaseFirestoreInternal.FIRFirestore
+import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRFirestore

-import cocoapods.FirebaseFirestoreInternal.FIRDocumentReference
+import swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIRDocumentReference
```

Note that the Clang module name (e.g., `FirebaseFirestoreInternal`) disappears from the import path — all Firebase classes are flattened under the same `swiftPMImport.<group>.<module>` prefix regardless of which Firebase module they come from.

### Bulk replacement

You can do a project-wide find-and-replace:

```
cocoapods.<anything>.FIR  →  swiftPMImport.org.jetbrains.kotlin.firebase.sample.kotlin.library.FIR
```

Or use a regex: `cocoapods\.\w+\.` → `swiftPMImport.<your.group>.<your.module>.`

## Framework Configuration Migration

The `framework {}` block moves from inside `cocoapods {}` to the `binaries` API on each target:

```diff
-cocoapods {
-    framework {
-        baseName = "KotlinLibrary"
-        isStatic = true
-    }
-}
+listOf(
+    iosArm64(),
+    iosSimulatorArm64(),
+).forEach { iosTarget ->
+    iosTarget.binaries.framework {
+        baseName = "KotlinLibrary"
+        isStatic = true
+    }
+}
```

This is the standard KMP way to declare frameworks outside of the CocoaPods plugin. The framework name, static/dynamic setting, and export declarations all transfer directly.

## iOS Project Reconfiguration

With CocoaPods, the Kotlin framework was delivered as a pod (`kotlin_library`). With SwiftPM import, you need to:

1. **Remove the Kotlin pod** from `Podfile`:
   ```diff
   -pod 'kotlin_library', :path => '../kotlin-library'
   ```

2. **Embed the framework** using the Gradle task. The KMP plugin provides an `integrateEmbedAndSign` task that copies the built framework into the Xcode build output. Add it as a Run Script build phase in Xcode.

3. **Add the linkage package**. The Gradle plugin generates an `_internal_linkage_SwiftPMImport` Swift package at `iosApp/_internal_linkage_SwiftPMImport/`. This package declares all SPM dependencies and must be added to the Xcode project as a local package dependency. It ensures the Firebase SPM libraries are linked into the final app binary. Use the `integrateLinkagePackage` task to set this up.

The generated `Package.swift` mirrors your `products` list:

```swift
// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_internal_linkage_SwiftPMImport",
  platforms: [.iOS("16.6")],
  dependencies: [
    .package(url: "https://github.com/firebase/firebase-ios-sdk.git", from: "12.9.0")
  ],
  targets: [
    .target(
      name: "_internal_linkage_SwiftPMImport",
      dependencies: [
        .product(name: "FirebaseAnalytics", package: "firebase-ios-sdk"),
        .product(name: "FirebaseAuth", package: "firebase-ios-sdk"),
        // ... all products listed in your Gradle config
      ]
    )
  ]
)
```

## Complete CocoaPods Removal

Swift-only Firebase pods (`FirebaseAILogic`, `FirebaseFunctions`, `FirebaseMLModelDownloader`) that were previously kept in the Podfile can also be migrated to SPM. They are available as SPM products from the same `firebase-ios-sdk` package:

| CocoaPods pod name | SPM product name |
|---|---|
| `FirebaseAILogic` | `FirebaseAI` |
| `FirebaseFunctions` | `FirebaseFunctions` |
| `FirebaseMLModelDownloader` | `FirebaseMLModelDownloader` |

Add these to the `products` list in `swiftPMDependencies`. They don't need `importedModules` entries since they are Swift-only (no ObjC headers for cinterop).

To fully deintegrate CocoaPods:

1. Delete `Podfile` and `Podfile.lock`
2. Delete the `Pods/` directory
3. Remove the `kotlinCocoapods` plugin alias from `libs.versions.toml`
4. Remove all CocoaPods references from `project.pbxproj`:
   - `Pods_iosApp.framework` build file and file reference
   - `Pods-iosApp.debug.xcconfig` / `Pods-iosApp.release.xcconfig` file references
   - `Pods` group and `Frameworks` group (if it only contained the Pods framework)
   - `[CP] Check Pods Manifest.lock` shell script build phase
   - `[CP] Embed Pods Frameworks` shell script build phase
   - `baseConfigurationReference` lines pointing to Pods xcconfig files
5. Remove `Pods/` from `.gitignore`

## Summary Checklist

- [ ] Update Kotlin to `2.4.0-titan-217` in `libs.versions.toml`
- [ ] Add JetBrains dev Maven repo to `settings.gradle.kts`
- [ ] Add buildscript constraint in root `build.gradle.kts`
- [ ] Remove `kotlin("native.cocoapods")` plugin from module
- [ ] Replace `cocoapods {}` block with `swiftPMDependencies {}` + `binaries.framework {}`
- [ ] Set `discoverModulesImplicitly = false`
- [ ] Map pod names to correct SPM products (watch for `-Beta` suffixes)
- [ ] Map pod names to correct Clang modules in `importedModules` (watch for `*Internal` names)
- [ ] Update all `cocoapods.*` imports to `swiftPMImport.<group>.<module>.*`
- [ ] Remove `pod 'kotlin_library'` from Podfile
- [ ] Add linkage package and embed-and-sign build phases to Xcode project
- [ ] Add Swift-only pods (`FirebaseAI`, `FirebaseFunctions`, `FirebaseMLModelDownloader`) as SPM products
- [ ] Delete `Podfile`, `Podfile.lock`, and `Pods/` directory
- [ ] Remove all CocoaPods references from `project.pbxproj`
- [ ] Remove `Pods/` from `.gitignore`
