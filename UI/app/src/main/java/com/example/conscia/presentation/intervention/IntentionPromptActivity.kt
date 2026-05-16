package com.example.conscia.presentation.intervention

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.conscia.ConsciaAppTheme
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.ui.intention.IntentionRoute
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IntentionPromptActivity : ComponentActivity() {

    @Inject
    lateinit var ruleRepository: RuleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appName = intent.getStringExtra("EXTRA_APP_NAME") ?: "App"
        val ruleId = intent.getLongExtra("EXTRA_RULE_ID", -1L)
        
        setContent {
            ConsciaAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IntentionRoute(
                        appName = appName,
                        onBackClick = { finish() },
                        onContinueClick = { intentions ->
                            lifecycleScope.launch {
                                if (ruleId != -1L && intentions.isNotEmpty()) {
                                    val currentRule = ruleRepository.getRuleById(ruleId)
                                    if (currentRule != null) {
                                        ruleRepository.updateRule(
                                            currentRule.copy(
                                                intentionLabel = intentions.joinToString(", "),
                                                updatedAt = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}
