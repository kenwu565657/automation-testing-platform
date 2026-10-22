plugins {
    java
    `java-library`
    `java-test-fixtures`
}

dependencies {
    api(project(":shared-domain"))

    // Jackson (runtime — this module provides actual serializers)
    api(libs.jackson.databind)
    api(libs.jackson.annotations)
    api(libs.jackson.datatype.jsr310)

    // Kafka client (for serializer/deserializer classes)
    compileOnly(libs.kafka.clients)
    testImplementation(libs.kafka.clients)

    // Spring MVC (for the shared base exception handler and API logging filter —
    // Spring services wire them in; compileOnly so Vert.x/Ktor consumers never see them)
    compileOnly(libs.spring.web)
    compileOnly(libs.spring.webmvc)
    compileOnly(libs.jakarta.servlet.api)

    // Vert.x (for the API logging route handler) + OTel (opportunistic traceId read)
    compileOnly(libs.vertx.core)
    compileOnly(libs.vertx.web)
    compileOnly(libs.opentelemetry.api)

    // Logging — api so consumers also get the JSON encoder backing the shared
    // logback-base.xml include (see src/main/resources/com/valdifly/infrastructure/logging/)
    implementation(libs.slf4j.api)
    api(libs.logstash.logback.encoder)

    // Architecture rules shared with service test suites (ArchUnit)
    testFixturesApi(libs.archunit.junit5)

    // Unit testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.logback.classic) // loads logback-base.xml through Joran in LogbackBaseConfigTest
    testImplementation(libs.spring.web)
    testImplementation(libs.spring.webmvc)
    testImplementation(libs.spring.test) // MockHttpServletRequest/Response for ApiLoggingFilterTest
    testImplementation(libs.jakarta.servlet.api)
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.vertx.web)
    testImplementation(libs.vertx.junit5)
    testImplementation(libs.opentelemetry.api)
}