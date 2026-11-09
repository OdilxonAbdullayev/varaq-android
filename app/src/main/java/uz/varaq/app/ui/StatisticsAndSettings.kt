package uz.varaq.app.ui

import android.Manifest
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.varaq.app.data.Book
import uz.varaq.app.data.BookStatus
import uz.varaq.app.data.ReaderSettings
import uz.varaq.app.data.ReadingSession
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun StatsScreen(books: List<Book>, sessions: List<ReadingSession>, settings: ReaderSettings) {
    val today = LocalDate.now()
    val zone = ZoneId.systemDefault()
    val year = today.year
    val finishedThisYear = books.count { b ->
        b.status == BookStatus.FINISHED && b.finishedAt?.let {
            Instant.ofEpochMilli(it).atZone(zone).year == year
        } == true
    }
    val allPages = sessions.sumOf { (it.endPage - it.startPage).coerceAtLeast(0) }
    val totalTime = sessions.sumOf { it.durationMillis }
    val streak = calculateStreak(sessions)
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val amounts = days.map { day ->
        sessions.filter { Instant.ofEpochMilli(it.finishedAt).atZone(zone).toLocalDate() == day }
            .sumOf { (it.endPage - it.startPage).coerceAtLeast(0) }
    }
    val maxPages = (amounts.maxOrNull() ?: 0).coerceAtLeast(settings.dailyGoal).coerceAtLeast(1)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)) {
        VaraqHeader("Sening natijalaring", "Har bir sahifa hisobda")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("O‘qilgan kitob", "${books.count { it.status == BookStatus.FINISHED }}", modifier = Modifier.weight(1f))
            MetricCard("Jami sahifa", "$allPages", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("O‘qish vaqti", "${totalTime / 3_600_000}", "soat (to‘liq)", Modifier.weight(1f))
            MetricCard("Ketma-ket", "$streak", "kun", Modifier.weight(1f))
        }
        VaraqCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Oxirgi 7 kun", style = MaterialTheme.typography.titleLarge)
                Text("Har kuni o‘qilgan sahifalar", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall)
                Row(
                    Modifier.fillMaxWidth().height(155.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    days.forEachIndexed { idx, date ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("${amounts[idx]}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(
                                Modifier.fillMaxWidth(.8f)
                                    .height((10 + 100f * amounts[idx] / maxPages).dp)
                                    .background(
                                        if (idx == 6) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp)
                                    )
                            )
                            Text(date.dayOfWeek.name.take(2), fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
        VaraqCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("$year-yilgi maqsad", style = MaterialTheme.typography.titleLarge)
                ReadingProgress(finishedThisYear.coerceAtMost(settings.annualGoal), settings.annualGoal, unit = "kitob")
                Text("$finishedThisYear / ${settings.annualGoal} kitob tugatildi",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (finishedThisYear >= settings.annualGoal) "Yillik maqsad bajarildi!" else
                    "Yana ${(settings.annualGoal - finishedThisYear).coerceAtLeast(0)} ta kitob qoldi.")
            }
        }
        VaraqCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Muvaffaqiyat belgilarim", style = MaterialTheme.typography.titleLarge)
                Achievement("🌱", "Ilk sahifa", allPages > 0)
                Achievement("🔥", "7 kunlik odat", streak >= 7)
                Achievement("📚", "5 ta kitob", books.count { it.status == BookStatus.FINISHED } >= 5)
                Achievement("🏆", "1000 sahifa", allPages >= 1000)
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun Achievement(icon: String, label: String, unlocked: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(icon, fontSize = 23.sp)
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(if (unlocked) "Olingan ✓" else "Hali yo‘q", style = MaterialTheme.typography.labelSmall,
            color = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun OnboardingScreen(onComplete: (String, Int, Int) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var daily by rememberSaveable { mutableStateOf("20") }
    var annual by rememberSaveable { mutableStateOf("20") }
    var error by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(30.dp))
        Icon(Icons.Outlined.AutoStories, null, modifier = Modifier.height(64.dp).width(64.dp),
            tint = MaterialTheme.colorScheme.primary)
        BrandTitle()
        Text("Har bir sahifa — yangi hikoya.", style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary)
        Text("Shaxsiy kutubxonangiz, o‘qish kundaligingiz va yutuqlaringiz — hammasi bir joyda.",
            style = MaterialTheme.typography.bodyLarge)
        VaraqCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Tanishib olaylik", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(name, { name = it.take(40) }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ismingiz qanday?") }, singleLine = true)
                OutlinedTextField(daily, { daily = it.filter(Char::isDigit).take(4) }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Kuniga nechta sahifa?") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(annual, { annual = it.filter(Char::isDigit).take(4) }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Yiliga nechta kitob?") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                if (error) Text("Ism va to‘g‘ri maqsadlarni kiriting", color = MaterialTheme.colorScheme.error)
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    val d = daily.toIntOrNull() ?: 0
                    val a = annual.toIntOrNull() ?: 0
                    error = name.isBlank() || d !in 1..1000 || a !in 1..1000
                    if (!error) onComplete(name, d, a)
                }) { Text("VARAQ'ni boshlash") }
            }
        }
        Text("Profil va kitoblaringiz dastlab faqat ushbu telefonda saqlanadi.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
    }
}

