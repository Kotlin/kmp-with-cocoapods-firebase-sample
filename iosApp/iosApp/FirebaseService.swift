import SwiftUI
import Combine
import KotlinLibrary

// Ensure KMP types are correctly aliased or used with their module prefix.
typealias KMPFirebaseUser = KotlinLibrary.FirebaseUser
typealias KMPError = KotlinLibrary.KMPError

class FirebaseService: ObservableObject {
    private let kmp: FirebaseKMP // Using the interface provided from KotlinLibrary

    // MARK: - Published Properties for SwiftUI
    @Published var isSignedIn: Bool = false
    @Published var currentUser: KMPFirebaseUser? = nil
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    // MARK: - Initialization
    init(kmpInstance: FirebaseKMP) {
        self.kmp = kmpInstance
    }

    // MARK: - Configuration
    func configure() {
        kmp.configure()
        self.updateAuthStatusFromKMP()
    }

    // MARK: - Authentication
    private func updateAuthStatusFromKMP() {
        // Ensure all updates to @Published properties are on the main thread
        DispatchQueue.main.async {
            let signedInStatus = self.kmp.isUserSignedIn()
            let kmpCurrentUser = self.kmp.getCurrentUser()

            // Check if the status or user actually changed to prevent redundant UI updates
            // and potential issues if called rapidly.
            if self.isSignedIn != signedInStatus || self.currentUser?.uid != kmpCurrentUser?.uid {
                self.isSignedIn = signedInStatus
                self.currentUser = kmpCurrentUser
                if !self.isSignedIn {
                    self.currentUser = nil // Explicitly clear if not signed in
                }
                // Resetting isLoading and errorMessage here might be too broad.
                // Consider resetting them more specifically within the methods that set them.
                // For now, keeping it as it was but wrapped in main.async.
                // self.isLoading = false // Let individual methods manage their isLoading state
                // self.errorMessage = nil
            }
        }
    }

    func signIn(email: String, password: String) async throws -> KMPFirebaseUser? {
        // Set loading and error message on the main thread
        await MainActor.run {
            self.isLoading = true
            self.errorMessage = nil
        }

        defer {
            Task { @MainActor in self.isLoading = false }
        }

        return try await withCheckedThrowingContinuation { continuation in
            kmp.signIn(email: email, password: password) { kmpUser, kmpError in
                if let error = kmpError {
                    Task { @MainActor in
                        self.errorMessage = error.localizedDescription
                    }
                    continuation.resume(throwing: error.nsError)
                } else {
                    // updateAuthStatusFromKMP will dispatch its own updates to main
                    self.updateAuthStatusFromKMP()
                    continuation.resume(returning: kmpUser)
                }
            }
        }
    }

    func signUp(email: String, password: String) async throws -> KMPFirebaseUser? {
        await MainActor.run {
            self.isLoading = true
            self.errorMessage = nil
        }

        defer {
            Task { @MainActor in self.isLoading = false }
        }

        return try await withCheckedThrowingContinuation { continuation in
            kmp.signUp(email: email, password: password) { kmpUser, kmpError in
                if let error = kmpError {
                    Task { @MainActor in
                        self.errorMessage = error.localizedDescription
                    }
                    continuation.resume(throwing: error.nsError)
                } else {
                    self.updateAuthStatusFromKMP()
                    continuation.resume(returning: kmpUser)
                }
            }
        }
    }

    func signOut() {
        _ = kmp.signOut()
        // updateAuthStatusFromKMP handles its own dispatching to main thread
        self.updateAuthStatusFromKMP()
    }

    func getCurrentUserFromService() -> KMPFirebaseUser? {
        return kmp.getCurrentUser()
    }

    func isUserCurrentlySignedIn() -> Bool {
        return kmp.isUserSignedIn()
    }

    // MARK: - Firestore Operations
    func saveUser(userId: String, userData: [String: Any]) async throws {
        await MainActor.run {
            self.isLoading = true // Consider a more specific loading state if needed
            self.errorMessage = nil
        }

        defer {
            Task { @MainActor in self.isLoading = false } // Reset general loading, or use specific one
        }

        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) -> Void in
            kmp.saveUser(userId: userId, userData: userData) { kmpError in
                if let error = kmpError {
                    Task { @MainActor in
                        self.errorMessage = error.localizedDescription
                    }
                    continuation.resume(throwing: error.nsError)
                } else {
                    continuation.resume(returning: ())
                }
            }
        }
    }

    func getUser(userId: String) async throws -> [String: Any]? {
        await MainActor.run {
            self.isLoading = true // Consider a more specific loading state
            self.errorMessage = nil
        }

        defer {
            Task { @MainActor in self.isLoading = false }
        }

        return try await withCheckedThrowingContinuation { continuation in
            kmp.getUser(userId: userId) { data, kmpError in
                if let error = kmpError {
                    Task { @MainActor in
                        self.errorMessage = error.localizedDescription
                    }
                    continuation.resume(throwing: error.nsError)
                } else {
                    continuation.resume(returning: data)
                }
            }
        }
    }

    // MARK: - Analytics (These methods don't typically update @Published properties directly)
    func logEvent(name: String, parameters: [String: Any]? = nil) {
        kmp.logEvent(name: name, parameters: parameters)
    }

    func setUserProperty(name: String, value: String) {
        kmp.setUserProperty(name: name, value: value)
    }

    func setAnalyticsUserId(userId: String) {
        kmp.setUserId(userId: userId)
    }
}
