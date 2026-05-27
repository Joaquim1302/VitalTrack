package com.app.vitaltrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.app.vitaltrack.navigation.NavGraph
import com.app.vitaltrack.ui.theme.VitalTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VitalTrackTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
