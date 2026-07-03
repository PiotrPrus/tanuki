package dev.tanuki.feature.mergerequests.domain

import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.Result

interface MergeRequestRepository {
    suspend fun getReviewRequested(): Result<List<MergeRequest>, DataError.Remote>
    suspend fun getAssignedToMe(): Result<List<MergeRequest>, DataError.Remote>
}
