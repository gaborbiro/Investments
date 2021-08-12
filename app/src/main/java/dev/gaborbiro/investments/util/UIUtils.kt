package dev.gaborbiro.investments

import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.annotation.ColorInt

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun String.getHighlightedText(
    highlightText: String? = null,
    @ColorInt highlightColor: Int,
    highlightForeground: Boolean = false
): SpannableString {
    val ss = SpannableString(this)
    val startIndex = if (highlightText.isNullOrEmpty()) -1 else this.toLowerCase()
        .indexOf(highlightText.toLowerCase())

    if (!highlightText.isNullOrEmpty() && startIndex >= 0) {
        ss.setSpan(
            if (highlightForeground)
                ForegroundColorSpan(highlightColor)
            else
                BackgroundColorSpan(highlightColor),
            startIndex,
            startIndex + highlightText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return ss
}
