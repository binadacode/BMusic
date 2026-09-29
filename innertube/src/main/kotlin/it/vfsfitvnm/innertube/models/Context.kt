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
                "ANDROID" -> "3"
                "ANDROID_VR" -> "28"
                "VISIONOS" -> "101"
                "IOS" -> "5"
                "TVHTML5_SIMPLY_EMBEDDED_PLAYER" -> "85"
                else -> null
            }

        companion object {
            val VisionOs = Client(
                clientName = "VISIONOS",
                clientVersion = "1.02",
                platform = "MOBILE",
                visitorData = null,
                deviceMake = "Apple",
                deviceModel = "RealityDevice17,1",
                osName = "visionOS",
                osVersion = "26.5.23O471",
                userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Safari/605.1.15"
            )

            val AndroidVr = Client(
                clientName = "ANDROID_VR",
                clientVersion = "1.61.48",
                platform = "MOBILE",
                visitorData = null,
                deviceMake = "Oculus",
                deviceModel = "Quest 3",
                osName = "Android",
                osVersion = "12L",
                androidSdkVersion = 32,
                userAgent = "com.google.android.apps.youtube.vr.oculus/1.61.48 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1)"
            )

            val Android = Client(
                clientName = "ANDROID",
                clientVersion = "20.10.38",
                platform = "MOBILE",
                visitorData = null,
                osName = "Android",
                osVersion = "15",
                androidSdkVersion = 35,
                userAgent = "com.google.android.youtube/20.10.38 (Linux; U; Android 15) gzip"
            )

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

        val VisionOs = Context(client = Client.VisionOs)

        val AndroidVr = Context(client = Client.AndroidVr)

        val Android = Context(client = Client.Android)

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
