package com.example.data.filesystem

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Metadata model representing a physical .py or .kv file on Android local storage.
 */
data class LocalProjectFile(
    val name: String,               // e.g. "main.py", "game_logic.py"
    val projectName: String,        // e.g. "Calculator", "Pong Game"
    val relativePath: String,       // e.g. "Calculator/main.py"
    val absolutePath: String,       // Full disk path
    val extension: String,          // "py" or "kv"
    val sizeBytes: Long,            // File size in bytes
    val lineCount: Int,             // Number of lines
    val lastModified: Long,         // Timestamp
    val isMainEntry: Boolean = false // True if main.py
) {
    val formattedSize: String
        get() = when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", sizeBytes / 1024.0)
            else -> String.format(Locale.US, "%.1f MB", sizeBytes / (1024.0 * 1024.0))
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }
}

data class StorageStatistics(
    val totalFiles: Int,
    val pythonFilesCount: Int,
    val kvFilesCount: Int,
    val totalBytes: Long,
    val rootStoragePath: String
) {
    val formattedTotalSize: String
        get() = when {
            totalBytes < 1024 -> "$totalBytes B"
            totalBytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", totalBytes / 1024.0)
            else -> String.format(Locale.US, "%.1f MB", totalBytes / (1024.0 * 1024.0))
        }
}

/**
 * Local File System Manager handling .py and .kv file operations
 * on Android local device storage.
 */
class LocalFileManager(private val context: Context) {

