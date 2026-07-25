package com.platform.testing.report.interfaces.plugin

import com.platform.testing.report.application.LiveExecutionTracker
import com.platform.testing.report.application.ReportAggregationService
import com.platform.testing.report.application.TrendAnalysisService
import com.platform.testing.report.interfaces.routes.reportRoutes
import com.platform.testing.report.interfaces.routes.sseRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    reportService: ReportAggregationService,
    trendService: TrendAnalysisService,
    liveTracker: LiveExecutionTracker
) {
    routing {
        reportRoutes(reportService, trendService)
        sseRoutes(liveTracker)
    }
}