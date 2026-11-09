package uz.varaq.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.preferencesStore: DataStore<Preferences> by preferencesDataStore("varaq_settings")

data class ReaderSettings(
    val name: String = "",
    val dailyGoal: Int = 20,
    val annualGoal: Int = 20,
    val darkMode: Boolean = false,
    val reminders: Boolean = false,
    val reminderHour: Int = 20,
    val founder: Boolean = false
)

class VaraqPreferences(private val context: Context) {
    private object Keys {
        val NAME = stringPreferencesKey("name")
        val DAILY = intPreferencesKey("daily_goal")
        val ANNUAL = intPreferencesKey("annual_goal")
        val DARK = booleanPreferencesKey("dark_mode")
        val REMINDERS = booleanPreferencesKey("reminders")
        val HOUR = intPreferencesKey("hour")
        val FOUNDER = booleanPreferencesKey("founder_reader")
    }

    val settings: Flow<ReaderSettings> = context.preferencesStore.data.map { prefs ->
        ReaderSettings(
            name = prefs[Keys.NAME].orEmpty(),
            dailyGoal = prefs[Keys.DAILY] ?: 20,
            annualGoal = prefs[Keys.ANNUAL] ?: 20,
            darkMode = prefs[Keys.DARK] ?: false,
            reminders = prefs[Keys.REMINDERS] ?: false,
            reminderHour = prefs[Keys.HOUR] ?: 20,
            founder = prefs[Keys.FOUNDER] ?: false
        )
    }

    suspend fun updateProfile(name: String, dailyGoal: Int, annualGoal: Int) {
        context.preferencesStore.edit { p ->
            p[Keys.NAME] = name.trim()
            p[Keys.DAILY] = dailyGoal.coerceAtLeast(1)
            p[Keys.ANNUAL] = annualGoal.coerceAtLeast(1)
        }
    }

    suspend fun updateTheme(dark: Boolean) {
        context.preferencesStore.edit { it[Keys.DARK] = dark }
    }

    suspend fun unlockFounder() {
        updateFounder(true)
    }

    suspend fun updateFounder(enabled: Boolean) {
        context.preferencesStore.edit { it[Keys.FOUNDER] = enabled }
    }

    suspend fun updateReminder(enabled: Boolean, hour: Int) {
        context.preferencesStore.edit { p ->
            p[Keys.REMINDERS] = enabled
            p[Keys.HOUR] = hour.coerceIn(0, 23)
        }
    }
}
