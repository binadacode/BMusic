package it.vfsfitvnm.innertube.requests

import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.URLProtocol
import io.ktor.http.path
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.Context
import it.vfsfitvnm.innertube.models.PlayerResponse
import it.vfsfitvnm.innertube.models.bodies.PlayerBody
import it.vfsfitvnm.innertube.utils.runCatchingNonCancellable

private const val playerHost = "www.youtube.com"
private const val iosApiKey = "AIzaSyB-63vPrdThhKuerbB2N_l7Kwwcxj6yUAc"
private const val playerMask = "playabilityStatus(status,reason),playerConfig.audioConfig,streamingData(adaptiveFormats,expiresInSeconds),videoDetails(videoId,title,author,lengthSeconds)"

suspend fun Innertube.player(body: PlayerBody) = runCatchingNonCancellable {
    val iosBody = body.copy(context = Context.Ios)

    client.post {
        url {
            protocol = URLProtocol.HTTPS
            host = playerHost
            path("youtubei", "v1", "player")
            parameters.append("prettyPrint", "false")
        }
        header("X-Goog-Api-Key", iosApiKey)
        clientHeaders(iosBody.context.client)
        setBody(iosBody)
        mask(playerMask)
    }.body<PlayerResponse>()
}
