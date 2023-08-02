package com.taxapprf.taxapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(R.layout.activity_login), Loading {
    val binding by viewBinding(ActivityLoginBinding::bind)
    val viewModel by viewModels<MainViewModel>()
    private val navController by lazy {
        Navigation.findNavController(this, R.id.nav_host_fragment_login)
    }

    // TODO при переходе из sign в accountNew не очищается бэкстэк

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.observeUser()
    }

    private fun MainViewModel.observeUser() {
        user.observe(this@LoginActivity) { userNullable ->
            println("userNull")
            userNullable?.let { user ->
                println("user")
                viewModel.name = user.name
                viewModel.email = user.email
                viewModel.phone = user.phone
                user.account?.let {
                    println("account")
                    navToMainActivity() }
                    ?: run {
                        println("accountNull")
                        navController.setGraph(R.navigation.login_navigation)
                        navToAccountFirst()
                    }
            } ?: run {
                println("userNull")
                navController.setGraph(R.navigation.login_navigation)
            }
        }
    }

    override fun onLoadingStart() {
        binding.loading.isVisible = true
    }

    override fun onLoadingStop() {
        binding.loading.isVisible = false
    }

    override fun onLoadingError(stringResId: Int) {
        onLoadingStop()
    }

    override fun onLoadingSuccess() {
        onLoadingStop()
    }

    private fun navToAccountFirst() {
        navController.navigate(R.id.action_sign_to_account_first)
    }

    private fun navToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}