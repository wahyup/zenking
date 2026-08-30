package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KivyProjectDao {
    @Query("SELECT * FROM kivy_projects ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllProjects(): Flow<List<KivyProjectEntity>>

    @Query("SELECT * FROM kivy_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): KivyProjectEntity?

    @Query("SELECT * FROM kivy_projects WHERE category = :category ORDER BY updatedAt DESC")
    fun getProjectsByCategory(category: String): Flow<List<KivyProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: KivyProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<KivyProjectEntity>)

    @Update
    suspend fun updateProject(project: KivyProjectEntity)

    @Delete
    suspend fun deleteProject(project: KivyProjectEntity)

    @Query("DELETE FROM kivy_projects WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM kivy_projects")
    suspend fun getProjectCount(): Int
}
