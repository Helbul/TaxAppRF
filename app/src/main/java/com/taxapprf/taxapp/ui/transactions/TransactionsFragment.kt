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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.domain.report.ReportModel
import com.taxapprf.domain.transaction.TransactionModel
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentTransactionsBinding
import com.taxapprf.taxapp.ui.BaseActionModeCallback
import com.taxapprf.taxapp.ui.BaseFragment
import com.taxapprf.taxapp.ui.checkStoragePermission
import com.taxapprf.taxapp.ui.dialogs.DeleteDialogFragment
import com.taxapprf.taxapp.ui.round
import com.taxapprf.taxapp.ui.share
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TransactionsFragment : BaseFragment(R.layout.fragment_transactions) {
    private val binding by viewBinding(FragmentTransactionsBinding::bind)
    private val viewModel by viewModels<TransactionsViewModel>()

    private val transactionAdapterCallback = object : TransactionsAdapterCallback {
        override fun onClick(transactionModel: TransactionModel) {
            navToTransactionDetail(transactionModel)
        }

        override fun onClickMore(transactionModel: TransactionModel) {
            showActionMode {
                object : BaseActionModeCallback {
                    override var menuInflater = requireActivity().menuInflater
                    override var menuId = R.menu.transaction_action_menu

                    override fun onActionItemClicked(
                        mode: ActionMode?,
                        item: MenuItem
                    ) = when (item.itemId) {
                        R.id.action_menu_delete -> {
                            navToTransactionDelete(transactionModel)
                            true
                        }

                        else -> false
                    }
                }
            }
        }

        override fun onSwiped(transactionModel: TransactionModel) {
            navToTransactionDelete(transactionModel)
        }
    }

    private val adapter = TransactionsAdapter(transactionAdapterCallback)
    private val transactionItemTouchHelper =
        TransactionsAdapterTouchHelperCallback(transactionAdapterCallback)
    private val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(transactionItemTouchHelper)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.attachWithAccount()
        currentStackSavedState.observeDelete()

        prepToolbar()
        prepView()
        prepListeners()
    }

    private fun prepToolbar() {
        toolbar.updateMenu(R.menu.transactions_toolbar) { menuItem ->
            when (menuItem.itemId) {
                R.id.toolbar_share_excel -> {
                    if (requireActivity().checkStoragePermission()) {
                        viewModel.getExcelToShare()
                    }

                    true
                }

                R.id.toolbar_export_excel -> {
                    if (requireActivity().checkStoragePermission()) {
                        viewModel.getExcelToStorage()
                    }

                    true
                }

                else -> false
            }
        }
    }

    private fun prepView() {
        binding.recyclerTransactions.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.recyclerTransactions)
    }

    private fun prepListeners() {
        fab.setOnClickListener {
            mainViewModel.report = viewModel.report
            findNavController().navigate(R.id.action_transactions_to_transaction_detail)
        }
    }

    private fun SavedStateHandle.observeDelete() {
        getLiveData<Int?>(DeleteDialogFragment.DELETE_TRANSACTION_ID).observe(viewLifecycleOwner) { result ->
            result?.let { viewModel.deleteTransaction(it) }
                ?: run {
                    transactionItemTouchHelper.cancelSwipe()
                    itemTouchHelper.attachToRecyclerView(null)
                    itemTouchHelper.attachToRecyclerView(binding.recyclerTransactions)
                }
        }
    }

    override fun onAuthReady() {
        super.onAuthReady()

        mainViewModel.report?.let { oldReportModel ->
            viewModel.report = oldReportModel
            mainViewModel.report = null

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.observeReport(oldReportModel.id).collectLatest { report ->
                        report?.let {
                            viewModel.report = it
                            it.updateToolbar()
                        }
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.observeTransactions().collectLatest { transactions ->
                        adapter.submitList(transactions)
                    }
                }
            }
        }
    }

    override fun onSuccessShare() {
        super.onSuccessShare()
        startIntentSendEmail()
    }

    override fun onSuccessImport() {
        super.onSuccessImport()
        launchSaveExcelToStorageIntent(viewModel.excelUri)
    }

    override fun onSuccessDelete() {
        super.onSuccessDelete()
        findNavController().popBackStack()
    }

    private fun ReportModel.updateToolbar() {
        val title = String.format(getString(R.string.transactions_title), name)
        val subtitle = String.format(getString(R.string.transactions_subtitle), tax.round())
        toolbar.updateToolbar(title, subtitle)
    }

    private fun startIntentSendEmail() {
        requireActivity().share(viewModel.excelUri)
    }

    private val importExcelToStorageIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }

    private fun launchSaveExcelToStorageIntent(uri: Uri) {
        with(requireActivity()) {
            if (checkStoragePermission()) {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                importExcelToStorageIntent.launch(intent)
            }
        }
    }

    private fun navToTransactionDelete(transactionModel: TransactionModel) {
        actionMode?.finish()
        findNavController().navigate(
            TransactionsFragmentDirections.actionTransactionsToDeleteDialog(
                transactionModel.id
            )
        )
    }

    private fun navToTransactionDetail(transactionModel: TransactionModel) {
        mainViewModel.report = viewModel.report
        mainViewModel.transaction = transactionModel
        findNavController().navigate(R.id.action_transactions_to_transaction_detail)
    }
}