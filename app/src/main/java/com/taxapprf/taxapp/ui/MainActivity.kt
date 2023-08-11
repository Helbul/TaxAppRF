package com.taxapprf.taxapp.ui

import android.os.Bundle
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.helper.widget.Layer
import androidx.core.view.isVisible
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.navigateUp
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.taxapprf.domain.account.AccountModel
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.databinding.ActivityMainBinding
import com.taxapprf.taxapp.ui.activity.MainAccountsAdapter
import com.taxapprf.taxapp.ui.activity.MainAccountsAdapterCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main), Loading {
    private val binding by viewBinding(ActivityMainBinding::bind)
    private val viewModel by viewModels<MainViewModel>()
    private val drawer by lazy { binding.drawerLayout }
    private val mAppBarConfiguration by lazy {
        AppBarConfiguration(
            setOf(
                R.id.currency_rates_today,
                R.id.reports,
                R.id.account_change,
                R.id.currency_converter,
            ),
            drawer
        )
    }
    private val navController by lazy {
        findNavController(this, R.id.nav_host_fragment_content_main)
    }

    private val accountsAdapter = MainAccountsAdapter {
        object : MainAccountsAdapterCallback {
            override fun onClick(accountModel: AccountModel) {
                viewModel.changeAccount(accountModel)
            }

            override fun onClickAdd() {
                if (navController.currentDestination?.id != R.id.account_add) {
                    binding.drawerLayout.close()
                    navToAccountAdd()
                }
            }
        }
    }

    private val header by lazy { binding.navView.getHeaderView(HEADER_POSITION) }
    private val recycler by lazy { header.findViewById<RecyclerView>(R.id.recyclerNavHeaderAccounts) }
    private var isAccountsExpand = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController.setGraph(R.navigation.mobile_navigation)
        setSupportActionBar(binding.appBarMain.toolbar)
        setupActionBarWithNavController(
            this@MainActivity,
            navController,
            mAppBarConfiguration
        )
        setupWithNavController(binding.navView, navController)
        prepDrawer()

        recycler.adapter = accountsAdapter
        header.findViewById<Layer>(R.id.layerNavHeaderAccounts).setOnClickListener {
            if (isAccountsExpand) expandLessAccounts()
            else expandMoreAccounts()
            isAccountsExpand = !isAccountsExpand
        }

        viewModel.state.observe(this@MainActivity) {
            when (it) {
                is ActivityBaseState.Loading -> loading()
                is ActivityBaseState.Error -> {}
                is ActivityBaseState.Success -> {}
                is ActivityBaseState.AccountEmpty -> {}
                else -> {}
            }
        }

        viewModel.account.observe(this@MainActivity) {
            val account = header.findViewById<TextView>(R.id.textNavHeaderUserAccount)
            account.text = it.name
        }

        viewModel.accounts.observe(this@MainActivity) {
            accountsAdapter.submitList(it)
        }
    }

    private fun expandMoreAccounts() {
        header.findViewById<ImageView>(R.id.imageNavHeaderUserAccountExpand)
            .setImageResource(R.drawable.ic_baseline_expand_less_24)
        recycler.isVisible = true
    }

    private fun expandLessAccounts() {
        header.findViewById<ImageView>(R.id.imageNavHeaderUserAccountExpand)
            .setImageResource(R.drawable.ic_baseline_expand_more_24)
        recycler.isVisible = false
    }

    private fun loading() {
    }

    private fun prepDrawer() {
        /*        val header = binding.navView.getHeaderView(0)
                header.setOnClickListener {
                    navController.navigate(R.id.account_change)
                    binding.navView.visibility = View.GONE
                }*/

        /*        val userAccount = header.findViewById<TextView>(R.id.textNavHeaderUserAccount)
                val settings: SharedPreferences =
                    this.getSharedPreferences(Settings.SETTINGSFILE.name, Context.MODE_PRIVATE)
                userAccount.text = settings.getString(Settings.ACCOUNT.name, "")*/
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment_content_main)
        return (navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp())
    }

    override fun onLoadingStart() {
        binding.appBarMain.content.loading.isVisible = true
    }

    override fun onLoadingStop() {
        binding.appBarMain.content.loading.isVisible = false
    }

    override fun onLoadingError(stringResId: Int) {
        onLoadingStop()
    }

    override fun onLoadingSuccess() {
        onLoadingStop()
    }

    private fun navToAccountAdd() {
        navController.navigate(R.id.action_global_accountAddFragment)
    }

    companion object {
        private const val HEADER_POSITION = 0
    }
}