package com.taxapprf.taxapp.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.taxapprf.taxapp.ui.activity.MainActivity
import com.taxapprf.taxapp.ui.activity.MainViewModel

open class BaseFragment(layoutId: Int) : Fragment(layoutId) {
    private val mainActivity by lazy { requireActivity() as MainActivity }
    private lateinit var baseViewModel: BaseViewModel
    protected val activityViewModel by activityViewModels<MainViewModel>()
    protected val currentStackSavedState by lazy {
        findNavController().currentBackStackEntry!!.savedStateHandle
    }
    protected val fab by lazy { mainActivity.binding.appBarMain.fab }
    protected val toolbar by lazy { mainActivity.toolbar }

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
        mainActivity.onLoadingStop()
    }

    private fun prepOnLoadingError(t: Throwable) {
        mainActivity.onLoadingStop()
        onLoadingError(t)
    }

    protected open fun onLoading() {
        mainActivity.onLoadingStart()
    }

    protected open fun onLoadingError(t: Throwable) {
        mainActivity.onLoadingError(t)
    }

    protected open fun onSuccess() {
        mainActivity.onLoadingSuccess()
    }

    protected open fun onLoadingWithEmpty() {
        mainActivity.onLoadingSuccess()
        // TODO доделать ракцию на пустой ответ
    }

    protected fun popBackStack() {
        findNavController().popBackStack()
    }
}