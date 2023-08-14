package com.taxapprf.taxapp.ui

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar


class MainToolbar(
    private val toolbar: Toolbar,
) {
    fun updateMenu(menuId: Int, menuListener: ((MenuItem) -> Boolean)? = null) {
        toolbar.menu.clear()
        toolbar.inflateMenu(menuId)
        toolbar.setOnMenuItemClickListener(menuListener)
    }

    fun updateToolbar(title: String? = null, subtitle: String? = null) {
        toolbar.title = title
        toolbar.subtitle = subtitle
    }
}