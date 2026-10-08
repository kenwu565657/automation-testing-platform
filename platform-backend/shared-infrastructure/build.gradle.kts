plugins {
    java
    `java-library`
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

    // Logging — api so consumers also get the JSON encoder backing the shared
    // logback-base.xml include (see src/main/resources/com/valdifly/infrastructure/logging/)
    implementation(libs.slf4j.api)
    api(libs.logstash.logback.encoder)

    // Unit testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.logback.classic) // loads logback-base.xml through Joran in LogbackBaseConfigTest
}