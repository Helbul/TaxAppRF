package com.taxapprf.taxapp.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.taxapprf.data.error.AuthErrorSessionExpired
import com.taxapprf.data.error.DataErrorAuth
import com.taxapprf.data.error.InputErrorEmailEmpty
import com.taxapprf.data.error.InputErrorEmailIncorrect
import com.taxapprf.data.error.InputErrorNameEmpty
import com.taxapprf.data.error.InputErrorPasswordLength
import com.taxapprf.data.error.InputErrorPhoneEmpty
import com.taxapprf.data.error.SignInErrorWrongPassword
import com.taxapprf.data.error.SignUpErrorEmailAlreadyUse
import com.taxapprf.taxapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.floor

fun View.showSnackBar(msg: Int) =
    Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).show()

fun String.isEmailPattern(): Boolean {
    val p = Pattern.compile("\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*\\.\\w{2,4}")
    return p.matcher(this).matches()
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Activity.checkStoragePermission(): Boolean {
    val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    return if (ContextCompat.checkSelfPermission(this, permission)
        == PackageManager.PERMISSION_GRANTED
    ) true
    else {
        ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
        false
    }
}

fun Date.format(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(this)
}

fun Double.format() = floor(this * 100.0) / 100.0

fun Throwable.getErrorDescription() = when (this) {
    is DataErrorAuth -> R.string.auth_error
    is AuthErrorSessionExpired -> R.string.auth_error_session_expire
    is InputErrorNameEmpty -> R.string.error_name_empty
    is InputErrorPhoneEmpty -> R.string.error_phone_empty
    is InputErrorEmailEmpty -> R.string.error_email_empty
    is InputErrorEmailIncorrect -> R.string.error_email_incorrect
    is InputErrorPasswordLength -> R.string.error_password_length
    is SignInErrorWrongPassword -> R.string.error_sign_in
    is SignUpErrorEmailAlreadyUse -> R.string.sign_up_error_email_already_use
    else -> throw this
}

fun Activity.share(uri: Uri) {
    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.type = "vnd.android.cursor.dir/email"
    emailIntent.putExtra(Intent.EXTRA_STREAM, uri)
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Расчёт налога от TaxApp")
    startActivity(Intent.createChooser(emailIntent, "Send email..."))
}