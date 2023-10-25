package com.taxapprf.taxapp.ui.transactions

import android.content.Intent
import android.net.Uri
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
import com.taxapprf.domain.transactions.TransactionModel
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentTransactionsBinding
import com.taxapprf.taxapp.ui.BaseActionModeCallback
import com.taxapprf.taxapp.ui.BaseFragment
import com.taxapprf.taxapp.ui.toAppDouble
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TransactionsFragment : BaseFragment(R.layout.fragment_transactions) {
    private val binding by viewBinding(FragmentTransactionsBinding::bind)
    private val viewModel by viewModels<TransactionsViewModel>()

    private val transactionAdapterCallback =
        object : TransactionsAdapterCallback {
            override fun onItemClick(transactionModel: TransactionModel) {
                navToTransactionDetail(transactionModel)
            }

            override fun onMoreClick(transactionModel: TransactionModel) {
                onShowMoreClick(transactionModel)
            }
        }

    private val adapter = TransactionsAdapter(transactionAdapterCallback)

    private val transactionAdapterTouchHelperCallback =
        object : TransactionsAdapterTouchHelperCallback {
            override fun onSwiped(transactionModel: TransactionModel) {
                showDeleteDialog(transactionModel)
            }
        }

    private val transactionTouchHelper =
        TransactionsAdapterTouchHelper(transactionAdapterTouchHelperCallback)
    private val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(transactionTouchHelper)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.attach()
        observeReport()
        observeTransactions()

        prepToolbar()
        prepView()
        prepListeners()
    }

    private fun prepToolbar() {
        toolbar.updateMenu(R.menu.toolbar_transactions) { menuItem ->
            when (menuItem.itemId) {
                R.id.toolbar_share_excel -> {
                    viewModel.shareReport()
                    true
                }

                R.id.toolbar_export_excel -> {
                    viewModel.exportReport()
                    true
                }

                else -> false
            }
        }
    }

    private fun prepView() {
        binding.recyclerTransactions.adapter = adapter
        adapter.localTransactionTypes = resources.getStringArray(R.array.transaction_types).toList()
        itemTouchHelper.attachToRecyclerView(binding.recyclerTransactions)
    }

    private fun prepListeners() {
        fab.setOnClickListener { navToTransactionDetail() }
    }

    private fun observeReport() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.observeReport()?.let { flow ->
                    flow.collectLatest { report ->
                        report?.updateToolbar()
                    }
                } ?: findNavController().popBackStack()
            }
        }
    }

    private fun observeTransactions() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.observeTransactions()?.let { flow ->
                    flow.collectLatest { transactions ->
                        transactions?.let {
                            if (transactions.isNotEmpty()) adapter.submitList(transactions)
                            else findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    override fun onSuccessImport(uri: Uri) {
        super.onSuccessImport(uri)
        launchImportExcelIntent(uri)
    }

    private fun ReportModel.updateToolbar() {
        val title = String.format(getString(R.string.transactions_title), name)
        val subtitle =
            String.format(getString(R.string.transactions_subtitle), taxRUB.toAppDouble())
        toolbar.updateTitles(title, subtitle)
    }

    private val importExcelIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }

    private fun launchImportExcelIntent(uri: Uri) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.action = Intent.ACTION_VIEW
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.type = "application/vnd.ms-excel"
        intent.setDataAndType(uri, "application/vnd.ms-excel")
        importExcelIntent.launch(intent)
    }

    private fun onShowMoreClick(transactionModel: TransactionModel) {
        showActionMode {
            object : BaseActionModeCallback {
                override var menuInflater = requireActivity().menuInflater
                override var menuId = R.menu.action_menu_transaction

                override fun onActionItemClicked(
                    mode: ActionMode?,
                    item: MenuItem
                ) = when (item.itemId) {
                    R.id.action_menu_delete -> {
                        showDeleteDialog(transactionModel)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun showDeleteDialog(transactionModel: TransactionModel) {
        actionMode?.finish()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_dialog_title)
            .setMessage(R.string.delete_dialog_message)
            .setPositiveButton(R.string.delete_dialog_ok) { _, _ ->
                viewModel.deleteTransaction(transactionModel)
            }
            .setNegativeButton(R.string.delete_dialog_cancel) { _, _ ->
                transactionTouchHelper.cancelSwipe()
                itemTouchHelper.attachToRecyclerView(null)
                itemTouchHelper.attachToRecyclerView(binding.recyclerTransactions)
            }
            .show()
    }

    private fun navToTransactionDetail(transactionModel: TransactionModel? = null) {
        val accountId = mainViewModel.accountId ?: return
        val reportId = viewModel.reportId ?: return

        findNavController().navigate(
            TransactionsFragmentDirections.actionTransactionsToTransactionDetail(
                accountId = accountId,
                reportId = reportId,
                transactionId = transactionModel?.id ?: 0
            )
        )
    }
}