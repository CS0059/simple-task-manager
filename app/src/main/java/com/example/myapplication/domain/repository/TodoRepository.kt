package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Todo
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getTodos(): Flow<List<Todo>>
    
    suspend fun insertTodo(todo: Todo)
    
    suspend fun updateTodo(todo: Todo)
    
    suspend fun deleteTodo(todo: Todo)
} 