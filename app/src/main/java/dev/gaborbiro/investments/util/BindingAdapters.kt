import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.databinding.BindingAdapter

@BindingAdapter("bind:highlightText", "bind:highlightColor", requireAll = true)
fun TextView.highlightText(text: String, @ColorInt highlightColor: Int) {
    val currentText = this.text.toString()
    this.text = currentText.getHighlightedText(text, highlightColor)
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