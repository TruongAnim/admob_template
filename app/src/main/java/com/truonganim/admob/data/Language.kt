package com.truonganim.admob.data

/**
 * Language data class
 * Represents a supported language in the app
 */
data class Language(
    val code: String,
    val name: String,
    val nativeName: String,
    val flag: String
)

/**
 * Supported languages
 * Ordered by popularity/usage
 */
object SupportedLanguages {
    val languages = listOf(
        Language(
            code = "en",
            name = "English",
            nativeName = "English",
            flag = "🇬🇧"
        ),
        Language(
            code = "es",
            name = "Spanish",
            nativeName = "Español",
            flag = "🇪🇸"
        ),
        Language(
            code = "zh",
            name = "Chinese",
            nativeName = "中文",
            flag = "🇨🇳"
        ),
        Language(
            code = "zh-Hant",
            name = "Chinese (Traditional)",
            nativeName = "繁體中文",
            flag = "🇹🇼"
        ),
        Language(
            code = "fr",
            name = "French",
            nativeName = "Français",
            flag = "🇫🇷"
        ),
        Language(
            code = "de",
            name = "German",
            nativeName = "Deutsch",
            flag = "🇩🇪"
        ),
        Language(
            code = "ja",
            name = "Japanese",
            nativeName = "日本語",
            flag = "🇯🇵"
        ),
        Language(
            code = "pt",
            name = "Portuguese",
            nativeName = "Português",
            flag = "🇵🇹"
        ),
        Language(
            code = "ru",
            name = "Russian",
            nativeName = "Русский",
            flag = "🇷🇺"
        ),
        Language(
            code = "ko",
            name = "Korean",
            nativeName = "한국어",
            flag = "🇰🇷"
        ),
        Language(
            code = "vi",
            name = "Vietnamese",
            nativeName = "Tiếng Việt",
            flag = "🇻🇳"
        )
    )
    
    fun getLanguageByCode(code: String): Language? {
        return languages.find { it.code == code }
    }
}

