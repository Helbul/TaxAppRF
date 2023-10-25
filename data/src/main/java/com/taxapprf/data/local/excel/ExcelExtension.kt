package com.taxapprf.data.local.excel

import android.os.Environment

fun getSystemDownloadPath() =
    "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path}/"

fun getFilePathInSystemDownload(filename: String) =
    "${getSystemDownloadPath()}$filename"