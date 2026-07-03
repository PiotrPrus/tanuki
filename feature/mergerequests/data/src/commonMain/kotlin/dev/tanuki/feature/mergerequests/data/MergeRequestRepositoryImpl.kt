package dev.tanuki.feature.mergerequests.data

import dev.tanuki.core.data.network.safeCall
import dev.tanuki.core.domain.util.DataError
import dev.tanuki.core.domain.util.Result
import dev.tanuki.core.domain.util.map
import dev.tanuki.feature.mergerequests.data.dto.MergeRequestDto
import dev.tanuki.feature.mergerequests.data.mapper.toMergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeRequest
import dev.tanuki.feature.mergerequests.domain.MergeRequestRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class MergeRequestRepositoryImpl(
    private val httpClient: HttpClient,
) : MergeRequestRepository {

    override suspend fun getReviewRequested() = fetch(scope = "reviews_for_me")

    override suspend fun getAssignedToMe() = fetch(scope = "assigned_to_me")

    private suspend fun fetch(scope: String): Result<List<MergeRequest>, DataError.Remote> =
        safeCall<List<MergeRequestDto>> {
            httpClient.get("merge_requests") {
                parameter("scope", scope)
                parameter("state", "opened")
                parameter("per_page", 30)
            }
        }.map { dtos -> dtos.map { it.toMergeRequest() } }
}
