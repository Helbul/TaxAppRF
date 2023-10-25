package com.taxapprf.taxapp.ui.currency.converter

import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.data.error.internal.currency.converter.DataErrorInternalCurrencyLoad
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentCurrencyConverterBinding
import com.taxapprf.taxapp.ui.BaseFragment
import com.taxapprf.taxapp.ui.hideKeyboard
import com.taxapprf.taxapp.ui.toEditorDouble
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CurrencyConverterFragment : BaseFragment(R.layout.fragment_currency_converter) {
    private val binding by viewBinding(FragmentCurrencyConverterBinding::bind)
    private val viewModel by viewModels<CurrencyConverterViewModel>()
    private lateinit var currenciesAdapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.attach()
        tryLoadCurrencyRates()

        toolbar.updateTitles()
        toolbar.updateMenu()

        setListeners()
    }

    private fun tryLoadCurrencyRates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateCurrencyRates.collect()
            }
        }
    }

    private fun setListeners() {
        binding.textInputEditCurrencyConverterSum.onFocusChangeListener =
            OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    binding.root.hideKeyboard()
                    var sum = binding.textInputEditCurrencyConverterSum.text.toString()
                    sum.ifEmpty {
                        sum = "0"
                        binding.textInputEditCurrencyConverterSum.setText(sum)
                    }
                    viewModel.recalculateSumRUB(sum)
                    updateSumRUB()
                }
            }

        binding.textInputEditCurrencyConverterSumRub.onFocusChangeListener =
            OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    binding.root.hideKeyboard()
                    var sumRUB = binding.textInputEditCurrencyConverterSumRub.text.toString()
                    sumRUB.ifEmpty {
                        sumRUB = "0"
                        binding.textInputEditCurrencyConverterSumRub.setText(sumRUB)
                    }
                    viewModel.recalculateSum(sumRUB)
                    updateSum()
                }
            }


        binding.autoCompleteTextViewCurrencyConverterCurrency.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.currencyOrdinal = position
                updateSumRUB()
            }
    }

    private fun prepCurrencies() {
        val currencies = viewModel.currencyRates.map { it.currency.name }
        currenciesAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, currencies)
        binding.autoCompleteTextViewCurrencyConverterCurrency.setAdapter(currenciesAdapter)
        binding.autoCompleteTextViewCurrencyConverterCurrency.setText(
            currencies[viewModel.currencyOrdinal],
            false
        )
    }

    private fun updateSum() {
        binding.textInputEditCurrencyConverterSum.setText(viewModel.sum.toEditorDouble())
    }

    private fun updateSumRUB() {
        binding.textInputEditCurrencyConverterSumRub.setText(viewModel.sumRUB.toEditorDouble())
    }

    override fun onLoading() {
        super.onLoading()
        binding.textInputLayoutCurrencyConverterCurrency.isEnabled = false
        binding.textInputLayoutCurrencyConverterSum.isEnabled = false
        binding.textInputLayoutCurrencyConverterSumRub.isEnabled = false
        binding.buttonCurrencyConverterConvert.isEnabled = false
    }

    override fun onLoadingRetry() {
        super.onLoadingRetry()
        lifecycleScope.launch {
            viewModel.updateCurrencyRates.collect()
        }
    }

    override fun onError(t: Throwable) {
        when (t) {
            is DataErrorInternalCurrencyLoad -> mainActivity.onLoadingErrorShowInUIWithRetry(t)
            else -> mainActivity.onLoadingErrorShowInSnackBar(t)
        }
    }

    override fun onSuccess() {
        super.onSuccess()
        prepCurrencies()
        updateSum()
        updateSumRUB()
        binding.textInputLayoutCurrencyConverterCurrency.isEnabled = true
        binding.textInputLayoutCurrencyConverterSum.isEnabled = true
        binding.textInputLayoutCurrencyConverterSumRub.isEnabled = true
        binding.buttonCurrencyConverterConvert.isEnabled = true
    }
}