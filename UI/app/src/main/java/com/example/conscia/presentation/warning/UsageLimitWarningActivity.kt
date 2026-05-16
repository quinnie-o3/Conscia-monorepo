package com.example.conscia.presentation.warning

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.conscia.ConsciaAppTheme

class UsageLimitWarningActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "This app"

        setContent {
            ConsciaAppTheme {
                // Nền hồng nhạt toàn màn hình để làm nổi bật thông báo
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFE4E6) // Nền hồng rất nhạt (Rose 100)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        WarningCard(
                            appName = appName,
                            onOkClick = {
                                // Quay về màn hình Home (Tương đương việc đóng app đang dùng)
                                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                startActivity(homeIntent)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_USAGE_TEXT = "extra_usage_text"
        const val EXTRA_LIMIT_TEXT = "extra_limit_text"
    }
}

@Composable
private fun WarningCard(appName: String, onOkClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f) // Căn đều các cạnh, chiếm 85% chiều ngang
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        // Viền đỏ, nền hồng như yêu cầu
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)), 
        border = BorderStroke(3.dp, Color(0xFFE11D48)) // Viền đỏ đậm (Rose 600)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Limit Reached!",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFBE123C), // Chữ đỏ đậm
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "You reached the limit for $appName today. If you want to extend more, please truy cập lại Conscia.",
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = Color(0xFF881337), // Chữ hồng đậm
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onOkClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)) // Nút đỏ
            ) {
                Text("OK", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
