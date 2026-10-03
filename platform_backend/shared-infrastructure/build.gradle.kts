plugins {
    java
    `java-library`
}

dependencies {
    api(project(":shared-domain"))
    api(project(":shared-utils"))

    // Jackson (runtime — this module provides actual serializers)
    api(libs.jackson.databind)
    api(libs.jackson.annotations)
    api(libs.jackson.datatype.jsr310)

    // Kafka client (for serializer/deserializer classes)
    compileOnly(libs.kafka.clients)
    testImplementation(libs.kafka.clients)

    // Flyway (for migration helper)
    compileOnly(libs.flyway.core)
    compileOnly(libs.flyway.postgresql)

    // PostgresSQL driver
    compileOnly(libs.postgresql)

    // Logging
    implementation(libs.slf4j.api)

    implementation(libs.hibernate.core)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Unit testing
    testImplementation(libs.junit.jupiter)
}