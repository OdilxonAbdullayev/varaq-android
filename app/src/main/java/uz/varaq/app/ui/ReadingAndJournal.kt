package uz.varaq.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import uz.varaq.app.data.Book
import uz.varaq.app.data.BookNote
import uz.varaq.app.data.BookStatus

@Composable
fun SessionScreen(book: Book, onBack: () -> Unit, onSave: suspend (Long, Int, Long) -> Unit, onSaved: () -> Unit) {
    var seconds by rememberSaveable(book.id) { mutableLongStateOf(0L) }
    var isRunning by rememberSaveable(book.id) { mutableStateOf(true) }
    var endPage by rememberSaveable(book.id) { mutableStateOf(book.currentPage.toString()) }
    var error by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            seconds += 1
        }
    }
    val hours = seconds / 3600
    val minutes = seconds / 60 % 60
    val secs = seconds % 60

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        VaraqHeader("O‘qish sessiyasi", back = onBack)
        BookCover(book, Modifier.size(width = 110.dp, height = 160.dp))
        Text(book.title, style = MaterialTheme.typography.headlineSmall)
        Text("Diqqatni faqat kitobga qarating", color = MaterialTheme.colorScheme.onSurfaceVariant)
        VaraqCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("O‘QISH VAQTI", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%02d:%02d:%02d".format(hours, minutes, secs),
                    fontFamily = FontFamily.Serif, fontSize = 42.sp, color = MaterialTheme.colorScheme.primary)
                OutlinedButton(onClick = { isRunning = !isRunning }) {
                    Icon(if (isRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, null)
                    Spacer(Modifier.width(7.dp))
                    Text(if (isRunning) "Pauza" else "Davom ettirish")
                }
            }
        }
        VaraqCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                Text("Sahifalar", style = MaterialTheme.typography.titleMedium)
                Text("Boshlangan sahifa: ${book.currentPage}  •  Jami: ${book.totalPages}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    endPage, { endPage = it.filter(Char::isDigit).take(6) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("Qaysi sahifada to‘xtadingiz?") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Button(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), enabled = !saving,
            onClick = {
                val target = endPage.toIntOrNull()
                error = if (target == null || target !in book.currentPage..book.totalPages)
                    "Sahifani ${book.currentPage}–${book.totalPages} oralig‘ida kiriting" else ""
                if (target != null && error.isEmpty()) {
                    saving = true
                    isRunning = false
                    scope.launch {
                        runCatching { onSave(book.id, target, seconds * 1000) }
                            .onSuccess { onSaved() }
                            .onFailure { error = it.message ?: "Sessiya saqlanmadi"; saving = false }
                    }
                }
            }
        ) { Text(if (saving) "Saqlanmoqda..." else "O‘qishni yakunlash") }
        TextButton(onClick = onBack) { Text("Saqlamasdan chiqish") }
        Spacer(Modifier.height(50.dp))
    }
}

