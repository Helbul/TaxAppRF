package com.taxapprf.taxapp.ui.activity

import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Layer
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.taxapprf.taxapp.R

class MainActivityDrawer(
    private val navView: NavigationView,
) {
    val header
        get() = navView.getHeaderView(HEADER_POSITION)
    val expand
        get() = navView.findViewById<ImageView>(R.id.imageNavHeaderUserAccountExpand)
    val recycler
        get() = header.findViewById<RecyclerView>(R.id.recyclerNavHeaderAccounts)
    val accounts
        get() = header.findViewById<Layer>(R.id.layerNavHeaderAccounts)
    val account
        get() = header.findViewById<TextView>(R.id.textNavHeaderUserAccount)

    private var isAccountsExpand = false

    private fun expandMoreAccounts() {
        expand.setImageResource(R.drawable.ic_baseline_expand_less_24)
        recycler.isVisible = true
    }

    private fun expandLessAccounts() {
        expand.setImageResource(R.drawable.ic_baseline_expand_more_24)
        recycler.isVisible = false
    }

    init {
        accounts.setOnClickListener {
            if (isAccountsExpand) expandLessAccounts()
            else expandMoreAccounts()
            isAccountsExpand = !isAccountsExpand
        }

    }

    companion object {
        private const val HEADER_POSITION = 0
    }
}