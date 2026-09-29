package it.vfsfitvnm.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import io.ktor.http.path
import io.ktor.utils.io.CancellationException
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.Context
import it.vfsfitvnm.innertube.models.PlayerResponse
import it.vfsfitvnm.innertube.models.bodies.PlayerBody
import it.vfsfitvnm.innertube.utils.runCatchingNonCancellable
import kotlinx.serialization.Serializable

private const val playerHost = "www.youtube.com"
private const val iosApiKey = "AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc"
private const val playerMask = "playabilityStatus(status,reason),playerConfig.audioConfig,streamingData(adaptiveFormats,expiresInSeconds),videoDetails(videoId,title,author,lengthSeconds)"

// Ordered by reliability: VISIONOS streams are unrestricted when a valid
// visitorData is supplied, ANDROID_VR streams don't require a poToken and
// aren't throttled for most content, IOS and ANDROID are last resorts.
internal val playerContexts = listOf(Context.VisionOs, Context.AndroidVr, Context.Ios, Context.Android)

@Serializable
private data class AttGetBody(
    val context: Context,
    val engagementType: String = "ENGAGEMENT_TYPE_UNBOUND"
)

@Serializable
private data class AttGetResponse(
    val responseContext: ResponseContext? = null
) {
    @Serializable
    data class ResponseContext(
        val visitorData: String? = null
    )
}

private var cachedVisitorData: String? = null

internal suspend fun Innertube.fetchVisitorData(refresh: Boolean = false): String? {
    if (!refresh) cachedVisitorData?.let { return it }

    return try {
        client.post {
            url {
                protocol = URLProtocol.HTTPS
                host = playerHost
                path("youtubei", "v1", "att", "get")
                parameters.append("prettyPrint", "false")
            }
            clientHeaders(Context.Android.client)
            setBody(AttGetBody(context = Context.Android))
        }.body<AttGetResponse>()
            .responseContext
            ?.visitorData
            ?.also { cachedVisitorData = it }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}

internal suspend fun Innertube.requestPlayer(body: PlayerBody, context: Context): PlayerResponse =
    client.post {
        url {
            protocol = URLProtocol.HTTPS
            host = playerHost
            path("youtubei", "v1", "player")
            parameters.append("prettyPrint", "false")
        }
        if (context == Context.Ios) {
            header("X-Goog-Api-Key", iosApiKey)
        }
        context.client.visitorData?.let { header("X-YouTube-Visitor-Data", it) }
        clientHeaders(context.client)
        setBody(body.copy(context = context))
        mask(playerMask)
    }.body()

private suspend fun Innertube.probeStreamUrl(url: String, userAgent: String?): Boolean = try {
    client.get(url) {
        userAgent?.let { header(HttpHeaders.UserAgent, it) }
        header(HttpHeaders.Range, "bytes=0-0")
    }.status.isSuccess()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    false
}

suspend fun Innertube.player(body: PlayerBody) = runCatchingNonCancellable {
    var rejected: Pair<PlayerResponse, Context>? = null
    var deadUrl: Pair<PlayerResponse, Context>? = null
    var failure: Exception? = null

    for (context in playerContexts) {
        var effectiveContext = context
        var response: PlayerResponse? = null

        var attempt = 0
        val maxAttempts = if (context == Context.VisionOs) 2 else 1
        while (attempt < maxAttempts) {
            attempt++

            if (context == Context.VisionOs) {
                effectiveContext = context.copy(
                    client = context.client.copy(
                        visitorData = fetchVisitorData(refresh = attempt > 1)
                    )
                )
            }

            response = try {
                requestPlayer(body, effectiveContext)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failure = e
                null
            }

            if (context != Context.VisionOs || response?.playabilityStatus?.status == "OK") break
        }

        if (response == null) continue

        val url = response.streamingData?.highestQualityFormat?.url
        if (response.playabilityStatus?.status == "OK" && url != null) {
            // Some IPs/videos get URLs that are rejected by googlevideo (403)
            // right away. Probe the url before handing it to the player.
            if (probeStreamUrl(url, effectiveContext.client.userAgent)) {
                return@runCatchingNonCancellable response to effectiveContext
            }
            if (deadUrl == null) deadUrl = response to effectiveContext
        } else if (rejected == null) {
            rejected = response to effectiveContext
        }
    }

    (deadUrl ?: rejected)?.let { return@runCatchingNonCancellable it }

    throw failure ?: error("No player response")
}
