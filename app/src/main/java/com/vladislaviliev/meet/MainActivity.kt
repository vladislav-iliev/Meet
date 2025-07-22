package com.vladislaviliev.meet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.vladislaviliev.meet.navigation.createAppGraph
import com.vladislaviliev.meet.ui.theme.MeetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeetTheme {
                val navController = rememberNavController()
                NavHost(navController, createAppGraph(navController))
            }
        }
    }
}