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

        binding.buttonSignInCreate.setOnClickListener { checkSignIn() }
        viewModel.attach()
    }

    override fun onSuccess() {
        super.onSuccess()

        mainViewModel.loading()
        drawer.showWithAuth()
        navToReports()
    }

    private fun checkSignIn() {
        val updateEmailResult = viewModel
            .checkEmail(binding.editSignInEmail.text)
            .updateEditError(binding.editSignInEmail)
        val updatePasswordResult = viewModel
            .checkPassword(binding.editSignInPassword.text)
            .updateEditError(binding.editSignInPassword)

        if (updateEmailResult && updatePasswordResult) viewModel.signIn()
    }

    private fun navToReports() {
        findNavController().navigate(R.id.action_sign_in_to_reports)
    }
}