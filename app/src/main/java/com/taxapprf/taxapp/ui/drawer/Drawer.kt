package com.taxapprf.taxapp.ui.drawer

import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Layer
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.taxapprf.data.MainRepositoryImpl.Companion.LOCAL_USER_EMAIL
import com.taxapprf.domain.main.account.AccountModel
import com.taxapprf.domain.main.user.UserModel
import com.taxapprf.domain.main.user.UserWithAccountsModel
import com.taxapprf.taxapp.R

class Drawer(
    private val drawerLayout: DrawerLayout,
    private val navigationView: NavigationView,
    private val navController: NavController,
    private val callback: DrawerCallback,
) {
    private val header = navigationView.getHeaderView(HEADER_POSITION)
    private val userAvatar = header.findViewById<ImageView>(R.id.image_drawer_header_user_avatar)
    private val userName = header.findViewById<TextView>(R.id.text_drawer_header_user_name)
    private val userEmail = header.findViewById<TextView>(R.id.text_drawer_header_user_email)
    private val expandAccounts
        get() = navigationView.findViewById<ImageView>(R.id.image_drawer_header_user_account_expand)
    private val accountsRecycler =
        header.findViewById<RecyclerView>(R.id.recycler_drawer_header_accounts)
    private val activeAccountLayer =
        header.findViewById<Layer>(R.id.layer_drawer_header_active_account)
    private val activeAccount: TextView =
        header.findViewById(R.id.text_drawer_header_user_active_account)

    private val accountsAdapterCallback =
        object : DrawerAccountsAdapterCallback {
            override fun switchAccount(accountModel: AccountModel) {
                closeDrawer()
                callback.switchAccount(accountModel)
            }

            override fun navToAddAccount() {
                closeDrawer()
                if (navController.currentDestination?.id != R.id.account_add)
                    navController.navigate(R.id.action_global_account_add)
            }
        }

    private val accountsAdapter = MainAccountsAdapter(accountsAdapterCallback)

    private val drawerListener =
        object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                if (accountsRecycler.isVisible) expandAccounts()
            }

            override fun onDrawerStateChanged(newState: Int) {}
        }

    init {
        accountsRecycler.adapter = accountsAdapter
        activeAccountLayer.setOnClickListener { expandAccounts() }
        navigationView.setNavigationItemSelectedListener {
            closeDrawer()
            it.onNavigationItemClick()
        }
        drawerLayout.addDrawerListener(drawerListener)
    }

    fun update(
        userWithAccountsModel: UserWithAccountsModel?,
        defaultUserName: String?
    ) {
        updateUser(
            user = userWithAccountsModel?.user,
            defaultUserName = defaultUserName
        )
        updateActiveAccount(
            activeAccount = userWithAccountsModel?.activeAccount
        )
        updateOtherAccounts(
            otherAccounts = userWithAccountsModel?.otherAccounts
        )
    }

    private fun updateUser(
        user: UserModel?,
        defaultUserName: String?,
    ) {
        if (user?.email != LOCAL_USER_EMAIL) {
            showWithAuth()
            userAvatar.setImageURI(user?.avatar)
            userName.text = user?.name
            userEmail.text = user?.email
        } else {
            hideWithoutAuth()
            userAvatar.setImageResource(R.mipmap.ic_launcher_round)
            userName.text = defaultUserName
            userEmail.text = null
        }
    }

    private fun updateActiveAccount(activeAccount: AccountModel?) {
        activeAccount?.let {
            this.activeAccount.text = it.name
        }
    }

    private fun updateOtherAccounts(otherAccounts: List<AccountModel>?) {
        otherAccounts?.let {
            accountsAdapter.submitList(it)
        }
    }

    private fun showWithAuth() {
        navigationView.menu.findItem(R.id.sign).isVisible = false
        navigationView.menu.findItem(R.id.sign_out).isVisible = true
    }

    private fun hideWithoutAuth() {
        navigationView.menu.findItem(R.id.sign).isVisible = true
        navigationView.menu.findItem(R.id.sign_out).isVisible = false
    }

    private fun expandAccounts() {
        accountsRecycler.apply {
            if (isVisible) {
                animate()
                    .alpha(0f)
                    .withEndAction {
                        expandAccounts.setImageResource(R.drawable.ic_baseline_expand_more_24)
                        isVisible = false
                    }
            } else {
                animate()
                    .alpha(1f)
                    .withStartAction {
                        expandAccounts.setImageResource(R.drawable.ic_baseline_expand_less_24)
                        alpha = 0f
                        isVisible = true
                    }
            }
        }
    }

    private fun MenuItem.onNavigationItemClick() =
        when (itemId) {
            R.id.reports -> {
                if (navController.currentDestination?.id != R.id.reports)
                    navController.navigate(R.id.action_global_reports)
                true
            }

            R.id.currency_rate -> {
                if (navController.currentDestination?.id != R.id.currency_rate)
                    navController.navigate(R.id.action_global_currency_rate)
                true
            }

            R.id.currency_converter -> {
                if (navController.currentDestination?.id != R.id.currency_converter)
                    navController.navigate(R.id.action_global_currency_converter)
                true
            }

            R.id.sign -> {
                if (navController.currentDestination?.id != R.id.sign)
                    navController.navigate(R.id.action_global_sign)
                true
            }

            R.id.sign_out -> {
                callback.signOut()
                true
            }

            else -> false
        }

    private fun closeDrawer() =
        drawerLayout.close()

    companion object {
        private const val HEADER_POSITION = 0
    }
}