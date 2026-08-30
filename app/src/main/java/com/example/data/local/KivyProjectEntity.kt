package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kivy_projects")
data class KivyProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val category: String, // "Responsive UI", "Games & Canvas", "Forms & Controls", "Navigation", "Custom"
    val pythonCode: String,
    val kvCode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isTemplate: Boolean = false,
    val tags: String = "kivy,python3"
)
