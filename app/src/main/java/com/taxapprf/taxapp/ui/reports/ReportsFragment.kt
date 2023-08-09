package com.taxapprf.taxapp.ui.reports

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.domain.report.ReportModel
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentTaxesBinding
import com.taxapprf.taxapp.ui.BaseFragment
import com.taxapprf.taxapp.ui.checkStoragePermission
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportsFragment : BaseFragment(R.layout.fragment_taxes) {
    private val binding by viewBinding(FragmentTaxesBinding::bind)
    private val viewModel by viewModels<ReportsViewModel>()
    private val adapter = ReportsAdapter {
        object : ReportsAdapterCallback {
            override fun onClick(reportModel: ReportModel) {
                navToTransactions(reportModel)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonTaxesAddTrans.setOnClickListener { navToTransactionNew() }
        binding.buttonTaxesLoading.setOnClickListener { navToSystemStorage() }
        binding.recyclerYearStatements.adapter = adapter

        viewModel.attachToBaseFragment()
        viewModel.reports.observe(viewLifecycleOwner) { adapter.submitList(it) }
        activityViewModel.account.observe(viewLifecycleOwner) { account ->
            viewModel.loadReports(account.name)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICK_FILE_RESULT_CODE -> {
                viewModel.saveReportsFromExcel(data)
            }
        }
    }

    private fun navToSystemStorage() {
        if (requireActivity().checkStoragePermission()) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/vnd.ms-excel"
            startActivityForResult(intent, PICK_FILE_RESULT_CODE)
        }
    }

    private fun navToTransactions(reportModel: ReportModel) {
        activityViewModel.report = reportModel
        findNavController().navigate(R.id.action_taxesFragment_to_transactionsFragment)
    }

    private fun navToTransactionNew() {
        findNavController().navigate(R.id.action_taxesFragment_to_newTransactionFragment)
    }

    companion object {
        private const val PICK_FILE_RESULT_CODE = 1001
    }
}