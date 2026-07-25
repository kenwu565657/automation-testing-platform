package com.platform.testing.report.interfaces.route

import com.platform.testing.report.application.ReportAggregationService
import com.platform.testing.report.application.TrendAnalysisService
import com.platform.testing.report.domain.repository.ReportSearchQuery
import com.platform.testing.report.domain.repository.TimeRange
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class InfoResponse(val service: String, val version: String, val status: String)

fun Routing.reportRoutes(reportService: ReportAggregationService, trendService: TrendAnalysisService) {

    get("/info") {
        call.respond(InfoResponse("report-service", "0.1.0", "running"))
    }

    route("/api/v1/reports") {

        // GET /api/v1/reports/{executionId}
        get("/{executionId}") {
            val id = call.parameters["executionId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing executionId")

            val report = reportService.getReport(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Report not found: $id")

            call.respond(report)
        }

        // GET /api/v1/reports/test-case/{testCaseId}
        get("/test-case/{testCaseId}") {
            val id = call.parameters["testCaseId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing testCaseId")
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

            val reports = reportService.getReportsForTestCase(id, limit)
            call.respond(reports)
        }

        // GET /api/v1/reports/search?testCaseName=...&status=...&testType=...&platform=...&from=...&to=...&page=...&size=...
        get("/search") {
            val params = call.request.queryParameters
            val query = ReportSearchQuery(
                testCaseName = params["testCaseName"],
                testType = params["testType"],
                status = params["status"],
                platform = params["platform"],
                tags = params["tags"]?.split(",") ?: emptyList(),
                timeRange = if (params["from"] != null && params["to"] != null) {
                    TimeRange(params["from"]!!, params["to"]!!)
                } else null,
                page = params["page"]?.toIntOrNull() ?: 0,
                size = params["size"]?.toIntOrNull() ?: 20
            )

            val result = reportService.search(query)
            call.respond(result)
        }
    }

    route("/api/v1/summary") {

        // GET /api/v1/summary?from=...&to=...
        get {
            val from = call.request.queryParameters["from"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing 'from' parameter")
            val to = call.request.queryParameters["to"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing 'to' parameter")

            val summary = reportService.getSummary(from, to)
            call.respond(summary)
        }
    }

    route("/api/v1/trends") {

        // GET /api/v1/trends/{testCaseId}?days=30
        get("/{testCaseId}") {
            val id = call.parameters["testCaseId"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing testCaseId")
            val days = call.request.queryParameters["days"]?.toIntOrNull() ?: 30

            val trend = trendService.getTrend(id, days)
            call.respond(trend)
        }
    }

    route("/api/v1/flaky-tests") {

        // GET /api/v1/flaky-tests?minExecutions=5&maxPassRate=0.8
        get {
            val minExec = call.request.queryParameters["minExecutions"]?.toIntOrNull() ?: 5
            val maxPass = call.request.queryParameters["maxPassRate"]?.toDoubleOrNull() ?: 0.8

            val flakyTests = trendService.getFlakyTests(minExec, maxPass)
            call.respond(flakyTests)
        }
    }
}