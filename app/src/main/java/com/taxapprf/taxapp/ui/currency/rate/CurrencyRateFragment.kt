package com.taxapprf.taxapp.ui.currency.rate

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.domain.toAppDate
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentCurrencyRateBinding
import com.taxapprf.taxapp.ui.BaseFragment
import com.taxapprf.taxapp.ui.getEpochDay
import com.taxapprf.taxapp.ui.showDatePickerDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CurrencyRateFragment : BaseFragment(R.layout.fragment_currency_rate) {
    private val binding by viewBinding(FragmentCurrencyRateBinding::bind)
    private val viewModel by viewModels<CurrencyRateViewModel>()
    private val adapter = CurrencyRateAdapter()

    private val datePickerListener =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            viewModel.date = getEpochDay(year, month, dayOfMonth)

            toolbar.updateToolbar(
                title = String.format(
                    getString(R.string.currency_rate_toolbar),
                    viewModel.date.toAppDate()
                )
            )

//            observeRateWithCurrencies()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.attach()

        prepToolbar()
        prepView()

        observeRateWithCurrencies()
    }

    private fun prepToolbar() {
        toolbar.updateToolbar(
            title = String.format(
                getString(R.string.currency_rate_toolbar),
                viewModel.date.toAppDate()
            )
        )

        toolbar.updateMenu(R.menu.toolbar_currency_rate) {
            when (it.itemId) {
                R.id.toolbar_calendar -> {
                    showDatePickerDialog(requireContext(), datePickerListener)
                    true
                }

                else -> false
            }
        }
    }

    private fun prepView() {
        binding.recyclerCurrencyRate.adapter = adapter
    }

    private fun observeRateWithCurrencies() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.ratesWithCurrency().collectLatest { rateWithCurrencies ->
                    rateWithCurrencies?.let {
                        adapter.submitList(it)
                    }
                }
            }
        }
    }
}