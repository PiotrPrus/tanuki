package dev.tanuki.feature.auth.data

import dev.tanuki.feature.auth.data.crypto.sha256
import dev.tanuki.feature.auth.domain.PkcePair
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

/**
 * Generates a PKCE verifier/challenge (S256) per GitLab's spec: verifier is 43-128 chars
 * from [A-Z a-z 0-9 - . _ ~]; challenge = base64url(sha256(verifier)) without padding.
 *
 * TODO(security): [Random] is not cryptographically secure — swap for a platform CSPRNG
 * (SecureRandom / SecRandomCopyBytes) before shipping.
 */
object PkceGenerator {
    private const val VERIFIER_LENGTH = 64
    private const val ALLOWED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"

    @OptIn(ExperimentalEncodingApi::class)
    private val base64Url = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

    fun generate(): PkcePair {
        val verifier = randomString(VERIFIER_LENGTH)
        val challenge = base64Url.encode(sha256(verifier.encodeToByteArray()))
        return PkcePair(codeVerifier = verifier, codeChallenge = challenge)
    }

    fun randomState(): String = randomString(24)

    private fun randomString(length: Int): String =
        buildString(length) { repeat(length) { append(ALLOWED[Random.nextInt(ALLOWED.length)]) } }
}
