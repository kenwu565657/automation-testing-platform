package com.valdifly.infrastructure.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.valdifly.domain.common.ValueObject;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Writes identity value objects as their inner string so Kafka JSON stays flat.
 */
public final class ValueObjectModule extends SimpleModule {

    public ValueObjectModule() {
        super("ValueObjectModule");
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addSerializers(new Serializers.Base() {
            @Override
            public JsonSerializer<?> findSerializer(
                    SerializationConfig config, JavaType type, BeanDescription beanDesc) {
                if (isConcreteValueObject(type)) {
                    return VALUE_SERIALIZER;
                }
                return null;
            }
        });
        context.addDeserializers(new Deserializers.Base() {
            @Override
            public JsonDeserializer<?> findBeanDeserializer(
                    JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
                if (isConcreteValueObject(type)) {
                    return new ValueObjectDeserializer(type.getRawClass());
                }
                return null;
            }
        });
    }

    private static boolean isConcreteValueObject(JavaType type) {
        Class<?> raw = type.getRawClass();
        return ValueObject.class.isAssignableFrom(raw) && !raw.isInterface();
    }

    private static final JsonSerializer<ValueObject<?>> VALUE_SERIALIZER = new JsonSerializer<>() {
        @Override
        public void serialize(ValueObject<?> value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            Object inner = value.value();
            if (inner == null) {
                gen.writeNull();
            } else if (inner instanceof String text) {
                gen.writeString(text);
            } else {
                serializers.defaultSerializeValue(inner, gen);
            }
        }
    };

    private static final class ValueObjectDeserializer extends JsonDeserializer<ValueObject<?>> {
        private final Class<?> type;

        private ValueObjectDeserializer(Class<?> type) {
            this.type = type;
        }

        @Override
        public ValueObject<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String text = parser.getValueAsString();
            if (text == null) {
                return null;
            }
            try {
                Method factory = type.getMethod("of", String.class);
                return (ValueObject<?>) factory.invoke(null, text);
            } catch (ReflectiveOperationException ex) {
                throw context.weirdStringException(text, type, "no of(String) factory");
            }
        }
    }
}
