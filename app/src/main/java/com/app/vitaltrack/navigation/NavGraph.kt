package com.app.vitaltrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.vitaltrack.screens.configuracoes.ConfiguracoesScreen
import com.app.vitaltrack.screens.dashboard.DashboardScreen
import com.app.vitaltrack.screens.exercicios.ExerciciosScreen
import com.app.vitaltrack.screens.gamification.GamificationScreen
import com.app.vitaltrack.screens.perfil.PerfilClienteScreen
import com.app.vitaltrack.screens.progresso.ProgressoScreen
import com.app.vitaltrack.screens.refeicoes.RefeicaoCadastroScreen
import com.app.vitaltrack.screens.transferencia.TransferirDadosScreen
import com.app.vitaltrack.screens.treinos.TreinoAcademiaScreen
import com.app.vitaltrack.screens.treinos.TreinoExecucaoScreen

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
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToProgresso = {
                    navController.navigate(Screen.Progress.route)
                },
                onNavigateToExercicios = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToGym = {
                    navController.navigate(Screen.GymWorkout.route)
                },
                onNavigateToMealRegistration = { date, typeId ->
                    navController.navigate(Screen.MealRegistration.createRoute(date, typeId))
                },
                onNavigateToExport = {
                    navController.navigate(Screen.TransferData.route)
                }
            )
        }
        val onBack = { navController.popBackStack() }
        composable(Screen.Search.route) {
            ExerciciosScreen(onNavigateToGym = { navController.navigate(Screen.GymWorkout.route) })
        }
        composable(Screen.Profile.route) {
            PerfilClienteScreen(onBackClick = { onBack() })
        }
        composable(Screen.Progress.route) {
            ProgressoScreen(
                onBackClick = { onBack() },
                onNavigateToExercicios = { navController.navigate(Screen.Search.route) },
                onNavigateToConfig = { navController.navigate(Screen.Settings.route) },
                onNavigateToExport = { navController.navigate(Screen.TransferData.route) },
                onNavigateToGamification = { navController.navigate(Screen.Gamification.route) }
            )
        }
        composable(Screen.Gamification.route) {
            GamificationScreen(onBackClick = { onBack() })
        }
        composable(Screen.GymWorkout.route) {
            TreinoAcademiaScreen(
                onBackClick = { onBack() },
                onNavigateToExecution = { cdSessao ->
                    navController.navigate(Screen.WorkoutExecution.createRoute(cdSessao))
                }
            )
        }
        composable(
            route = Screen.WorkoutExecution.route,
            arguments = listOf(navArgument("cdSessao") { type = NavType.LongType })
        ) { backStackEntry ->
            val cdSessao = backStackEntry.arguments?.getLong("cdSessao") ?: 0L
            TreinoExecucaoScreen(
                cdSessao = cdSessao,
                onFinish = { onBack() }
            )
        }
        composable(Screen.Settings.route) {
            ConfiguracoesScreen(
                onBackClick = { onBack() },
                onNavigateToExercicios = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToExport = {
                    navController.navigate(Screen.TransferData.route)
                },
                onNavigateToProgresso = {
                    navController.navigate(Screen.Progress.route)
                }
            )
        }
        composable(Screen.TransferData.route) {
            TransferirDadosScreen(
                onBackClick = { onBack() },
                onNavigateToExercicios = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToConfig = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToProgresso = {
                    navController.navigate(Screen.Progress.route)
                }
            )
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
