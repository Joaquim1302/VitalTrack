package com.app.vitaltrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.vitaltrack.screens.configuracoes.ConfiguracoesScreen
import com.app.vitaltrack.screens.dashboard.DashboardScreen
import com.app.vitaltrack.screens.refeicoes.RefeicaoCadastroScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToConfig = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToMealRegistration = { date, typeId ->
                    navController.navigate(Screen.MealRegistration.createRoute(date, typeId))
                }
            )
        }
        composable(Screen.Settings.route) {
            ConfiguracoesScreen(onBackClick = {
                navController.popBackStack()
            })
        }
        composable(
            route = Screen.MealRegistration.route,
            arguments = listOf(
                navArgument("date") { type = NavType.StringType },
                navArgument("typeId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            val typeId = backStackEntry.arguments?.getInt("typeId") ?: 0
            RefeicaoCadastroScreen(
                date = date,
                typeId = typeId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
