package com.taxapprf.taxapp.ui.reports

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.domain.report.ReportModel
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentReportsBinding
import com.taxapprf.taxapp.ui.BaseActionModeCallback
import com.taxapprf.taxapp.ui.BaseFragment
import com.taxapprf.taxapp.ui.checkStoragePermission
import com.taxapprf.taxapp.ui.dialogs.DeleteDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportsFragment : BaseFragment(R.layout.fragment_reports) {
    private val binding by viewBinding(FragmentReportsBinding::bind)
    private val viewModel by viewModels<ReportsViewModel>()
    private val adapter = ReportsAdapter { reportsAdapterCallback }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.attachWithAccount()
        currentStackSavedState.observeDelete()

        prepToolbar()

        fab.setOnClickListener { navToTransactionDetail() }
        binding.recyclerYearStatements.adapter = adapter

        viewModel.observerReports()
    }

    private fun ReportsViewModel.observerReports() {
        reports.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    override fun onAuthReady() {
        super.onAuthReady()
        viewModel.loadReports()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICK_FILE_RESULT_CODE -> {
                viewModel.saveReportsFromExcel(data)
            }
        }
    }

    private fun prepToolbar() {
        toolbar.updateToolbar(getString(R.string.taxes_name))
        toolbar.updateMenu(R.menu.reports_toolbar) {
            when (it.itemId) {
                R.id.toolbar_import_excel -> {
                    navToSystemStorage()
                    true
                }

                else -> false
            }
        }
    }

    private fun SavedStateHandle.observeDelete() {
        getLiveData<Boolean>(DeleteDialogFragment.DELETE_ACCEPTED).observe(viewLifecycleOwner) {
            if (it) viewModel.deleteReport()
            else viewModel.deleteReport = null
        }
    }

    private val reportsAdapterCallback =
        object : ReportsAdapterCallback {
            override fun onClick(reportModel: ReportModel) {
                navToTransactions(reportModel)
            }

            override fun onClickMore(reportModel: ReportModel) {
                showActionMode {
                    object : BaseActionModeCallback {
                        override var menuInflater = requireActivity().menuInflater
                        override var menuId = R.menu.report_action_menu

                        override fun onActionItemClicked(
                            mode: ActionMode?,
                            item: MenuItem
                        ) = when (item.itemId) {
                            R.id.action_menu_delete -> {
                                viewModel.deleteReport = reportModel
                                actionMode?.finish()
                                navToDeleteDialog()
                                true
                            }

                            else -> false
                        }
                    }
                }
            }
        }

    private fun navToDeleteDialog() {
        findNavController().navigate(R.id.action_reports_to_delete_dialog)
    }

    private fun navToSystemStorage() {
        if (requireActivity().checkStoragePermission()) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/vnd.ms-excel"
            startActivityForResult(intent, PICK_FILE_RESULT_CODE)
        }
    }

    private fun navToTransactions(reportModel: ReportModel) {
        mainViewModel.report = reportModel
        findNavController().navigate(R.id.action_reports_to_transactions)
    }

    private fun navToTransactionDetail() {
        findNavController().navigate(R.id.action_reports_to_transaction_detail)
    }

    companion object {
        private const val PICK_FILE_RESULT_CODE = 1001
    }
}