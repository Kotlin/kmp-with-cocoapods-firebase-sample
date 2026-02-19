// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_internal_linkage_SwiftPMImport",
  platforms: [
    .iOS("16.6")
  ],
  products: [
    .library(
      name: "_internal_linkage_SwiftPMImport",
      type: .none,
      targets: ["_internal_linkage_SwiftPMImport"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/firebase/firebase-ios-sdk.git",
      from: "12.9.0",
    )
  ],
  targets: [
    .target(
      name: "_internal_linkage_SwiftPMImport",
      dependencies: [
        .product(
          name: "FirebaseAI",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseAnalytics",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseAppCheck",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseAppDistribution-Beta",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseAuth",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseCore",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseCrashlytics",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseDatabase",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseFirestore",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseFunctions",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseInAppMessaging-Beta",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseInstallations",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseMessaging",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseMLModelDownloader",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebasePerformance",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseRemoteConfig",
          package: "firebase-ios-sdk",
        ),
        .product(
          name: "FirebaseStorage",
          package: "firebase-ios-sdk",
        )
      ]
    )
  ]
)
