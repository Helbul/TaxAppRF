package com.taxapprf.taxapp.ui.activity

import androidx.recyclerview.widget.RecyclerView
import com.taxapprf.taxapp.databinding.ActivityMainDrawerHeaderAddBinding

class MainAccountsAdapterAddViewHolder(
    binding: ActivityMainDrawerHeaderAddBinding,
    private val callback: MainAccountsAdapterCallback,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.setOnClickListener {
            callback.onClickAdd()
        }
    }
}