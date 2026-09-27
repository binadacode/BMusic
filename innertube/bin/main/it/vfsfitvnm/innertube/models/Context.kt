package it.vfsfitvnm.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class Context(
    val client: Client,
    val thirdParty: ThirdParty? = null,
) {
    @Serializable
    data class Client(
        val clientName: String,
        val clientVersion: String,
        val platform: String = "MOBILE",
        val hl: String = "en",
        val gl: String = "US",
        val deviceMake: String? = null,
        val deviceModel: String? = null,
        val visitorData: String? = "CgtEUlRINDFjdm1YayjX1pSaBg%3D%3D",
        val osName: String? = null,
        val osVersion: String? = null,
        val androidSdkVersion: Int? = null,
        val userAgent: String? = null
    ) {
        val clientId: String?
            get() = when (clientName) {
                "WEB_REMIX" -> "67"
                "ANDROID_MUSIC" -> "21"
                "IOS" -> "5"
                "TVHTML5_SIMPLY_EMBEDDED_PLAYER" -> "85"
                else -> null
            }

        companion object {
            val AndroidMusic = Client(
                clientName = "ANDROID_MUSIC",
                clientVersion = "6.42.52",
                platform = "MOBILE",
                visitorData = null,
                osName = "Android",
                osVersion = "14",
                androidSdkVersion = 34,
                userAgent = "com.google.android.apps.youtube.music/6.42.52 (Linux; U; Android 14; en_US; Pixel 8 Pro; Build/UQ1A.240205.004)"
            )

            val Ios = Client(
                clientName = "IOS",
                clientVersion = "20.10.4",
                visitorData = null,
                deviceMake = "Apple",
                deviceModel = "iPhone16,2",
                osName = "iOS",
                osVersion = "18.3.1.22D72",
                userAgent = "com.google.ios.youtube/20.10.4 (iPhone16,2; U; CPU iOS 18_3_1 like Mac OS X; en_US)"
            )
        }
    }

    @Serializable
    data class ThirdParty(
        val embedUrl: String,
    )

    companion object {
        val DefaultWeb = Context(
            client = Client(
                clientName = "WEB_REMIX",
                clientVersion = "1.20220918",
                platform = "DESKTOP",
            )
        )

        val AndroidMusic = Context(client = Client.AndroidMusic)

        val Ios = Context(client = Client.Ios)

        val DefaultAgeRestrictionBypass = Context(
            client = Client(
                clientName = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
                clientVersion = "2.0",
                platform = "TV"
            )
        )
    }
}
