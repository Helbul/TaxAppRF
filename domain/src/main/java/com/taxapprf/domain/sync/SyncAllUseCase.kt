package com.taxapprf.domain.sync

import com.taxapprf.domain.SyncRepository
import com.taxapprf.domain.tax.UpdateAllEmptySumRUBAndTaxRUBUseCase
import javax.inject.Inject

class SyncAllUseCase @Inject constructor(
    private val syncRepository: SyncRepository,
    private val updateAllEmptySumRUBAndTaxRUBUseCase: UpdateAllEmptySumRUBAndTaxRUBUseCase
) {
    suspend fun execute() =
        let {
            syncRepository.syncAll()
            updateAllEmptySumRUBAndTaxRUBUseCase.execute()
        }
}