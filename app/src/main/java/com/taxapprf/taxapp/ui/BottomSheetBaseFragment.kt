package com.taxapprf.taxapp.ui

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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
import com.taxapprf.taxapp.ui.activity.MainViewModel

open class BottomSheetBaseFragment(layoutId: Int) : BottomSheetDialogFragment(layoutId) {
    private val loading by lazy { requireActivity() as LoadingFragment }
    private lateinit var baseViewModel: BaseViewModel
    protected val activityViewModel by activityViewModels<MainViewModel>()

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

        val stringResId = when (t) {
            is DataErrorAuth -> R.string.auth_error
            is AuthErrorSessionExpired -> R.string.auth_error_session_expire
            is InputErrorNameEmpty -> R.string.error_name_empty
            is InputErrorPhoneEmpty -> R.string.error_phone_empty
            is InputErrorEmailEmpty -> R.string.error_email_empty
            is InputErrorEmailIncorrect -> R.string.error_email_incorrect
            is InputErrorPasswordLength -> R.string.error_password_length
            is SignInErrorWrongPassword -> R.string.error_sign_in
            is SignUpErrorEmailAlreadyUse -> R.string.sign_up_error_email_already_use
            else -> throw t
        }

        onLoadingError(stringResId)
    }

    protected open fun onLoading() {
        loading.onLoadingStart()
    }

    protected open fun onLoadingError(stringResId: Int) {
        loading.onLoadingError(stringResId)
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