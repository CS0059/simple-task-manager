package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Todo
import com.example.myapplication.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

data class TodoUseCases(
    private val repository: TodoRepository
) {
    fun getTodos(): Flow<List<Todo>> = repository.getTodos()
    
    suspend fun addTodo(todo: Todo) {
        repository.insertTodo(todo)
    }
    
    suspend fun updateTodo(todo: Todo) {
        repository.updateTodo(todo)
    }
    
    suspend fun deleteTodo(todo: Todo) {
        repository.deleteTodo(todo)
    }
}

class ToggleTodo(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(todo: Todo) {
        repository.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
    }
} 