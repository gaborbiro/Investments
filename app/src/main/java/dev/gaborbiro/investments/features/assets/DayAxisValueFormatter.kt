package dev.gaborbiro.investments.features.assets

import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate


class DayAxisValueFormatter : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val delta = LocalDate.now().toEpochDay() - value.toLong()
        val localDate = LocalDate.ofEpochDay(value.toLong())
        val now = LocalDate.now()
        val epochMonths = localDate.year * 12 + localDate.month.value
        val nowEpochMonths = now.year * 12 + now.month.value
        val deltaStr = when {
            delta > 365 -> (delta / 365).toInt().toString() + "y"
            epochMonths < nowEpochMonths -> (nowEpochMonths - epochMonths).toString() + "m"
            else -> delta.toString() + "d"
        }
        return "-$deltaStr"
    }
}