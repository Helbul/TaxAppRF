package com.taxapprf.taxapp.ui

import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import com.taxapprf.data.toLocalDate
import com.taxapprf.taxapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.text.DecimalFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.regex.Pattern

val state: MutableSharedFlow<BaseState> = MutableSharedFlow()

fun <T> Flow<T>.showLoading() =
    this
        .onStart { state.emit(Loading) }
        .catch { state.emit(Error(it)) }
        .onEach { state.emit(Success) }

fun <T> Flow<T>.makeHot(coroutineScope: CoroutineScope, initialValue: T? = null) =
    this
        .flowOn(Dispatchers.IO)
        .stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5000L),
            initialValue
        )

fun CoordinatorLayout.showSnackBar(msg: Int) {
    val snack = Snackbar.make(this, msg, Snackbar.LENGTH_SHORT)
    snack.anchorView = this.findViewById(R.id.fab)
    snack.show()
}

fun String.isEmailIncorrect(): Boolean {
    val p = Pattern.compile("\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*\\.\\w{2,4}")
    return !p.matcher(this).matches()
}

fun String.isErrorPasswordRange() = length < 8 || length > 16

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun String.showDatePickerDialog(
    context: Context,
    listener: DatePickerDialog.OnDateSetListener
) {
    toLocalDate()
        ?.let {
            DatePickerDialog(
                context, listener,
                it.year,
                it.monthValue - 1,
                it.dayOfMonth
            ).show()
        }
        ?: run {
            com.taxapprf.taxapp.ui.showDatePickerDialog(context, listener)
        }
}

fun showDatePickerDialog(
    context: Context,
    listener: DatePickerDialog.OnDateSetListener
) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context, listener,
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH] - 1,
        calendar[Calendar.DAY_OF_MONTH]
    ).show()
}

fun getEpochDay(year: Int, month: Int, dayOfMonth: Int) =
    LocalDate.of(year, month + 1, dayOfMonth).toEpochDay()

val appDoubleFormatter = DecimalFormat("#,##0.00")
fun Double.toAppDouble(): String = appDoubleFormatter.format(this)

val editorDoubleFormatter = DecimalFormat("###.##")
fun Double.toEditorDouble(): String = editorDoubleFormatter.format(this).replace(",", ".")