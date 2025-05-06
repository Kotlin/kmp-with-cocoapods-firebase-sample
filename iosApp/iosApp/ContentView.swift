import SwiftUI
import KotlinLibrary

// Main ContentView
struct ContentView: View {
    @EnvironmentObject var firebaseService: FirebaseService // Injected from App struct

    var body: some View {
        // The view will automatically re-render when firebaseService.isSignedIn changes
        if firebaseService.isSignedIn {
            MainView()
        } else {
            AuthContainerView()
        }
    }
}

// Container for Login/Registration views
struct AuthContainerView: View {
    @EnvironmentObject var firebaseService: FirebaseService // Gets the service from the environment
    @State private var showingRegistration = false

    var body: some View {
        NavigationView {
            VStack {
                if showingRegistration {
                    RegistrationView()
                } else {
                    LoginView()
                }

                Button(action: {
                    showingRegistration.toggle()
                    firebaseService.errorMessage = nil // Clear previous errors when switching forms
                }) {
                    Text(showingRegistration ? "Already have an account? Log in" : "Don't have an account? Sign up")
                        .foregroundColor(.blue)
                        .padding()
                }
            }
            .padding()
            .navigationTitle(showingRegistration ? "Create Account" : "Login")
        }
        // Apply a view modifier for handling keyboard dismissal if needed, e.g., on tap gesture.
        // .onTapGesture {
        //     hideKeyboard()
        // }
    }

    // private func hideKeyboard() {
    //     UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    // }
}

struct LoginView: View {
    @EnvironmentObject var firebaseService: FirebaseService
    @State private var email = ""
    @State private var password = ""
    // Local errorMessage for immediate feedback, or rely solely on firebaseService.errorMessage
    @State private var viewErrorMessage = ""


    var body: some View {
        VStack(spacing: 20) {
            TextField("Email", text: $email)
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
                .textContentType(.emailAddress) // For autofill
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(8)

            SecureField("Password", text: $password)
                .textContentType(.password) // For autofill
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(8)

            // Display error message from the service or local view error
            let currentErrorMessage = viewErrorMessage.isEmpty ? firebaseService.errorMessage : viewErrorMessage
            if let errorMessage = currentErrorMessage, !errorMessage.isEmpty {
                Text(errorMessage)
                    .foregroundColor(.red)
                    .font(.caption)
                    .multilineTextAlignment(.center)
            }

            Button(action: loginUser) {
                if firebaseService.isLoading { // Use loading state from service
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle())
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue.opacity(0.7)) // Indicate disabled state visually
                        .foregroundColor(.white)
                        .cornerRadius(8)
                } else {
                    Text("Login")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
            }
            .disabled(firebaseService.isLoading) // Disable button when loading
        }
        .onChange(of: email) { _ in viewErrorMessage = "" } // Clear local error on input change
        .onChange(of: password) { _ in viewErrorMessage = "" }
    }

    private func loginUser() {
        viewErrorMessage = "" // Clear local error
        firebaseService.errorMessage = nil // Clear service error

        guard !email.isEmpty, !password.isEmpty else {
            viewErrorMessage = "Please enter email and password."
            return
        }

        Task {
            do {
                // isLoading and global errorMessage are handled by firebaseService
                // isSignedIn will be updated by firebaseService, triggering ContentView refresh
                let kmpUser = try await firebaseService.signIn(email: email, password: password)
                if kmpUser != nil {
                    firebaseService.logEvent(name: "login_success", parameters: ["method": "email"])
                }
                // No need to set isAuthenticated locally, ContentView reacts to firebaseService.isSignedIn
            } catch {
                // firebaseService.signIn already sets firebaseService.errorMessage
                // If the error is already on firebaseService.errorMessage, no need to set viewErrorMessage
                // unless you want to override or add more context.
                firebaseService.logEvent(name: "login_failure", parameters: ["method": "email", "error": error.localizedDescription])
                print("Login failed in View: \(error.localizedDescription)")
            }
        }
    }
}

struct RegistrationView: View {
    @EnvironmentObject var firebaseService: FirebaseService
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var displayName = ""
    // Local errorMessage for immediate feedback
    @State private var viewErrorMessage = ""

