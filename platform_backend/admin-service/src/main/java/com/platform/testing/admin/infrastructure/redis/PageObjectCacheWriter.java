package com.platform.testing.admin.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.testing.domain.pageobject.PageObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Writes page-object locator data to Redis.
 * The Engine Service reads this during test execution.
 *
 * Key:   pageobject:{id}
 * Value: { "elementName": { "strategy": "CSS", "value": "#user" }, ... }
 * TTL:   1 hour
 */
@Component
public class PageObjectCacheWriter {

    private static
    final Logger log = LoggerFactory.getLogger(PageObjectCacheWriter.class);
    private static final String KEY_PREFIX = "pageobject:";
    private static final Duration TTL = Duration.ofHours(1);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public PageObjectCacheWriter(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public void writeToCache(PageObject pageObject) {
        try {
            var key = KEY_PREFIX + pageObject.getId().toString();
            var value = objectMapper.writeValueAsString(pageObject.toLocatorMap());
            redis.opsForValue().set(key, value, TTL);
            log.debug("Cached page object: {} ({})", pageObject.getName(), key);
        } catch (Exception e) {
            log.warn("Failed to cache page object: {}", pageObject.getId(), e);
        }
    }

    public void removeFromCache(String pageObjectId) {
        redis.delete(KEY_PREFIX + pageObjectId);
    }
}
