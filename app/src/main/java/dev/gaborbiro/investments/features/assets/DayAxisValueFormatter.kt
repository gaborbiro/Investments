package dev.gaborbiro.investments.features.assets

import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate


class DayAxisValueFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val deltaDays = LocalDate.now().toEpochDay() - value.toLong()
        val localDate = LocalDate.ofEpochDay(value.toLong())
        val now = LocalDate.now()
        val epochMonths = localDate.year * 12 + localDate.month.value
        val nowEpochMonths = now.year * 12 + now.month.value
        val deltaStr = when {
            deltaDays > 365 -> (deltaDays / 365).toInt().toString() + "y"
            deltaDays > 30 -> (nowEpochMonths - epochMonths).toString() + "m"
            deltaDays > 7 -> (deltaDays / 7).toInt().toString() + "w"
            else -> deltaDays.toString() + "d"
        }
        return "$deltaStr"
    }
}