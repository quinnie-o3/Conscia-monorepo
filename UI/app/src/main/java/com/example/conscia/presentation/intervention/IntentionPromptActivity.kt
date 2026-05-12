package com.example.conscia.presentation.intervention

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.conscia.ConsciaAppTheme
import com.example.conscia.data.AppDatabase
import com.example.conscia.data.TrackedAppsDataStore
import com.example.conscia.data.rule.RuleRepository
import com.example.conscia.ui.intention.IntentionRoute
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class IntentionPromptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appName = intent.getStringExtra("EXTRA_APP_NAME") ?: "App"
        val ruleId = intent.getLongExtra("EXTRA_RULE_ID", -1L)
        val ruleRepository = RuleRepository(AppDatabase.getDatabase(this).ruleDao())
        
        setContent {
            val dataStore = remember { TrackedAppsDataStore(this@IntentionPromptActivity) }
            val isDarkMode by dataStore.isDarkModeFlow.collectAsState(initial = false)

            ConsciaAppTheme(darkTheme = isDarkMode) {
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
