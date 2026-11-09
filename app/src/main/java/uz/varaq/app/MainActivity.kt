package uz.varaq.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.varaq.app.model.VaraqViewModel
import uz.varaq.app.ui.BookDetailScreen
import uz.varaq.app.ui.BookEditorScreen
import uz.varaq.app.ui.EmptyState
import uz.varaq.app.ui.HomeScreen
import uz.varaq.app.ui.JournalScreen
import uz.varaq.app.ui.LibraryScreen
import uz.varaq.app.ui.OnboardingScreen
import uz.varaq.app.ui.SessionScreen
import uz.varaq.app.ui.SettingsScreen
import uz.varaq.app.ui.StatsScreen
import uz.varaq.app.ui.VaraqTheme
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {
    private val viewModel: VaraqViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VaraqApp(viewModel) }
    }
}

@Composable
private fun VaraqApp(vm: VaraqViewModel) {
    val books by vm.books.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val reader by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var tab by rememberSaveable { mutableStateOf("home") }
    var overlay by rememberSaveable { mutableStateOf("") }
    var selectedBookId by rememberSaveable { mutableLongStateOf(0L) }
    var preferredJournalBook by rememberSaveable { mutableLongStateOf(0L) }
    var importedUri by remember { mutableStateOf<Uri?>(null) }
    var reminderHourToSet by remember { mutableIntStateOf(20) }
    val createBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            runCatching {
                val json = vm.exportBackup()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
                        writer.write(json)
                    } ?: error("Faylga yozib bo‘lmadi")
                }
            }.onSuccess { vm.notify("Zaxira fayl saqlandi ✓") }
                .onFailure { vm.notify("Eksport xatosi: ${it.message}") }
        }
    }
    val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importedUri = uri
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.updateReminder(true, reminderHourToSet)
        else vm.notify("Eslatmalar uchun bildirishnomaga ruxsat kerak")
    }
    LaunchedEffect(vm.message) {
        vm.message?.let { text -> snackbar.showSnackbar(text); vm.clearMessage() }
    }
    val prefs = reader
    VaraqTheme(darkTheme = prefs?.darkMode == true) {
        if (prefs == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (prefs.name.isBlank()) {
            OnboardingScreen { name, daily, annual -> vm.setupReader(name, daily, annual) }
        } else {
            val book = books.firstOrNull { it.id == selectedBookId }
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbar) },
                bottomBar = {
                    if (overlay.isEmpty()) {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            listOf(
                                Triple("home", "Bosh sahifa", Icons.Outlined.Home),
                                Triple("library", "Kutubxona", Icons.Outlined.LibraryBooks),
                                Triple("journal", "Kundalik", Icons.Outlined.EditNote),
                                Triple("stats", "Natijalar", Icons.Outlined.Insights)
                            ).forEach { (key, label, icon) ->
                                NavigationBarItem(
                                    selected = tab == key,
                                    onClick = { tab = key; if (key == "journal") preferredJournalBook = 0L },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.padding(padding).fillMaxSize()) {
                    when (overlay) {
                        "editor" -> BookEditorScreen(
                            existing = book,
                            onBack = { overlay = if (selectedBookId > 0) "detail" else "" },
                            onSave = { item -> vm.saveBook(item) },
                            onSaved = { id -> selectedBookId = id; overlay = "detail"; vm.notify("Kitob saqlandi ✓") }
                        )
                        "detail" -> {
                            if (book != null) BookDetailScreen(
                                book = book, notes = notes, sessions = sessions,
                                onBack = { overlay = "" },
                                onEdit = { overlay = "editor" },
                                onRead = { overlay = "session" },
                                onNotes = { preferredJournalBook = book.id; overlay = ""; tab = "journal" },
                                onDelete = {
                                    scope.launch {
                                        runCatching { vm.deleteBook(book) }
                                            .onSuccess { overlay = ""; tab = "library"; selectedBookId = 0L; vm.notify("Kitob o‘chirildi") }
                                            .onFailure { vm.notify("Xatolik: ${it.message}") }
                                    }
                                }
                            ) else EmptyState("Kitob topilmadi", "Kutubxonaga qayting")
                        }
                        "session" -> {
                            if (book != null) SessionScreen(
                                book = book, onBack = { overlay = "detail" },
                                onSave = { id, page, time -> vm.completeSession(id, page, time) },
                                onSaved = { overlay = "detail"; vm.notify("O‘qish sessiyasi saqlandi ✓") }
                            ) else EmptyState("Kitob topilmadi", "Kutubxonaga qayting")
                        }
                        "settings" -> SettingsScreen(
                            settings = prefs,
                            onBack = { overlay = "" },
                            onProfileSave = { name, daily, annual ->
                                vm.updateProfile(name, daily, annual); vm.notify("Profil saqlandi ✓")
                            },
                            onFounderUnlock = { vm.unlockFounder(it) },
                            onThemeToggle = { vm.updateTheme(it) },
                            onReminderToggle = { enabled, hour ->
                                if (enabled && Build.VERSION.SDK_INT >= 33 &&
                                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    reminderHourToSet = hour
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else vm.updateReminder(enabled, hour)
                            },
                            onExport = { createBackup.launch("varaq-backup.json") },
                            onImport = { pickBackup.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }
                        )
                        else -> when (tab) {
                            "home" -> HomeScreen(prefs, books, sessions,
                                onBook = { selectedBookId = it; overlay = "detail" },
                                onStart = { selectedBookId = it; overlay = "session" },
                                onAdd = { selectedBookId = 0L; overlay = "editor" },
                                onSettings = { overlay = "settings" })
                            "library" -> LibraryScreen(books,
                                onBook = { selectedBookId = it; overlay = "detail" },
                                onAdd = { selectedBookId = 0L; overlay = "editor" })
                            "journal" -> JournalScreen(books, notes, preferredJournalBook,
                                onAddNote = { vm.addNote(it) },
                                onDeleteNote = { vm.deleteNote(it) },
                                onAddBook = { selectedBookId = 0L; overlay = "editor" })
                            "stats" -> StatsScreen(books, sessions, prefs)
                        }
                    }
                }
            }
        }
        importedUri?.let { uri ->
            AlertDialog(
                onDismissRequest = { importedUri = null },
                title = { Text("Zaxiradan tiklash") },
                text = { Text("Diqqat: joriy kitoblar, qaydlar va o‘qish tarixining o‘rnini fayldagi ma'lumotlar egallaydi. Avval eksport qilib qo‘yish tavsiya etiladi.") },
                confirmButton = { TextButton(onClick = {
                    importedUri = null
                    scope.launch {
                        runCatching {
                            val json = withContext(Dispatchers.IO) { readBackup(context, uri) }
                            vm.importBackup(json)
                        }.onSuccess { overlay = ""; tab = "library"; selectedBookId = 0L; vm.notify("Zaxira tiklandi ✓") }
                            .onFailure { vm.notify("Import xatosi: ${it.message}") }
                    }
                }) { Text("Tiklash") } },
                dismissButton = { TextButton(onClick = { importedUri = null }) { Text("Bekor qilish") } }
            )
        }
    }
}

private fun readBackup(context: Context, uri: Uri): String {
    val input = context.contentResolver.openInputStream(uri) ?: error("Fayl ochilmadi")
    input.use { stream ->
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0
        while (true) {
            val n = stream.read(buffer)
            if (n < 0) break
            total += n
            require(total <= 25_000_000) { "Zaxira fayli 25 MB dan oshmasligi kerak" }
            out.write(buffer, 0, n)
        }
        return out.toString(Charsets.UTF_8.name())
    }
}
