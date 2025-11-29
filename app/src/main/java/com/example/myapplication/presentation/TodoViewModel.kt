package com.example.myapplication.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Todo
import com.example.myapplication.domain.usecase.TodoUseCases
import com.example.myapplication.util.SoundManager
import com.example.myapplication.util.TodoNotificationManager
import androidx.core.content.edit
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class TodoViewModel(
    private val todoUseCases: TodoUseCases,
    private val soundManager: SoundManager,
    private val notificationManager: TodoNotificationManager,
    application: Application
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("app_preferences", Application.MODE_PRIVATE)

    private val _sortByDateEnabled = MutableStateFlow(isSortByDateEnabled())
    
    val todos = combine(
        todoUseCases.getTodos(),
        _sortByDateEnabled
    ) { todoList, sortEnabled ->
        if (sortEnabled) {
            todoList.sortedByDescending { it.createdDate }
        } else {
            todoList.sortedBy { it.id }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getTodo(id: Int): Todo? {
        return todos.value.find { it.id == id }
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundManager.setSoundEnabled(enabled)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        notificationManager.setNotificationsEnabled(enabled)
    }

    fun isNotificationsEnabled(): Boolean {
        return notificationManager.isNotificationsEnabled()
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit {
            putBoolean("dark_theme_enabled", enabled)
        }
    }

    fun isDarkThemeEnabled(): Boolean {
        return prefs.getBoolean("dark_theme_enabled", false)
    }

    fun setSortByDate(enabled: Boolean) {
        prefs.edit {
            putBoolean("sort_by_date_enabled", enabled)
        }
        _sortByDateEnabled.value = enabled
    }

    fun isSortByDateEnabled(): Boolean {
        return prefs.getBoolean("sort_by_date_enabled", false)
    }

    fun addTodo(
        title: String,
        description: String,
        startTime: Long? = null,
        endTime: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ) {
        viewModelScope.launch {
            try {
                val todo = Todo(
                    title = title,
                    description = description,
                    isCompleted = false,
                    createdDate = System.currentTimeMillis(),
                    startTime = startTime,
                    endTime = endTime,
                    startDate = startDate,
                    endDate = endDate
                )
                todoUseCases.addTodo(todo)
                soundManager.playTaskCreatedSound()
                if (startTime != null || endTime != null || startDate != null || endDate != null) {
                    notificationManager.scheduleTaskNotifications(todo)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateTodo(
        id: Int,
        title: String,
        description: String,
        startTime: Long? = null,
        endTime: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ) {
        viewModelScope.launch {
            try {
                val currentTodos = todos.value
                currentTodos.find { it.id == id }?.let { todo ->
                    val updatedTodo = todo.copy(
                        title = title,
                        description = description,
                        startTime = startTime,
                        endTime = endTime,
                        startDate = startDate,
                        endDate = endDate
                    )
                    todoUseCases.updateTodo(updatedTodo)
                    notificationManager.cancelTaskNotifications(todo)
                    if (startTime != null || endTime != null || startDate != null || endDate != null) {
                        notificationManager.scheduleTaskNotifications(updatedTodo)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleTodoComplete(todo: Todo) {
        viewModelScope.launch {
            try {
                val updatedTodo = todo.copy(isCompleted = !todo.isCompleted)
                todoUseCases.updateTodo(updatedTodo)
                
                if (updatedTodo.isCompleted) {
                    notificationManager.cancelTaskNotifications(todo)
                    soundManager.playTaskCompletedSound()
                } else {
                    val currentTime = System.currentTimeMillis()
                    if ((todo.endTime != null && currentTime < todo.endTime) || 
                        (todo.startTime != null && currentTime < todo.startTime)) {
                        notificationManager.scheduleTaskNotifications(updatedTodo)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                todoUseCases.deleteTodo(todo)
                soundManager.playTaskDeletedSound()
                notificationManager.cancelTaskNotifications(todo)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
} 