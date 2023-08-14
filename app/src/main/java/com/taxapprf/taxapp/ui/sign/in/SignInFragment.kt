package com.taxapprf.taxapp.ui.sign.`in`

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.FragmentSignInBinding
import com.taxapprf.taxapp.ui.BaseFragment
import com.taxapprf.taxapp.ui.BaseState
import com.taxapprf.taxapp.ui.getErrorDescription
import com.taxapprf.taxapp.ui.showSnackBar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInFragment : BaseFragment(R.layout.fragment_sign_in) {
    private val binding by viewBinding(FragmentSignInBinding::bind)
    private val viewModel by viewModels<SignInViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSignInCancel.setOnClickListener { popBackStack() }
        binding.buttonSignInOk.setOnClickListener { signIn() }

        viewModel.attachToBaseFragment()
        viewModel.observeState()
    }

    private fun SignInViewModel.observeState() =
        state.observe(viewLifecycleOwner) {
            when (it) {
                is BaseState.Success -> {
                    activityViewModel.loading()
                    drawer.showAuth()
                    navToReports()
                }

                else -> {}
            }
        }

    override fun onLoadingError(t: Throwable) {
        binding.root.showSnackBar(t.getErrorDescription())
    }

    private fun signIn() {
        val inputEmail = binding.editSignInEmail.text.toString()
        val inputPassword = binding.editSignInPassword.text.toString()
        viewModel.signIn(inputEmail, inputPassword)
    }

    private fun navToReports() {
        findNavController().navigate(R.id.action_global_reports)
    }
}