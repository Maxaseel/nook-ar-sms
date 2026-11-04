package solutions.semweb.nook.chat

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Arabic 12-hour time formatting for timestamps visible in chats. */
object ChatDateTimeFormatter {
    private val arabicLocale = Locale.forLanguageTag("ar")

    fun formatTime(date: Date): String = format("h:mm a", date)

    fun formatDateTime(date: Date, includeYear: Boolean = false): String {
        val pattern = if (includeYear) "d/M/yyyy h:mm a" else "d/M h:mm a"
        return format(pattern, date)
    }

    fun formatDateSeparator(date: Date): String = format("EEEE، d MMMM yyyy", date)

    private fun format(pattern: String, date: Date): String {
        return SimpleDateFormat(pattern, arabicLocale).format(date).map { character ->
            if (character in '0'..'9') {
                ('٠'.code + (character - '0')).toChar()
            } else {
                character
            }
        }.joinToString("")
    }
}
