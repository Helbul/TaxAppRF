package com.taxapprf.taxapp.ui.activity

import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.data.error.DataErrorAuth
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.ActivityMainBinding
import com.taxapprf.taxapp.ui.MainDrawer
import com.taxapprf.taxapp.ui.MainToolbar
import com.taxapprf.taxapp.ui.getErrorDescription
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    val binding by viewBinding(ActivityMainBinding::bind)
    private val viewModel by viewModels<MainViewModel>()

    private val mAppBarConfiguration by lazy {
        AppBarConfiguration(topLevelDestinations, binding.drawerLayout)
    }

    val navController by lazy {
        findNavController(this, R.id.nav_host_fragment_content_main)
    }

    val drawer by lazy { MainDrawer(binding.navView) }
    val toolbar by lazy { MainToolbar(binding.appBarMain.toolbar) }

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
        //menuInflater.inflate(R.menu.reports_toolbar, menu)
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

    private val topLevelDestinations = setOf(
        R.id.sign,
        R.id.currency_rates_today,
        R.id.reports,
        R.id.currency_converter,
    )

    private val fabVisibleDestinations = setOf(
        R.id.reports,
        R.id.transactions,
    )

    private val notAuthDestinations = setOf(
        R.id.sign,
        R.id.sign_in,
        R.id.sign_up
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
            if (accounts.isNotEmpty()) {
                accountsAdapter.submitList(accounts.filter { !it.active })
                fabVisibilityManager()
            } else {
                onLoadingError(DataErrorAuth())
            }
        }
    }

    private fun fabVisibilityManager() {
        lifecycle.coroutineScope.launch {
            navController.currentBackStack.collectLatest { navBackStackEntries ->
                viewModel.account.value?.let {
                    if (fabVisibleDestinations.contains(navBackStackEntries.last().destination.id))
                        binding.appBarMain.fab.show()
                    else
                        binding.appBarMain.fab.hide()
                }
            }
        }
    }

    private fun navToAccountAdd() {
        navController.navigate(R.id.action_global_account_add)
    }
}