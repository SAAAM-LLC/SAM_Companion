package com.saaam.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SAMCompanionTheme {
                SettingsScreen(
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "SAM Settings",
                    color = Color(0xFF64ffda),
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF64ffda)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF0a1128)
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Neural Architecture Section
            item {
                SettingsSection(
                    title = "Neural Architecture",
                    icon = Icons.Default.Psychology
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Hidden Dimension Slider
                        SliderSetting(
                            title = "Hidden Dimension",
                            subtitle = "Current: ${uiState.configuration.hiddenDim}",
                            value = uiState.configuration.hiddenDim.toFloat(),
                            valueRange = 128f..1024f,
                            steps = 7,
                            onValueChange = { viewModel.updateHiddenDim(it.toInt()) }
                        )

                        // Layer Count Slider
                        SliderSetting(
                            title = "Layer Count",
                            subtitle = "Current: ${uiState.configuration.layerCount}",
                            value = uiState.configuration.layerCount.toFloat(),
                            valueRange = 2f..12f,
                            steps = 10,
                            onValueChange = { viewModel.updateLayerCount(it.toInt()) }
                        )

                        // Auto Evolution Toggle
                        SwitchSetting(
                            title = "Auto Evolution",
                            subtitle = "Automatically grow architecture when needed",
                            checked = uiState.configuration.autoEvolution,
                            onCheckedChange = viewModel::updateAutoEvolution
                        )
                    }
                }
            }

            // Learning & Behavior Section
            item {
                SettingsSection(
                    title = "Learning & Behavior",
                    icon = Icons.Default.School
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Communication Style
                        DropdownSetting(
                            title = "Communication Style",
                            subtitle = "How SAM expresses thoughts",
                            selectedValue = uiState.configuration.communicationStyle,
                            options = listOf(
                                "adaptive" to "Adaptive (Changes based on context)",
                                "analytical" to "Analytical (Structured and logical)",
                                "creative" to "Creative (Expressive and imaginative)",
                                "concise" to "Concise (Brief and direct)",
                                "detailed" to "Detailed (Comprehensive explanations)"
                            ),
                            onValueChange = viewModel::updateCommunicationStyle
                        )

                        // Background Dreaming
                        SwitchSetting(
                            title = "Background Dreaming",
                            subtitle = "Process concepts during idle periods",
                            checked = uiState.configuration.backgroundDreaming,
                            onCheckedChange = viewModel::updateBackgroundDreaming
                        )

                        // Dream Interval
                        if (uiState.configuration.backgroundDreaming) {
                            SliderSetting(
                                title = "Dream Interval",
                                subtitle = "${uiState.configuration.dreamInterval / 60} minutes",
                                value = uiState.configuration.dreamInterval.toFloat(),
                                valueRange = 60f..600f, // 1-10 minutes
                                steps = 9,
                                onValueChange = { viewModel.updateDreamInterval(it.toInt()) }
                            )
                        }
                    }
                }
            }

            // Privacy & Data Section
            item {
                SettingsSection(
                    title = "Privacy & Data",
                    icon = Icons.Default.Security
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SwitchSetting(
                            title = "Privacy Mode",
                            subtitle = "Keep all conversations private (no sharing)",
                            checked = uiState.configuration.privacyMode,
                            onCheckedChange = viewModel::updatePrivacyMode
                        )

                        ActionSetting(
                            title = "Export Data",
                            subtitle = "Save conversations and SAM's growth",
                            icon = Icons.Default.FileDownload,
                            onClick = { viewModel.exportData() }
                        )

                        ActionSetting(
                            title = "Import Data",
                            subtitle = "Restore from backup",
                            icon = Icons.Default.FileUpload,
                            onClick = { viewModel.importData() }
                        )

                        ActionSetting(
                            title = "Clear All Data",
                            subtitle = "Reset SAM to initial state",
                            icon = Icons.Default.DeleteForever,
                            onClick = { viewModel.clearAllData() },
                            isDestructive = true
                        )
                    }
                }
            }

            // Performance Section
            item {
                SettingsSection(
                    title = "Performance",
                    icon = Icons.Default.Speed
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Max Concept Memory
                        SliderSetting(
                            title = "Max Concept Memory",
                            subtitle = "${uiState.configuration.maxConceptMemory} concepts",
                            value = uiState.configuration.maxConceptMemory.toFloat(),
                            valueRange = 1000f..50000f,
                            steps = 49,
                            onValueChange = { viewModel.updateMaxConceptMemory(it.toInt()) }
                        )

                        InfoSetting(
                            title = "Memory Usage",
                            subtitle = "${uiState.memoryUsageMB}MB used"
                        )

                        InfoSetting(
                            title = "Performance Mode",
                            subtitle = uiState.performanceMode
                        )
                    }
                }
            }

            // About Section
            item {
                SettingsSection(
                    title = "About",
                    icon = Icons.Default.Info
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        InfoSetting(
                            title = "App Version",
                            subtitle = "1.0.0"
                        )

                        InfoSetting(
                            title = "SAM Version",
                            subtitle = "Neural Architecture v${uiState.configuration.growthLevel}"
                        )

                        ActionSetting(
                            title = "Tutorial",
                            subtitle = "Learn how to use SAM",
                            icon = Icons.Default.Help,
                            onClick = { viewModel.startTutorial() }
                        )

                        ActionSetting(
                            title = "Send Feedback",
                            subtitle = "Help improve SAM",
                            icon = Icons.Default.Feedback,
                            onClick = { viewModel.sendFeedback() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1a1a2e).copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF64ffda),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = Color(0xFF64ffda),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun SliderSetting(
    title: String,
    subtitle: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = subtitle,
                color = Color(0xFF80cbc4),
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF64ffda),
                activeTrackColor = Color(0xFF64ffda),
                inactiveTrackColor = Color(0xFF64ffda).copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun SwitchSetting(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp
            )
            Text(
                text = subtitle,
                color = Color(0xFF80cbc4),
                fontSize = 14.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF64ffda),
                checkedTrackColor = Color(0xFF64ffda).copy(alpha = 0.5f),
                uncheckedThumbColor = Color(0xFF80cbc4),
                uncheckedTrackColor = Color(0xFF80cbc4).copy(alpha = 0.3f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSetting(
    title: String,
    subtitle: String,
    selectedValue: String,
    options: List<Pair<String, String>>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp
        )
        Text(
            text = subtitle,
            color = Color(0xFF80cbc4),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = options.find { it.first == selectedValue }?.second ?: selectedValue,
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF64ffda),
                    unfocusedBorderColor = Color(0xFF64ffda).copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (value, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onValueChange(value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActionSetting(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isDestructive) {
            Color(0xFFf06292).copy(alpha = 0.1f)
        } else {
            Color(0xFF64ffda).copy(alpha = 0.1f)
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDestructive) {
                Color(0xFFf06292).copy(alpha = 0.3f)
            } else {
                Color(0xFF64ffda).copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isDestructive) Color(0xFFf06292) else Color(0xFF64ffda),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF80cbc4),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun InfoSetting(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp
        )
        Text(
            text = subtitle,
            color = Color(0xFF80cbc4),
            fontSize = 14.sp
        )
    }
}

// Settings ViewModel
data class SettingsUIState(
    val configuration: SAMConfiguration = SAMConfiguration(),
    val memoryUsageMB: Int = 0,
    val performanceMode: String = "Balanced",
    val isLoading: Boolean = false,
    val showDialog: Boolean = false,
    val dialogMessage: String = ""
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUIState())
    val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

    private lateinit var dataManager: SAMDataManager

    init {
        // Initialize with current configuration
        loadCurrentConfiguration()
    }

    private fun loadCurrentConfiguration() {
        // Load configuration from data manager
        // Implementation would read from DataStore
    }

    fun updateHiddenDim(value: Int) {
        _uiState.value = _uiState.value.copy(
            configuration = _uiState.value.configuration.copy(hiddenDim = value)
        )
        saveConfiguration()
    }

    fun updateLayerCount(value: Int) {
        _uiState.value = _uiState.value.copy(
            configuration = _uiState.value.configuration.copy(layerCount = value)
        )
        saveConfiguration()
    }

    fun updateAutoEvolution(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            configuration = _uiState.value.configuration.copy(autoEvolution = enabled)
        )
        saveConfiguration()
    }

    fun updateCommunicationStyle(style: String) {
        _uiState.value = _uiState.value.copy(
            configuration = _uiState.value.configuration.copy(communicationStyle = style)
        )
        saveConfiguration()
    }

    fun updateBackgroundDreaming(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            configuration = _uiState.value.configuration.copy(backgroundDreaming = enabled)
        )
        saveConfiguration()
    }

    fun updateDreamInterval(interval: Int) {
        _uiState.value = _uiState.value.copy(
            configuration = _uiState.value.configuration.copy(dreamInterval = interval)
        )
        saveConfiguration()
    }

    fun updatePrivacyMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            configuration = _uiState.value.configuration.copy(privacyMode = enabled)
        )
        saveConfiguration()
    }

    fun updateMaxConceptMemory(maxMemory: Int) {
        _uiState.value = _uiState.value.copy(
            configuration = _uiState.value.configuration.copy(maxConceptMemory = maxMemory)
        )
        saveConfiguration()
    }

    private fun saveConfiguration() {
        viewModelScope.launch {
            // Save to DataStore
            // dataManager.updateConfiguration(_uiState.value.configuration)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Implementation for data export
            delay(1000) // Simulate export time

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                showDialog = true,
                dialogMessage = "Data exported successfully!"
            )
        }
    }

    fun importData() {
        // Implementation for data import
        _uiState.value = _uiState.value.copy(
            showDialog = true,
            dialogMessage = "Import functionality will be available soon!"
        )
    }

    fun clearAllData() {
        // Implementation for clearing all data
        _uiState.value = _uiState.value.copy(
            showDialog = true,
            dialogMessage = "Are you sure you want to clear all data? This cannot be undone."
        )
    }

    fun startTutorial() {
        // Start tutorial flow
    }

    fun sendFeedback() {
        // Open feedback mechanism
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showDialog = false)
    }
}
