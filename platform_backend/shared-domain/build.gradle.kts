plugins {
    java
    `java-library`
}

dependencies {
    implementation(project(":shared-utils"))
    testImplementation(libs.junit.jupiter)
}
