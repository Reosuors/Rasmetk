package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SupabaseSyncManager
import com.example.ui.DrawingViewModel
import com.example.ui.MainViewsContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Supabase client
    SupabaseSyncManager.initialize(
      context = applicationContext,
      url = "https://jjanuivrwsqvfqzvrwmu.supabase.co",
      key = "sb_publishable_yjyu8Q0o_TSRM6rDOZ8BTw_MfgGHyG3"
    )

    setContent {
      MyApplicationTheme {
        val viewModel: DrawingViewModel = viewModel()
        
        // Handle incoming Supabase deep links
        intent?.data?.let { uri ->
            val drawingCode = uri.getQueryParameter("id")
            if (!drawingCode.isNullOrBlank()) {
                viewModel.importDrawingByCode(drawingCode) { _, _ -> }
            }
        }

        MainViewsContainer(viewModel = viewModel)
      }
    }
  }
}

