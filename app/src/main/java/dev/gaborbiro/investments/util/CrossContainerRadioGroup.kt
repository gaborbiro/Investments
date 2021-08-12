package dev.gaborbiro.investments.util

import android.view.View
import android.widget.CompoundButton

class CrossContainerRadioGroup(
    vararg buttons: CompoundButton,
    private val onCheckChanged: (View) -> Unit
) {

    private val buttons: MutableList<CompoundButton> = mutableListOf()

    init {
        buttons.forEach(this::add)
    }

    private fun add(button: CompoundButton) {
        if (!buttons.contains(button)) {
            buttons.add(button)
            button.setOnClickListener {
                onCheckChanged(it)
                buttons.filter { it != button }.forEach {
                    it.isChecked = false
                }
            }
        }
    }
}