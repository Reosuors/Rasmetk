package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DrawingViewModel
import com.example.ui.MainViewsContainer
import com.example.ui.theme.MyApplicationTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth

// تهيأة سوبابيز الأساسية
val supabase = createSupabaseClient(
    supabaseUrl = "https://jjanuivrwsqvfqzvrwmu.supabase.co",
    supabaseKey = "sb_publishable_yjyu8Q0o_TSRM6rDOZ8BTw_MfgGHyG3"
) {
    install(Auth)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val viewModel: DrawingViewModel = viewModel()

                // معالجة الروابط النظيفة بدون استدعاء مكتبات فايربيس
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
