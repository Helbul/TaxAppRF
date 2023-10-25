package com.taxapprf.domain.deleted

import com.taxapprf.domain.DeletedRepository
import com.taxapprf.domain.SyncRepository
import javax.inject.Inject

class DeleteReportsUseCase @Inject constructor(
    private val deletedRepository: DeletedRepository,
    private val syncRepository: SyncRepository,
) {
    suspend fun execute(deleteReportsModel: DeleteReportsModel) {
        deletedRepository.deleteReports(deleteReportsModel)
        syncRepository.syncDeleted()
    }
}