package com.valdifly.infrastructure.web.vertx;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.valdifly.infrastructure.logging.LogFields;
import com.valdifly.infrastructure.logging.api.ApiAccessLogger;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
class ApiLoggingHandlerTest {

    private ListAppender<ILoggingEvent> events;
    private HttpServer server;
    private int port;

    @BeforeEach
    void startServer(Vertx vertx) throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(ApiAccessLogger.class);
        events = new ListAppender<>();
        events.start();
        logger.addAppender(events);

        var router = Router.router(vertx);
        router.route().handler(ApiLoggingHandler.create(Set.of("/health")));
        router.get("/info").handler(context -> context.response().setStatusCode(200).end("ok"));
        router.get("/health").handler(context -> context.response().setStatusCode(200).end("up"));

        server = vertx.createHttpServer().requestHandler(router).listen(0)
                .toCompletionStage().toCompletableFuture().join();
        port = server.actualPort();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.close().toCompletionStage().toCompletableFuture().join();
        }
        Logger logger = (Logger) LoggerFactory.getLogger(ApiAccessLogger.class);
        logger.detachAppender(events);
        MDC.clear();
    }

    @Test
    void emitsOneStructuredLinePerRequest() throws Exception {
        var response = get("/info", "X-Correlation-Id", "corr-9");

        assertEquals(200, response.statusCode());
        assertEquals("ok", response.body());
        awaitEvents(1);
        var mdc = events.list.get(0).getMDCPropertyMap();
        assertEquals("GET", mdc.get(LogFields.HTTP_METHOD));
        assertEquals("/info", mdc.get(LogFields.HTTP_PATH));
        assertEquals("200", mdc.get(LogFields.HTTP_STATUS));
        assertEquals("corr-9", mdc.get(LogFields.CORRELATION_ID));
        assertEquals("127.0.0.1", mdc.get(LogFields.CLIENT_IP));
        assertNull(MDC.get(LogFields.HTTP_METHOD), "nothing may linger on the MDC after emit");
    }

    @Test
    void generatesCorrelationIdWhenNoHeader() throws Exception {
        get("/info");

        awaitEvents(1);
        String correlationId = events.list.get(0).getMDCPropertyMap().get(LogFields.CORRELATION_ID);
        assertTrue(correlationId != null && !correlationId.isBlank(), "a correlation id is always present");
    }

    @Test
    void skipsExcludedPaths() throws Exception {
        get("/health");

        awaitEvents(0);
        assertTrue(events.list.isEmpty());
    }

    /** The end handler fires server-side and races the client reading the response — poll briefly. */
    private void awaitEvents(int expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3_000;
        while (events.list.size() < expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        if (expected > 0) {
            assertEquals(expected, events.list.size(), "access-log event did not arrive within 3s");
        }
    }

    private HttpResponse<String> get(String path, String... headers) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET();
        if (headers.length > 0) {
            builder = builder.headers(headers);
        }
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
