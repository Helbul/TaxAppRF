package com.taxapprf.taxapp.ui

interface LoadingFragment {
    fun onLoadingStart()

    fun onLoadingStop()

    fun onLoadingError(stringResId: Int)

    fun onLoadingSuccess()
}