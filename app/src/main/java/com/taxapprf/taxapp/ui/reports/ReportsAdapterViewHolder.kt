package com.taxapprf.taxapp.ui.reports

import androidx.recyclerview.widget.RecyclerView
import com.taxapprf.domain.report.ReportModel
import com.taxapprf.taxapp.databinding.FragmentReportsAdapterItemBinding

class ReportsAdapterViewHolder(
    private val binding: FragmentReportsAdapterItemBinding,
    private val callback: ReportsAdapterCallback,
) : RecyclerView.ViewHolder(binding.root) {
    private lateinit var report: ReportModel

    init {
        binding.root.setOnClickListener {
            callback.onClick(report)
        }

        binding.buttonReportsAdapterItemMore.setOnClickListener {
            callback.onClickMore(report)
        }
    }

    fun bind(reportAdapterModel: ReportModel) {
        report = reportAdapterModel

        binding.textReportsAdapterItemYear.text = report.key
        binding.textReportsAdapterItemTax.text = report.tax.toString()
    }
}