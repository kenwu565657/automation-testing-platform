import groovy.json.JsonOutput
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

plugins {
    java
    idea
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.shadow) apply false
}

tasks.register("coverageSummary") {
    group = "verification"
    description = "Runs JVM tests and prints per-module JaCoCo coverage."
    val modules = subprojects.sortedBy { it.path }
    dependsOn(modules.map { "${it.path}:jacocoTestReport" })

    doLast {
        val parser = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "")
            setAttribute("http://javax.xml.XMLConstants/property/accessExternalSchema", "")
        }.newDocumentBuilder()

        println("\nJVM test coverage (JaCoCo)")
        println("%-30s %10s %10s  %s".format("Module", "Lines", "Branches", "Report"))
        val results = mutableListOf<Map<String, Any?>>()
        for (module in modules) {
            val test = module.tasks.named<Test>("test").get()
            val report = module.tasks.named<JacocoReport>("jacocoTestReport").get()
            if (test.state.skipMessage == "NO-SOURCE") {
                println("%-30s %10s %10s  %s".format(module.path, "N/A", "N/A", "No test sources"))
                results.add(mapOf("id" to module.name, "lines" to null, "branches" to null, "reason" to "No test sources"))
                continue
            }
            if (report.state.skipMessage == "SKIPPED") {
                println("%-30s %10s %10s  %s".format(module.path, "N/A", "N/A", "No execution data"))
                results.add(mapOf("id" to module.name, "lines" to null, "branches" to null, "reason" to "No execution data"))
                continue
            }
            val xml = report.reports.xml.outputLocation.get().asFile
            check(xml.isFile) { "Missing JaCoCo report for ${module.path}: $xml" }
            val root = parser.parse(xml).documentElement
            check(root.tagName == "report") { "Invalid JaCoCo report: $xml" }
            fun percentage(type: String): String {
                val nodes = root.childNodes
                val counters = (0 until nodes.length).asSequence()
                    .map { nodes.item(it) }
                    .filterIsInstance<Element>()
                    .filter { it.tagName == "counter" && it.getAttribute("type") == type }
                    .toList()
                check(counters.size <= 1) { "Duplicate $type counters in $xml" }
                val counter = counters.singleOrNull() ?: return "N/A"
                val covered = counter.getAttribute("covered").toLong()
                val missed = counter.getAttribute("missed").toLong()
                check(covered >= 0 && missed >= 0) { "Invalid $type counter in $xml" }
                val total = covered + missed
                return if (total == 0L) "N/A" else String.format(Locale.ROOT, "%.1f%%", covered * 100.0 / total)
            }
            val html = report.reports.html.outputLocation.get().asFile.resolve("index.html")
            val line = percentage("LINE")
            val branch = percentage("BRANCH")
            println("%-30s %10s %10s  %s".format(module.path, line, branch, rootProject.relativePath(html)))
            results.add(mapOf(
                "id" to module.name,
                "lines" to line.removeSuffix("%").toDoubleOrNull(),
                "branches" to branch.removeSuffix("%").toDoubleOrNull(),
                "report" to rootProject.relativePath(html)
            ))
        }
        val output = rootProject.layout.buildDirectory.file("reports/coverage/jvm.json").get().asFile
        output.parentFile.mkdirs()
        output.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(results)) + "\n")
        println("N/A means no measurable data; percentages are informational, not thresholds.")
    }
}

allprojects {
    group = "com.valdifly"
    version = "0.1.0-SNAPSHOT"
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")

    val libs = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
    configure<JacocoPluginExtension> {
        toolVersion = libs.findVersion("jacoco").get().requiredVersion
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-parameters"))
    }

    tasks.withType<Test> { 
        useJUnitPlatform() 
        finalizedBy("jacocoTestReport")
    }

    tasks.withType<JacocoReport> {
        dependsOn(tasks.withType<Test>())
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
