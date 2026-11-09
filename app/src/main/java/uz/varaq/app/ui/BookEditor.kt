package uz.varaq.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.varaq.app.data.Book
import uz.varaq.app.data.BookStatus
import uz.varaq.app.util.CoverFiles

@Composable
fun BookEditorScreen(
    existing: Book?,
    onBack: () -> Unit,
    onSave: suspend (Book) -> Long,
    onSaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by rememberSaveable(existing?.id) { mutableStateOf(existing?.title ?: "") }
    var author by rememberSaveable(existing?.id) { mutableStateOf(existing?.author ?: "") }
    var genre by rememberSaveable(existing?.id) { mutableStateOf(existing?.genre ?: "") }
    var total by rememberSaveable(existing?.id) { mutableStateOf(existing?.totalPages?.toString() ?: "") }
    var current by rememberSaveable(existing?.id) { mutableStateOf(existing?.currentPage?.toString() ?: "0") }
    var status by rememberSaveable(existing?.id) { mutableStateOf(existing?.status ?: BookStatus.WISHLIST) }
    var rating by rememberSaveable(existing?.id) { mutableStateOf(existing?.rating ?: 0) }
    var review by rememberSaveable(existing?.id) { mutableStateOf(existing?.review ?: "") }
    var coverPath by rememberSaveable(existing?.id) { mutableStateOf(existing?.coverPath ?: "") }
    var error by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val pickCover = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            runCatching { withContext(Dispatchers.IO) { CoverFiles.copyImage(context, uri) } }
                .onSuccess { coverPath = it }
                .onFailure { error = it.message ?: "Rasm yuklanmadi" }
        }
    }
    Column(Modifier.fillMaxSize()) {
        VaraqHeader(if (existing == null) "Yangi kitob" else "Kitobni tahrirlash", back = onBack)
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Kutubxonangizga kitob qo‘shing", style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                BookCover(Book(title = title.ifBlank { "Yangi kitob" }, coverPath = coverPath),
                    Modifier.size(width = 100.dp, height = 145.dp))
                OutlinedButton(onClick = { pickCover.launch("image/*") }) {
                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
                    Text(" Muqova tanlash")
                }
            }
            OutlinedTextField(title, { title = it }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Kitob nomi *") }, singleLine = true)
            OutlinedTextField(author, { author = it }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Muallif") }, singleLine = true)
            OutlinedTextField(genre, { genre = it }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Janr (ixtiyoriy)") }, singleLine = true)
            OutlinedTextField(total, { total = it.filter(Char::isDigit).take(6) }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Jami sahifalar *") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(current, { current = it.filter(Char::isDigit).take(6) }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Hozirgi sahifa") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Text("Kitob holati", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf(BookStatus.WISHLIST to "Rejada", BookStatus.READING to "O‘qilmoqda",
                    BookStatus.FINISHED to "Tugatilgan").forEach { (value, label) ->
                    FilterChip(selected = status == value, onClick = { status = value }, label = { Text(label) })
                }
            }
            Text("Shaxsiy baho", style = MaterialTheme.typography.titleMedium)
            Row {
                (1..5).forEach { star ->
                    IconButton(onClick = { rating = if (rating == star) 0 else star }) {
                        Icon(if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "$star yulduz", tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            OutlinedTextField(review, { review = it }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Kitob haqidagi taassurotingiz") }, minLines = 3)
            if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)
            Button(onClick = {
                val pages = total.toIntOrNull()
                val page = current.toIntOrNull()
                error = when {
                    title.isBlank() -> "Kitob nomini kiriting"
                    pages == null || pages !in 1..100_000 -> "Jami sahifani 1–100000 oralig‘ida kiriting"
                    page == null || page !in 0..pages -> "Hozirgi sahifa 0 dan jami sahifagacha bo‘lsin"
                    else -> ""
                }
                if (error.isBlank() && pages != null && page != null) {
                    saving = true
                    scope.launch {
                        runCatching {
                            onSave((existing ?: Book(title = title)).copy(
                                title = title, author = author, genre = genre, totalPages = pages,
                                currentPage = if (status == BookStatus.FINISHED) pages else if (status == BookStatus.WISHLIST) 0 else page,
                                status = status, coverPath = coverPath, rating = rating, review = review
                            ))
                        }.onSuccess(onSaved).onFailure { error = it.message ?: "Saqlashda xatolik" }
                        saving = false
                    }
                }
            }, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
                Text(if (saving) "Saqlanmoqda..." else "Kitobni saqlash")
            }
            Spacer(Modifier.height(85.dp))
        }
    }
}
