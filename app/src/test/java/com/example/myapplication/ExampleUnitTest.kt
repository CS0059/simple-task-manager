package com.example.myapplication

import com.example.myapplication.domain.model.Todo
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun todo_creation_test() {
        val todo = Todo(
            id = 1,
            title = "Test Todo",
            description = "Test Description",
            isCompleted = false,
            startTime = null,
            endTime = null,
            startDate = null,
            endDate = null
        )

        assertEquals("Test Todo", todo.title)
        assertEquals("Test Description", todo.description)
        assertFalse(todo.isCompleted)
        assertEquals(1, todo.id)
    }

    @Test
    fun todo_completion_test() {
        val todo = Todo(
            id = 1,
            title = "Test",
            description = "",
            isCompleted = false,
            startTime = null,
            endTime = null,
            startDate = null,
            endDate = null
        )

        val completedTodo = todo.copy(isCompleted = true)

        assertFalse(todo.isCompleted)
        assertTrue(completedTodo.isCompleted)
    }
}