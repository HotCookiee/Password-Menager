package com.yourname.passwordmanager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.yourname.passwordmanager.ui.screen.AddEditPasswordScreen
import com.yourname.passwordmanager.ui.screen.MainScreen
import com.yourname.passwordmanager.ui.viewmodel.PasswordViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

/**
 * Navigation routes for the app
 */
object NavigationRoutes {
    const val MAIN = "main"
    const val ADD_PASSWORD = "add_password"
    const val EDIT_PASSWORD = "edit_password/{passwordId}"
    
    fun editPassword(passwordId: Long): String {
        return "edit_password/$passwordId"
    }
}

/**
 * Main navigation component for the Password Manager app
 */
@Composable
fun PasswordManagerNavigation(
    navController: NavHostController,
    viewModel: PasswordViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.MAIN
    ) {
        // Main screen - displays list of passwords
        composable(NavigationRoutes.MAIN) {
            MainScreen(
                viewModel = viewModel,
                onAddPasswordClick = {
                    navController.navigate(NavigationRoutes.ADD_PASSWORD)
                },
                onPasswordClick = { password ->
                    viewModel.selectPassword(password)
                    navController.navigate(NavigationRoutes.editPassword(password.id))
                }
            )
        }
        
        // Add new password screen
        composable(NavigationRoutes.ADD_PASSWORD) {
            AddEditPasswordScreen(
                viewModel = viewModel,
                passwordEntry = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Edit existing password screen
        composable(
            route = NavigationRoutes.EDIT_PASSWORD,
            arguments = listOf(
                navArgument("passwordId") { 
                    type = NavType.LongType 
                }
            )
        ) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getLong("passwordId") ?: 0L
            val selectedPassword = viewModel.selectedPassword.collectAsState().value
            
            // If we have the selected password, use it; otherwise fetch by ID
            if (selectedPassword != null && selectedPassword.id == passwordId) {
                AddEditPasswordScreen(
                    viewModel = viewModel,
                    passwordEntry = selectedPassword,
                    onNavigateBack = {
                        viewModel.clearSelectedPassword()
                        navController.popBackStack()
                    }
                )
            } else {
                // Fallback: fetch password by ID
                // This handles cases where the app was killed and restarted
                LaunchedEffect(passwordId) {
                    viewModel.getPasswordById(passwordId) { password ->
                        if (password != null) {
                            viewModel.selectPassword(password)
                        } else {
                            // Password not found, navigate back
                            navController.popBackStack()
                        }
                    }
                }
                
                // Show loading while fetching
                selectedPassword?.let { password ->
                    AddEditPasswordScreen(
                        viewModel = viewModel,
                        passwordEntry = password,
                        onNavigateBack = {
                            viewModel.clearSelectedPassword()
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Navigation extensions for better type safety
 */
object NavigationExtensions {
    
    /**
     * Navigate to add password screen
     */
    fun NavHostController.navigateToAddPassword() {
        navigate(NavigationRoutes.ADD_PASSWORD)
    }
    
    /**
     * Navigate to edit password screen
     */
    fun NavHostController.navigateToEditPassword(passwordId: Long) {
        navigate(NavigationRoutes.editPassword(passwordId))
    }
    
    /**
     * Navigate back to main screen, clearing the back stack
     */
    fun NavHostController.navigateToMainAndClearBackStack() {
        navigate(NavigationRoutes.MAIN) {
            popUpTo(NavigationRoutes.MAIN) {
                inclusive = true
            }
        }
    }
    
    /**
     * Safe pop back stack
     */
    fun NavHostController.safePopBackStack(): Boolean {
        return if (previousBackStackEntry != null) {
            popBackStack()
        } else {
            false
        }
    }
}