    private val rootProjectsDir: File
        get() {
            val dir = File(context.filesDir, "python_kivy_projects")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    /**
     * Lists all .py and .kv files stored in local project directories.
     */
    fun listAllFiles(): List<LocalProjectFile> {
        val fileList = mutableListOf<LocalProjectFile>()
        val root = rootProjectsDir

        val projectDirs = root.listFiles()?.filter { it.isDirectory } ?: emptyList()
        for (pDir in projectDirs) {
            val files = pDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".py") || it.name.endsWith(".kv")) } ?: emptyList()
            for (f in files) {
                val lines = try { f.readLines().size } catch (e: Exception) { 0 }
                fileList.add(
                    LocalProjectFile(
                        name = f.name,
                        projectName = pDir.name,
                        relativePath = "${pDir.name}/${f.name}",
                        absolutePath = f.absolutePath,
                        extension = f.extension.lowercase(Locale.ROOT),
                        sizeBytes = f.length(),
                        lineCount = lines,
                        lastModified = f.lastModified(),
                        isMainEntry = f.name.equals("main.py", ignoreCase = true)
                    )
                )
            }
        }

        // Also check root folder for loose files
        val rootLooseFiles = root.listFiles()?.filter { it.isFile && (it.name.endsWith(".py") || it.name.endsWith(".kv")) } ?: emptyList()
        for (f in rootLooseFiles) {
            val lines = try { f.readLines().size } catch (e: Exception) { 0 }
            fileList.add(
                LocalProjectFile(
                    name = f.name,
                    projectName = "Root",
                    relativePath = f.name,
                    absolutePath = f.absolutePath,
                    extension = f.extension.lowercase(Locale.ROOT),
                    sizeBytes = f.length(),
                    lineCount = lines,
                    lastModified = f.lastModified(),
                    isMainEntry = f.name.equals("main.py", ignoreCase = true)
                )
            )
        }

        return fileList.sortedWith(compareBy({ it.projectName }, { !it.isMainEntry }, { it.name }))
    }

    /**
     * Returns files belonging to a specific project folder.
     */
    fun getFilesForProject(projectName: String): List<LocalProjectFile> {
        val sanitized = sanitizeDirectoryName(projectName)
        val pDir = File(rootProjectsDir, sanitized)
        if (!pDir.exists() || !pDir.isDirectory) return emptyList()

        val files = pDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".py") || it.name.endsWith(".kv")) } ?: emptyList()
        return files.map { f ->
            val lines = try { f.readLines().size } catch (e: Exception) { 0 }
            LocalProjectFile(
                name = f.name,
                projectName = sanitized,
                relativePath = "$sanitized/${f.name}",
                absolutePath = f.absolutePath,
                extension = f.extension.lowercase(Locale.ROOT),
                sizeBytes = f.length(),
                lineCount = lines,
                lastModified = f.lastModified(),
                isMainEntry = f.name.equals("main.py", ignoreCase = true)
            )
        }.sortedWith(compareBy({ !it.isMainEntry }, { it.name }))
    }

    /**
     * Reads the text content of a file from local storage.
     */
    fun readFileContent(file: LocalProjectFile): String {
        return try {
            File(file.absolutePath).readText(Charsets.UTF_8)
        } catch (e: Exception) {
            "# Error reading file: ${e.localizedMessage}"
        }
    }

    /**
     * Saves code content directly to a specified project file on local storage.
     */
    fun saveFile(projectName: String, fileName: String, content: String): LocalProjectFile {
        val sanitizedProject = sanitizeDirectoryName(projectName)
        val sanitizedFile = sanitizeFileName(fileName)

        val projectDir = File(rootProjectsDir, sanitizedProject)
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }

        val targetFile = File(projectDir, sanitizedFile)
        targetFile.writeText(content, Charsets.UTF_8)

        val lineCount = content.lines().size
        return LocalProjectFile(
            name = sanitizedFile,
            projectName = sanitizedProject,
            relativePath = "$sanitizedProject/$sanitizedFile",
            absolutePath = targetFile.absolutePath,
            extension = targetFile.extension.lowercase(Locale.ROOT),
            sizeBytes = targetFile.length(),
            lineCount = lineCount,
            lastModified = targetFile.lastModified(),
            isMainEntry = sanitizedFile.equals("main.py", ignoreCase = true)
        )
    }

    /**
     * Creates a new .py or .kv file in the project folder with boilerplate template.
     */
    fun createNewFile(projectName: String, rawFileName: String, initialContent: String? = null): LocalProjectFile {
        var finalName = sanitizeFileName(rawFileName)
        if (!finalName.endsWith(".py") && !finalName.endsWith(".kv")) {
            finalName += ".py"
        }

        val content = initialContent ?: if (finalName.endsWith(".kv")) {
            """# KV Lang Layout Definition
<CustomWidget>:
    orientation: 'vertical'
    padding: 10
    spacing: 8
    Label:
        text: "Custom KV Component"
        font_size: 18
"""
        } else {
            """# Python 3 Module: $finalName
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.label import Label
from kivy.uix.button import Button

def get_helper_message():
    return "Hello from $finalName!"
"""
        }

        return saveFile(projectName, finalName, content)
    }

    /**
     * Deletes a file from local storage.
     */
    fun deleteFile(file: LocalProjectFile): Boolean {
        return try {
            val f = File(file.absolutePath)
            if (f.exists()) f.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Renames a file on disk.
     */
    fun renameFile(file: LocalProjectFile, newFileName: String): LocalProjectFile? {
        val sanitizedNewName = sanitizeFileName(newFileName)
        val oldFile = File(file.absolutePath)
        val newFile = File(oldFile.parentFile, sanitizedNewName)

        return if (oldFile.renameTo(newFile)) {
            LocalProjectFile(
                name = sanitizedNewName,
                projectName = file.projectName,
                relativePath = "${file.projectName}/$sanitizedNewName",
                absolutePath = newFile.absolutePath,
                extension = newFile.extension.lowercase(Locale.ROOT),
                sizeBytes = newFile.length(),
                lineCount = try { newFile.readLines().size } catch (e: Exception) { 0 },
                lastModified = newFile.lastModified(),
                isMainEntry = sanitizedNewName.equals("main.py", ignoreCase = true)
            )
        } else {
            null
        }
    }

    /**
     * Duplicates a file in local storage.
     */
    fun duplicateFile(file: LocalProjectFile): LocalProjectFile? {
        val original = File(file.absolutePath)
        if (!original.exists()) return null

        val nameWithoutExt = file.name.substringBeforeLast(".")
        val ext = file.extension
        val newName = "${nameWithoutExt}_copy.$ext"

        val content = original.readText(Charsets.UTF_8)
        return saveFile(file.projectName, newName, content)
    }

    /**
     * Computes storage usage statistics.
     */
    fun getStorageStats(): StorageStatistics {
        val all = listAllFiles()
        val totalBytes = all.sumOf { it.sizeBytes }
        val pyCount = all.count { it.extension == "py" }
        val kvCount = all.count { it.extension == "kv" }

        return StorageStatistics(
            totalFiles = all.size,
            pythonFilesCount = pyCount,
            kvFilesCount = kvCount,
            totalBytes = totalBytes,
            rootStoragePath = rootProjectsDir.absolutePath
        )
    }

    /**
     * Synchronizes projects from database to disk, ensuring each project
     * has its main.py and app.kv files stored in context.filesDir.
     */
    fun syncProjectToDisk(projectName: String, pythonCode: String, kvCode: String) {
        val sanitized = sanitizeDirectoryName(projectName)
        saveFile(sanitized, "main.py", pythonCode)
        if (kvCode.isNotBlank()) {
            saveFile(sanitized, "app.kv", kvCode)
        }
    }

    /**
     * Shares a .py file via Android standard Intent / Share Sheet.
     */
    fun shareFile(file: LocalProjectFile) {
        try {
            val content = readFileContent(file)
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_TITLE, file.name)
                putExtra(Intent.EXTRA_SUBJECT, "Python Script: ${file.name}")
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(sendIntent, "Share Python Script (${file.name})").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sanitizeDirectoryName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9._-]"), "_").trim()
            .ifBlank { "Project" }
    }

    private fun sanitizeFileName(name: String): String {
        var clean = name.replace(Regex("[^a-zA-Z0-9._-]"), "_").trim()
        if (clean.isBlank()) clean = "script.py"
        return clean
    }
}
