package com.example.conscia.presentation.warning

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
        renderWarning(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        renderWarning(intent)
    }

    private fun renderWarning(intent: Intent) {
        val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "This app"
        val usageText = intent.getStringExtra(EXTRA_USAGE_TEXT).orEmpty()
        val limitText = intent.getStringExtra(EXTRA_LIMIT_TEXT).orEmpty()

        setContent {
            ConsciaAppTheme {
                BackHandler {
                    goHome()
                    finish()
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFE4E6)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        WarningCard(
                            appName = appName,
                            usageText = usageText,
                            limitText = limitText,
                            onOkClick = {
                                goHome()
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    companion object {
        const val EXTRA_APP_NAME = "extra_app_name"
        const val EXTRA_USAGE_TEXT = "extra_usage_text"
        const val EXTRA_LIMIT_TEXT = "extra_limit_text"
    }
}

@Composable
private fun WarningCard(
    appName: String,
    usageText: String,
    limitText: String,
    onOkClick: () -> Unit
) {
    val detailText = if (usageText.isNotBlank() && limitText.isNotBlank()) {
        "$appName has reached $usageText today, over the $limitText limit. Open Conscia if you want to extend this rule."
    } else {
        "You reached the limit for $appName today. Open Conscia if you want to extend this rule."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
        border = BorderStroke(3.dp, Color(0xFFE11D48))
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
                color = Color(0xFFBE123C),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = detailText,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = Color(0xFF881337),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onOkClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
            ) {
                Text("OK", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
