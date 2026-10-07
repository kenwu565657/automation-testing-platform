package com.valdifly.infrastructure.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.status.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads the shipped logback-base.xml exactly the way a service does — a wrapper
 * config that sets SERVICE_NAME and includes the resource — so a bad include
 * path or an unresolvable encoder class fails here, not at first boot.
 */
class LogbackBaseConfigTest {

    @TempDir
    Path tempDir;

    @Test
    void configuresCleanlyBehindAServiceWrapper() throws Exception {
        Path wrapper = Files.writeString(tempDir.resolve("logback.xml"), """
                <configuration>
                    <property name="SERVICE_NAME" value="config-test"/>
                    <include resource="com/valdifly/infrastructure/logging/logback-base.xml"/>
                </configuration>
                """);

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(wrapper.toFile());

        assertTrue(context.getStatusManager().getCopyOfStatusList().stream()
                        .noneMatch(status -> status.getLevel() == Status.ERROR),
                () -> "logback-base.xml produced status errors: " + copyOfStatuses(context));
        assertNotNull(context.getLogger(Logger.ROOT_LOGGER_NAME).getAppender("JSON_CONSOLE"),
                "root logger must have the JSON console appender attached");
    }

    private static String copyOfStatuses(LoggerContext context) {
        StringBuilder sb = new StringBuilder();
        for (Status status : context.getStatusManager().getCopyOfStatusList()) {
            sb.append(status.getLevel()).append(' ').append(status.getMessage()).append("; ");
        }
        return sb.toString();
    }
}
