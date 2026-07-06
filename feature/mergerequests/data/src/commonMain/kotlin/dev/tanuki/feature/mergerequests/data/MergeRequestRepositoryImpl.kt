package dev.tanuki.feature.mergerequests.data

import dev.tanuki.core.data.diff.DiffDto
import dev.tanuki.core.data.diff.toFileDiff
import dev.tanuki.core.data.network.safeCall
import dev.tanuki.core.data.network.safeCallEmpty
import dev.tanuki.core.domain.diff.FileDiff
import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.EmptyResult
import dev.tanuki.core.domain.util.Result
import dev.tanuki.core.domain.util.map
import dev.tanuki.feature.mergerequests.data.dto.ApprovalsDto
import dev.tanuki.feature.mergerequests.data.dto.DiscussionDto
import dev.tanuki.feature.mergerequests.data.dto.MergeRequestDto
import dev.tanuki.feature.mergerequests.data.dto.MrCommitDto
import dev.tanuki.feature.mergerequests.data.dto.MrPipelineDto
import dev.tanuki.feature.mergerequests.data.dto.toApprovalInfo
import dev.tanuki.feature.mergerequests.data.dto.toDiscussion
import dev.tanuki.feature.mergerequests.data.dto.toMrCommit
import dev.tanuki.feature.mergerequests.data.dto.toMrPipeline
import dev.tanuki.feature.mergerequests.data.mapper.toMergeRequest
import dev.tanuki.feature.mergerequests.domain.ApprovalInfo
import dev.tanuki.feature.mergerequests.domain.Discussion
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeRequestFilter
import dev.tanuki.feature.mergerequests.domain.MergeRequestRepository
import dev.tanuki.feature.mergerequests.domain.MrCommit
import dev.tanuki.feature.mergerequests.domain.MrPipeline
import dev.tanuki.feature.mergerequests.domain.NewDiffComment
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put

class MergeRequestRepositoryImpl(
    private val httpClient: HttpClient,
) : MergeRequestRepository {

    override suspend fun getReviewRequested() = fetch(scope = "reviews_for_me")

    override suspend fun getAssignedToMe() = fetch(scope = "assigned_to_me")

    override suspend fun getProjectMergeRequests(
        projectId: Long,
        filter: MergeRequestFilter,
    ): Result<List<MergeRequest>, DataError.Remote> =
        safeCall<List<MergeRequestDto>> {
            httpClient.get("projects/$projectId/merge_requests") {
                parameter("state", filter.apiValue)
                parameter("order_by", "updated_at")
                parameter("sort", "desc")
                parameter("per_page", 30)
            }
        }.map { dtos -> dtos.map { it.toMergeRequest() } }

    override suspend fun getMergeRequest(
        projectId: Long,
        iid: Long,
    ): Result<MergeRequest, DataError.Remote> =
        safeCall<MergeRequestDto> {
            httpClient.get("projects/$projectId/merge_requests/$iid")
        }.map { it.toMergeRequest() }

    override suspend fun getDiffs(
        projectId: Long,
        iid: Long,
    ): Result<List<FileDiff>, DataError.Remote> =
        safeCall<List<DiffDto>> {
            httpClient.get("projects/$projectId/merge_requests/$iid/diffs") {
                parameter("per_page", 50)
            }
        }.map { dtos -> dtos.map { it.toFileDiff() } }

    override suspend fun approve(projectId: Long, iid: Long): EmptyResult<DataError.Remote> =
        safeCallEmpty {
            httpClient.post("projects/$projectId/merge_requests/$iid/approve")
        }

    override suspend fun merge(projectId: Long, iid: Long): EmptyResult<DataError.Remote> =
        safeCallEmpty {
            httpClient.put("projects/$projectId/merge_requests/$iid/merge")
        }

    override suspend fun rebase(projectId: Long, iid: Long, skipCi: Boolean): EmptyResult<DataError.Remote> =
        safeCallEmpty {
            httpClient.put("projects/$projectId/merge_requests/$iid/rebase") {
                if (skipCi) parameter("skip_ci", true)
            }
        }

    override suspend fun comment(
        projectId: Long,
        iid: Long,
        body: String,
    ): EmptyResult<DataError.Remote> =
        safeCallEmpty {
            httpClient.post("projects/$projectId/merge_requests/$iid/notes") {
                parameter("body", body)
            }
        }

    override suspend fun getDiscussions(
        projectId: Long,
        iid: Long,
    ): Result<List<Discussion>, DataError.Remote> =
        safeCall<List<DiscussionDto>> {
            httpClient.get("projects/$projectId/merge_requests/$iid/discussions") {
                parameter("per_page", 100)
            }
        }.map { dtos -> dtos.map { it.toDiscussion() } }

    override suspend fun addDiffComment(
        projectId: Long,
        iid: Long,
        body: String,
        target: NewDiffComment,
    ): EmptyResult<DataError.Remote> =
        safeCallEmpty {
            httpClient.post("projects/$projectId/merge_requests/$iid/discussions") {
                parameter("body", body)
                parameter("position[position_type]", "text")
                parameter("position[base_sha]", target.refs.baseSha)
                parameter("position[start_sha]", target.refs.startSha)
                parameter("position[head_sha]", target.refs.headSha)
                parameter("position[new_path]", target.newPath)
                parameter("position[old_path]", target.oldPath)
                target.newLine?.let { parameter("position[new_line]", it) }
                target.oldLine?.let { parameter("position[old_line]", it) }
            }
        }

    override suspend fun replyToDiscussion(
        projectId: Long,
        iid: Long,
        discussionId: String,
        body: String,
    ): EmptyResult<DataError.Remote> =
        safeCallEmpty {
            httpClient.post("projects/$projectId/merge_requests/$iid/discussions/$discussionId/notes") {
                parameter("body", body)
            }
        }

    override suspend fun resolveDiscussion(
        projectId: Long,
        iid: Long,
        discussionId: String,
        resolved: Boolean,
    ): EmptyResult<DataError.Remote> =
        safeCallEmpty {
            httpClient.put("projects/$projectId/merge_requests/$iid/discussions/$discussionId") {
                parameter("resolved", resolved)
            }
        }

    override suspend fun getCommits(
        projectId: Long,
        iid: Long,
    ): Result<List<MrCommit>, DataError.Remote> =
        safeCall<List<MrCommitDto>> {
            httpClient.get("projects/$projectId/merge_requests/$iid/commits") { parameter("per_page", 100) }
        }.map { dtos -> dtos.map { it.toMrCommit() } }

    override suspend fun getPipelines(
        projectId: Long,
        iid: Long,
    ): Result<List<MrPipeline>, DataError.Remote> =
        safeCall<List<MrPipelineDto>> {
            httpClient.get("projects/$projectId/merge_requests/$iid/pipelines") { parameter("per_page", 20) }
        }.map { dtos -> dtos.map { it.toMrPipeline() } }

    override suspend fun getApprovals(
        projectId: Long,
        iid: Long,
    ): Result<ApprovalInfo, DataError.Remote> =
        safeCall<ApprovalsDto> {
            httpClient.get("projects/$projectId/merge_requests/$iid/approvals")
        }.map { it.toApprovalInfo() }

    private suspend fun fetch(scope: String): Result<List<MergeRequest>, DataError.Remote> =
        safeCall<List<MergeRequestDto>> {
            httpClient.get("merge_requests") {
                parameter("scope", scope)
                parameter("state", "opened")
                parameter("per_page", 30)
            }
        }.map { dtos -> dtos.map { it.toMergeRequest() } }
}
