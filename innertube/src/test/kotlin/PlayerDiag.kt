import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import it.vfsfitvnm.innertube.Innertube
import it.vfsfitvnm.innertube.models.Context
import it.vfsfitvnm.innertube.models.bodies.PlayerBody
import it.vfsfitvnm.innertube.requests.player
import kotlinx.coroutines.runBlocking
import org.junit.Test

class PlayerDiag {
    @Test
    @Throws(Exception::class)
    fun test() {
        runBlocking {
            val result = Innertube.player(PlayerBody(videoId = "dQw4w9WgXcQ"))
            result?.onSuccess { r ->
                println("STATUS=${r.playabilityStatus?.status} REASON=${r.playabilityStatus?.reason}")
                val format = r.streamingData?.highestQualityFormat
                println("highest=itag ${format?.itag} mime=${format?.mimeType} url?=${format?.url != null}")
                val url = format?.url
                if (url != null) {
                    try {
                        val resp = Innertube.client.get(url) {
                            header(HttpHeaders.UserAgent, Context.Ios.client.userAgent)
                            header(HttpHeaders.Range, "bytes=0-1023")
                        }
                        println("STREAM HTTP ${resp.status.value} len=${resp.headers[HttpHeaders.ContentLength]} type=${resp.headers[HttpHeaders.ContentType]}")
                    } catch (e: Exception) {
                        println("STREAM ERROR: $e")
                    }
                }
            }?.onFailure {
                println("FAILURE: $it")
                it.cause?.let { c -> println("CAUSE: $c") }
            } ?: println("RESULT NULL")
        }
    }
}
