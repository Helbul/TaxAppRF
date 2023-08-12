package com.taxapprf.taxapp.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.taxapprf.data.error.DataErrorAuth
import com.taxapprf.data.error.InputErrorEmailEmpty
import com.taxapprf.data.error.InputErrorEmailIncorrect
import com.taxapprf.data.error.InputErrorNameEmpty
import com.taxapprf.data.error.InputErrorPasswordLength
import com.taxapprf.data.error.InputErrorPhoneEmpty
import com.taxapprf.data.error.SignInErrorWrongPassword
import com.taxapprf.data.error.SignUpErrorEmailAlreadyUse
import com.taxapprf.data.error.AuthErrorSessionExpired
import com.taxapprf.taxapp.R
import com.taxapprf.taxapp.ui.activity.MainActivity
import com.taxapprf.taxapp.ui.activity.MainViewModel

open class BaseFragment(layoutId: Int) : Fragment(layoutId) {
    private val loading by lazy { requireActivity() as MainActivity }
    private lateinit var baseViewModel: BaseViewModel
    protected val activityViewModel by activityViewModels<MainViewModel>()
    protected val currentStackSavedState by lazy {
        findNavController().currentBackStackEntry!!.savedStateHandle
    }

    protected fun BaseViewModel.attachToBaseFragment() {
        baseViewModel = this
        baseViewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is BaseState.Finish -> popBackStack()
                is BaseState.Loading -> onLoading()
                is BaseState.Error -> prepOnLoadingError(it.t)
                is BaseState.SuccessWithEmpty -> onLoadingWithEmpty()
                is BaseState.SuccessEdit -> onSuccess()
                is BaseState.SuccessDelete -> onSuccess()
                is BaseState.Success -> onSuccess()
                else -> {}
            }
        }
    }

    override fun onPause() {
        super.onPause()
        loading.onLoadingStop()
    }

    private fun prepOnLoadingError(t: Throwable) {
        loading.onLoadingStop()
        onLoadingError(t)
    }

    protected open fun onLoading() {
        loading.onLoadingStart()
    }

    protected open fun onLoadingError(t: Throwable) {
        loading.onLoadingError(t)
    }

    protected open fun onSuccess() {
        loading.onLoadingSuccess()
    }

    protected open fun onLoadingWithEmpty() {
        loading.onLoadingSuccess()
        // TODO доделать ракцию на пустой ответ
    }

    protected fun popBackStack() {
        findNavController().popBackStack()
    }
}