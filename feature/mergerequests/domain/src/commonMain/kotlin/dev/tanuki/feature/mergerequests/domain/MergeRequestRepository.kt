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

    /** All discussions (threads) on the MR, incl. diff-anchored ones. */
    suspend fun getDiscussions(projectId: Long, iid: Long): Result<List<Discussion>, DataError.Remote>

    /** Start a new discussion anchored to a diff line. */
    suspend fun addDiffComment(
        projectId: Long,
        iid: Long,
        body: String,
        target: NewDiffComment,
    ): EmptyResult<DataError.Remote>

    /** Reply to an existing discussion thread. */
    suspend fun replyToDiscussion(
        projectId: Long,
        iid: Long,
        discussionId: String,
        body: String,
    ): EmptyResult<DataError.Remote>

    /** Resolve or unresolve a discussion thread. */
    suspend fun resolveDiscussion(
        projectId: Long,
        iid: Long,
        discussionId: String,
        resolved: Boolean,
    ): EmptyResult<DataError.Remote>

    /** Commits included in the MR, newest first. */
    suspend fun getCommits(projectId: Long, iid: Long): Result<List<MrCommit>, DataError.Remote>

    /** Pipelines run for the MR, newest first. */
    suspend fun getPipelines(projectId: Long, iid: Long): Result<List<MrPipeline>, DataError.Remote>

    /** Approval state for the MR (who approved, how many left). */
    suspend fun getApprovals(projectId: Long, iid: Long): Result<ApprovalInfo, DataError.Remote>
}
