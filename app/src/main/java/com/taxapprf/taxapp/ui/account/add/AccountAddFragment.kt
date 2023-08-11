package com.taxapprf.taxapp.ui.account.add

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentAccountAddBinding
import com.taxapprf.taxapp.ui.BaseFragment
import com.taxapprf.taxapp.ui.BaseState
import com.taxapprf.taxapp.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountAddFragment : BaseFragment(R.layout.fragment_account_add) {
    private val binding by viewBinding(FragmentAccountAddBinding::bind)
    private val viewModel by viewModels<AccountAddViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonNewAccountCreate.setOnClickListener { accountCreate() }
        binding.buttonNewAccountCancel.setOnClickListener { popBackStack() }

        viewModel.attachToBaseFragment()
        viewModel.observeState()
    }

    private fun AccountAddViewModel.observeState() =
        state.observe(viewLifecycleOwner) {
            when (it) {
                is BaseState.Success -> navToMainActivity()
                else -> {}
            }
        }

    private fun accountCreate() {
        val accountName = binding.editNewAccountName.text.toString()
        viewModel.saveAccount(accountName)
    }

    private fun navToMainActivity() {
        startActivity(Intent(activity, MainActivity::class.java))
        requireActivity().finish()
    }
}