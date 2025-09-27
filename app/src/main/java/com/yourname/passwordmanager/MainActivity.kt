package com.yourname.passwordmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.yourname.passwordmanager.data.database.PasswordDatabase
import com.yourname.passwordmanager.navigation.PasswordManagerNavigation
import com.yourname.passwordmanager.repository.PasswordRepository
import com.yourname.passwordmanager.security.CryptoManager
import com.yourname.passwordmanager.ui.theme.PasswordManagerTheme
import com.yourname.passwordmanager.ui.viewmodel.PasswordViewModel

/**
 * Main Activity - Entry point of the application
 */

class MainActivity : ComponentActivity() {
    
    private lateinit var viewModel: PasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Initialize dependencies
        setupDependencies()
        
        setContent {
            PasswordManagerTheme {
                PasswordManagerApp(viewModel = viewModel)
            }
        }
    }
    
    /**
     * Set up dependency injection manually
     * In a larger app, you might want to use Dagger Hilt or similar
     */
    private fun setupDependencies() {
        val database = PasswordDatabase.getDatabase(this)
        val cryptoManager = CryptoManager(this)
        val repository = PasswordRepository(
            dao = database.passwordDao(),
            cryptoManager = cryptoManager
        )
        viewModel = PasswordViewModel(repository)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clear sensitive data when activity is destroyed
        viewModel.clearMessages()
        viewModel.clearSelectedPassword()
    }
}

/**
 * Main app composable
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordManagerApp(
    viewModel: PasswordViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PasswordManagerNavigation(
            navController = navController,
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * Preview for the main app
 */
@Composable
@Preview(showBackground = true)
fun PasswordManagerAppPreview() {
    PasswordManagerTheme {
        //Preview with mock data - you'd need to create a mock ViewModel
        //PasswordManagerApp(viewModel = mockViewModel)
    }
}