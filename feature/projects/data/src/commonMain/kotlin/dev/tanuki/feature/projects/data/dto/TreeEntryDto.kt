package dev.tanuki.feature.projects.data.dto

import dev.tanuki.feature.projects.domain.RepoEntry
import kotlinx.serialization.Serializable

@Serializable
data class TreeEntryDto(
    val name: String,
    val type: String,
    val path: String,
)

fun TreeEntryDto.toRepoEntry(): RepoEntry = RepoEntry(
    name = name,
    path = path,
    isDirectory = type == "tree",
)
