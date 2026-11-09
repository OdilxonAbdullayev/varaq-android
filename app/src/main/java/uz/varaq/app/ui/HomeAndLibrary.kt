package uz.varaq.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.varaq.app.data.Book
import uz.varaq.app.data.BookNote
import uz.varaq.app.data.BookStatus
import uz.varaq.app.data.ReaderSettings
import uz.varaq.app.data.ReadingSession
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HomeScreen(
    reader: ReaderSettings,
    books: List<Book>,
    sessions: List<ReadingSession>,
    onBook: (Long) -> Unit,
    onStart: (Long) -> Unit,
    onAdd: () -> Unit,
    onSettings: () -> Unit
) {
    val reading = books.firstOrNull { it.status == BookStatus.READING }
    val today = LocalDate.now()
    val todaySessions = sessions.filter {
        Instant.ofEpochMilli(it.finishedAt).atZone(ZoneId.systemDefault()).toLocalDate() == today
    }
    val pagesToday = todaySessions.sumOf { (it.endPage - it.startPage).coerceAtLeast(0) }
    val streakDays = calculateStreak(sessions)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            BrandTitle()
            IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, contentDescription = "Sozlamalar") }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Salom, ${reader.name.take(30)}!", style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary)
            Text("Bugun yana bir sahifa o‘qiymizmi?", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (reading != null) {
            VaraqCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("HOZIR O‘QILMOQDA", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.6.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        BookCover(reading, Modifier.size(width = 84.dp, height = 122.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(reading.title, style = MaterialTheme.typography.titleLarge)
                            Text(reading.author, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            ReadingProgress(reading.currentPage, reading.totalPages)
                        }
                    }
                    Button(onClick = { onStart(reading.id) }, Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("O‘qishni davom ettirish")
                    }
                }
            }
        } else {
            VaraqCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Yangi hikoyani boshlang", style = MaterialTheme.typography.titleLarge)
                    Text("Birinchi kitobingizni qo‘shing yoki rejadagi kitobni o‘qishni boshlang.",
                        style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = onAdd) { Icon(Icons.Outlined.Add, null); Text("Kitob qo‘shish") }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("Bugun o‘qildi", "$pagesToday", "${reader.dailyGoal} sahifa maqsad", Modifier.weight(1f))
            MetricCard("Davomiylik", "$streakDays", "kunlik odat", Modifier.weight(1f))
        }
        if (reader.dailyGoal > 0) {
            VaraqCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bugungi maqsad", style = MaterialTheme.typography.titleMedium)
                    ReadingProgress(pagesToday.coerceAtMost(reader.dailyGoal), reader.dailyGoal)
                    Text(if (pagesToday >= reader.dailyGoal) "Ajoyib! Bugungi reja bajarildi. ✨"
                        else "Yana ${(reader.dailyGoal - pagesToday).coerceAtLeast(0)} sahifa qoldi.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Mening kitoblarim", style = MaterialTheme.typography.titleLarge)
            Text("${books.size} ta", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        books.take(3).forEach { book -> BookRow(book, onClick = { onBook(book.id) }) }
        if (books.isEmpty()) {
            EmptyState("Kutubxonangiz bo‘sh", "Har bir kitob bilan yangi xotiralar boshlanadi.")
        }
        Spacer(Modifier.height(75.dp))
    }
}

fun calculateStreak(sessions: List<ReadingSession>): Int {
    val days = sessions.filter { it.durationMillis > 0 || it.endPage > it.startPage }.map {
        Instant.ofEpochMilli(it.finishedAt).atZone(ZoneId.systemDefault()).toLocalDate()
    }.toSet()
    var day = LocalDate.now()
    if (!days.contains(day)) day = day.minusDays(1)
    var streak = 0
    while (days.contains(day)) { streak++; day = day.minusDays(1) }
    return streak
}

@Composable
fun LibraryScreen(books: List<Book>, onBook: (Long) -> Unit, onAdd: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf("ALL") }
    val filtered = remember(books, query, filter) {
        books.filter { book ->
            (filter == "ALL" || book.status == filter) &&
                (book.title.contains(query, ignoreCase = true) || book.author.contains(query, ignoreCase = true) ||
                 book.genre.contains(query, ignoreCase = true))
        }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        VaraqHeader("Mening kutubxonam", "Barcha kitoblar bir joyda", action = {
            IconButton(onClick = onAdd) { Icon(Icons.Outlined.Add, contentDescription = "Kitob qo‘shish") }
        })
        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Kitob yoki muallifni qidirish") }, singleLine = true,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(vertical = 10.dp).horizontalScroll(rememberScrollState())) {
            listOf("ALL" to "Barchasi", BookStatus.READING to "O‘qilmoqda",
                BookStatus.FINISHED to "O‘qilgan", BookStatus.WISHLIST to "Rejada").forEach { (value, label) ->
                FilterChip(selected = filter == value, onClick = { filter = value }, label = {
                    Text(label, style = MaterialTheme.typography.labelSmall)
                })
            }
        }
        if (filtered.isEmpty()) {
            EmptyState("Kitob topilmadi", "Boshqa nom bilan qidiring yoki yangi kitob qo‘shing.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtered, key = { it.id }) { book -> BookRow(book, onClick = { onBook(book.id) }) }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }
}

@Composable
fun BookDetailScreen(
    book: Book,
    notes: List<BookNote>,
    sessions: List<ReadingSession>,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRead: () -> Unit,
    onNotes: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by remember { mutableStateOf(false) }
    val bookNotes = notes.filter { it.bookId == book.id }
    val bookSessions = sessions.filter { it.bookId == book.id }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        VaraqHeader("Kitob haqida", back = onBack, action = {
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = "Tahrirlash") }
        })
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                BookCover(book, Modifier.size(width = 136.dp, height = 196.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(book.title, style = MaterialTheme.typography.headlineSmall)
                    Text(book.author.ifBlank { "Muallif kiritilmagan" }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    StatusTag(book.status)
                    if (book.genre.isNotBlank()) Text(book.genre, style = MaterialTheme.typography.bodySmall)
                }
            }
            VaraqCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReadingProgress(book.currentPage, book.totalPages)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard("Qaydlar", bookNotes.size.toString(), modifier = Modifier.weight(1f))
                        MetricCard("Vaqt", formatTime(bookSessions.sumOf { it.durationMillis }), modifier = Modifier.weight(1f))
                    }
                }
            }
            if (book.rating > 0) Text("Sizning bahoyingiz: ${"★".repeat(book.rating)}", color = MaterialTheme.colorScheme.secondary)
            if (book.review.isNotBlank()) {
                VaraqCard(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Shaxsiy taassurot", style = MaterialTheme.typography.titleMedium)
                        Text(book.review)
                    }
                }
            }
            Button(onClick = onRead, enabled = book.status != BookStatus.FINISHED, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (book.status == BookStatus.FINISHED) "Kitob tugatilgan ✓" else "O‘qishni boshlash")
            }
            OutlinedButton(onClick = onNotes, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.BookmarkAdd, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Iqtiboslar va qaydlar (${bookNotes.size})")
            }
            TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null)
                Spacer(Modifier.width(7.dp))
                Text("Kitobni o‘chirish", color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(90.dp))
        }
    }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Kitob o‘chirilsinmi?") },
        text = { Text("Unga tegishli qaydlar va o‘qish tarixi ham o‘chadi. Bu amalni qaytarib bo‘lmaydi.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("O‘chirish") } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Bekor qilish") } }
    )
}
