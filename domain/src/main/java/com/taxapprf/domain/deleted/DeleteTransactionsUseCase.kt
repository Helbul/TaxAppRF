package com.taxapprf.domain.deleted

import com.taxapprf.domain.DeletedRepository
import com.taxapprf.domain.SyncRepository
import javax.inject.Inject

class DeleteTransactionsUseCase @Inject constructor(
    private val deletedRepository: DeletedRepository,
    private val syncRepository: SyncRepository,
) {
    suspend fun execute(deleteTransactionsModel: DeleteTransactionsModel) {
        deletedRepository.deleteTransactions(deleteTransactionsModel)
        syncRepository.syncDeleted()
    }
}