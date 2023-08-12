package com.taxapprf.domain.account


data class SwitchAccountModel(
    val oldAccountName: String,
    val newAccountName: String
)