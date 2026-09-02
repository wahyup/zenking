package com.example

import com.example.data.local.DefaultProjects
import com.example.engine.PythonInterpreter
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testDefaultProjectsExecuteSuccessfully() {
        val interpreter = PythonInterpreter()
        for (project in DefaultProjects.list) {
            val result = interpreter.run(project.pythonCode, project.kvCode)
            assertTrue("Project ${project.name} failed with error: ${result.error}", result.isSuccess)
            assertNotNull("Project ${project.name} should have a root widget", result.rootWidget)
        }
    }
}
