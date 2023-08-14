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
            callback.onClick(transaction)
        }
    }

    fun bind(transactionModel: TransactionModel) {
        transaction = transactionModel

        binding.textViewTransactionsAdapterItemName.text = transaction.name
        binding.textViewTransactionsAdapterItemType.text = transaction.type
        binding.textViewTransactionsAdapterItemDate.text = transaction.date
        binding.textViewTransactionsAdapterItemSum.text = transaction.sum.toString()
        binding.textViewTransactionsAdapterItemCurrency.text = transaction.currency
        binding.textViewTransactionsAdapterItemRateCbr.text = transaction.rateCBR.toString()
        binding.textViewTransactionsAdapterItemTax.text = transaction.tax.toString()
    }
}