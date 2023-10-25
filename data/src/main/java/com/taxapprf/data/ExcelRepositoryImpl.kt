package com.taxapprf.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.taxapprf.data.error.excel.DataErrorExcelRead
import com.taxapprf.data.error.excel.DataErrorExcelWrite
import com.taxapprf.data.local.excel.ExcelCreator
import com.taxapprf.data.local.excel.ExcelParser
import com.taxapprf.data.local.excel.getFilePathInSystemDownload
import com.taxapprf.data.local.room.LocalExcelDao
import com.taxapprf.data.local.room.model.excel.ExcelTransactionDataModel
import com.taxapprf.domain.ExcelRepository
import com.taxapprf.domain.currency.Currencies
import com.taxapprf.domain.excel.ExcelTransactionModel
import com.taxapprf.domain.excel.ExportExcelModel
import com.taxapprf.domain.excel.ImportExcelModel
import com.taxapprf.domain.main.transaction.SaveTransactionModel
import com.taxapprf.domain.toAppDate
import com.taxapprf.domain.transactions.TransactionTypes
import com.taxapprf.taxapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class ExcelRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localExcelDao: LocalExcelDao,
) : ExcelRepository {
    override suspend fun export(exportExcelModel: ExportExcelModel): Uri? {
        with(exportExcelModel) {
            val localReportEntity = localExcelDao.getLocalReportEntity(reportId)
                ?: throw DataErrorExcelWrite()
            val localTransactionModels = localExcelDao.getExcelTransactions(reportId)

            if (localTransactionModels.isEmpty()) throw DataErrorExcelWrite()

            reportName = localReportEntity.remoteKey
            reportTax = localReportEntity.taxRUB
            transactions = localTransactionModels.map { it.toExcelTransactionModel() }

            reportTitle = context.getString(R.string.excel_report_title)
            reportInfo = context.getString(R.string.excel_report_info)

            transactionTitles = listOf(
                context.getString(R.string.excel_transaction_name),
                context.getString(R.string.excel_transaction_type),
                context.getString(R.string.excel_transaction_date),
                context.getString(R.string.excel_transaction_sum),
                context.getString(R.string.excel_transaction_currency_char_code),
                context.getString(R.string.excel_transaction_currency_rate),
                context.getString(R.string.excel_transaction_tax),
            )
        }

        return ExcelCreator().createWorkbook(exportExcelModel)?.let { book ->
            val fileName = getExcelFileName()

            val file = File(getFilePathInSystemDownload(fileName))
            val outFile = FileOutputStream(file)

            book.write(outFile)
            outFile.close()

            FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                file
            )
        }
    }

    override suspend fun import(importExcelModel: ImportExcelModel): List<SaveTransactionModel> {
        val contentResolver = context.contentResolver
        contentResolver.openInputStream(importExcelModel.uri)?.use {
            return ExcelParser().parse(it, importExcelModel.accountId)
        }

        throw DataErrorExcelRead()
    }

    private fun getExcelFileName(): String {
        val date = LocalDateTime.now().format(formatter)
        return "Statement-$date.xls"
    }

    private fun ExcelTransactionDataModel.toExcelTransactionModel() =
        ExcelTransactionModel(
            name = name,
            appDate = date.toAppDate(),
            typeName = TransactionTypes.values()[typeOrdinal].name,
            sum = sum,
            currencyCharCode = Currencies.values()[currencyOrdinal].name,
            currencyRate = currencyRate,
            tax = taxRUB
        )

    companion object {
        private const val PATTERN_EXCEL_DATE_TIME = "uuuu-MM-dd_HH-mm-ss"
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(PATTERN_EXCEL_DATE_TIME)
    }
}