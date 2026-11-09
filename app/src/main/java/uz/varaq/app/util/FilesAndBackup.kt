package uz.varaq.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import uz.varaq.app.data.Book
import uz.varaq.app.data.BookNote
import uz.varaq.app.data.ReadingSession
import uz.varaq.app.data.ReaderSettings
import uz.varaq.app.data.VaraqDatabase
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

object CoverFiles {
    fun copyImage(context: Context, uri: Uri): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArrayOutputStream()
            val piece = ByteArray(8192)
            var size = 0
            while (true) {
                val n = input.read(piece)
                if (n < 0) break
                size += n
                require(size <= 15_000_000) { "Rasm 15 MB dan kichik bo‘lishi kerak" }
                buffer.write(piece, 0, n)
            }
            buffer.toByteArray()
        } ?: error("Rasm ochilmadi")

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Bu rasm formati qo‘llab-quvvatlanmaydi" }
        var sample = 1
        while (bounds.outWidth / sample > 1200 || bounds.outHeight / sample > 1600) sample *= 2
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size,
            BitmapFactory.Options().apply { inSampleSize = sample }
        ) ?: error("Rasm ochilmadi")
        val maxWidth = 840
        val maxHeight = 1120
        val ratio = minOf(1f, minOf(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height))
        val scaled = if (ratio < 1f) Bitmap.createScaledBitmap(
            bitmap, (bitmap.width * ratio).toInt().coerceAtLeast(1), (bitmap.height * ratio).toInt().coerceAtLeast(1), true
        ) else bitmap
        val dir = File(context.filesDir, "covers").also { it.mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 82, it) }
        if (scaled !== bitmap) scaled.recycle()
        bitmap.recycle()
        return file.absolutePath
    }

    fun restoreFromBase64(context: Context, value: String): String {
        if (value.isBlank()) return ""
        require(value.length <= 5_000_000) { "Muqova hajmi juda katta" }
        val bytes = Base64.decode(value, Base64.DEFAULT)
        require(bytes.size <= 4_000_000) { "Muqova hajmi juda katta" }
        val dir = File(context.filesDir, "covers").also { it.mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    fun encode(path: String): String {
        val file = File(path)
        if (!file.isFile || file.length() > 4_000_000) return ""
        return Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
    }
}

object BackupManager {
    private fun JSONObject.nullableLong(key: String): Long? = if (isNull(key)) null else optLong(key)
    private fun JSONObject.nullableInt(key: String): Int? = if (isNull(key)) null else optInt(key)

    suspend fun export(context: Context, db: VaraqDatabase, settings: ReaderSettings): String = withContext(Dispatchers.IO) {
        val dao = db.dao()
        val books = dao.allBooks()
        val notes = dao.allNotes()
        val sessions = dao.allSessions()
        val payload = JSONObject().apply {
            put("format", "VARAQ-BACKUP")
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("settings", JSONObject().apply {
                put("name", settings.name)
                put("dailyGoal", settings.dailyGoal)
                put("annualGoal", settings.annualGoal)
                put("darkMode", settings.darkMode)
                put("founder", settings.founder)
            })
            put("books", JSONArray().apply {
                books.forEach { book -> addBook(book) }
            })
            put("notes", JSONArray().apply {
                notes.forEach { note ->
                    put(JSONObject().apply {
                        put("id", note.id); put("bookId", note.bookId)
                        put("kind", note.kind); put("text", note.text)
                        put("page", note.page ?: JSONObject.NULL); put("createdAt", note.createdAt)
                    })
                }
            })
            put("sessions", JSONArray().apply {
                sessions.forEach { session ->
                    put(JSONObject().apply {
                        put("id", session.id); put("bookId", session.bookId)
                        put("startedAt", session.startedAt); put("finishedAt", session.finishedAt)
                        put("startPage", session.startPage); put("endPage", session.endPage)
                        put("durationMillis", session.durationMillis)
                    })
                }
            })
        }
        payload.toString(2)
    }

    private fun JSONArray.addBook(book: Book) {
        put(JSONObject().apply {
            put("id", book.id); put("title", book.title); put("author", book.author)
            put("totalPages", book.totalPages); put("currentPage", book.currentPage)
            put("status", book.status); put("genre", book.genre)
            put("coverBase64", if (book.coverPath.isNotBlank()) CoverFiles.encode(book.coverPath) else "")
            put("rating", book.rating); put("review", book.review)
            put("createdAt", book.createdAt); put("startedAt", book.startedAt ?: JSONObject.NULL)
            put("finishedAt", book.finishedAt ?: JSONObject.NULL); put("updatedAt", book.updatedAt)
        })
    }

    data class ImportedSettings(val name: String, val dailyGoal: Int, val annualGoal: Int, val darkMode: Boolean, val founder: Boolean)

    suspend fun import(context: Context, db: VaraqDatabase, json: String): ImportedSettings = withContext(Dispatchers.IO) {
        require(json.length <= 25_000_000) { "Fayl juda katta (25 MB limit)" }
        val root = JSONObject(json)
        require(root.optString("format") == "VARAQ-BACKUP" && root.optInt("version") == 1) {
            "Bu VARAQ 1.0 zaxira fayli emas"
        }
        val bookArray = root.getJSONArray("books")
        val noteArray = root.getJSONArray("notes")
        val sessionArray = root.getJSONArray("sessions")
        require(bookArray.length() <= 10_000 && noteArray.length() <= 50_000 && sessionArray.length() <= 150_000) {
            "Fayldagi yozuvlar soni juda katta"
        }
        val books = mutableListOf<Book>()
        val notes = mutableListOf<BookNote>()
        val sessions = mutableListOf<ReadingSession>()
        for (i in 0 until bookArray.length()) {
            val b = bookArray.getJSONObject(i)
            val total = b.getInt("totalPages").coerceIn(1, 100_000)
            val id = b.getLong("id")
            require(id > 0) { "Kitob ID xato" }
            val title = b.getString("title").trim()
            require(title.isNotEmpty()) { "Kitob nomi bo‘sh" }
            books += Book(
                id = id, title = title, author = b.optString("author"), totalPages = total,
                currentPage = b.optInt("currentPage").coerceIn(0, total),
                status = b.optString("status", "WISHLIST"), genre = b.optString("genre"),
                coverPath = CoverFiles.restoreFromBase64(context, b.optString("coverBase64")),
                rating = b.optInt("rating").coerceIn(0, 5), review = b.optString("review"),
                createdAt = b.optLong("createdAt"), startedAt = b.nullableLong("startedAt"),
                finishedAt = b.nullableLong("finishedAt"), updatedAt = b.optLong("updatedAt")
            )
        }
        val bookIds = books.map { it.id }.toSet()
        require(bookIds.size == books.size) { "Takrorlangan kitob ID" }
        for (i in 0 until noteArray.length()) {
            val n = noteArray.getJSONObject(i)
            val bookId = n.getLong("bookId")
            require(bookId in bookIds) { "Qayd kitobi topilmadi" }
            notes += BookNote(
                id = n.getLong("id"), bookId = bookId, kind = n.optString("kind", "THOUGHT"),
                text = n.getString("text"), page = n.nullableInt("page"), createdAt = n.optLong("createdAt")
            )
        }
        for (i in 0 until sessionArray.length()) {
            val s = sessionArray.getJSONObject(i)
            val bookId = s.getLong("bookId")
            require(bookId in bookIds) { "O‘qish sessiyasi kitobi topilmadi" }
            sessions += ReadingSession(
                id = s.getLong("id"), bookId = bookId, startedAt = s.getLong("startedAt"),
                finishedAt = s.getLong("finishedAt"), startPage = s.getInt("startPage"),
                endPage = s.getInt("endPage"), durationMillis = s.getLong("durationMillis")
            )
        }
        val dao = db.dao()
        db.withTransaction {
            dao.deleteAllSessions()
            dao.deleteAllNotes()
            dao.deleteAllBooks()
            books.forEach { dao.insertBook(it) }
            notes.forEach { dao.insertNote(it) }
            sessions.forEach { dao.insertSession(it) }
        }
        val pref = root.getJSONObject("settings")
        ImportedSettings(
            name = pref.optString("name", "Kitobxon"),
            dailyGoal = pref.optInt("dailyGoal", 20).coerceAtLeast(1),
            annualGoal = pref.optInt("annualGoal", 20).coerceAtLeast(1),
            darkMode = pref.optBoolean("darkMode"), founder = pref.optBoolean("founder")
        )
    }
}
