package com.taxapprf.taxapp.ui.currency.converter

import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

        toolbar.updateMenu()

        prepCurrencies()
        setListeners()
        viewModel.attachToBaseFragment()
        viewModel.observeConverter()
    }

    private fun CurrencyConverterViewModel.observeConverter() {
        sum.observe(viewLifecycleOwner) {
            binding.editConverterUp.setText(it.toString())
        }

        sumRub.observe(viewLifecycleOwner) {
            binding.editConverterDown.setText(it.toString())
        }
    }

    private fun setListeners() {
        binding.editConverterUp.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.root.hideKeyboard()
                val newSum = binding.editConverterUp.text.toString()
                if (newSum != "") viewModel.setSum(newSum.toDouble())
            }
        }

        binding.editConverterDown.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.root.hideKeyboard()
                val newSum = binding.editConverterDown.text.toString()
                if (newSum != "") viewModel.setSumRub(newSum.toDouble())
            }
        }

        binding.spinnerConverter.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
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

    private fun prepCurrencies() {
        val currencies = resources.getStringArray(R.array.transaction_currencies)
        currenciesAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, currencies)
        currenciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerConverter.adapter = currenciesAdapter
        binding.spinnerConverter.setSelection(
            currencies.indexOf(resources.getString(R.string.transaction_currency_usd))
        )
    }
}