package com.taxapprf.taxapp.ui.transactions

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.domain.transaction.TransactionModel
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentTransactionsBinding
import com.taxapprf.taxapp.ui.BaseFragment
import com.taxapprf.taxapp.ui.BaseState
import com.taxapprf.taxapp.ui.activity.MainViewModel
import com.taxapprf.taxapp.ui.checkStoragePermission
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TransactionsFragment : BaseFragment(R.layout.fragment_transactions) {
    private val binding by viewBinding(FragmentTransactionsBinding::bind)
    private val viewModel by viewModels<TransactionsViewModel>()
    private val adapter = TransactionsAdapter {
        object : TransactionsAdapterCallback {
            override fun onClick(transactionModel: TransactionModel) {
                navToTransactionDetail(transactionModel)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerTransactions.adapter = adapter

        fab.setOnClickListener {
            activityViewModel.report = viewModel.report
            findNavController().navigate(R.id.action_transactions_to_transaction_detail)
        }

        binding.buttonTransSendEmail.setOnClickListener {
            if (requireActivity().checkStoragePermission()) {
                viewModel.sendExcelReport()
            }
        }

        binding.buttonTransDeleteYear.setOnClickListener { navToTransactionDelete() }

        viewModel.attachToBaseFragment()
        currentStackSavedState.observeDelete()
        activityViewModel.observeAccount()
        viewModel.observeTransactions()
        viewModel.observeState()
    }

    private fun SavedStateHandle.observeDelete() {
        getLiveData<Boolean>(TRANSACTIONS_DELETE_DIALOG_RESULT).observe(viewLifecycleOwner) {
            if (it) {
                viewModel.deleteTax()
            }
        }
    }

    private fun MainViewModel.observeAccount() =
        account.observe(viewLifecycleOwner) { account ->
            viewModel.account = account
            activityViewModel.report?.let {
                viewModel.report = it
                activityViewModel.report = null
                viewModel.loadTransactions()
                updateUI()
            }
        }

    private fun TransactionsViewModel.observeTransactions() =
        transactions.observe(viewLifecycleOwner) { transaction ->
            transaction?.let {
                adapter.submitList(it)
                updateUI()
            }
        }

    private fun TransactionsViewModel.observeState() =
        state.observe(viewLifecycleOwner) {
            when (it) {
                is BaseState.SuccessSendEmail -> startIntentSendEmail()
                is BaseState.SuccessDelete -> popBackStack()
                else -> {}
            }
        }

    private fun updateUI() {
        binding.textTransYearSum.text = String.format(
            getString(R.string.transactions_tax_sum),
            viewModel.report.year,
            viewModel.report.tax
        )
    }

    /*    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            val textYearTax = binding!!.textTransYearSum
            viewModel.getSumTaxes().observe(viewLifecycleOwner, object : Observer<Double?> {
                override fun onChanged(yearTax: Double) {
                }
            })
            val buttonDownload: ImageButton = binding!!.buttonTransDownload
            buttonDownload.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if (isStoragePermissionGranted) {
                        fileName = viewModel.downloadStatement()
                        if (fileName!!.exists()) {
                            Snackbar.make(v, "Отчет скачан.", Snackbar.LENGTH_SHORT).show()
                            val uri = FileProvider.getUriForFile(
                                context!!, context!!.applicationContext.packageName + ".provider",
                                fileName!!
                            )
                            Log.d("OLGA", "onClick download uri: " + uri.path)
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startActivity(intent)
                        } else {
                            Snackbar.make(v, "Не удалось скачать отчет.", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            })
*/

    private fun startIntentSendEmail() {
        viewModel.emailUri?.let {
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "vnd.android.cursor.dir/email"
            emailIntent.putExtra(Intent.EXTRA_STREAM, it)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Расчёт налога от TaxApp")
            requireActivity().startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }
    }

    private fun navToTransactionDelete() {
        findNavController().navigate(R.id.action_transactions_to_transactions_delete_dialog)
    }

    private fun navToTransactionDetail(transactionModel: TransactionModel) {
        activityViewModel.transaction = transactionModel
        findNavController().navigate(R.id.action_transactions_to_transaction_detail)
    }

    companion object {
        const val TRANSACTIONS_DELETE_DIALOG_RESULT = "transactions_delete_dialog_result"
    }
}