package com.sproutigy.commons.jsonright.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

@JsonSerialize(using=ClassedJSON.MySerializer.class)
@JsonDeserialize(using=ClassedJSON.MyDeserializer.class)
public final class ClassedJSON {
    private Object value;

    public ClassedJSON() { }

    public ClassedJSON(Object value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public <T> T get() {
        return (T)value;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        return (T)value;
    }

    public void set(Object value) {
        this.value = value;
    }

    public JSON asJSON() {
        return JSON.serialize(this);
    }

    public boolean isEmpty() {
        return value == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassedJSON that = (ClassedJSON) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        if (value != null) {
            return value.toString();
        }
        return "null";
    }

    public static JSON serialize(Object o) {
        return new ClassedJSON(o).asJSON();
    }

    public static <T> T deserialize(String json) {
        return deserialize(new JSON(json));
    }

    public static <T> T deserialize(JSON json) {
        return json.deserialize(ClassedJSON.class).get();
    }

    public static String fetchClassName(String json) {
        return fetchClassName(JSON.fromString(json));
    }

    public static String fetchClassName(JSON json) {
        return json.nodeObject().fields().next().getKey();
    }

    static class MySerializer extends StdSerializer<ClassedJSON> {

        public MySerializer() {
            this(null);
        }

        public MySerializer(Class<ClassedJSON> t) {
            super(t);
        }

        @Override
        public void serialize(ClassedJSON classedJSON, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            Object value = classedJSON.get();
            jsonGenerator.writeStartObject();
            if (value != null) {
                jsonGenerator.writeFieldName(value.getClass().getName());
                jsonGenerator.writeObject(value);
            }
            jsonGenerator.writeEndObject();
        }
    }

    static class MyDeserializer extends StdDeserializer<ClassedJSON> {

        public MyDeserializer() {
            this(null);
        }

        public MyDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ClassedJSON deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            ClassedJSON ret = new ClassedJSON();

            JsonToken currentToken;
            while ((currentToken = jsonParser.nextValue()) != null) {
                if (currentToken == START_OBJECT) {
                    String className = jsonParser.getCurrentName();
                    try {
                        Object val = jsonParser.readValueAs(Class.forName(className));
                        ret.set(val);
                    } catch (ClassNotFoundException e) {
                        throw new IOException("Class not found: " + className, e);
                    }
                }
                if (currentToken == END_OBJECT) {
                    return ret;
                }
            }

            return ret;
        }
    }
}
