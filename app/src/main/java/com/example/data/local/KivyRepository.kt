package com.example.data.local

import kotlinx.coroutines.flow.Flow

class KivyRepository(private val dao: KivyProjectDao) {
    val allProjects: Flow<List<KivyProjectEntity>> = dao.getAllProjects()

    fun getProjectsByCategory(category: String): Flow<List<KivyProjectEntity>> =
        dao.getProjectsByCategory(category)

    suspend fun getProjectById(id: Long): KivyProjectEntity? =
        dao.getProjectById(id)

    suspend fun insert(project: KivyProjectEntity): Long =
        dao.insertProject(project)

    suspend fun update(project: KivyProjectEntity) =
        dao.updateProject(project)

    suspend fun delete(project: KivyProjectEntity) =
        dao.deleteProject(project)

    suspend fun deleteById(id: Long) =
        dao.deleteById(id)

    suspend fun ensureDefaultProjects() {
        if (dao.getProjectCount() == 0) {
            dao.insertAll(DefaultProjects.list)
        }
    }
}
