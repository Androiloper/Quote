// ui/main/MainActivity.kt
package com.example.quotex.ui.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.quotex.service.ProverbService
import com.example.quotex.ui.promises.PromisesActivity
import com.example.quotex.ui.theme.QuoteXTheme
import com.example.quotex.util.WorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var workScheduler: WorkScheduler

    private val REQUEST_CODE_POST_NOTIFICATIONS = 1002

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkOverlayPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions()
        workScheduler.scheduleQuoteUpdates()

        viewModel.displayMode.observe(this) { mode ->
            if (mode != 0) {
                checkAndStartService(mode)
            } else {
                stopProverbService()
            }
        }

        setContent {
            QuoteXTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    MainScreen(
                        modifier = Modifier.padding(paddingValues),
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

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    overlayPermissionLauncher.launch(intent)
                } catch (e: Exception) {
                    Timber.e(e, "Error requesting overlay permission")
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                viewModel.setDisplayMode(0)
            }
        }
    }

    private fun checkAndStartService(mode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Timber.w("Cannot start service: overlay permission not granted")
            return
        }

        try {
            val intent = Intent(this, ProverbService::class.java)
            intent.putExtra("displayMode", mode)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Timber.d("Service started with mode: $mode")
        } catch (e: Exception) {
            Timber.e(e, "Error starting service")
        }
    }

    private fun stopProverbService() {
        val intent = Intent(this, ProverbService::class.java)
        stopService(intent)
        Timber.d("Service stopped")
    }
}