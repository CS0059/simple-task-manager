package com.example.myapplication.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.domain.model.Todo
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.core.content.edit

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val savedLanguage = prefs.getString("selected_language", "tr")
        updateLocale(savedLanguage ?: "tr")

        checkNotificationPermission()

        setContent {
            val viewModel: TodoViewModel = koinViewModel()
            val isDarkTheme = remember { mutableStateOf(viewModel.isDarkThemeEnabled()) }
            val isSortByDate = remember { mutableStateOf(viewModel.isSortByDateEnabled()) }
            
            MyApplicationTheme(darkTheme = isDarkTheme.value) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoApp(
                        isDarkTheme = isDarkTheme,
                        isSortByDate = isSortByDate,
                        viewModel = viewModel,
                        onLanguageChanged = { newLanguage ->
                            prefs.edit { putString("selected_language", newLanguage) }
                            updateLocale(newLanguage)
                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                setPackage(packageName)
                                action = "com.example.myapplication.ACTION_RESTART"
                            }
                            finish()
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.action == "com.example.myapplication.ACTION_RESTART" && 
            intent.getPackage() == packageName) {
            setIntent(intent)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun TodoApp(
    viewModel: TodoViewModel,
    isDarkTheme: MutableState<Boolean>,
    isSortByDate: MutableState<Boolean>,
    onLanguageChanged: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var editingTodoId by remember { mutableStateOf<Int?>(null) }

    var isNotificationsEnabled by remember { mutableStateOf(viewModel.isNotificationsEnabled()) }
    var isSoundsEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE) }
    var selectedLanguage by remember { mutableStateOf(prefs.getString("selected_language", "tr") ?: "tr") }
    
    val todos by viewModel.todos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            brush = Brush.horizontalGradient(
                                colors = if (isDarkTheme.value) {
                                    listOf(
                                        Color(0xFFE0E0E0),
                                        Color(0xFFFAFAFA)
                                    )
                                } else {
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            ),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Start
                    )
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = { showSettings = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.settings),
                                modifier = Modifier.size(26.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                        IconButton(
                            onClick = { showInfo = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = stringResource(R.string.info),
                                modifier = Modifier.size(26.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 12.dp,
                    hoveredElevation = 8.dp,
                    focusedElevation = 8.dp
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (todos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Text(
                            text = stringResource(R.string.no_tasks),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.25.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(todos) { todo ->
                        TodoItem(
                            todo = todo,
                            onToggleComplete = { viewModel.toggleTodoComplete(todo) },
                            onDelete = { viewModel.deleteTodo(todo) },
                            onEdit = {
                                editingTodoId = todo.id
                                title = todo.title
                                description = todo.description
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AddTodoDialog(
                onDismiss = { showDialog = false },
                onConfirm = { newTitle, newDescription, startTime, endTime, startDate, endDate ->
                    if (editingTodoId != null) {
                        viewModel.updateTodo(editingTodoId!!, newTitle, newDescription, startTime, endTime, startDate, endDate)
                    } else {
                        viewModel.addTodo(newTitle, newDescription, startTime, endTime, startDate, endDate)
                    }
                    showDialog = false
                    editingTodoId = null
                    title = ""
                    description = ""
                },
                initialTitle = title,
                initialDescription = description,
                initialStartTime = editingTodoId?.let { viewModel.getTodo(it)?.startTime },
                initialEndTime = editingTodoId?.let { viewModel.getTodo(it)?.endTime },
                initialStartDate = editingTodoId?.let { viewModel.getTodo(it)?.startDate },
                initialEndDate = editingTodoId?.let { viewModel.getTodo(it)?.endDate },
                isEditMode = editingTodoId != null
            )
        }

        if (showInfo) {
            AlertDialog(
                onDismissRequest = { showInfo = false },
                title = { 
                    Text(
                        stringResource(R.string.info),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ListItem(
                            headlineContent = { 
                                Text(
                                    stringResource(R.string.app_version),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            supportingContent = { Text("1.0.0") }
                        )
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                        ListItem(
                            headlineContent = { 
                                Text(
                                    stringResource(R.string.developer),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            supportingContent = { Text("Colorful Şener") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInfo = false }) {
                        Text(stringResource(R.string.close))
                    }
                }
            )
        }

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { 
                    Text(
                        stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ListItem(
                            headlineContent = { 
                                Text(
                                    stringResource(R.string.notifications),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = isNotificationsEnabled,
                                    onCheckedChange = { enabled ->
                                        isNotificationsEnabled = enabled
                                        viewModel.setNotificationsEnabled(enabled)
                                    }
                                )
                            },
                            supportingContent = { 
                                Text(
                                    if (isNotificationsEnabled) 
                                        stringResource(R.string.notifications_enabled)
                                    else 
                                        stringResource(R.string.notifications_disabled)
                                )
                            }
                        )
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        ListItem(
                            headlineContent = { 
                                Text(
                                    stringResource(R.string.sounds),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = isSoundsEnabled,
                                    onCheckedChange = { 
                                        isSoundsEnabled = it
                                        viewModel.setSoundEnabled(it)
                                    }
                                )
                            },
                            supportingContent = { 
                                Text(
                                    if (isSoundsEnabled) 
                                        stringResource(R.string.sounds_enabled)
                                    else 
                                        stringResource(R.string.sounds_disabled)
                                )
                            }
                        )
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        ListItem(
                            headlineContent = { 
                                Text(
                                    stringResource(R.string.theme),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = { 
                                        isDarkTheme.value = !isDarkTheme.value
                                        viewModel.setDarkTheme(isDarkTheme.value)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme.value) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                        contentDescription = if (isDarkTheme.value) "Dark Mode" else "Light Mode",
                                        tint = if (isDarkTheme.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            supportingContent = { 
                                Text(
                                    if (isDarkTheme.value) 
                                        stringResource(R.string.dark_theme) 
                                    else 
                                        stringResource(R.string.light_theme)
                                ) 
                            }
                        )
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        ListItem(
                            headlineContent = { 
                                Text(
                                    stringResource(R.string.language),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            trailingContent = {
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedButton(
                                        onClick = { expanded = true },
                                        modifier = Modifier.menuAnchor()
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                when (selectedLanguage) {
                                                    "tr" -> "🇹🇷"
                                                    "en" -> "🇬🇧"
                                                    "de" -> "🇩🇪"
                                                    "es" -> "🇪🇸"
                                                    "fr" -> "🇫🇷"
                                                    "it" -> "🇮🇹"
                                                    "ja" -> "🇯🇵"
                                                    "zh" -> "🇨🇳"
                                                    else -> "🇹🇷"
                                                }
                                            )
                                            Text(
                                                when (selectedLanguage) {
                                                    "tr" -> stringResource(R.string.turkish)
                                                    "en" -> stringResource(R.string.english)
                                                    "de" -> stringResource(R.string.german)
                                                    "es" -> stringResource(R.string.spanish)
                                                    "fr" -> stringResource(R.string.french)
                                                    "it" -> stringResource(R.string.italian)
                                                    "ja" -> stringResource(R.string.japanese)
                                                    "zh" -> stringResource(R.string.chinese)
                                                    else -> stringResource(R.string.turkish)
                                                }
                                            )
                                        }
                                    }
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { 
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("🇹🇷")
                                                    Text(stringResource(R.string.turkish))
                                                }
                                            },
                                            onClick = { 
                                                selectedLanguage = "tr"
                                                expanded = false
                                                onLanguageChanged("tr")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { 
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("🇬🇧")
                                                    Text(stringResource(R.string.english))
                                                }
                                            },
                                            onClick = { 
                                                selectedLanguage = "en"
                                                expanded = false
                                                onLanguageChanged("en")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { 
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("🇩🇪")
                                                    Text(stringResource(R.string.german))
                                                }
                                            },
                                            onClick = { 
                                                selectedLanguage = "de"
                                                expanded = false
                                                onLanguageChanged("de")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { 
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("🇪🇸")
                                                    Text(stringResource(R.string.spanish))
                                                }
                                            },
                                            onClick = { 
                                                selectedLanguage = "es"
                                                expanded = false
                                                onLanguageChanged("es")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { 
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("🇫🇷")
                                                    Text(stringResource(R.string.french))
                                                }
                                            },
                                            onClick = { 
                                                selectedLanguage = "fr"
                                                expanded = false
                                                onLanguageChanged("fr")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { 
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("🇮🇹")
                                                    Text(stringResource(R.string.italian))
                                                }
                                            },
                                            onClick = { 
                                                selectedLanguage = "it"
                                                expanded = false
                                                onLanguageChanged("it")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { 
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("🇯🇵")
                                                    Text(stringResource(R.string.japanese))
                                                }
                                            },
                                            onClick = { 
                                                selectedLanguage = "ja"
                                                expanded = false
                                                onLanguageChanged("ja")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { 
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("🇨🇳")
                                                    Text(stringResource(R.string.chinese))
                                                }
                                            },
                                            onClick = { 
                                                selectedLanguage = "zh"
                                                expanded = false
                                                onLanguageChanged("zh")
                                            }
                                        )
                                    }
                                }
                            }
                        )
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        ListItem(
                            headlineContent = { 
                                Text(
                                    stringResource(R.string.sort_order),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = isSortByDate.value,
                                    onCheckedChange = { 
                                        isSortByDate.value = it
                                        viewModel.setSortByDate(it)
                                    }
                                )
                            },
                            supportingContent = { 
                                Text(
                                    if (isSortByDate.value) 
                                        stringResource(R.string.sort_by_date_enabled)
                                    else 
                                        stringResource(R.string.sort_by_date_disabled)
                                ) 
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSettings = false }) {
                        Text(stringResource(R.string.close))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItem(
    todo: Todo,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleComplete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (todo.isCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (todo.isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (todo.description.isNotBlank()) {
                        Text(
                            text = todo.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (todo.isCompleted)
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (todo.startDate != null || todo.endDate != null || todo.startTime != null || todo.endTime != null) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        if (todo.startDate != null || todo.endDate != null) {
                            Text(
                                text = when {
                                    todo.startDate != null && todo.endDate != null -> 
                                        "${dateFormatter.format(Date(todo.startDate))} - ${dateFormatter.format(Date(todo.endDate))}"
                                    todo.startDate != null -> dateFormatter.format(Date(todo.startDate))
                                    else -> dateFormatter.format(Date(todo.endDate!!))
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                        if (todo.startTime != null || todo.endTime != null) {
                            Text(
                                text = when {
                                    todo.startTime != null && todo.endTime != null ->
                                        "${timeFormatter.format(Date(todo.startTime))} - ${timeFormatter.format(Date(todo.endTime))}"
                                    todo.startTime != null -> timeFormatter.format(Date(todo.startTime))
                                    else -> timeFormatter.format(Date(todo.endTime!!))
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long?, Long?, Long?, Long?) -> Unit,
    initialTitle: String = "",
    initialDescription: String = "",
    initialStartTime: Long? = null,
    initialEndTime: Long? = null,
    initialStartDate: Long? = null,
    initialEndDate: Long? = null,
    isEditMode: Boolean = false
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(initialStartTime) }
    var endTime by remember { mutableStateOf(initialEndTime) }
    var startDate by remember { mutableStateOf(initialStartDate) }
    var endDate by remember { mutableStateOf(initialEndDate) }
    
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                stringResource(
                    if (isEditMode) R.string.edit_task 
                    else R.string.add_task
                )
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.task_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.task_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (title.isNotBlank() && (startTime != null || endTime != null || startDate != null || endDate != null)) {
                                onConfirm(title, description, startTime, endTime, startDate, endDate)
                                onDismiss()
                            }
                        }
                    )
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.date_range),
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = startDate?.let { dateFormatter.format(Date(it)) }
                                    ?: stringResource(R.string.start_date),
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        OutlinedButton(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = endDate?.let { dateFormatter.format(Date(it)) }
                                    ?: stringResource(R.string.end_date),
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.time_range),
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = startTime?.let { timeFormatter.format(Date(it)) }
                                    ?: stringResource(R.string.start_time),
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        OutlinedButton(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = endTime?.let { timeFormatter.format(Date(it)) }
                                    ?: stringResource(R.string.end_time),
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && (startTime != null || endTime != null || startDate != null || endDate != null)) {
                        onConfirm(title, description, startTime, endTime, startDate, endDate)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && (startTime != null || endTime != null || startDate != null || endDate != null)
            ) {
                Text(
                    stringResource(
                        if (isEditMode) R.string.save
                        else R.string.add
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
    
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = it
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.select))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            endDate = it
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.select))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showStartTimePicker) {
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text(stringResource(R.string.start_time)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val timeState = rememberTimePickerState()
                    TimePicker(state = timeState)
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, timeState.hour)
                                set(Calendar.MINUTE, timeState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            startTime = calendar.timeInMillis
                            showStartTimePicker = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(stringResource(R.string.select_time))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
    
    if (showEndTimePicker) {
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text(stringResource(R.string.end_time)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val timeState = rememberTimePickerState()
                    TimePicker(state = timeState)
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, timeState.hour)
                                set(Calendar.MINUTE, timeState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            endTime = calendar.timeInMillis
                            showEndTimePicker = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Text(stringResource(R.string.select_time))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}
