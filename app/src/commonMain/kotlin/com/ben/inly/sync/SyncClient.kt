package com.ben.inly.sync

import com.ben.inly.data.local.prefs.SyncConstants
import com.ben.inly.domain.sync.SyncEnvelope
import com.ben.inly.domain.sync.SyncPayload
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// Make sure you import your new SyncPayload class here!
// import com.ben.inly.domain.sync.SyncPayload

class SyncClient(
    private val serverUrl: String,
    private val authToken: String
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            // Adding ignoreUnknownKeys here is a good safety net for mobile clients
            json(Json { ignoreUnknownKeys = true })
        }
        install(Auth) {
            bearer {
                loadTokens { BearerTokens(authToken, "") }
            }
        }
    }

    suspend fun pushChanges(changes: List<SyncEnvelope>) {
        if (changes.isEmpty()) return

        client.post("$serverUrl${SyncConstants.ROUTE_PUSH}") {
            contentType(ContentType.Application.Json)
            // THE FIX: Wrap the list in the payload object
            setBody(SyncPayload(changes))
        }
    }

    suspend fun fetchChanges(): List<SyncEnvelope> {
        // THE FIX: Expect the payload object from the server, then extract the list
        val response = client.get("$serverUrl${SyncConstants.ROUTE_FETCH}")
        val payload: SyncPayload = response.body()
        return payload.changes
    }
}