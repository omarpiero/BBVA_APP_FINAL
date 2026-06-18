package com.example.appbanco_s8

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.appbanco_s8.navigation.AppNavGraph
import com.example.appbanco_s8.ui.theme.Appbanco_s8Theme

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            Appbanco_s8Theme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}