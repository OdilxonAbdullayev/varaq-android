package uz.varaq.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.varaq.app.data.Book
import uz.varaq.app.data.BookStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VaraqCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { Box(Modifier.padding(16.dp)) { content() } }
}

@Composable
fun VaraqHeader(title: String, subtitle: String? = null, back: (() -> Unit)? = null, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (back != null) {
            IconButton(onClick = back) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Orqaga") }
            Spacer(Modifier.width(4.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (action != null) action()
    }
}

@Composable
fun BrandTitle() {
    Text("VARAQ.", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.primary)
}

@Composable
fun BookCover(book: Book, modifier: Modifier = Modifier) {
    val image = remember(book.coverPath) {
        if (book.coverPath.isBlank()) null else try {
            BitmapFactory.decodeFile(book.coverPath)?.asImageBitmap()
        } catch (_: Exception) { null }
    }
    val colors = listOf(Color(0xFF365245), Color(0xFF886851), Color(0xFF7C5653), Color(0xFF4A5566), Color(0xFF8F8A60))
    Box(
        modifier = modifier.clip(RoundedCornerShape(10.dp))
            .background(if (image != null) MaterialTheme.colorScheme.surfaceVariant else colors[(book.title.hashCode() and Int.MAX_VALUE) % colors.size]),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            Image(image, contentDescription = "${book.title} muqovasi", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        } else {
            Column(
                Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.AutoStories, contentDescription = null, tint = Color.White.copy(alpha = .6f), modifier = Modifier.size(26.dp))
                Text(book.title.uppercase(Locale.ROOT), color = Color.White, fontFamily = FontFamily.Serif,
                    fontSize = 12.sp, lineHeight = 15.sp, maxLines = 4, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun ReadingProgress(page: Int, total: Int, modifier: Modifier = Modifier, unit: String = "sahifa") {
    val fraction = if (total == 0) 0f else (page.toFloat() / total).coerceIn(0f, 1f)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$page / $total $unit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${(fraction * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        LinearProgressIndicator(
            progress = { fraction }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun StatusTag(status: String) {
    val label = when (status) {
        BookStatus.READING -> "O‘qilmoqda"
        BookStatus.FINISHED -> "Tugatilgan"
        else -> "Rejada"
    }
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BookRow(book: Book, onClick: () -> Unit, modifier: Modifier = Modifier) {
    VaraqCard(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            BookCover(book, Modifier.size(width = 75.dp, height = 108.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(book.author.ifBlank { "Muallif ko‘rsatilmagan" }, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                StatusTag(book.status)
                if (book.status != BookStatus.WISHLIST) ReadingProgress(book.currentPage, book.totalPages)
            }
        }
    }
}

@Composable
fun EmptyState(title: String, description: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Outlined.AutoStories, contentDescription = null, modifier = Modifier.size(52.dp),
            tint = MaterialTheme.colorScheme.secondary)
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
    }
}

@Composable
fun MetricCard(label: String, value: String, caption: String = "", modifier: Modifier = Modifier) {
    VaraqCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            if (caption.isNotBlank()) Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun formatDate(timestamp: Long): String = SimpleDateFormat("dd.MM.yyyy", Locale("uz")).format(Date(timestamp))
fun formatTime(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000
    return if (totalMinutes >= 60) "${totalMinutes / 60} soat ${totalMinutes % 60} daq" else "$totalMinutes daqiqa"
}
