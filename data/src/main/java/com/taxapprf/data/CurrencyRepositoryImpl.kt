package com.taxapprf.data

import com.taxapprf.data.local.room.LocalCurrencyDao
import com.taxapprf.data.local.room.entity.LocalCurrencyRateEntity
import com.taxapprf.data.remote.cbr.RemoteCurrencyDao
import com.taxapprf.data.remote.cbr.entity.RemoteValCursEntity
import com.taxapprf.data.remote.cbr.entity.RemoteValuteEntity
import com.taxapprf.domain.CurrencyRepository
import com.taxapprf.domain.currency.Currencies
import com.taxapprf.domain.currency.CurrencyRateModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val networkManager: NetworkManager,
    private val localCurrencyDao: LocalCurrencyDao,
    private val remoteCurrencyDao: RemoteCurrencyDao,
) : CurrencyRepository {
    override suspend fun getCurrencyRate(currencyOrdinal: Int, date: Long) =
        localCurrencyDao.getCurrencyRate(currencyOrdinal, date)?.rate
            ?: tryGetRemote(date, currencyOrdinal)
                ?.getCurrencyRate(currencyOrdinal)?.rate

    override suspend fun getCurrencyRateModels(date: Long) =
        localCurrencyDao.getCurrenciesRate(date).let { ratesWithCurrency ->
            ratesWithCurrency.ifEmpty {
                tryGetRemote(date)
                localCurrencyDao.getCurrenciesRate(date)
            }
        }.map { it.toCurrencyRateModel() }

    private fun tryGetRemote(date: Long, currencyOrdinal: Int? = null) =
        try {
            if (networkManager.isConnection) {
                val formattedDate = date.toCBRDate()
                remoteCurrencyDao.getValCurs(formattedDate).execute().body()
                    .cacheResult(date, currencyOrdinal)
            } else null
        } catch (_: Exception) {
            null
        }

    private fun Long.toCBRDate() =
        LocalDate.ofEpochDay(this).format(formatter_cbr_date)

    private fun RemoteValCursEntity?.cacheResult(
        date: Long,
        currencyOrdinal: Int?
    ): List<LocalCurrencyRateEntity> {
        val localRates = mutableListOf<LocalCurrencyRateEntity>()

        this?.currencies?.forEach { valute ->
            valute.charCode?.let { charCode ->
                try {
                    val ordinal = Currencies.valueOf(charCode).ordinal
                    valute.toLocalCBRRateEntity(ordinal, date)
                        ?.let { localRates.add(it) }
                } catch (_: Exception) {

                }
            }
        } ?: run {
            currencyOrdinal?.let {
                localRates.add(
                    getLocalCurrencyRateEntity(
                        currencyOrdinal,
                        date,
                        RATE_NOT_LOADED_ON_CBR
                    )
                )
            }
        }

        localCurrencyDao.saveRates(localRates)

        return localRates
    }

    private fun List<LocalCurrencyRateEntity>.getCurrencyRate(currencyOrdinal: Int) =
        find { it.currencyOrdinal == currencyOrdinal }

    private fun RemoteValuteEntity.toLocalCBRRateEntity(
        cbrCurrencyId: Int,
        date: Long,
    ): LocalCurrencyRateEntity? {
        try {
            val nominal = nominal ?: return null
            val value = value ?: return null
            val rate = value.formatValueDot().toDouble() / nominal

            return getLocalCurrencyRateEntity(cbrCurrencyId, date, rate)
        } catch (_: Exception) {
            return null
        }
    }

    private fun getLocalCurrencyRateEntity(currencyOrdinal: Int, date: Long, rate: Double? = null) =
        LocalCurrencyRateEntity(
            currencyOrdinal = currencyOrdinal,
            date = date,
            rate = rate
        )

    private fun String.formatValueDot() = replace(',', '.')

    private fun LocalCurrencyRateEntity.toCurrencyRateModel() =
        CurrencyRateModel(
            currency = Currencies.values()[currencyOrdinal],
            rate = rate
        )

    companion object {
        private const val PATTERN_CBR_DATE = "dd/MM/uuuu"
        private val formatter_cbr_date = DateTimeFormatter.ofPattern(PATTERN_CBR_DATE)
        private const val RATE_NOT_LOADED_ON_CBR = -1.0
    }
}