    var body: some View {
        VStack(spacing: 20) {
            TextField("Email", text: $email)
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
                .textContentType(.emailAddress)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(8)

            TextField("Display Name", text: $displayName)
                .textContentType(.name) // For autofill
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(8)

            SecureField("Password", text: $password)
                .textContentType(.newPassword) // For autofill suggestions
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(8)

            SecureField("Confirm Password", text: $confirmPassword)
                .textContentType(.newPassword)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(8)

            let currentErrorMessage = viewErrorMessage.isEmpty ? firebaseService.errorMessage : viewErrorMessage
            if let errorMessage = currentErrorMessage, !errorMessage.isEmpty {
                Text(errorMessage)
                    .foregroundColor(.red)
                    .font(.caption)
                    .multilineTextAlignment(.center)
            }

            Button(action: registerUser) {
                if firebaseService.isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle())
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue.opacity(0.7))
                        .foregroundColor(.white)
                        .cornerRadius(8)
                } else {
                    Text("Create Account")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
            }
            .disabled(firebaseService.isLoading)
        }
        .onChange(of: email) { _ in viewErrorMessage = "" }
        .onChange(of: password) { _ in viewErrorMessage = "" }
        .onChange(of: confirmPassword) { _ in viewErrorMessage = "" }
        .onChange(of: displayName) { _ in viewErrorMessage = "" }
    }

    private func registerUser() {
        viewErrorMessage = ""
        firebaseService.errorMessage = nil

        guard !email.isEmpty, !displayName.isEmpty else {
            viewErrorMessage = "Please enter email and display name."
            return
        }
        guard !password.isEmpty else {
            viewErrorMessage = "Please enter a password."
            return
        }
        guard password == confirmPassword else {
            viewErrorMessage = "Passwords don't match."
            return
        }

        Task {
            do {
                // firebaseService.signUp now directly returns KMPFirebaseUser?
                let kmpUserResult = try await firebaseService.signUp(email: email, password: password)

                // Access uid directly from KMPFirebaseUser
                guard let userId = kmpUserResult?.uid ?? firebaseService.currentUser?.uid else {
                    viewErrorMessage = "Failed to get user ID after registration."
                    // Optionally sign out if critical user data step fails, though service might handle this
                    // firebaseService.signOut()
                    firebaseService.logEvent(name: "registration_failure", parameters: ["reason": "missing_uid"])
                    return
                }

                let userData: [String: Any] = [
                    "email": email,
                    "displayName": displayName,
                    "createdAt": Date().timeIntervalSince1970 // Consider server timestamp for production
                ]

                try await firebaseService.saveUser(userId: userId, userData: userData)

                firebaseService.setAnalyticsUserId(userId: userId)
                firebaseService.setUserProperty(name: "user_type", value: "new_user")
                firebaseService.logEvent(name: "sign_up_success", parameters: ["method": "email"])

                // Authentication state is already updated by firebaseService.signUp()
                // which calls updateAuthStatusFromKMP() internally.
            } catch {
                // firebaseService methods should set firebaseService.errorMessage
                firebaseService.logEvent(name: "registration_failure", parameters: ["error": error.localizedDescription])
                print("Registration failed in View: \(error.localizedDescription)")
            }
        }
    }
}

struct MainView: View {
    @EnvironmentObject var firebaseService: FirebaseService
    @State private var fetchedUserData: [String: Any]? = nil // Store fetched user data locally for ProfileView
    @State private var isLoadingProfile = false // Local loading state for profile data fetch

