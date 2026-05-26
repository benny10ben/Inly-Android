package com.ben.inly.sync

import com.ben.inly.data.local.prefs.SettingsManager
import com.ben.inly.data.local.prefs.SyncConstants
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import com.ben.inly.domain.sync.SyncEnvelope
import com.ben.inly.domain.sync.SyncPayload
import com.ben.inly.domain.sync.SyncRepository
import io.ktor.server.auth.*

import kotlinx.serialization.json.Json // Make sure this is imported

fun startSyncServer(settingsManager: SettingsManager, syncRepository: SyncRepository) {
    val port = settingsManager.getSyncPort().let { if (it <= 0) SyncConstants.DEFAULT_PORT else it }

    embeddedServer(Netty, host = "0.0.0.0", port = port) {
        // 1. Add ignoreUnknownKeys to prevent future crashes if models change
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(Authentication) {
            bearer(SyncConstants.AUTH_REALM) {
                authenticate { tokenCredential ->
                    if (tokenCredential.token == settingsManager.getSyncAuthToken()) {
                        UserIdPrincipal("authorized-mobile")
                    } else null
                }
            }
        }

        routing {
            authenticate(SyncConstants.AUTH_REALM) {

                get(SyncConstants.ROUTE_FETCH) {
                    val changes = syncRepository.collectLocalChanges()
                    // 2. Wrap the outgoing list in the Payload class
                    call.respond(SyncPayload(changes))
                }

                post(SyncConstants.ROUTE_PUSH) {
                    // 3. Expect the Payload class coming in
                    val payload = call.receive<SyncPayload>()
                    syncRepository.applyRemoteChanges(payload.changes)
                    call.respond(io.ktor.http.HttpStatusCode.OK)
                }
            }
        }
    }.start(wait = false)
}