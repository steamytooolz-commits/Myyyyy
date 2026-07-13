package com.example.lifesim.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifesim.data.local.AppSettingsManager
import com.example.lifesim.data.remote.LLMApiService
import com.example.lifesim.data.remote.LLMChatRequest
import com.example.lifesim.data.remote.LLMMessage
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.util.Constants
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: AppSettingsManager,
    apiService: LLMApiService,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Loaded settings state
    var apiKey by remember { mutableStateOf(settingsManager.getApiKey()) }
    var baseUrl by remember { mutableStateOf(settingsManager.getBaseUrl()) }
    var modelName by remember { mutableStateOf(settingsManager.getModel()) }
    var temperature by remember { mutableStateOf(settingsManager.getTemperature()) }
    var maxTokens by remember { mutableStateOf(settingsManager.getMaxTokens().toFloat()) }

    var isApiKeyVisible by remember { mutableStateOf(false) }
    var saveStatusMessage by remember { mutableStateOf<String?>(null) }

    // Test connection states
    var isTesting by remember { mutableStateOf(false) }
    var testSuccess by remember { mutableStateOf<Boolean?>(null) }
    var testFeedbackMsg by remember { mutableStateOf<String?>(null) }
    var testLatency by remember { mutableStateOf(0L) }

    val hasKeysSet = apiKey.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Custom Top Bar
        TopAppBar(
            title = {
                Text(
                    "AI Configuration",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Go back", tint = TextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundSurface,
                titleContentColor = TextPrimary
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Status Summary banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (hasKeysSet) Success.copy(alpha = 0.12f) else Warning.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (hasKeysSet) Success.copy(alpha = 0.2f) else Warning.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasKeysSet) Icons.Rounded.CloudQueue else Icons.Rounded.CloudOff,
                            contentDescription = null,
                            tint = if (hasKeysSet) Success else Warning,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (hasKeysSet) "Custom AI Activated" else "Offline Simulation Mode",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (hasKeysSet) {
                                "Dynamic AI story generation is live using model: $modelName"
                            } else {
                                "Using standard pre-compiled events. Add an API key below to unlock infinite AI generation!"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Save / Status notification
            AnimatedVisibility(visible = saveStatusMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Success, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = saveStatusMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // API Credentials Section
            Text(
                "API CREDENTIALS",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // API Key
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key / Bearer Token") },
                        placeholder = { Text("sk-or-lh-...") },
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                    contentDescription = "Toggle API Key visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Border,
                            focusedLabelColor = Accent,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Base URL
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("API Base URL") },
                        placeholder = { Text("https://api.openai.com/v1/") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Border,
                            focusedLabelColor = Accent,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Base URL Quick Presets
                    Text(
                        "PRESET ENDPOINTS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetChip(label = "OpenAI", selected = baseUrl == "https://api.openai.com/v1/") {
                            baseUrl = "https://api.openai.com/v1/"
                            modelName = "gpt-4o-mini"
                        }
                        PresetChip(label = "DeepSeek", selected = baseUrl == "https://api.deepseek.com/v1/") {
                            baseUrl = "https://api.deepseek.com/v1/"
                            modelName = "deepseek-chat"
                        }
                        PresetChip(label = "OpenRouter", selected = baseUrl == "https://openrouter.ai/api/v1/") {
                            baseUrl = "https://openrouter.ai/api/v1/"
                            modelName = "google/gemini-2.5-flash"
                        }
                        PresetChip(label = "Local", selected = baseUrl.contains("localhost") || baseUrl.contains("10.0.2.2")) {
                            baseUrl = "http://10.0.2.2:1234/v1/" // 10.0.2.2 is host loopback from android emulator
                            modelName = "local-model"
                        }
                    }
                }
            }

            // Model Settings Section
            Text(
                "MODEL PARAMETERS",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Model Name
                    OutlinedTextField(
                        value = modelName,
                        onValueChange = { modelName = it },
                        label = { Text("Model Name ID") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Border,
                            focusedLabelColor = Accent,
                            unfocusedLabelColor = TextSecondary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Model Quick Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetChip(label = "gpt-4o-mini", selected = modelName == "gpt-4o-mini") {
                            modelName = "gpt-4o-mini"
                        }
                        PresetChip(label = "deepseek-chat", selected = modelName == "deepseek-chat") {
                            modelName = "deepseek-chat"
                        }
                        PresetChip(label = "gemini-2.5", selected = modelName.contains("gemini")) {
                            modelName = "google/gemini-2.5-flash"
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Temperature Slider
                    val tempLabel = when {
                        temperature < 0.4f -> "Deterministic"
                        temperature < 0.7f -> "Factual"
                        temperature < 1.1f -> "Balanced (Recommended)"
                        temperature < 1.5f -> "Creative"
                        else -> "Wild / Chaotic"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Temperature", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${String.format("%.1f", temperature)} · $tempLabel",
                            style = MaterialTheme.typography.labelMedium,
                            color = Info,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 0.0f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Info,
                            activeTrackColor = Info,
                            inactiveTrackColor = Border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Max Tokens Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Max Tokens Limit", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${maxTokens.toInt()} tokens",
                            style = MaterialTheme.typography.labelMedium,
                            color = Info,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Slider(
                        value = maxTokens,
                        onValueChange = { maxTokens = it },
                        valueRange = 100f..1500f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = Info,
                            activeTrackColor = Info,
                            inactiveTrackColor = Border
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Connection Diagnostic / Testing Block
            Text(
                "API DIAGNOSTICS & TESTING",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Test your active settings by hitting the diagnostic probe button. This calls the endpoint in real-time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isTesting = true
                                    testSuccess = null
                                    testFeedbackMsg = null

                                    // Temporarily save to SharedPrefs so interceptor uses the latest input values immediately!
                                    settingsManager.setApiKey(apiKey)
                                    settingsManager.setBaseUrl(baseUrl)
                                    settingsManager.setModel(modelName)
                                    settingsManager.setTemperature(temperature)
                                    settingsManager.setMaxTokens(maxTokens.toInt())

                                    try {
                                        val testRequest = LLMChatRequest(
                                            model = modelName,
                                            messages = listOf(
                                                LLMMessage("system", "Respond with a single word confirming success."),
                                                LLMMessage("user", "Hello! Are you online? Respond with: Active")
                                            ),
                                            max_tokens = 20
                                        )

                                        var responseText: String? = null
                                        val timeTaken = measureTimeMillis {
                                            val response = apiService.generateNarrative(testRequest)
                                            if (response.isSuccessful) {
                                                responseText = response.body()?.choices?.firstOrNull()?.message?.content
                                                testSuccess = true
                                            } else {
                                                testSuccess = false
                                                testFeedbackMsg = "HTTP Error ${response.code()}: ${response.errorBody()?.string() ?: "Unknown error"}"
                                            }
                                        }
                                        testLatency = timeTaken

                                        if (testSuccess == true) {
                                            testFeedbackMsg = "Connection successfully verified in $testLatency ms! Response: \"${responseText?.trim() ?: "Empty Response"}\""
                                        }
                                    } catch (e: Exception) {
                                        testSuccess = false
                                        testFeedbackMsg = "Exception: ${e.localizedMessage ?: "Failed to connect to the host endpoint"}"
                                    } finally {
                                        isTesting = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isTesting
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Testing...", style = MaterialTheme.typography.labelMedium)
                            } else {
                                Icon(Icons.Rounded.PlayArrow, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Run Connection Test", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    // Test Feedback Log
                    if (testSuccess != null || testFeedbackMsg != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (testSuccess == true) Success.copy(alpha = 0.12f) else Error.copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (testSuccess == true) Icons.Rounded.VerifiedUser else Icons.Rounded.Cancel,
                                        contentDescription = null,
                                        tint = if (testSuccess == true) Success else Error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (testSuccess == true) "PROBE SUCCESSFUL" else "PROBE FAILED",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (testSuccess == true) Success else Error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = testFeedbackMsg ?: "No feedback received",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Big Save and Action row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Save settings
                Button(
                    onClick = {
                        settingsManager.setApiKey(apiKey)
                        settingsManager.setBaseUrl(baseUrl)
                        settingsManager.setModel(modelName)
                        settingsManager.setTemperature(temperature)
                        settingsManager.setMaxTokens(maxTokens.toInt())

                        saveStatusMessage = "Settings saved successfully! Restart the game or age up to use the new configuration."
                        scope.launch {
                            kotlinx.coroutines.delay(4000)
                            saveStatusMessage = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Highlight),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Rounded.Save, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Settings", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }

                // Reset Button
                OutlinedButton(
                    onClick = {
                        apiKey = ""
                        baseUrl = Constants.LLM_API_BASE_URL
                        modelName = Constants.LLM_MODEL
                        temperature = 0.8f
                        maxTokens = Constants.MAX_TOKENS.toFloat()

                        settingsManager.setApiKey("")
                        settingsManager.setBaseUrl(Constants.LLM_API_BASE_URL)
                        settingsManager.setModel(Constants.LLM_MODEL)
                        settingsManager.setTemperature(0.8f)
                        settingsManager.setMaxTokens(Constants.MAX_TOKENS)

                        saveStatusMessage = "Settings reset to default values."
                        scope.launch {
                            kotlinx.coroutines.delay(3000)
                            saveStatusMessage = null
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Defaults", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Accent else BackgroundSurface)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color.White else TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
