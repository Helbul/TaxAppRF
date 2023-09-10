package com.taxapprf.taxapp.ui.reports

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
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
    lateinit var itemTouchHelper: ItemTouchHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.attachWithAccount()
        currentStackSavedState.observeDelete()

        prepToolbar()

        fab.setOnClickListener { navToTransactionDetail() }
        binding.recyclerYearStatements.adapter = adapter

        itemTouchHelper = ItemTouchHelper(ReportTouchHelperCallback(reportsAdapterCallback))
        itemTouchHelper.attachToRecyclerView(binding.recyclerYearStatements)

        viewModel.observerReports()
    }

    private fun ReportsViewModel.observerReports() {
        reports.observe(viewLifecycleOwner) { adapter.submitList(it) }
    }

    override fun onAuthReady() {
        super.onAuthReady()
        viewModel.loadReports()
    }

    override fun onLoadingRetry() {
        super.onLoadingRetry()
        viewModel.loadReports()
    }

    private fun prepToolbar() {
        toolbar.updateToolbar(getString(R.string.taxes_name))
        toolbar.updateMenu(R.menu.reports_toolbar) {
            when (it.itemId) {
                R.id.toolbar_import_excel -> {
                    launchExportExcelToFirebaseIntent()
                    true
                }

                else -> false
            }
        }
    }

    private fun SavedStateHandle.observeDelete() {
        getLiveData<Boolean>(DeleteDialogFragment.DELETE_ACCEPTED).observe(viewLifecycleOwner) {
            if (it) viewModel.deleteReport()
            else {
                viewModel.deleteReport = null
            }
        }
    }

    private val reportsAdapterCallback =
        object : ReportsAdapterCallback {
            override fun onClick(reportModel: ReportModel) {
                navToTransactions(reportModel)
            }

            override fun onClickDelete(reportModel: ReportModel) {
                viewModel.deleteReport = reportModel
                navToDeleteDialog()
//                showActionMode {
//                    object : BaseActionModeCallback {
//                        override var menuInflater = requireActivity().menuInflater
//                        override var menuId = R.menu.report_action_menu
//
//                        override fun onActionItemClicked(
//                            mode: ActionMode?,
//                            item: MenuItem
//                        ) = when (item.itemId) {
//                            R.id.action_menu_delete -> {
//                                viewModel.deleteReport = reportModel
//                                actionMode?.finish()
//                                navToDeleteDialog()
//                                true
//                            }
//
//                            else -> false
//                        }
//                    }
//                }
            }

            override fun onSwiped(position: Int) {
                viewModel.onSwipedReport(position)
                viewModel.deleteReport()
            }
        }

    private fun navToDeleteDialog() {
        findNavController().navigate(R.id.action_reports_to_delete_dialog)
    }

    private val exportExcelToFirebaseIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.saveReportsFromExcel(it.data)
        }

    private fun launchExportExcelToFirebaseIntent() {
        with(requireActivity()) {
            if (checkStoragePermission()) {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/vnd.ms-excel"
                exportExcelToFirebaseIntent.launch(intent)
            }
        }
    }

    private fun navToTransactions(reportModel: ReportModel) {
        mainViewModel.report = reportModel
        findNavController().navigate(R.id.action_reports_to_transactions)
    }

    private fun navToTransactionDetail() {
        findNavController().navigate(R.id.action_reports_to_transaction_detail)
    }
}