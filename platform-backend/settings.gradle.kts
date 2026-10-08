rootProject.name = "platform-backend"

// ── Shared modules ──
include("shared-domain")
include("shared-infrastructure")
include("shared-utils")

// ── Services ──
include("gateway-service")
include("admin-service")
include("engine-service")
project(":engine-service").projectDir = file("../engines/java")
include("engine-coordinator-service")
include("report-service")
