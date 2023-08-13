package com.taxapprf.taxapp.ui.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.taxapprf.domain.report.ReportModel
import com.taxapprf.taxapp.databinding.FragmentReportsAdapterItemBinding

class ReportsAdapter(
    private val callback: () -> ReportsAdapterCallback,
) : ListAdapter<ReportModel, ReportsAdapterViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportsAdapterViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FragmentReportsAdapterItemBinding.inflate(inflater, parent, false)
        return ReportsAdapterViewHolder(binding, callback.invoke())
    }

    override fun onBindViewHolder(holder: ReportsAdapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<ReportModel>() {
        override fun areItemsTheSame(
            oldItem: ReportModel,
            newItem: ReportModel
        ) = oldItem.year == newItem.year

        override fun areContentsTheSame(
            oldItem: ReportModel,
            newItem: ReportModel
        ) = oldItem == newItem
    }
}