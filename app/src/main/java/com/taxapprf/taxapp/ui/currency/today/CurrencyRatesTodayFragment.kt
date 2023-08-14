package com.taxapprf.taxapp.ui.currency.today

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentRatesTodayBinding
import com.taxapprf.taxapp.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrencyRatesTodayFragment : BaseFragment(R.layout.fragment_rates_today) {
    private val binding by viewBinding(FragmentRatesTodayBinding::bind)
    private val viewModel by viewModels<CurrencyRatesTodayViewModel>()
    private val adapter = CurrencyRatesTodayAdapter()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.updateMenu()

        binding.recyclerviewCurrencies.adapter = adapter

        viewModel.attachToBaseFragment()
        viewModel.currencies.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            println(it)
        }
    }
}