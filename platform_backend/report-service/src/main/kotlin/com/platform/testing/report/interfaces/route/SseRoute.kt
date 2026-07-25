package com.platform.testing.report.interfaces.route

import com.platform.testing.report.application.LiveExecutionTracker
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*

/**
 * SSE endpoint for live execution tracking.
 *
 * Frontend connects to:
 *   GET /api/v1/executions/{executionId}/live
 *
 * Receives a stream of ServerSentEvent with JSON data:
 *   data: {"executionId":"...","status":"RUNNING","currentStepIndex":3,...}
 *
 * Stream ends when execution reaches PASSED, FAILED, or ABORTED.
 */
fun Routing.sseRoutes(liveTracker: LiveExecutionTracker) {

    route("/api/v1/executions") {

        sse("/{executionId}/live") {
            val executionId = call.parameters["executionId"]
            if (executionId == null) {
                send(ServerSentEvent(data = """{"error":"Missing executionId"}""", event = "error"))
                return@sse
            }

            val intervalMs = call.request.queryParameters["interval"]?.toLongOrNull() ?: 1000

            liveTracker.track(executionId, intervalMs).collect { stateJson ->
                send(ServerSentEvent(data = stateJson, event = "execution-state"))
            }

            // Send a final "complete" event so the client knows the stream is done
            send(ServerSentEvent(data = """{"executionId":"$executionId","done":true}""", event = "complete"))
        }
    }
}
