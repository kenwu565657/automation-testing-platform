plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    // ── Shared ──
    implementation(project(":shared-domain"))
    implementation(project(":shared-utils"))
    implementation(project(":shared-infrastructure"))

    // ── Spring Boot ──
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.micrometer.tracing.bridge.otel)
    implementation(libs.opentelemetry.exporter.otlp.bom)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.spring.boot.starter.data.redis)
    implementation(libs.spring.kafka)
    implementation(libs.aws.s3)
    implementation(libs.aws.apache.client)
    implementation(libs.elasticjob.lite.core)
    implementation(libs.elasticjob.simple.executor)
    implementation(libs.j2objc.annotations)

    constraints {
        implementation(libs.j2objc.annotations)
    }

    // ── Database ──
    runtimeOnly(libs.postgresql)
    implementation(libs.bundles.flyway)

    // ── Utilities ──
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // ── Testing ──
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.jjwt.impl)
    testImplementation(libs.jjwt.jackson)
    testImplementation(libs.opentelemetry.sdk.bom)
    testRuntimeOnly(libs.h2)
}