package com.rinconsur.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.rinconsur.app.navegations.AppNavigation
import com.rinconsur.app.view.theme.AppTheme
import com.rinconsur.app.viewmodels.LoginViewModel
import com.rinconsur.app.viewmodels.ParametrosViewModel
import com.rinconsur.app.viewmodels.PostViewModel
import com.rinconsur.app.viewmodels.RegistroViewModel
import com.rinconsur.app.viewmodels.UserViewModel

class MainActivity : ComponentActivity() {

    // ViewModels (capa VM de MVVM) con alcance de la Activity
    private val loginViewModel: LoginViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val registroViewModel: RegistroViewModel by viewModels()
    private val parametrosViewModel: ParametrosViewModel by viewModels()
    private val postViewModel: PostViewModel by viewModels() // API Rest (Guía 14)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppNavigation(loginViewModel, userViewModel, registroViewModel, parametrosViewModel, postViewModel)
            }
        }
    }
}
