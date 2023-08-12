package com.taxapprf.taxapp.ui.activity

import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.data.error.DataErrorAuth
import com.taxapprf.domain.account.AccountModel
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.ActivityMainBinding
import com.taxapprf.taxapp.ui.getErrorDescription
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val binding by viewBinding(ActivityMainBinding::bind)
    private val viewModel by viewModels<MainViewModel>()

    private val mAppBarConfiguration by lazy {
        AppBarConfiguration(topLevelDestination, binding.drawerLayout)
    }

    val navController by lazy {
        findNavController(this, R.id.nav_host_fragment_content_main)
    }

    private val drawer by lazy { MainActivityDrawer(binding.navView) }

    private val accountsAdapter = MainAccountsAdapter {
        object : MainAccountsAdapterCallback {
            override fun onClick(accountName: String) {
                binding.drawerLayout.close()
                viewModel.switchAccount(accountName)
            }

            override fun onClickAdd() {
                if (navController.currentDestination?.id != R.id.account_add) {
                    binding.drawerLayout.close()
                    navToAccountAdd()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navIsUserNotSignIn()

        navController.setGraph(R.navigation.mobile_navigation)

        setSupportActionBar(binding.appBarMain.toolbar)
        setupActionBarWithNavController(
            this@MainActivity, navController, mAppBarConfiguration
        )
        setupWithNavController(binding.navView, navController)

        drawer.recycler.adapter = accountsAdapter

        binding.appBarMain.content.loadingRetry.setOnClickListener { viewModel.loading() }

        viewModel.observeState()
        viewModel.observeAccounts()
        viewModel.observeAccount()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment_content_main)
        return (navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp())
    }

    fun onLoadingStart() {
        with(binding.appBarMain.content) {
            loadingErrorGroup.isVisible = false
            loading.isVisible = true
        }
    }

    fun onLoadingStop() {
        binding.appBarMain.content.loading.isVisible = false
    }

    fun onLoadingError(t: Throwable) {
        onLoadingStop()
        with(binding.appBarMain.content) {
            loadingErrorGroup.isVisible = true
            loadingErrorMessage.setText(t.getErrorDescription())
        }
    }

    fun onLoadingSuccess() {
        onLoadingStop()
    }

    private val topLevelDestination = setOf(
        R.id.sign,
        R.id.currency_rates_today,
        R.id.reports,
        R.id.currency_converter,
    )

    private fun MainViewModel.observeState() {
        state.observe(this@MainActivity) {
            when (it) {
                is Loading -> onLoadingStart()
                is Error -> onLoadingError(it.t)
                is Success -> onLoadingSuccess()
            }
        }
    }

    private fun MainViewModel.observeAccount() {
        account.observe(this@MainActivity) {
            drawer.account.text = it.name
        }
    }

    private fun MainViewModel.observeAccounts() {
        accounts.observe(this@MainActivity) { accounts ->
            if (accounts.isNotEmpty()) accountsAdapter.submitList(accounts.filter { !it.active })
            else onLoadingError(DataErrorAuth())
        }
    }

    private fun navIsUserNotSignIn() {
        if (!viewModel.isSignIn) navToSign()
    }

    private fun navToSign() {
        navController.navigate(R.id.action_global_sign)
    }

    private fun navToAccountAdd() {
        navController.navigate(R.id.action_global_account_add)
    }
}