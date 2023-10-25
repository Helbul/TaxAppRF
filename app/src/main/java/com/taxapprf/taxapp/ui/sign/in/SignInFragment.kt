package com.taxapprf.taxapp.ui.sign.`in`

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentSignInBinding
import com.taxapprf.taxapp.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : BaseFragment(R.layout.fragment_sign_in) {
    private val binding by viewBinding(FragmentSignInBinding::bind)
    private val viewModel by viewModels<SignInViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.attach()

        toolbar.updateTitles()
        toolbar.updateMenu()
        updateUI()

        binding.buttonSignIn.setOnClickListener { checkSignIn() }
    }

    override fun onSuccess() {
        super.onSuccess()
        mainViewModel.updateUserWithAccounts()
        navToReports()
    }

    private fun updateUI() {
        binding.editSignInEmail.setText(viewModel.email)
        binding.editSignInPassword.setText(viewModel.password)
    }

    private fun checkSignIn() {
        viewModel.email = binding.editSignInEmail.text.toString()
        viewModel.password = binding.editSignInPassword.text.toString()

        val updateEmailResult = viewModel.checkEmail()
            .updateEditError(binding.editSignInEmail)
        val updatePasswordResult = viewModel.checkPassword()
            .updateEditError(binding.editSignInPassword)

        if (updateEmailResult && updatePasswordResult) viewModel.signIn()
    }

    private fun navToReports() {
        findNavController().navigate(R.id.action_global_reports)
    }
}