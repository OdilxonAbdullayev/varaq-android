package uz.varaq.app.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uz.varaq.app.data.Book
import uz.varaq.app.data.BookNote
import uz.varaq.app.data.BookStatus
import uz.varaq.app.data.ReaderSettings
import uz.varaq.app.data.ReadingSession
import uz.varaq.app.data.VaraqDatabase
import uz.varaq.app.data.VaraqPreferences
import uz.varaq.app.util.BackupManager
import uz.varaq.app.util.ReadingReminders
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class VaraqViewModel(application: Application) : AndroidViewModel(application) {
    private val db = VaraqDatabase.get(application)
    private val dao = db.dao()
    private val preferences = VaraqPreferences(application)

    val books = dao.observeBooks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val notes = dao.observeNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val sessions = dao.observeSessions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings = preferences.settings.map { it as ReaderSettings? }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    var message by mutableStateOf<String?>(null)
        private set

    fun notify(messageText: String) { message = messageText }
    fun clearMessage() { message = null }

    fun setupReader(name: String, dailyGoal: Int, annualGoal: Int) {
        viewModelScope.launch { preferences.updateProfile(name, dailyGoal, annualGoal) }
    }

    suspend fun saveBook(book: Book): Long = withContext(Dispatchers.IO) {
        require(book.title.isNotBlank()) { "Kitob nomini kiriting" }
        require(book.totalPages in 1..100_000) { "Sahifalar soni noto‘g‘ri" }
        val now = System.currentTimeMillis()
        val page = book.currentPage.coerceIn(0, book.totalPages)
        val status = when {
            page == book.totalPages -> BookStatus.FINISHED
            book.status == BookStatus.FINISHED -> BookStatus.FINISHED
            page > 0 && book.status == BookStatus.WISHLIST -> BookStatus.READING
            else -> book.status
        }
        val prepared = book.copy(
            title = book.title.trim(), author = book.author.trim(), totalPages = book.totalPages,
            currentPage = if (status == BookStatus.FINISHED) book.totalPages else page,
            status = status, updatedAt = now,
            startedAt = if (status == BookStatus.READING) book.startedAt ?: now else book.startedAt,
            finishedAt = if (status == BookStatus.FINISHED) book.finishedAt ?: now else null
        )
        if (prepared.id == 0L) dao.insertBook(prepared) else {
            dao.updateBook(prepared)
            prepared.id
        }
    }

    suspend fun deleteBook(book: Book) = withContext(Dispatchers.IO) {
        dao.deleteBook(book)
    }

    suspend fun addNote(note: BookNote) = withContext(Dispatchers.IO) {
        require(note.text.isNotBlank()) { "Qayd matnini kiriting" }
        require(dao.bookById(note.bookId) != null) { "Kitob topilmadi" }
        dao.insertNote(note.copy(text = note.text.trim()))
    }

    suspend fun deleteNote(note: BookNote) = withContext(Dispatchers.IO) { dao.deleteNote(note) }

    suspend fun completeSession(bookId: Long, targetPage: Int, readingTimeMs: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val book = dao.bookById(bookId) ?: error("Kitob topilmadi")
            require(targetPage in book.currentPage..book.totalPages) { "Sahifa oralig‘i noto‘g‘ri" }
            val now = System.currentTimeMillis()
            dao.insertSession(
                ReadingSession(
                    bookId = bookId,
                    startedAt = now - readingTimeMs.coerceAtLeast(0L),
                    finishedAt = now,
                    startPage = book.currentPage,
                    endPage = targetPage,
                    durationMillis = readingTimeMs.coerceAtLeast(0L)
                )
            )
            dao.updateBook(book.copy(
                currentPage = targetPage,
                status = if (targetPage == book.totalPages) BookStatus.FINISHED else BookStatus.READING,
                startedAt = book.startedAt ?: now,
                finishedAt = if (targetPage == book.totalPages) now else null,
                updatedAt = now
            ))
        }
    }

    fun unlockFounder(code: String) {
        if (code.trim().uppercase() != "VARAQ-BIRINCHI") {
            notify("Kod noto‘g‘ri")
            return
        }
        viewModelScope.launch {
            preferences.unlockFounder()
            notify("Birinchi kitobxon belgisi ochildi! ✨")
        }
    }

    fun updateProfile(name: String, daily: Int, annual: Int) {
        viewModelScope.launch { preferences.updateProfile(name, daily, annual) }
    }

    fun updateTheme(dark: Boolean) {
        viewModelScope.launch { preferences.updateTheme(dark) }
    }

    fun updateReminder(enabled: Boolean, hour: Int) {
        viewModelScope.launch {
            preferences.updateReminder(enabled, hour)
            ReadingReminders.schedule(getApplication(), enabled, hour)
        }
    }

    suspend fun exportBackup(): String = BackupManager.export(
        getApplication(), db, settings.value ?: ReaderSettings()
    )

    suspend fun importBackup(text: String) {
        val imported = BackupManager.import(getApplication(), db, text)
        preferences.updateProfile(imported.name, imported.dailyGoal, imported.annualGoal)
        preferences.updateTheme(imported.darkMode)
        preferences.updateFounder(imported.founder)
    }
}