    var body: some View {
        NavigationView {
            VStack {
                if isLoadingProfile {
                    ProgressView("Loading profile...")
                        .padding()
                } else if let userData = fetchedUserData {
                    ProfileView(userData: userData) // Pass fetched data to ProfileView
                } else if firebaseService.currentUser != nil { // Check if user exists but data fetch failed or pending
                     Text("Profile data could not be loaded. Try again later.")
                        .foregroundColor(.orange)
                        .padding()
                        .multilineTextAlignment(.center)
                        // Retry on appear if data is nil but user is signed in
                        // .onAppear is already on the VStack, this might cause rapid reloads.
                        // Consider a manual refresh button or more controlled retry logic.
                }
                else {
                    // This state should ideally not be reached if MainView is only shown for signed-in users.
                    // If it is, it means firebaseService.currentUser became nil unexpectedly.
                    Text("Not signed in or no user data available.")
                        .padding()
                        .multilineTextAlignment(.center)
                }

                Spacer() // Pushes button to bottom or use as needed

                Button(action: {
                    firebaseService.logEvent(name: "sign_out_attempt", parameters: nil) // Log before action
                    firebaseService.signOut()
                    // ContentView will switch to AuthContainerView based on firebaseService.isSignedIn
                }) {
                    Text("Sign Out")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.red)
                        .foregroundColor(.white)
                        .cornerRadius(8)
                }
                .padding()
            }
            .onAppear(perform: loadUserProfileData) // Load data when view appears
            .navigationTitle("Profile")
            .toolbar { // Example: Add a refresh button
                ToolbarItem(placement: .navigationBarTrailing) {
                    // Show refresh only if user is signed in, data isn't loaded, and not currently loading
                    if firebaseService.currentUser != nil && fetchedUserData == nil && !isLoadingProfile {
                        Button(action: loadUserProfileData) {
                            Image(systemName: "arrow.clockwise")
                        }
                    }
                }
            }
        }
    }

    private func loadUserProfileData() {
        // Ensure there's a current user to load data for
        guard let userId = firebaseService.currentUser?.uid else {
            print("MainView: No current user ID to load data for. Clearing local data.")
            fetchedUserData = nil // Clear any stale data
            isLoadingProfile = false // Ensure loading is stopped
            return
        }

        // Avoid re-fetching if data is already loaded and not currently loading,
        // unless explicitly desired (e.g., via a refresh button).
        // This check is simplified; for pull-to-refresh, you'd handle state differently.
        if fetchedUserData != nil && !isLoadingProfile {
             // print("User data already loaded.")
             // return // Commented out to allow re-fetch if called explicitly (e.g. by refresh)
        }

        isLoadingProfile = true
        firebaseService.errorMessage = nil // Clear previous global errors

        Task {
            defer { isLoadingProfile = false }
            do {
                fetchedUserData = try await firebaseService.getUser(userId: userId)
                if fetchedUserData != nil {
                    firebaseService.logEvent(name: "view_profile_success", parameters: nil)
                } else {
                    // Handle case where getUser returns nil without throwing an error
                    print("MainView: Fetched user data is nil for user ID: \(userId)")
                    // firebaseService.errorMessage = "Could not retrieve profile details." // Set global error if appropriate
                }
            } catch {
                // firebaseService.getUser should set its own errorMessage
                firebaseService.logEvent(name: "view_profile_failure", parameters: ["error": error.localizedDescription])
                print("Error loading user data in MainView: \(error.localizedDescription)")
                // fetchedUserData will remain nil or previous value
            }
        }
    }
}

// ProfileView remains largely the same as it's a display component
struct ProfileView: View {
    let userData: [String: Any] // Expects data to be passed in

    var body: some View {
        ScrollView { // Make profile scrollable if content exceeds screen height
            VStack(alignment: .leading, spacing: 16) {
                HStack {
                    // Display user's photo if available, otherwise a placeholder
                    if let photoURLString = userData["photoURL"] as? String, let photoURL = URL(string: photoURLString) {
                        AsyncImage(url: photoURL) { image in
                            image.resizable()
                                 .aspectRatio(contentMode: .fill)
                                 .frame(width: 80, height: 80)
                                 .clipShape(Circle())
                        } placeholder: {
                            ProgressView()
                                .frame(width: 80, height: 80)
                        }
                    } else {
                        Image(systemName: "person.circle.fill")
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(width: 80, height: 80)
                            .foregroundColor(.blue)
                    }

                    VStack(alignment: .leading) {
                        Text(userData["displayName"] as? String ?? "No Name")
                            .font(.title)
                        Text(userData["email"] as? String ?? "No Email")
                            .font(.subheadline)
                            .foregroundColor(.gray)
                    }
                    .padding(.leading, 8)
                    Spacer()
                }
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(12)

                VStack(alignment: .leading, spacing: 8) {
                    Text("Account Information")
                        .font(.headline)
                        .padding(.bottom, 4)

                    if let createdAt = userData["createdAt"] as? TimeInterval {
                        HStack {
                            Image(systemName: "calendar")
                            Text("Member since: \(memberSince(createdAt: createdAt))")
                        }
                    } else {
                        HStack {
                            Image(systemName: "calendar.badge.exclamationmark")
                            Text("Member since: Not available")
                        }
                    }
                    // Add more user details here if available in userData
                    // Example:
                    // if let location = userData["location"] as? String {
                    //     HStack {
                    //         Image(systemName: "location.fill")
                    //         Text("Location: \(location)")
                    //     }
                    // }
                }
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading) // Ensure it takes full width
                .background(Color(.systemGray6))
                .cornerRadius(12)

                Spacer() // Pushes content to the top
            }
            .padding()
        }
    }
}

