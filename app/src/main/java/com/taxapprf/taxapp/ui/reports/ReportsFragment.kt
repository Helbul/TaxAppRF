package com.taxapprf.taxapp.ui.reports

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.taxapprf.domain.transactions.ReportModel
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentReportsBinding
import com.taxapprf.taxapp.ui.BaseActionModeCallback
import com.taxapprf.taxapp.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReportsFragment : BaseFragment(R.layout.fragment_reports) {
    private val binding by viewBinding(FragmentReportsBinding::bind)
    private val viewModel by viewModels<ReportsViewModel>()

    private val reportsAdapterCallback =
        object : ReportsAdapterCallback {
            override fun onItemClick(reportModel: ReportModel) {
                navToTransactions(reportModel)
            }

            override fun onMoreClick(reportModel: ReportModel) {
                showMoreClick(reportModel)
            }
        }

    private val adapter = ReportsAdapter(reportsAdapterCallback)

    private val reportsAdapterTouchHelperCallback =
        object : ReportsAdapterTouchHelperCallback {
            override fun onSwiped(reportModel: ReportModel) {
                showDeleteDialog(reportModel)
            }
        }

    private val reportTouchHelper = ReportAdapterTouchHelper(reportsAdapterTouchHelperCallback)
    private val itemTouchHelper = ItemTouchHelper(reportTouchHelper)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.attach()
        observeAccount()
        observeReports()

        prepToolbar()
        prepViews()
        setListeners()
    }

    private fun observeAccount() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.userWithAccounts.collectLatest { userWithAccountsModel ->
                    userWithAccountsModel?.let {
                        userWithAccountsModel.activeAccount?.let {
                            viewModel.updateReports(it.id)
                        }
                    }
                }
            }
        }
    }

    private fun observeReports() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reports.collectLatest { reports ->
                    adapter.submitList(reports)
                }
            }
        }
    }

    private fun prepToolbar() {
        toolbar.updateTitles(getString(R.string.menu_reports))
        toolbar.updateMenu(R.menu.toolbar_reports) {
            when (it.itemId) {
                R.id.toolbar_import_excel -> {
                    launchImportExcelIntent()
                    true
                }

                else -> false
            }
        }
    }

    private fun prepViews() {
        binding.recyclerReports.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.recyclerReports)
    }

    private fun setListeners() {
        fab.setOnClickListener { navToTransactionDetail() }
    }

    private fun showMoreClick(reportModel: ReportModel) {
        showActionMode {
            object : BaseActionModeCallback {
                override var menuInflater = requireActivity().menuInflater
                override var menuId = R.menu.action_menu_report

                override fun onActionItemClicked(
                    mode: ActionMode?,
                    item: MenuItem
                ) = when (item.itemId) {
                    R.id.action_menu_delete -> {
                        showDeleteDialog(reportModel)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun showDeleteDialog(reportModel: ReportModel) {
        actionMode?.finish()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_dialog_title)
            .setMessage(R.string.delete_dialog_message)
            .setPositiveButton(R.string.delete_dialog_ok) { _, _ ->
                viewModel.deleteReport(reportModel)
            }
            .setNegativeButton(R.string.delete_dialog_cancel) { _, _ ->
                reportTouchHelper.cancelSwipe()
                itemTouchHelper.attachToRecyclerView(null)
                itemTouchHelper.attachToRecyclerView(binding.recyclerReports)
            }
            .show()
    }

    private val importExcelRegisterForActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.importExcel(it.data)
        }

    private fun launchImportExcelIntent() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/vnd.ms-excel"
        importExcelRegisterForActivityResult.launch(intent)
    }

    private fun navToTransactions(reportModel: ReportModel) {
        findNavController().navigate(
            ReportsFragmentDirections.actionReportsToTransactions(reportModel.id)
        )
    }

    private fun navToTransactionDetail() {
        mainViewModel.accountId?.let { accountId ->
            val directions =
                ReportsFragmentDirections.actionReportsToTransactionDetail(accountId)
            findNavController().navigate(directions)
        }
    }
}