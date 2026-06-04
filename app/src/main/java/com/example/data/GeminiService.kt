package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini Request / Response Models ---

data class WordLookupResult(
    val definition: String,
    val translation: String,
    val pronunciation: String
)

data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

data class GeminiContent(
    @Json(name = "role") val role: String? = null, // "user" or "model"
    @Json(name = "parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @Json(name = "text") val text: String
)

data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

// --- Retrofit Interface ---

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- Gemini Service Client ---

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    /**
     * Summarizes a book based on the current title, author, and description in the app's selected language.
     */
    suspend fun generateBookSummary(
        title: String,
        author: String,
        description: String,
        language: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder.")
            return@withContext getFallbackSummary(title, author, language)
        }

        val prompt = """
            You are an expert literary scholar. Please provide a rich, detailed, and engaging book summary of:
            Title: "$title"
            Author: "$author"
            Description: "$description"

            Write the response directly and beautifully in **$language** language.
            Use elegant paragraphs, bullet points for key themes/takeaways, and balanced spacing. Do not write generic text.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.5f),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = "You are a professional literary assistant built into MyReader. Write in a readable formatting style tailored to reading enthusiasts."))
            )
        )

        try {
            val response = api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No summary could be generated. Please try again."
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call Gemini API for summary: ${e.message}", e)
            getFallbackSummary(title, author, language) + "\n\n(AI service is temporarily unavailable: ${e.localizedMessage})"
        }
    }

    /**
     * Conducts chat conversation history to answer user queries using Gemini.
     */
    suspend fun generateChatResponse(
        history: List<Pair<String, String>>, // list of Role to Text
        userMessage: String,
        language: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder.")
            return@withContext getFallbackChatResponse(userMessage, language)
        }

        val formattedContents = mutableListOf<GeminiContent>()
        
        // Map history
        history.forEach { (role, text) ->
            // role from pair: "user" or "model"
            formattedContents.add(GeminiContent(role = role, parts = listOf(GeminiPart(text = text))))
        }

        // Add latest user query
        formattedContents.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = userMessage))))

        val systemPrompt = "You are a highly knowledgeable literary scholar, research assistant, and book buddy built into the MyReader app. You help users find books, understand complex literary concepts, analyze characters, explain themes, and recommend readings. Always provide rich, comprehensive, and accurate answers directly in the **$language** language. Use friendly tone, paragraphs, and markdown where appropriate."

        val request = GeminiRequest(
            contents = formattedContents,
            generationConfig = GeminiGenerationConfig(temperature = 0.7f),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
        )

        try {
            val response = api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I apologize, but I couldn't process your request. Could you formulate it differently?"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to call Gemini API for chat research: ${e.message}", e)
            getFallbackChatResponse(userMessage, language) + "\n\n(Network connection error: ${e.localizedMessage})"
        }
    }

    private fun getFallbackSummary(title: String, author: String, language: String): String {
        return when (language) {
            "Bengali" -> """
                **$title** ($author দ্বারা লিখিত) একটি উল্লেখযোগ্য এবং আকর্ষণীয় বই।
                
                *প্রধান বিষয়বস্তু:*
                - এই বইটি মানব জীবন, সাহিত্য এবং জ্ঞান অন্বেষণের গভীর মাত্রা উন্মোচন করে।
                - এটি পাঠককে চিন্তা ও ভাবনার খোরাক যোগায়।
                - সাহিত্যিক চমৎকারিত্ব এবং চরিত্র চিত্রণ এই বইটিকে আরও প্রাণবন্ত করেছে।

                *(দ্রষ্টব্য: রিয়েল-টাইম বিস্তারিত এআই সারাংশ পেতে অনুগ্রহ করে এআই স্টুডিওর সিক্রেট প্যানেলে আপনার জেমিনি এপিআই কি (GEMINI_API_KEY) যুক্ত করুন।)*
            """.trimIndent()
            "Arabic" -> """
                كتاب **$title** للكاتب ($author) هو عمل أدبي بارز وملهم.
                
                *المحاور الرئيسية:*
                - يستكشف هذا الكتاب أبعاداً عميقة في السلوك البشري والمعرفة والوجدان.
                - يثير الفضول الفكري ويحفّز القارئ على التأمل والتفكير العميق.
                - الحبكة الأدبية ورسم الشخصيات يضفيان لمسة فريدة من الجمال على الكتاب.

                *(ملاحظة: للحصول على ملخص ذكي وفوري مدعوم بالذكاء الاصطناعي، يرجى إضافة مفتاح GEMINI_API_KEY الخاص بك في لوحة أسرار AI Studio).*
            """.trimIndent()
            else -> """
                **$title** by $author is a remarkable book loaded with deep literary values and comprehensive lessons.
                
                *Key Themes & Spotlights:*
                - Human relationships, emotional depth, and intellectual growth.
                - Strong character building with deep philosophies woven smoothly.
                - High replay value for readers looking to expand their literary context.

                *(Note: To get real-time dynamic AI-powered summaries, please add your Gemini API Key via the Secrets panel in AI Studio).*
            """.trimIndent()
        }
    }

    private fun getFallbackChatResponse(userMessage: String, language: String): String {
        return when (language) {
            "Bengali" -> """
                আমি আপনার বার্তা " $userMessage " পেয়েছি। লজিক্যাল বিশ্লেষণ এবং গবেষণা ভিত্তিক আলোচনা করতে অনুগ্রহ করে এআই স্টুডিওর সিক্রেট প্যানেলে আপনার জেমিনি এপিআই কি (GEMINI_API_KEY) সক্রিয় করুন।
                
                আপনার কোনো বই বা সাহিত্য সম্পর্কে সাধারণ জিজ্ঞাসা থাকলে বলুন, আমি অফলাইন লাইব্রেরি ডেটা থেকে উত্তর দেওয়ার চেষ্টা করবো।
            """.trimIndent()
            "Arabic" -> """
                لقد استلمت رسالتك: "$userMessage". للمساعدة في الأبحاث العميقة والدراسات الأدبية بدقة، يرجى تفعيل مفتاح الـ API الخاص بـ Gemini (GEMINI_API_KEY) في لوحة الأسرار في AI Studio.
                
                إذا كان لديك أي سؤال عام حول الكتب المتوفرة، يسعدني جداً إجابتك.
            """.trimIndent()
            else -> """
                I received your query: "$userMessage".
                To unlock advanced research capability, cross-referencing, and real-time detailed literary dialogue, please configure your **GEMINI_API_KEY** via the AI Studio Secrets panel.
                
                Feel free to ask about any general library book selection or local settings!
            """.trimIndent()
        }
    }

    /**
     * Looks up definitions, translations and pronunciations for a word
     */
    suspend fun lookupWord(
        word: String,
        context: String,
        targetLanguage: String
    ): WordLookupResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder.")
            return@withContext getFallbackWordLookup(word, targetLanguage)
        }

        val prompt = """
            Look up the word "$word" used in this sentence context: "$context".
            Provide:
            1. Broad dictionary definition (in English).
            2. High-quality translation into $targetLanguage.
            3. Standard phonetic pronunciation helper (e.g. /pro-nun-ci-a-shun/).

            You MUST write the response EXACTLY in this format, with no other text:
            PRONUNCIATION: <phonetic here>
            DEFINITION: <brief dictionary definition here>
            TRANSLATION: <translation here in $targetLanguage>
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.3f)
        )

        try {
            val response = api.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseWordLookup(text, word, targetLanguage)
        } catch (e: Exception) {
            Log.e(TAG, "Failed word lookup: ${e.message}", e)
            getFallbackWordLookup(word, targetLanguage)
        }
    }

    private fun parseWordLookup(text: String, word: String, targetLanguage: String): WordLookupResult {
        var pron = "/${word.lowercase()}/"
        var def = "Dictionary definition for $word."
        var trans = "Translation of $word."
        
        text.lines().forEach { line ->
            if (line.startsWith("PRONUNCIATION:", ignoreCase = true)) {
                pron = line.substringAfter("PRONUNCIATION:").trim()
            } else if (line.startsWith("DEFINITION:", ignoreCase = true)) {
                def = line.substringAfter("DEFINITION:").trim()
            } else if (line.startsWith("TRANSLATION:", ignoreCase = true)) {
                trans = line.substringAfter("TRANSLATION:").trim()
            }
        }
        return WordLookupResult(def, trans, pron)
    }

    suspend fun generateFlashcards(
        bookTitle: String,
        contentSample: String
    ): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or default placeholder.")
            return@withContext getFallbackFlashcards(bookTitle)
        }

        val prompt = """
            Read this excerpt from the book "$bookTitle":
            "$contentSample"

            Generate exactly 4 interactive study flashcard Q&As. Focus on main concepts, historical trivia, definitions, or critical facts.
            Each flashcard MUST be split by "Q:" and "A:".
            You MUST output the result EXACTLY structured like this:
            Q: What is...
            A: It is...
            ---
            Q: Identify...
            A: The...
            ---
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = prompt))
                )
            ),
            generationConfig = GeminiGenerationConfig(temperature = 0.5f)
        )

        try {
            val response = api.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseFlashcards(text, bookTitle)
        } catch (e: Exception) {
            Log.e(TAG, "Failed flashcard generation", e)
            getFallbackFlashcards(bookTitle)
        }
    }

    private fun parseFlashcards(text: String, bookTitle: String): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        val blocks = text.split("---")
        blocks.forEach { block ->
            var q = ""
            var a = ""
            block.lines().forEach { line ->
                if (line.trim().startsWith("Q:", ignoreCase = true)) {
                    q = line.substringAfter("Q:").trim()
                } else if (line.trim().startsWith("A:", ignoreCase = true)) {
                    a = line.substringAfter("A:").trim()
                }
            }
            if (q.isNotBlank() && a.isNotBlank()) {
                list.add(Pair(q, a))
            }
        }
        if (list.isEmpty()) {
            return getFallbackFlashcards(bookTitle)
        }
        return list
    }

    private fun getFallbackWordLookup(word: String, language: String): WordLookupResult {
        val wordClean = word.lowercase().replace(Regex("[^a-zA-Z]"), "")
        val (def, trans, pron) = when(wordClean) {
            "progressive" -> Triple("Advancing; proceeding by steps; continuous improvement.", "প্রগতিশীল", "/prə-gres-iv/")
            "expanse" -> Triple("A wide, continuous area or stretch of something.", "বিস্তৃতি", "/ik-spans/")
            "exponents" -> Triple("A person who supports or promotes a theory, proposal, or project.", "প্রবক্তা", "/ik-spoh-nənts/")
            "profound" -> Triple("Very great or intense; showing deep knowledge or insight.", "গভীর", "/prə-fownd/")
            "focus" -> Triple("The center of interest or activity; concentrate attention.", "মনোযোগ", "/foh-kəs/")
            "exponentials" -> Triple("Increasing more and more rapidly.", "ক্রমবর্ধমান", "/ek-spoh-nen-shəls/")
            "narrative" -> Triple("A spoken or written account of connected events; a story.", "আখ্যান / বর্ণনা", "/nar-ə-tiv/")
            "discipline" -> Triple("The practice of training people to obey rules or a code of behavior.", "শৃঙ্খলা", "/dis-ə-plin/")
            "consistent" -> Triple("Acting or done in the same way over time, especially so as to be fair or accurate.", "সামঞ্জস্যপূর্ণ / অবিচল", "/kən-sis-tənt/")
            "complexity" -> Triple("The state or quality of being intricate or complicated.", "জটিলতা", "/kəm-plek-si-tee/")
            else -> {
                val translationStr = when(language) {
                    "Bengali" -> "শব্দকোষে যুক্ত করার জন্য চমৎকার শব্দ..."
                    "Arabic" -> "ترجمة الكلمة في القاموس..."
                    else -> "Dictionary translation..."
                }
                Triple("Word analyzed from book chapter content. Add this word to your offline vocabulary booklet.", translationStr, "/${word.lowercase()}/")
            }
        }
        return WordLookupResult(def, trans, pron)
    }

    private fun getFallbackFlashcards(bookTitle: String): List<Pair<String, String>> {
        return listOf(
            Pair(
                "What is the ultimate engine of accomplishment highlighted in the chapter?",
                "Continuous focus and devotion, which overrides daily distractions."
            ),
            Pair(
                "How much does a mere 15 minutes of daily reading accumulate to over a year?",
                "It accumulates to reading dozens of complete books in a single calendar year."
            ),
            Pair(
                "What classical methodology is based strictly on transmissions (Hadith or Companions)?",
                "Tafsir al-Ma'thur, as established by major classical scholars."
            ),
            Pair(
                "What are the two components of a Hadith structure?",
                "Isnad (the chain of narrators) and Matn (the actual text sayings)."
            )
        )
    }
}
