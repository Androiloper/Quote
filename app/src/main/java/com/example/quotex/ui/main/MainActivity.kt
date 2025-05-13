// ui/main/MainActivity.kt - Improved permissions handling
package com.example.quotex.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.lifecycleScope
import com.example.quotex.ui.navigation.AppNavigation
import com.example.quotex.ui.theme.QuoteXTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val viewModel: MainViewModel by viewModels()

    // Activity result launcher for overlay permission
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Check if permission was granted after returning
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                Log.d(TAG, "Overlay permission granted, starting service")
                viewModel.checkAndRestartProverbService(this)
            } else {
                Log.d(TAG, "Overlay permission denied, disabling display mode")
                // If permission denied, set display mode to 0 (off)
                viewModel.setDisplayMode(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")

        // Ensure permissions are checked on startup
        checkRequiredPermissions()

        setContent {
            QuoteXTheme {
                AppNavigation(
                    mainViewModel = viewModel
                )

                // Observe display mode changes to check permissions when enabled
                val displayMode = viewModel.displayMode.observeAsState(0)

                LaunchedEffect(displayMode.value) {
                    if (displayMode.value > 0) {
                        ensureOverlayPermission()
                    }
                }
            }
        }

        // Set up service monitoring
        lifecycleScope.launch {
            viewModel.checkAndRestartProverbService(this@MainActivity)
        }
    }

    override fun onResume() {
        super.onResume()
        // Check service when activity is resumed
        viewModel.checkAndRestartProverbService(this)
    }

    private fun checkRequiredPermissions() {
        // For Android 13+ (API 33+), check notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                Log.d(TAG, "Notification permission granted: $isGranted")
            }

            // Request notification permission if needed
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Check overlay permission
        ensureOverlayPermission()
    }

    private fun ensureOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.d(TAG, "Overlay permission not granted, showing dialog")
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("To display quotes on your lock screen, QuoteX needs permission to draw over other apps.")
                    .setPositiveButton("Grant Permission") { _, _ ->
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        overlayPermissionLauncher.launch(intent)
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                        viewModel.setDisplayMode(0) // Disable display mode if permission denied
                    }
                    .show()
            } else {
                Log.d(TAG, "Overlay permission already granted")
            }
        }
    }
}