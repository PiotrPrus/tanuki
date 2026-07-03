package dev.tanuki.feature.auth.domain

/** A PKCE (RFC 7636) verifier/challenge pair for one OAuth login attempt. */
data class PkcePair(
    val codeVerifier: String,
    val codeChallenge: String,
)
