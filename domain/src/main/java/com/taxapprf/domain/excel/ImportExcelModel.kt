package com.taxapprf.domain.excel

import android.net.Uri

data class ImportExcelModel(
    val accountId: Int,
    val uri: Uri,
)