package com.app.punchinapplication.presentation.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.punchinapplication.presentation.screen.HomeScreen
import com.app.punchinapplication.presentation.screen.LoginScreen
import com.app.punchinapplication.presentation.screen.PunchInScreen
import com.app.punchinapplication.presentation.screen.RouteScreen

/**
 * Navigation graph for the app
 * Handles navigation between all screens
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "login"
) {
    val isLocked by ScreenLockManager.isLocked.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            HomeScreen(
                onNavigateToPunchIn = {
                    navController.navigate("punch_in")
                },
                onNavigateToRoute = {
                    if (!isLocked) {
                        navController.navigate("route")
                    }
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }

                },
                isLocked = isLocked
            )
        }
        
        composable("punch_in") {
            PunchInScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("route") {
            RouteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                isLocked = isLocked
            )
        }
    }
}

