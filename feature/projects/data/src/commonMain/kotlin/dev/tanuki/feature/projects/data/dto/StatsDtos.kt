package dev.tanuki.feature.projects.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagDto(val name: String)

@Serializable
data class PipelineDto(val status: String? = null)

@Serializable
data class ReleaseDto(@SerialName("tag_name") val tagName: String? = null)

@Serializable
data class CommitDto(@SerialName("created_at") val createdAt: String? = null)
