package com.taxapprf.domain.account


data class SwitchAccountModel(
    val oldAccountModel: AccountModel,
    val newAccountModel: AccountModel
)