package com.taxapprf.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.taxapprf.data.local.room.entity.LocalCurrencyRateEntity

@Dao
interface LocalCurrencyDao {
    @Query("SELECT * FROM cbr_rate WHERE currency_ordinal = :currencyOrdinal AND date = :date LIMIT 1")
    fun getCurrencyRate(currencyOrdinal: Int, date: Long): LocalCurrencyRateEntity?

    @Query("SELECT * FROM cbr_rate WHERE date =:date")
    fun getCurrenciesRate(date: Long): List<LocalCurrencyRateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRates(localCBRRateEntities: List<LocalCurrencyRateEntity>): List<Long>
}