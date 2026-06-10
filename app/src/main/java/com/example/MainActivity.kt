package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DrawingViewModel
import com.example.ui.MainViewsContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: DrawingViewModel = viewModel()
        
        // Handle incoming Firebase deep links
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

