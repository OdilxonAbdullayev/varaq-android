package uz.varaq.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

object BookStatus {
    const val READING = "READING"
    const val FINISHED = "FINISHED"
    const val WISHLIST = "WISHLIST"
}

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String = "",
    val totalPages: Int = 1,
    val currentPage: Int = 0,
    val status: String = BookStatus.WISHLIST,
    val genre: String = "",
    val coverPath: String = "",
    val rating: Int = 0,
    val review: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(entity = Book::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("bookId")]
)
data class BookNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val kind: String = "THOUGHT",
    val text: String,
    val page: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sessions",
    foreignKeys = [ForeignKey(entity = Book::class, parentColumns = ["id"], childColumns = ["bookId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("bookId")]
)
data class ReadingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val startedAt: Long,
    val finishedAt: Long,
    val startPage: Int,
    val endPage: Int,
    val durationMillis: Long
)

@Dao
interface VaraqDao {
    @Query("SELECT * FROM books ORDER BY updatedAt DESC, id DESC")
    fun observeBooks(): Flow<List<Book>>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC, id DESC")
    fun observeNotes(): Flow<List<BookNote>>

    @Query("SELECT * FROM sessions ORDER BY finishedAt DESC, id DESC")
    fun observeSessions(): Flow<List<ReadingSession>>

    @Query("SELECT * FROM books ORDER BY id")
    suspend fun allBooks(): List<Book>

    @Query("SELECT * FROM notes ORDER BY id")
    suspend fun allNotes(): List<BookNote>

    @Query("SELECT * FROM sessions ORDER BY id")
    suspend fun allSessions(): List<ReadingSession>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun bookById(id: Long): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: BookNote): Long

    @Delete
    suspend fun deleteNote(note: BookNote)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ReadingSession): Long

    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()
}

@Database(entities = [Book::class, BookNote::class, ReadingSession::class], version = 1, exportSchema = false)
abstract class VaraqDatabase : RoomDatabase() {
    abstract fun dao(): VaraqDao

    companion object {
        @Volatile private var instance: VaraqDatabase? = null
        fun get(context: Context): VaraqDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, VaraqDatabase::class.java, "varaq.db")
                .build().also { instance = it }
        }
    }
}
