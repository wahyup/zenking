package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.filesystem.LocalFileManager
import com.example.data.filesystem.LocalProjectFile
import com.example.data.filesystem.StorageStatistics
import com.example.data.local.DefaultProjects
import com.example.data.local.KivyDatabase
import com.example.data.local.KivyProjectEntity
import com.example.data.local.KivyRepository
import com.example.engine.KivyRuntime
import com.example.engine.KivyUiState
import com.example.engine.KivyWidgetNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KivyStudioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KivyRepository
    val fileManager = LocalFileManager(application)
    val runtime = KivyRuntime()

    val allProjects: StateFlow<List<KivyProjectEntity>>

    private val _currentProject = MutableStateFlow<KivyProjectEntity?>(null)
    val currentProject: StateFlow<KivyProjectEntity?> = _currentProject.asStateFlow()

    private val _currentPythonCode = MutableStateFlow("")
    val currentPythonCode: StateFlow<String> = _currentPythonCode.asStateFlow()

    private val _currentKvCode = MutableStateFlow("")
    val currentKvCode: StateFlow<String> = _currentKvCode.asStateFlow()

    private val _localFiles = MutableStateFlow<List<LocalProjectFile>>(emptyList())
    val localFiles: StateFlow<List<LocalProjectFile>> = _localFiles.asStateFlow()

    private val _projectFiles = MutableStateFlow<List<LocalProjectFile>>(emptyList())
    val projectFiles: StateFlow<List<LocalProjectFile>> = _projectFiles.asStateFlow()

    private val _activeLocalFile = MutableStateFlow<LocalProjectFile?>(null)
    val activeLocalFile: StateFlow<LocalProjectFile?> = _activeLocalFile.asStateFlow()

    private val _storageStats = MutableStateFlow(
        StorageStatistics(0, 0, 0, 0L, "")
    )
    val storageStats: StateFlow<StorageStatistics> = _storageStats.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isInspectMode = MutableStateFlow(false)
    val isInspectMode: StateFlow<Boolean> = _isInspectMode.asStateFlow()

    private val _selectedInspectWidget = MutableStateFlow<KivyWidgetNode?>(null)
    val selectedInspectWidget: StateFlow<KivyWidgetNode?> = _selectedInspectWidget.asStateFlow()

    private val _deviceFrameMode = MutableStateFlow("phone") // "phone", "tablet", "fullscreen"
    val deviceFrameMode: StateFlow<String> = _deviceFrameMode.asStateFlow()

    private val _activeTab = MutableStateFlow(0) // 0: Editor, 1: Runner, 2: Console/REPL, 3: Projects, 4: Docs
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    init {
        val db = KivyDatabase.getInstance(application)
        repository = KivyRepository(db.kivyProjectDao())

        allProjects = repository.allProjects
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        viewModelScope.launch {
            repository.ensureDefaultProjects()
            // Sync default projects to local disk
            for (proj in DefaultProjects.list) {
                fileManager.syncProjectToDisk(proj.name, proj.pythonCode, proj.kvCode)
            }
            refreshFileSystem()

            // Load first project by default
            val defaultProj = DefaultProjects.list.first()
            loadProject(defaultProj)
            // Pre-run default app
            runtime.executeCode(defaultProj.pythonCode, defaultProj.kvCode)
        }
    }

    fun selectTab(index: Int) {
        _activeTab.value = index
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun refreshFileSystem() {
        val all = fileManager.listAllFiles()
        _localFiles.value = all
        _storageStats.value = fileManager.getStorageStats()

        _currentProject.value?.let { proj ->
            val pFiles = fileManager.getFilesForProject(proj.name)
            _projectFiles.value = pFiles
            if (_activeLocalFile.value == null && pFiles.isNotEmpty()) {
                _activeLocalFile.value = pFiles.firstOrNull { it.isMainEntry } ?: pFiles.first()
            }
        }
    }

    fun loadProject(project: KivyProjectEntity) {
        _currentProject.value = project
        _currentPythonCode.value = project.pythonCode
        _currentKvCode.value = project.kvCode
        _selectedInspectWidget.value = null

        // Sync to disk
        fileManager.syncProjectToDisk(project.name, project.pythonCode, project.kvCode)
        val pFiles = fileManager.getFilesForProject(project.name)
        _projectFiles.value = pFiles
        _activeLocalFile.value = pFiles.firstOrNull { it.isMainEntry } ?: pFiles.firstOrNull()
        refreshFileSystem()
    }

    fun loadLocalFile(file: LocalProjectFile) {
        val content = fileManager.readFileContent(file)
        _activeLocalFile.value = file

        if (file.extension == "kv") {
            _currentKvCode.value = content
        } else {
            _currentPythonCode.value = content
        }
        _statusMessage.value = "Loaded ${file.name} from local storage"
    }

    fun saveCurrentFileToDisk() {
        val proj = _currentProject.value ?: return
        val activeFile = _activeLocalFile.value
        val fileName = activeFile?.name ?: "main.py"
        val content = if (fileName.endsWith(".kv")) _currentKvCode.value else _currentPythonCode.value

        val saved = fileManager.saveFile(proj.name, fileName, content)
        _activeLocalFile.value = saved

        // Update database as well
        if (fileName.equals("main.py", ignoreCase = true)) {
            val updated = proj.copy(pythonCode = content, updatedAt = System.currentTimeMillis())
            _currentProject.value = updated
            viewModelScope.launch { repository.update(updated) }
        } else if (fileName.equals("app.kv", ignoreCase = true)) {
            val updated = proj.copy(kvCode = content, updatedAt = System.currentTimeMillis())
            _currentProject.value = updated
            viewModelScope.launch { repository.update(updated) }
        }

        refreshFileSystem()
        _statusMessage.value = "Saved ${saved.name} to disk (${saved.formattedSize})"
    }

    fun createNewLocalFile(projectName: String, fileName: String, initialContent: String? = null) {
        val created = fileManager.createNewFile(projectName, fileName, initialContent)
        refreshFileSystem()
        loadLocalFile(created)
        _statusMessage.value = "Created ${created.name} in $projectName"
    }

    fun deleteLocalFile(file: LocalProjectFile) {
        val deleted = fileManager.deleteFile(file)
        if (deleted) {
            refreshFileSystem()
            _statusMessage.value = "Deleted ${file.name} from storage"
            // If active file was deleted, switch to another
            if (_activeLocalFile.value?.absolutePath == file.absolutePath) {
                val remaining = _projectFiles.value.firstOrNull()
                if (remaining != null) {
                    loadLocalFile(remaining)
                }
            }
        }
    }

    fun duplicateLocalFile(file: LocalProjectFile) {
        val duplicated = fileManager.duplicateFile(file)
        if (duplicated != null) {
            refreshFileSystem()
            _statusMessage.value = "Duplicated ${file.name} -> ${duplicated.name}"
        }
    }

    fun renameLocalFile(file: LocalProjectFile, newName: String) {
        val renamed = fileManager.renameFile(file, newName)
        if (renamed != null) {
            refreshFileSystem()
            _activeLocalFile.value = renamed
            _statusMessage.value = "Renamed to ${renamed.name}"
        }
    }

    fun shareLocalFile(file: LocalProjectFile) {
        fileManager.shareFile(file)
    }

    fun importPythonCode(fileName: String, code: String, projectName: String? = null) {
        val targetProject = projectName ?: _currentProject.value?.name ?: "Imported Scripts"
        val saved = fileManager.saveFile(targetProject, fileName, code)
        refreshFileSystem()
        loadLocalFile(saved)
        _statusMessage.value = "Successfully imported $fileName"
    }

    fun updatePythonCode(code: String) {
        _currentPythonCode.value = code
        _currentProject.value?.let { proj ->
            val updated = proj.copy(pythonCode = code, updatedAt = System.currentTimeMillis())
            _currentProject.value = updated
            viewModelScope.launch {
                repository.update(updated)
            }
            fileManager.saveFile(proj.name, _activeLocalFile.value?.name ?: "main.py", code)
        }
    }

    fun updateKvCode(code: String) {
        _currentKvCode.value = code
        _currentProject.value?.let { proj ->
            val updated = proj.copy(kvCode = code, updatedAt = System.currentTimeMillis())
            _currentProject.value = updated
            viewModelScope.launch {
                repository.update(updated)
            }
            fileManager.saveFile(proj.name, _activeLocalFile.value?.name ?: "app.kv", code)
        }
    }

    fun runCurrentCode() {
        runtime.executeCode(_currentPythonCode.value, _currentKvCode.value)
        _activeTab.value = 1 // Switch to Runner tab
    }

    fun toggleInspectMode() {
        _isInspectMode.value = !_isInspectMode.value
        if (!_isInspectMode.value) {
            _selectedInspectWidget.value = null
        }
    }

    fun selectInspectWidget(widget: KivyWidgetNode) {
        _selectedInspectWidget.value = widget
    }

    fun clearInspectWidget() {
        _selectedInspectWidget.value = null
    }

    fun setDeviceFrameMode(mode: String) {
        _deviceFrameMode.value = mode
    }

    fun createNewProject(name: String, category: String) {
        viewModelScope.launch {
            val templateCode = """# Kivy Python 3 Application
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label

class MyApp(App):
    def build(self):
        self.title = "$name"
        layout = BoxLayout(orientation='vertical', padding=16, spacing=12)
        
        self.label = Label(text="Hello from $name!", font_size=20, color=[0.2, 0.9, 0.6, 1])
        btn = Button(text="Click Me", background_color=[0.1, 0.6, 0.9, 1], size_hint=(1, 0.3))
        btn.bind(on_press=self.on_click)
        
        layout.add_widget(self.label)
        layout.add_widget(btn)
        return layout

    def on_click(self, instance):
        self.label.text = "⚡ Kivy Button Clicked!"
        print("[App] Button pressed!")

if __name__ == '__main__':
    MyApp().run()
"""
            val newProj = KivyProjectEntity(
                name = name.ifBlank { "Untitled Project" },
                description = "Custom Python 3 & Kivy project",
                category = category,
                pythonCode = templateCode,
                kvCode = ""
            )
            val newId = repository.insert(newProj)
            val inserted = newProj.copy(id = newId)

            fileManager.syncProjectToDisk(inserted.name, inserted.pythonCode, inserted.kvCode)
            loadProject(inserted)
            runCurrentCode()
            _activeTab.value = 0
            _statusMessage.value = "Created project ${inserted.name} on local storage"
        }
    }

    fun deleteProject(project: KivyProjectEntity) {
        viewModelScope.launch {
            repository.delete(project)
            if (_currentProject.value?.id == project.id) {
                val remaining = DefaultProjects.list.first()
                loadProject(remaining)
                runCurrentCode()
            }
            refreshFileSystem()
            _statusMessage.value = "Deleted project ${project.name}"
        }
    }

    fun toggleFavorite(project: KivyProjectEntity) {
        viewModelScope.launch {
            val updated = project.copy(isFavorite = !project.isFavorite)
            repository.update(updated)
            if (_currentProject.value?.id == project.id) {
                _currentProject.value = updated
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            for (p in DefaultProjects.list) {
                repository.insert(p)
                fileManager.syncProjectToDisk(p.name, p.pythonCode, p.kvCode)
            }
            loadProject(DefaultProjects.list.first())
            runCurrentCode()
            refreshFileSystem()
        }
    }
}