@Composable
fun SettingsScreen(
    settings: ReaderSettings,
    onBack: () -> Unit,
    onProfileSave: (String, Int, Int) -> Unit,
    onFounderUnlock: (String) -> Unit,
    onThemeToggle: (Boolean) -> Unit,
    onReminderToggle: (Boolean, Int) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(settings.name) }
    var daily by rememberSaveable { mutableStateOf(settings.dailyGoal.toString()) }
    var annual by rememberSaveable { mutableStateOf(settings.annualGoal.toString()) }
    var error by remember { mutableStateOf("") }
    var founderCode by rememberSaveable { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        VaraqHeader("Sozlamalar", "Shaxsiy o‘qish makoningiz", back = onBack)
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            VaraqCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Text("Profil va maqsadlar", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(name, { name = it.take(40) }, label = { Text("Ism") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(daily, { daily = it.filter(Char::isDigit).take(4) }, label = { Text("Kunlik sahifalar") },
                        modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(annual, { annual = it.filter(Char::isDigit).take(4) }, label = { Text("Yillik kitoblar") },
                        modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
                    Button(onClick = {
                        val d = daily.toIntOrNull() ?: 0; val a = annual.toIntOrNull() ?: 0
                        error = if (name.isBlank() || d !in 1..1000 || a !in 1..1000) "To‘g‘ri ma'lumot kiriting" else ""
                        if (error.isBlank()) onProfileSave(name, d, a)
                    }, modifier = Modifier.fillMaxWidth()) { Text("Saqlash") }
                }
            }
            VaraqCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Ko‘rinish va eslatmalar", style = MaterialTheme.typography.titleLarge)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Tungi rejim", style = MaterialTheme.typography.titleMedium)
                            Text("Ko‘zga qulay qorong‘i dizayn", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = settings.darkMode, onCheckedChange = onThemeToggle)
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Kunlik eslatma", style = MaterialTheme.typography.titleMedium)
                            Text("O‘qish vaqtini yodga soladi", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(checked = settings.reminders, onCheckedChange = { onReminderToggle(it, settings.reminderHour) })
                    }
                    Text("Eslatma vaqti", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(8 to "08:00", 13 to "13:00", 20 to "20:00").forEach { (hour, label) ->
                            FilterChip(selected = hour == settings.reminderHour, onClick = {
                                onReminderToggle(settings.reminders, hour)
                            }, label = { Text(label) })
                        }
                    }
                    Text("Android energiya tejashiga ko‘ra eslatmalar belgilangan vaqtdan biroz kech kelishi mumkin.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            VaraqCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Zaxira nusxasi", style = MaterialTheme.typography.titleLarge)
                    Text("Kitoblar, muqovalar, qaydlar va o‘qish tarixini JSON fayl orqali saqlang yoki tiklang.",
                        style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.CloudUpload, null); Spacer(Modifier.width(7.dp)); Text("Ma'lumotlarni eksport qilish")
                    }
                    OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.CloudDownload, null); Spacer(Modifier.width(7.dp)); Text("Zaxiradan tiklash")
                    }
                    Text("Tiklash telefondagi mavjud kutubxonani almashtiradi.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            VaraqCard(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (settings.founder) {
                        Text("✦  FOUNDER READER", color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleMedium)
                        Text("Ilk kitobxon uchun maxsus xat", style = MaterialTheme.typography.titleLarge)
                        Text("VARAQ'ning birinchi sahifasidan boshlab bu sayohatda bo‘lganingiz uchun rahmat. " +
                            "Har bir o‘qigan kitobingiz sizga yangi orzular, yangi fikrlar va go‘zal xotiralar olib kelsin. 📖",
                            style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text("Do‘stingiz uchun sovg‘a", style = MaterialTheme.typography.titleLarge)
                        Text("Maxsus kod bilan Founder Reader belgisini oching.",
                            style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(founderCode, { founderCode = it }, modifier = Modifier.fillMaxWidth(),
                            singleLine = true, label = { Text("Maxsus kod") })
                        OutlinedButton(onClick = { onFounderUnlock(founderCode) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Belgini ochish")
                        }
                    }
                }
            }
            Text("VARAQ 1.0 • Offline • Reklamasiz • Mahalliy ma'lumotlar", modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(80.dp))
        }
    }
}
