package it.vfsfitvnm.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerResponse(
    val playabilityStatus: PlayabilityStatus? = null,
    val playerConfig: PlayerConfig? = null,
    val streamingData: StreamingData? = null,
    val videoDetails: VideoDetails? = null,
) {
    @Serializable
    data class PlayabilityStatus(
        val status: String? = null,
        val reason: String? = null
    )

    @Serializable
    data class PlayerConfig(
        val audioConfig: AudioConfig? = null
    ) {
        @Serializable
        data class AudioConfig(
            private val loudnessDb: Double? = null
        ) {
            // For music clients only
            val normalizedLoudnessDb: Float?
                get() = loudnessDb?.plus(7)?.toFloat()
        }
    }

    @Serializable
    data class StreamingData(
        val expiresInSeconds: String? = null,
        val adaptiveFormats: List<AdaptiveFormat>? = null
    ) {
        val highestQualityFormat: AdaptiveFormat?
            get() = adaptiveFormats?.findLast { it.itag == 251 || it.itag == 140 }

        @Serializable
        data class AdaptiveFormat(
            val itag: Int,
            val mimeType: String,
            val bitrate: Long? = null,
            val averageBitrate: Long? = null,
            val contentLength: Long? = null,
            val audioQuality: String? = null,
            val approxDurationMs: Long? = null,
            val lastModified: Long? = null,
            val loudnessDb: Double? = null,
            val audioSampleRate: Int? = null,
            val audioChannels: Int? = null,
            val url: String? = null,
            val signatureCipher: String? = null,
        )
    }

    @Serializable
    data class VideoDetails(
        val videoId: String? = null,
        val title: String? = null,
        val author: String? = null,
        val lengthSeconds: String? = null
    )
}
