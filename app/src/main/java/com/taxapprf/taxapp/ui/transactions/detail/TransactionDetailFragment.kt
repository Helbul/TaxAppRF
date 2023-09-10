package com.taxapprf.taxapp.ui.transactions.detail

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentTransactionDetailBinding
import com.taxapprf.taxapp.ui.BottomSheetBaseFragment
import com.taxapprf.taxapp.ui.getTransactionType
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class TransactionDetailFragment : BottomSheetBaseFragment(R.layout.fragment_transaction_detail) {
    private val binding by viewBinding(FragmentTransactionDetailBinding::bind)
    private val viewModel by viewModels<TransactionDetailViewModel>()

    private lateinit var currenciesAdapter: ArrayAdapter<String>
    private lateinit var typeAdapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.attachWithAccount()

        prepCurrencies()
        prepTypes()
        prepListeners()
    }

    override fun onAuthReady() {
        super.onAuthReady()
        viewModel.report = mainViewModel.report
        viewModel.transaction = mainViewModel.transaction
        viewModel.currency = resources.getString(R.string.transaction_currency_usd)

        mainViewModel.report = null
        mainViewModel.transaction = null

        updateUI()
    }

    private fun prepCurrencies() {
        val currencies = resources.getStringArray(R.array.transaction_currencies)
        currenciesAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        currenciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTransactionDetailCurrencies.adapter = currenciesAdapter
        binding.spinnerTransactionDetailCurrencies.setSelection(
            currencies.indexOf(resources.getString(R.string.transaction_currency_usd))
        )
    }

    private fun prepTypes() {
        val types = resources.getStringArray(R.array.transaction_types)
//        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        typeAdapter = ArrayAdapter(requireContext(), R.layout.list_item, types)
        (binding.spinnerTransactionDetailType as? AutoCompleteTextView)?.setAdapter(typeAdapter)


//        binding.spinnerTransactionDetailType.adapter = typeAdapter
//        binding.spinnerTransactionDetailType.setSelection(
//            types.indexOf(resources.getString(R.string.transaction_type_trade))
//        )
    }

    private fun prepListeners() {
        binding.buttonTransactionDetailDatePicker.setOnClickListener {
            showDatePicker {
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    viewModel.checkDate(year, month, dayOfMonth)
                    updateDateText()
                }
            }
        }

        binding.buttonTransactionDetailSave.setOnClickListener {
            with(binding) {
                val updateNameResult = viewModel
                    .checkName(editTextTransactionDetailName.text)
                    .updateEditError(editTextTransactionDetailName)
                val updateDateResult = viewModel
                    .checkDate(editTextTransactionDetailDate.text)
                    .updateEditError(editTextTransactionDetailDate)
                val updateSumResult = viewModel
                    .checkSum(editTextTransactionDetailSum.text)
                    .updateEditError(editTextTransactionDetailSum)

                if (updateNameResult && updateDateResult && updateSumResult) {
                    // TODO переместить во вьюмодел фрагмента? Привязка к жизненному циклу. Запускать глобал флоу?
                    mainViewModel.saveTransaction(viewModel.getSaveTransactionModel())
                    findNavController().popBackStack()
                }
            }
        }

        binding.spinnerTransactionDetailType.onItemSelectedListener =
            object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    typeAdapter.getItem(position)?.let {
                        viewModel.type = requireActivity().getTransactionType(it)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }


        binding.spinnerTransactionDetailCurrencies.onItemSelectedListener =
            object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    currenciesAdapter.getItem(position)?.let {
                        viewModel.currency = it
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    override fun onSuccess() {
        super.onSuccess()
        findNavController().popBackStack()
    }

    private fun updateUI() {
        with(viewModel) {
            binding.editTextTransactionDetailName.setText(name)
            updateDateText()
            updateSumText()
            updateCurrency(currency)
            updateType(type)
        }
    }

    private fun updateDateText() {
        binding.editTextTransactionDetailDate.setText(viewModel.date)
    }

    private fun updateSumText() {
        if (viewModel.sum > 0)
            binding.editTextTransactionDetailSum.setText(viewModel.sum.toString())
    }

    private fun updateCurrency(currency: String) {
        binding.spinnerTransactionDetailCurrencies.setSelection(
            currenciesAdapter.getPosition(currency)
        )
    }

    private fun updateType(type: String) {
//        binding.spinnerTransactionDetailType.setSelection(typeAdapter.getPosition(type))
        Log.d("OLGA", "type STRING: $type")
        Log.d("OLGA", "typeAdapter.getPosition(type) INT: ${typeAdapter.getPosition(type)}")
        binding.spinnerTransactionDetailType.setSelection(typeAdapter.getPosition(type))
    }

    private fun showDatePicker(listener: () -> DatePickerDialog.OnDateSetListener) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(), listener.invoke(),
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        ).show()
    }
}