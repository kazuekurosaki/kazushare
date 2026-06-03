// name=app/src/main/java/com/example/filestorage/ui/MainActivity.kt
package com.example.filestorage.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.filestorage.ui.auth.LoginScreen
import com.example.filestorage.ui.theme.FileStorageTheme
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.gms.auth.api.signin.GoogleSignIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.filestorage.ui.auth.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launcher untuk Google sign-in intent
        val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)
                val idToken = account?.idToken
                // We'll pass idToken to ViewModel (see LoginScreen lambda)
                // But here we could forward via a shared/hosted viewmodel; for simplicity LoginScreen uses Hilt ViewModel injected and a callback.
            } catch (e: Exception) {
                // handle error
            }
        }

        setContent {
            FileStorageTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val authViewModel: AuthViewModel = hiltViewModel()
                    // Entry point: show login screen; navigation not implemented fully here (demo)
                    LoginScreen(
                        onGoogleClick = {
                            val signInIntent: Intent = googleSignInClient.signInIntent
                            googleLauncher.launch(signInIntent)
                        },
                        viewModel = authViewModel,
                        onNavigateToRegister = { /* TODO navigate */ }
                    )
                }
            }
        }
    }
}
