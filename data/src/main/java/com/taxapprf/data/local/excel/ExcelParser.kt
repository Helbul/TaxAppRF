package com.taxapprf.data.local.excel

import com.taxapprf.data.error.excel.DataErrorExcelRead
import com.taxapprf.data.toLocalDate
import com.taxapprf.domain.currency.Currencies
import com.taxapprf.domain.main.transaction.SaveTransactionModel
import com.taxapprf.domain.transactions.TransactionTypes
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import java.io.InputStream


class ExcelParser {
    private val transactions = mutableListOf<SaveTransactionModel>()

    private lateinit var book: HSSFWorkbook
    private lateinit var sheet: Sheet
    private lateinit var row: Row

    fun parse(inputStream: InputStream, accountId: Int): List<SaveTransactionModel> {
        book = HSSFWorkbook(inputStream)
        sheet = book.getSheetAt(0)
        val rowIterator: Iterator<Row> = sheet.iterator()

        row = rowIterator.next()
        row = rowIterator.next()
        row = rowIterator.next()
        row = rowIterator.next()

        while (rowIterator.hasNext()) {
            row = rowIterator.next()

            val transaction = SaveTransactionModel(
                accountId = accountId,
                transactionTypeOrdinal = transactionTypeOrdinal,
                currencyOrdinal = currencyOrdinal,
                name = name,
                date = date,
                sum = sum,
            )
            transactions.add(transaction)
        }

        try {


        } catch (e: Exception) {
            throw DataErrorExcelRead(e.message)
        }

        return transactions
    }

    private val transactionTypeOrdinal
        get() = TransactionTypes.valueOf(row.getCell(1).stringCellValue).ordinal
    private val currencyOrdinal
        get() = Currencies.valueOf(row.getCell(4).stringCellValue).ordinal
    private val name
        get() = row.getCell(0).stringCellValue
    private val date
        get() = row.getCell(2).stringCellValue.toLocalDate()!!.toEpochDay()
    private val sum
        get() = row.getCell(3).numericCellValue
}