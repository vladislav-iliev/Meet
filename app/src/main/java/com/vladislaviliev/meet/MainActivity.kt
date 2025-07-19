package com.vladislaviliev.meet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vladislaviliev.meet.ui.theme.MeetTheme
import com.vladislaviliev.meet.ui.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeetTheme {
                LoginScreen(
                    onLoginClicked = { _, _ -> },
                    onForgotPasswordClicked = {}
                )
            }
        }
    }
}