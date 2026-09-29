import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.Context
import it.vfsfitvnm.innertube.models.bodies.PlayerBody
import it.vfsfitvnm.innertube.requests.fetchVisitorData
import it.vfsfitvnm.innertube.requests.player
import it.vfsfitvnm.innertube.requests.playerContexts
import it.vfsfitvnm.innertube.requests.requestPlayer
import kotlinx.coroutines.runBlocking
import org.junit.Test

class PlayerDiag {
    private val videoId = "kJQP7kiw5Fk"

    @Test
    @Throws(Exception::class)
    fun test(): Unit = runBlocking {
        // 1) Per-client player request + stream probe
        for (baseContext in playerContexts) {
            try {
                val context = if (baseContext == Context.VisionOs) {
                    baseContext.copy(
                        client = baseContext.client.copy(
                            visitorData = Innertube.fetchVisitorData()
                        )
                    )
                } else baseContext

                val r = Innertube.requestPlayer(PlayerBody(videoId = videoId), context)
                val format = r.streamingData?.highestQualityFormat
                val url = format?.url
                println("${context.client.clientName}: status=${r.playabilityStatus?.status} " +
                        "reason=${r.playabilityStatus?.reason} itag=${format?.itag} url?=${url != null}")
                if (url != null) {
                    val probeOk = try {
                        Innertube.client.get(url) {
                            header(HttpHeaders.UserAgent, context.client.userAgent)
                            header(HttpHeaders.Range, "bytes=0-0")
                        }.status.isSuccess()
                    } catch (e: Exception) {
                        println("   probe ERROR: $e"); false
                    }
                    println("   probe=$probeOk")
                }
            } catch (e: Exception) {
                println("${baseContext.client.clientName}: REQUEST FAILED $e")
            }
        }

        // 2) Full pipeline used by the app, then sequential 512KiB range reads
        Innertube.player(PlayerBody(videoId = videoId))?.onSuccess { (r, context) ->
            println("PICKED=${context.client.clientName} STATUS=${r.playabilityStatus?.status}")
            val url = r.streamingData?.highestQualityFormat?.url ?: return@onSuccess
            val chunk = 512 * 1024
            for (i in 0 until 6) {
                val start = i * chunk
                val t0 = System.currentTimeMillis()
                try {
                    val resp = Innertube.client.get(url) {
                        header(HttpHeaders.UserAgent, context.client.userAgent)
                        header(HttpHeaders.Range, "bytes=$start-${start + chunk - 1}")
                    }
                    var n = 0
                    val channel = resp.bodyAsChannel()
                    val buffer = ByteArray(64 * 1024)
                    while (!channel.isClosedForRead) {
                        n += channel.readAvailable(buffer, 0, buffer.size).takeIf { it > 0 } ?: break
                    }
                    println("chunk $i -> HTTP ${resp.status.value}, $n bytes in ${System.currentTimeMillis() - t0}ms")
                } catch (e: Exception) {
                    println("chunk $i -> ERROR: $e")
                }
            }
        }
    }
}
