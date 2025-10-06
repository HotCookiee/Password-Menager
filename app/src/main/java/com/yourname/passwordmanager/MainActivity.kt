package com.yourname.passwordmanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.yourname.passwordmanager.data.database.PasswordDatabase
import com.yourname.passwordmanager.navigation.PasswordManagerNavigation
import com.yourname.passwordmanager.repository.PasswordRepository
import com.yourname.passwordmanager.security.AuthManager
import com.yourname.passwordmanager.security.CryptoManager
import com.yourname.passwordmanager.ui.screen.LockScreen
import com.yourname.passwordmanager.ui.theme.PasswordManagerTheme
import com.yourname.passwordmanager.ui.viewmodel.PasswordViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: PasswordViewModel
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setupDependencies()

        setContent {
            PasswordManagerTheme {
                AppContent(
                    viewModel = viewModel,
                    authManager = authManager,
                    activity = this
                )
            }
        }
    }

    private fun setupDependencies() {
        val database = PasswordDatabase.getDatabase(this)
        val cryptoManager = CryptoManager(this)
        val repository = PasswordRepository(
            dao = database.passwordDao(),
            cryptoManager = cryptoManager
        )
        viewModel = PasswordViewModel(repository)
        authManager = AuthManager(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearMessages()
        viewModel.clearSelectedPassword()
    }
}

@Composable
fun AppContent(
    viewModel: PasswordViewModel,
    authManager: AuthManager,
    activity: MainActivity
) {
    var showLockScreen by remember { mutableStateOf(true) }
    var authError by remember { mutableStateOf(false) }

    val isAuthAvailable = remember { authManager.isAuthAvailable() }

    // Auto-trigger authentication on start
    LaunchedEffect(Unit) {
        if (isAuthAvailable) {
            authManager.authenticate(
                activity = activity,
                onSuccess = {
                    showLockScreen = false
                    authError = false
                },
                onError = { errorCode, errorString ->
                    authError = true
                }
            )
        } else {
            // Skip auth if not available
            showLockScreen = false
        }
    }

    if (showLockScreen && isAuthAvailable) {
        LockScreen(
            authError = authError,
            onAuthenticate = {
                authManager.authenticate(
                    activity = activity,
                    onSuccess = {
                        showLockScreen = false
                        authError = false
                    },
                    onError = { errorCode, errorString ->
                        authError = true
                    }
                )
            }
        )
    } else {
        PasswordManagerApp(viewModel = viewModel)
    }
}

@Composable
fun PasswordManagerApp(
    viewModel: PasswordViewModel,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val navController = rememberNavController()

    androidx.compose.material3.Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PasswordManagerNavigation(
            navController = navController,
            viewModel = viewModel,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        )
    }
}