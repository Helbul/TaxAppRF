package com.taxapprf.taxapp.ui.transactions

import androidx.recyclerview.widget.RecyclerView
import com.taxapprf.domain.transaction.TransactionModel
import com.taxapprf.taxapp.databinding.FragmentTransactionsAdapterItemBinding

class TransactionAdapterViewHolder(
    private val binding: FragmentTransactionsAdapterItemBinding,
    private val callback: TransactionsAdapterCallback,
) : RecyclerView.ViewHolder(binding.root) {
    private lateinit var transaction: TransactionModel

    init {
        binding.root.setOnClickListener {
            callback.onClick(transaction.key)
        }
    }

    fun bind(transactionModel: TransactionModel) {
        transaction = transactionModel

        binding.textTransItemId.text = transaction.id
        binding.textTransItemType.text = transaction.type
        binding.textTransItemDate.text = transaction.date
        binding.textTransItemSum.text = transaction.sum.toString()
        binding.textTransItemCurrency.text = transaction.currency
        binding.textTransItemCB.text = transaction.rateCentralBank.toString()
        binding.textTransItemSumRub.text = transaction.sumRub.toString()
    }
}