// Helper function
private func memberSince(createdAt: TimeInterval) -> String {
    let date = Date(timeIntervalSince1970: createdAt)
    let formatter = DateFormatter()
    formatter.dateStyle = .medium
    formatter.timeStyle = .none // Or .short if you want time
    return formatter.string(from: date)
}

struct ContentView_Previews: PreviewProvider {

    // Helper function to create a configured FirebaseService for a specific state
    static func createMockService(configure: (MockFirebaseKMP) -> Void) -> FirebaseService {
        // Assuming IMockFirebaseKMP is the interface and MockFirebaseKMP is the concrete class
        // and it's accessible from KotlinLibrary module.
        let mockKmp = FirebaseKMPFactory().mock()
        configure(mockKmp) // Apply custom configurations
        return FirebaseService(kmpInstance: mockKmp)
    }

    static var previews: some View {
        Group {
            // Scenario 1: User is Signed Out
            ContentView()
                .environmentObject(createMockService { mock in
                    mock.reset() // Ensures clean state
                    // signedInUserOverride is nil by default after reset
                })
                .previewDisplayName("Signed Out")

//            // Scenario 2: User is Signed In (Profile visible)
//            ContentView()
//                .environmentObject(createMockService { mock in
//                    mock.reset()
//                    let previewUser = KotlinLibrary.FirebaseUser(uid: "previewUser123", email: "kate.bell@example.com", displayName: "Kate Bell", photoURL: "https://placehold.co/100x100/E8117F/white?text=KB")
//                    mock.signedInUserOverride = previewUser
//
//                    // Ensure userDatabase is mutable if using subscript assignment
//                    // The Kotlin MockFirebaseKMP has `var userDatabase: MutableMap<String, Map<String, Any>>`
//                    // which bridges to NSMutableDictionary.
//                    let userData: [String: Any] = [
//                        "displayName": "Kate Bell (from DB)",
//                        "email": "kate.bell@example.com",
//                        "createdAt": NSNumber(value: Date().timeIntervalSince1970 - 86400 * 30), // 30 days ago
//                        "photoURL": "https://placehold.co/100x100/E8117F/white?text=KB"
//                    ]
//                    mock.userDatabase.setObject(userData, forKey: "previewUser123" as NSString)
//                })
//                .previewDisplayName("Signed In - Profile")
//
//            // Scenario 3: Login View - Simulating an error
//            ContentView()
//                .environmentObject(createMockService { mock in
//                    mock.reset()
//                    // To see the error on LoginView, ensure isSignedIn is false
//                    mock.signedInUserOverride = nil
//                    // Simulate an error that would be shown on the LoginView
//                    // This error will be picked up by FirebaseService when kmp.signIn is called
//                    // For immediate display, FirebaseService.errorMessage would need to be set.
//                    // The mock's simulateError is for when a KMP method is *called*.
//                    // To show an error message *before* an action, you'd set firebaseService.errorMessage directly
//                    // For this preview, we'll rely on the error appearing after a failed login attempt.
//                    // To test this, you'd type something and press login in the preview.
//                    // For a direct error display:
//                    // let service = FirebaseService(kmpInstance: mock)
//                    // service.errorMessage = "Preview: Test error message"
//                    // return service // (This approach needs adjustment to fit the helper)
//                })
//                .previewDisplayName("Login View (Initial)") // Error will appear on action
//
//             // Scenario 4: MainView - Loading profile data
//            ContentView()
//                .environmentObject(createMockService { mock in
//                    mock.reset()
//                    let previewUser = KotlinLibrary.FirebaseUser(uid: "loadingUser123", email: "loading@example.com", displayName: "Loading User")
//                    mock.signedInUserOverride = previewUser
//                    // Don't put data in userDatabase for "loadingUser123" to simulate loading state
//                    // The MainView's loadUserProfileData will be called.
//                    // To keep it loading indefinitely for preview, the mock's getUser could delay or never complete.
//                    // For simplicity, this will show loading then "could not load".
//                })
//                .previewDisplayName("Profile - Loading")
        }
    }
}
