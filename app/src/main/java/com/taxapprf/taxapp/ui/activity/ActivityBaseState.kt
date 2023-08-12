package com.taxapprf.taxapp.ui.activity

sealed class ActivityBaseState
object Loading : ActivityBaseState()
data class Error(val t: Throwable) : ActivityBaseState()
object Success : ActivityBaseState()