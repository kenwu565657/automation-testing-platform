package com.platform.testing.admin.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.testing.domain.pageobject.PageObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageObjectCacheWriterTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PageObjectCacheWriter cacheWriter;

    @BeforeEach
    void setUp() {
        // We only mock operations if they are actually called. 
        // lenient().when(redis.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void writeToCache_SuccessfullyCachesPageObject() throws Exception {
        // Arrange
        UUID pageObjectId = UUID.randomUUID();
        PageObject mockPageObject = mock(PageObject.class);
        when(mockPageObject.getId()).thenReturn(pageObjectId);
        when(mockPageObject.getName()).thenReturn("LoginPage");
        
        Map<String, Object> locatorMap = Map.of("username", Map.of("strategy", "CSS", "value", "#user"));
        when(mockPageObject.toLocatorMap()).thenReturn(locatorMap);

        String jsonValue = "{\"username\":{\"strategy\":\"CSS\",\"value\":\"#user\"}}";
        when(objectMapper.writeValueAsString(locatorMap)).thenReturn(jsonValue);

        when(redis.opsForValue()).thenReturn(valueOperations);

        // Act
        cacheWriter.writeToCache(mockPageObject);

        // Assert
        verify(redis.opsForValue()).set(eq("pageobject:" + pageObjectId), eq(jsonValue), eq(Duration.ofHours(1)));
    }

    @Test
    void writeToCache_HandlesExceptionGracefully() throws Exception {
        // Arrange
        UUID pageObjectId = UUID.randomUUID();
        PageObject mockPageObject = mock(PageObject.class);
        when(mockPageObject.getId()).thenReturn(pageObjectId);
        when(mockPageObject.toLocatorMap()).thenReturn(Map.of());

        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Test Exception") {});

        // Act
        cacheWriter.writeToCache(mockPageObject);

        // Assert
        // Verify that redis template was never called since exception happened during serialization
        verify(redis, never()).opsForValue();
    }

    @Test
    void removeFromCache_DeletesKeyFromRedis() {
        // Arrange
        String pageObjectId = UUID.randomUUID().toString();

        // Act
        cacheWriter.removeFromCache(pageObjectId);

        // Assert
        verify(redis).delete("pageobject:" + pageObjectId);
    }
}