@Composable
fun JournalScreen(
    books: List<Book>,
    notes: List<BookNote>,
    preferredBookId: Long,
    onAddNote: suspend (BookNote) -> Unit,
    onDeleteNote: suspend (BookNote) -> Unit,
    onAddBook: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var filterBook by rememberSaveable(preferredBookId) { mutableLongStateOf(preferredBookId) }
    var filterKind by rememberSaveable { mutableStateOf("ALL") }
    var isEditorOpen by remember { mutableStateOf(false) }
    var deletingNote by remember { mutableStateOf<BookNote?>(null) }
    var error by remember { mutableStateOf("") }
    val filtered = notes.filter {
        (filterBook == 0L || filterBook == it.bookId) && (filterKind == "ALL" || filterKind == it.kind)
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        VaraqHeader("Kitob kundaligi", "Iqtiboslar, fikrlar va xotiralar", action = {
            IconButton(onClick = { if (books.isNotEmpty()) { error = ""; isEditorOpen = true } else onAddBook() }) {
                Icon(Icons.Outlined.Add, contentDescription = "Qayd qo‘shish")
            }
        })
        Text("Kitob tanlang", style = MaterialTheme.typography.labelMedium)
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 7.dp)) {
            item { FilterChip(filterBook == 0L, { filterBook = 0L }, label = { Text("Barcha kitoblar") }) }
            items(books, key = { it.id }) { book ->
                FilterChip(filterBook == book.id, { filterBook = book.id }, label = {
                    Text(book.title, maxLines = 1)
                })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL" to "Hammasi", "QUOTE" to "Iqtiboslar", "THOUGHT" to "Fikrlar").forEach { (key, label) ->
                FilterChip(filterKind == key, { filterKind = key }, label = { Text(label) })
            }
        }
        if (filtered.isEmpty()) {
            EmptyState("Hali qayd yo‘q", "Yoqtirgan jumlalar yoki o‘qiganlaringizdan chiqargan xulosalarni saqlang.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { note ->
                    val book = books.firstOrNull { it.id == note.bookId }
                    VaraqCard(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (note.kind == "QUOTE") "❝  IQTIBOS" else "✎  FIKR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                                IconButton(onClick = { deletingNote = note }) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Qaydni o‘chirish")
                                }
                            }
                            Text(note.text, style = MaterialTheme.typography.bodyLarge)
                            Text("${book?.title.orEmpty()}${if (note.page != null) "  •  ${note.page}-sahifa" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
    if (isEditorOpen && books.isNotEmpty()) NoteEditorDialog(
        books = books, defaultBookId = filterBook.takeIf { id -> books.any { it.id == id } } ?: books.first().id,
        onDismiss = { isEditorOpen = false },
        onSave = { note ->
            scope.launch {
                runCatching { onAddNote(note) }
                    .onSuccess { isEditorOpen = false }
                    .onFailure { error = it.message ?: "Qayd saqlanmadi" }
            }
        }, error = error
    )
    deletingNote?.let { note -> AlertDialog(
        onDismissRequest = { deletingNote = null },
        title = { Text("Qaydni o‘chirish") },
        text = { Text("Bu qaydni butunlay o‘chirmoqchimisiz?") },
        confirmButton = { TextButton(onClick = {
            scope.launch { onDeleteNote(note) }
            deletingNote = null
        }) { Text("O‘chirish") } },
        dismissButton = { TextButton(onClick = { deletingNote = null }) { Text("Bekor qilish") } }
    ) }
}

@Composable
private fun NoteEditorDialog(
    books: List<Book>, defaultBookId: Long, onDismiss: () -> Unit,
    onSave: (BookNote) -> Unit, error: String
) {
    var selectedId by remember { mutableLongStateOf(defaultBookId) }
    var kind by remember { mutableStateOf("QUOTE") }
    var content by remember { mutableStateOf("") }
    var page by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yangi qayd") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Kitob", style = MaterialTheme.typography.labelMedium)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(books, key = { it.id }) { book ->
                        FilterChip(selectedId == book.id, { selectedId = book.id }, label = {
                            Text(book.title, maxLines = 1)
                        })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    FilterChip(kind == "QUOTE", { kind = "QUOTE" }, label = { Text("Iqtibos") })
                    FilterChip(kind == "THOUGHT", { kind = "THOUGHT" }, label = { Text("Fikr") })
                }
                OutlinedTextField(content, { content = it }, label = { Text("Matn") }, minLines = 3,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(page, { page = it.filter(Char::isDigit) }, label = { Text("Sahifa (ixtiyoriy)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                if (formError.isNotBlank() || error.isNotBlank())
                    Text(formError.ifBlank { error }, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = {
            formError = if (content.isBlank()) "Matn kiriting" else ""
            if (formError.isBlank()) onSave(BookNote(bookId = selectedId, kind = kind, text = content,
                page = page.toIntOrNull()))
        }) { Text("Saqlash") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Bekor qilish") } }
    )
}
