package com.platform.testing.report.infrastructure.elasticsearch

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Low-level async HTTP client for Elasticsearch.
 * Uses Ktor HttpClient — fully non-blocking via coroutines.
 */
class ElasticsearchClient(host: String = "localhost", port: Int = 9200, scheme: String = "http") {
    private val baseUrl = "$scheme://$host:$port"

    private val http = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
        }
        install(Logging) {
            level = LogLevel.NONE
        }
        engine {
            maxConnectionsCount = 100
            endpoint {
                maxConnectionsPerRoute = 20
                keepAliveTime = 5000
                connectTimeout = 5000
            }
        }
    }

    suspend fun index(index: String, id: String, document: String): HttpResponse =
        http.put("$baseUrl/$index/_doc/$id") {
            contentType(ContentType.Application.Json)
            setBody(document)
        }

    suspend fun get(index: String, id: String): HttpResponse =
        http.get("$baseUrl/$index/_doc/$id")

    suspend fun search(index: String, query: String): HttpResponse =
        http.post("$baseUrl/$index/_search") {
            contentType(ContentType.Application.Json)
            setBody(query)
        }

    suspend fun createIndex(index: String, mappings: String): HttpResponse =
        http.put("$baseUrl/$index") {
            contentType(ContentType.Application.Json)
            setBody(mappings)
        }

    suspend fun indexExists(index: String): Boolean {
        val resp = http.head("$baseUrl/$index")
        return resp.status == HttpStatusCode.OK
    }

    fun close() = http.close()
}
