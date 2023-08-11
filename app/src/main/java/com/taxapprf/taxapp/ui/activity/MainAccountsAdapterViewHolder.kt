package com.taxapprf.taxapp.ui.activity

import androidx.recyclerview.widget.RecyclerView
import com.taxapprf.domain.account.AccountModel
import com.taxapprf.taxapp.databinding.ActivityMainDrawerHeaderItemBinding

class MainAccountsAdapterViewHolder(
    private val binding: ActivityMainDrawerHeaderItemBinding,
    private val callback: MainAccountsAdapterCallback,
) : RecyclerView.ViewHolder(binding.root) {
    private lateinit var account: AccountModel

    init {
        binding.drawerHeaderItem.setOnClickListener {
            callback.onClick(account)
        }
    }

    fun bind(accountModel: AccountModel) {
        account = accountModel

        binding.drawerHeaderItemName.text = account.name
    }
}