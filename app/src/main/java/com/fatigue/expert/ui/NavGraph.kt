package com.fatigue.expert.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fatigue.expert.FatigueViewModel
import com.fatigue.expert.ui.screens.*
import com.fatigue.expert.ui.screens.app.*

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: FatigueViewModel = viewModel()
) {
    NavHost(navController = navController, startDestination = "intro") {

        // ── User App Flow ──

        composable("intro") {
            IntroScreen(onContinue = { name ->
                viewModel.userName.value = name
                navController.navigate("main") {
                    popUpTo("intro") { inclusive = true }
                }
            })
        }

        composable("main") {
            MainScreen(
                onNavigateToPreTrip = { navController.navigate("pretrip_intro") },
                onNavigateToMonitoring = { navController.navigate("monitoring_intro") },
                onNavigateToExpertPanel = { navController.navigate("expert_home") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToResults = { navController.navigate("session_results") },
                onNavigateToEvalSurvey = { navController.navigate("expert_survey") },
                onEndSession = {
                    viewModel.endSession()
                    navController.navigate("intro") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        // Pre-trip flow: Intro → Survey → Tests (with checklist) → Results
        composable("pretrip_intro") {
            PreTripIntroScreen(
                onStart = { navController.navigate("survey") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("survey") {
            SurveyScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onComplete = { },
                onNavigateToTests = {
                    navController.navigate("tests") {
                        popUpTo("survey") { inclusive = true }
                    }
                }
            )
        }

        composable("tests") {
            TestsMenuScreen(
                viewModel = viewModel,
                onNavigateToTest = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() },
                onGoHome = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        composable("reaction_test") {
            ReactionTestScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable("arithmetic_test") {
            ArithmeticTestScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable("memory_test") {
            SectorMemoryTestScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable("timer_test") {
            TimerTestScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        // Monitoring flow: Intro → Sensors/Simulation → Results
        composable("monitoring_intro") {
            MonitoringIntroScreen(
                onStart = { navController.navigate("sensors") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("sensors") {
            SensorsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onGoHome = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }

        composable("session_results") {
            SessionResultsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        // ── Expert Panel ──

        composable("expert_home") {
            HomeScreen(
                onNavigateToModule = { module -> navController.navigate("module/$module") },
                onNavigateToScenario = { scenario -> navController.navigate("scenario/$scenario") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("expert_survey") {
            ExpertSurveyScreen(onBack = { navController.popBackStack() })
        }

        composable("module/{moduleName}") { backStackEntry ->
            val moduleName = backStackEntry.arguments?.getString("moduleName") ?: "fatigue"
            ModuleScreen(
                moduleName = moduleName,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("scenario/full") {
            FullAssessmentScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable("scenario/monitoring") {
            MonitoringScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable("scenario/quick") {
            QuickCheckScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
