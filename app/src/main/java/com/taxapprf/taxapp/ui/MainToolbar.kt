package com.taxapprf.taxapp.ui

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.taxapprf.taxapp.R


class MainToolbar(
    private val toolbar: Toolbar,
) {
    fun updateMenu(menuId: Int = R.menu.main_toolbar, menuListener: ((MenuItem) -> Boolean)? = null) {
        toolbar.menu.clear()
        toolbar.inflateMenu(menuId)
        toolbar.setOnMenuItemClickListener(menuListener)
    }

    fun updateToolbar(title: String? = null, subtitle: String? = null) {
        toolbar.title = title
        toolbar.subtitle = subtitle
    }
}