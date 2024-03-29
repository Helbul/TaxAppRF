package com.taxapprf.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.taxapprf.data.getEpochTime
import com.taxapprf.data.local.room.LocalDatabase.Companion.ACCOUNT_ID
import com.taxapprf.data.local.room.LocalDatabase.Companion.DEFAULT_ID
import com.taxapprf.data.local.room.LocalDatabase.Companion.ID
import com.taxapprf.data.local.room.entity.LocalReportEntity.Companion.TABLE_NAME
import com.taxapprf.data.sync.IS_SYNC
import com.taxapprf.data.sync.ISyncLocalModel
import com.taxapprf.data.sync.REMOTE_KEY
import com.taxapprf.data.sync.SYNC_AT

@Entity(tableName = TABLE_NAME)
data class LocalReportEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    val id: Int = DEFAULT_ID,

    @ColumnInfo(name = ACCOUNT_ID)
    val accountId: Int,

    @ColumnInfo(name = SUM_RUB)
    val sumRUB: Double = DEFAULT_SUM_RUB,
    @ColumnInfo(name = TAX_RUB)
    val taxRUB: Double = DEFAULT_TAX_RUB,
    @ColumnInfo(name = SIZE)
    val size: Int = DEFAULT_SIZE,

    @ColumnInfo(name = REMOTE_KEY)
    override val remoteKey: String,
    @ColumnInfo(name = IS_SYNC)
    override val isSync: Boolean = false,
    @ColumnInfo(name = SYNC_AT)
    override val syncAt: Long = getEpochTime(),
) : ISyncLocalModel {
    companion object {
        const val TABLE_NAME = "report"

        const val SUM_RUB = "sum_rub"
        const val TAX_RUB = "tax_rub"
        const val SIZE = "size"

        const val DEFAULT_SUM_RUB = 0.0
        const val DEFAULT_TAX_RUB = 0.0
        const val DEFAULT_SIZE = 0
    }
}