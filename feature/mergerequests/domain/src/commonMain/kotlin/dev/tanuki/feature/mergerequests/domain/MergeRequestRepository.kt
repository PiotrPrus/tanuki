package dev.tanuki.feature.mergerequests.domain

import dev.tanuki.core.domain.diff.FileDiff
import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.EmptyResult
import dev.tanuki.core.domain.util.Result

interface MergeRequestRepository {
    suspend fun getReviewRequested(): Result<List<MergeRequest>, DataError.Remote>
    suspend fun getAssignedToMe(): Result<List<MergeRequest>, DataError.Remote>

    /** All merge requests in a single project, filtered by lifecycle [filter], newest first. */
    suspend fun getProjectMergeRequests(
        projectId: Long,
        filter: MergeRequestFilter,
    ): Result<List<MergeRequest>, DataError.Remote>

    suspend fun getMergeRequest(projectId: Long, iid: Long): Result<MergeRequest, DataError.Remote>
    suspend fun getDiffs(projectId: Long, iid: Long): Result<List<FileDiff>, DataError.Remote>

    suspend fun approve(projectId: Long, iid: Long): EmptyResult<DataError.Remote>
    suspend fun merge(projectId: Long, iid: Long): EmptyResult<DataError.Remote>
    suspend fun comment(projectId: Long, iid: Long, body: String): EmptyResult<DataError.Remote>
}
