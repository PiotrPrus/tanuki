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
import dev.tanuki.feature.mergerequests.data.dto.MergeRequestDto
import dev.tanuki.feature.mergerequests.data.mapper.toMergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeRequestFilter
import dev.tanuki.feature.mergerequests.domain.MergeRequestRepository
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

    private suspend fun fetch(scope: String): Result<List<MergeRequest>, DataError.Remote> =
        safeCall<List<MergeRequestDto>> {
            httpClient.get("merge_requests") {
                parameter("scope", scope)
                parameter("state", "opened")
                parameter("per_page", 30)
            }
        }.map { dtos -> dtos.map { it.toMergeRequest() } }
}
