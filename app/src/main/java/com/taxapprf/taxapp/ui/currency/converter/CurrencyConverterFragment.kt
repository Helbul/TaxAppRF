package com.taxapprf.taxapp.ui.currency.converter

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentCurrencyConverterBinding
import com.taxapprf.taxapp.ui.BaseFragment
import com.taxapprf.taxapp.ui.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CurrencyConverterFragment : BaseFragment(R.layout.fragment_currency_converter) {
    private val binding by viewBinding(FragmentCurrencyConverterBinding::bind)
    private val viewModel by viewModels<CurrencyConverterViewModel>()
    private lateinit var currenciesAdapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loading()

        toolbar.updateMenu()

        prepCurrencies()
        setListeners()
        viewModel.attach()
        viewModel.observeConverter()
    }

    private fun CurrencyConverterViewModel.observeConverter() {
        sum.observe(viewLifecycleOwner) {
            binding.editCurrencyConverterSum.setText(it.toString())
        }

        sumRub.observe(viewLifecycleOwner) {
            binding.editCurrencyConverterSumRub.setText(it.toString())
        }
    }

    private fun setListeners() {
        binding.editCurrencyConverterSum.onFocusChangeListener =
            OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    binding.root.hideKeyboard()
                    val newSum = binding.editCurrencyConverterSum.text.toString()
                    if (newSum != "") viewModel.setSum(newSum.toDouble())
                }
            }

        binding.editCurrencyConverterSumRub.onFocusChangeListener =
            OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    binding.root.hideKeyboard()
                    val newSum = binding.editCurrencyConverterSumRub.text.toString()
                    if (newSum != "") viewModel.setSumRub(newSum.toDouble())
                }
            }

//        binding.spinnerCurrencyConverterSum.onItemSelectedListener = object :
//            AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//
//                Log.d("OLGA", "onItemSelected currenciesAdapter.getItem(position): ${currenciesAdapter.getItem(position)}")
//                currenciesAdapter.getItem(position)?.let {
//                    viewModel.currency = it
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//        }

        binding.spinnerCurrencyConverterSum.onItemClickListener =
            AdapterView.OnItemClickListener {
                    adapterView, view, position, id ->

                Log.d("OLGA", "onItemSelected currenciesAdapter.getItem(position): ${adapterView.getItemAtPosition(position)}")
                adapterView.getItemAtPosition(position)?.let {
                    viewModel.currency = adapterView.getItemAtPosition(position).toString()
                }
            }
    }


    private fun prepCurrencies() {
        val currencies = resources.getStringArray(R.array.transaction_currencies)
        currenciesAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        //currenciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        currenciesAdapter = ArrayAdapter(requireContext(), R.layout.list_item, currencies)
        (binding.spinnerCurrencyConverterSum as? AutoCompleteTextView)?.setAdapter(currenciesAdapter)

        //binding.spinnerCurrencyConverterSum.adapter = currenciesAdapter

//        binding.spinnerCurrencyConverterSum.setSelection(
//            currencies.indexOf(resources.getString(R.string.transaction_currency_usd))
//        )
    }

    override fun onLoadingRetry() {
        super.onLoadingRetry()
        viewModel.loading()
    }
}