package com.taxapprf.taxapp.ui.activity

import androidx.lifecycle.MutableLiveData
import com.taxapprf.taxapp.ui.activity.ActivityBaseState
import com.taxapprf.taxapp.ui.activity.Error
import com.taxapprf.taxapp.ui.activity.Loading
import com.taxapprf.taxapp.ui.activity.Success

class ActivityStateLiveData : MutableLiveData<ActivityBaseState>() {
    fun loading() {
        postValue(Loading)
    }

    fun error(t: Throwable) {
        postValue(Error(t))
    }

    fun success() {
        postValue(Success)
    }
}