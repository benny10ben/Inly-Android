package com.ben.inly.domain.util

import com.joestelmach.natty.Parser
import java.util.Date

class AndroidTaskExtractor : TaskExtractor {

    private val triggerPhrases = listOf(
        "remind me to ",
        "don't forget to ",
        "i need to ",
        "please remind me to ",
        "add a reminder to ",
        "remind me ",
        "please add a reminder to "
    )

    override fun extractTaskAndDate(transcript: String): TaskExtractionResult {
        if (transcript.isBlank()) return TaskExtractionResult("", null)

        var cleanText = transcript.trim()
        var extractedDate: Date? = null
        var matchedDateText = ""

        try {
            val parser = Parser()
            val groups = parser.parse(cleanText)
            if (groups.isNotEmpty()) {
                val group = groups[0]
                if (group.dates.isNotEmpty()) {
                    extractedDate = group.dates[0]
                    matchedDateText = group.text
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (matchedDateText.isNotEmpty()) {
            cleanText = cleanText.replace(matchedDateText, "", ignoreCase = true)
        }

        for (phrase in triggerPhrases) {
            if (cleanText.startsWith(phrase, ignoreCase = true)) {
                cleanText = cleanText.substring(phrase.length)
                break
            }
        }

        cleanText = cleanText.trim()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("^[^a-zA-Z0-9]+"), "")
            .replace(Regex("(?i)^(in|on|at|by)\\s+"), "")

        if (cleanText.isNotEmpty()) {
            cleanText = cleanText.replaceFirstChar { it.uppercase() }
        } else {
            cleanText = "Voice reminder"
        }

        return TaskExtractionResult(
            taskText = cleanText,
            timestamp = extractedDate?.time
        )
    }
}