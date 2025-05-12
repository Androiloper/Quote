package com.example.quotex.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.quotex.ui.promises.PromisesActivity
import com.example.quotex.ui.theme.QuoteXTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuoteXTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                MainScreen(
                    viewModel = viewModel,
                    onPromisesClick = {
                        startActivity(Intent(this, PromisesActivity::class.java))
                    },
                    showSnackbar = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }
        }
    }
}