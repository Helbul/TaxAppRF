package com.taxapprf.taxapp.ui

import androidx.appcompat.view.ActionMode
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.taxapprf.taxapp.ui.activity.MainActivity
import com.taxapprf.taxapp.ui.activity.MainViewModel

open class BottomSheetBaseFragment(layoutId: Int) : BottomSheetDialogFragment(layoutId),
    BaseFragmentInterface {
    override val mainActivity
        get() = requireActivity() as MainActivity
    override val mainViewModel by activityViewModels<MainViewModel>()
    override val fragment
        get() = this
    override lateinit var baseViewModel: BaseViewModel

    //override var actionMode: ActionMode? = null
}