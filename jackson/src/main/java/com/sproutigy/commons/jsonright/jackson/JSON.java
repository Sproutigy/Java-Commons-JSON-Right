package com.sproutigy.commons.jsonright.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;


@JsonSerialize(using = JSON.Serializer.class)
@JsonDeserialize(using = JSON.Deserializer.class)
public final class JSON implements Cloneable {

    public static final String MIME_TYPE = "application/json";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);
    public static final String CONTENT_TYPE_WITH_DEFAULT_ENCODING = MIME_TYPE + "; charset="+DEFAULT_ENCODING.toLowerCase();

    private static ObjectMapper objectMapper = null;
    private String str;
    private Formatting strFormatting = null;
    private transient JsonNode node;

    public enum Formatting {
        Unknown, Compact, Pretty
    }

    public JSON() {
    }

    public JSON(String jsonString) {
        this(jsonString, Formatting.Unknown);
    }

    public JSON(String jsonString, Formatting formatting) {
        this.str = jsonString.trim();
        this.strFormatting = formatting;
    }

    public JSON(JsonNode jsonNode) {
        this.node = jsonNode;
    }

    public JSON(byte[] data) {
        this(data, 0, data.length);
    }

    public JSON(byte[] data, int offset, int length) {
        if (data.length > 0) {
            Charset charset = null;

            if (data[offset] == 0) {
                if (data.length >= 4) {
                    if (data[offset+1] == 0 && data[offset+2] == 0 && data[offset+3] != 0) {
                        charset = Charset.forName("UTF-32BE");
                    } else {
                        charset = Charset.forName("UTF-16BE");
                    }
                }
            }
            else {
                if (data.length > 1) {
                    if (data[offset+1] == 0) {
                        if (data.length >= 4) {
                            if (data[offset+2] == 0 && data[offset+3] == 0) {
                                charset = Charset.forName("UTF-32LE");
                            } else {
                                charset = Charset.forName("UTF-16LE");
                            }
                        }
                    } else {
                        charset = DEFAULT_CHARSET;
                    }
                }
            }

            if (charset == null) {
                throw new IllegalArgumentException("Invalid JSON data");
            }

            this.str = new String(data, offset, length, charset).trim();
            this.strFormatting = Formatting.Unknown;
        }
    }

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            synchronized (JSON.class) {
                if (objectMapper == null) {
                    objectMapper = new ObjectMapper();
                    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    objectMapper.setDefaultPrettyPrinter(null);
                    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                    objectMapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
                    objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                }
            }
        }
        return objectMapper;
    }

    public static JSON newObject() {
        return new JSON("{}");
    }

    public static JSON newArray() {
        return new JSON("[]");
    }

    public static JSON newNull() {
        return new JSON(NullNode.getInstance());
    }

    public static ObjectNode newObjectNode() {
        return getObjectMapper().createObjectNode();
    }

    public static ArrayNode newArrayNode() {
        return getObjectMapper().createArrayNode();
    }

    public static JsonNode newNullNode() {
        return NullNode.getInstance();
    }

    public static JSON fromString(String jsonString) {
        return new JSON(jsonString);
    }

    public static JSON fromNode(JsonNode jsonNode) {
        return new JSON(jsonNode);
    }

    public static JSON fromBytes(byte[] data) {
        return new JSON(data);
    }

    public static JSON fromBytes(byte[] data, int offset, int length) {
        return new JSON(data, offset, length);
    }

    public static JSON serialize(Object o) {
        if (o == null)
            return newNull();

        return new JSON(serializeToStringCompact(o));
    }

    public static JSON primitive(byte v) {
        return JSON.builder().value(v).build();
    }

    public static JSON primitive(short v) {
        return JSON.builder().value(v).build();
    }

    public static JSON primitive(int v) {
        return JSON.builder().value(v).build();
    }

    public static JSON primitive(long v) {
        return JSON.builder().value(v).build();
    }

    public static JSON primitive(float v) {
        return JSON.builder().value(v).build();
    }

    public static JSON primitive(double v) {
        return JSON.builder().value(v).build();
    }

    public static JSON primitive(BigInteger v) {
        return JSON.builder().value(v).build();
    }

    public static JSON primitive(BigDecimal v) {
        return JSON.builder().value(v).build();
    }

    public static JSON primitive(boolean v) {
        return JSON.builder().value(v).build();
    }

    public static JSON primitive(String v) {
        return JSON.builder().value(v).build();
    }

    public static BuilderRoot builder() {
        return builder(Formatting.Compact);
    }

    public static BuilderRoot builder(Formatting formatting) {
        return new Builder(formatting);
    }

    public static BuilderRoot builderCompact() {
        return builder(Formatting.Compact);
    }

    public static BuilderRoot builderPretty() {
        return builder(Formatting.Pretty);
    }

    public static String serializeToStringCompact(Object o) {
        if (o == null) {
            return null;
        }

        try {
            return getObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serializeToStringPretty(Object o) {
        if (o == null) {
            return null;
        }

        try {
            return getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonGenerator generator(OutputStream out) {
        try {
            return getObjectMapper().getFactory().createGenerator(out, JsonEncoding.UTF8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonGenerator generatorPretty(OutputStream out) {
        return generator(out).setPrettyPrinter(new DefaultPrettyPrinter());
    }

    public static String compactString(String jsonString) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(jsonString);
            return getObjectMapper().writeValueAsString(jsonNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyString(String jsonString) {
        try {
            JsonNode jsonNode = getObjectMapper().readTree(jsonString);
            return getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        if (str != null) {
            return new JSON(str);
        }
        else {
            return new JSON(toStringCompact());
        }
    }

    public JsonNode node() {
        if (node == null) {
            if (str != null && !str.isEmpty()) {
                if (str.equals("{}")) {
                    node = newObjectNode();
                } else if (str.equals("[]")) {
                    node = newArrayNode();
                } else {
                    try {
                        node = getObjectMapper().readTree(str);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                node = newNullNode();
            }
            str = null;
        }
        return node;
    }

    public ObjectNode nodeObject() {
        if (node == null) {
            if (str != null) {
                return (ObjectNode) node();
            } else {
                node = getObjectMapper().createObjectNode();
            }
        }
        return (ObjectNode) node;
    }

    public ArrayNode nodeArray() {
        if (node == null) {
            if (str != null) {
                return (ArrayNode) node();
            } else {
                node = getObjectMapper().createArrayNode();
            }
        }
        return (ArrayNode) node;
    }

    public JSON set(String json) {
        return set(json, Formatting.Unknown);
    }

    public JSON set(String json, Formatting formatting) {
        clear();

        this.str = json;
        this.strFormatting = formatting;
        return this;
    }

    public JSON set(JsonNode json) {
        clear();

        this.node = json;
        return this;
    }

    public JSON set(JSON other) {
        return set(other, true);
    }

    public JSON set(JSON other, boolean deepCopy) {
        clear();

        if (other.str != null) {
            this.str = other.str;
            this.strFormatting = other.strFormatting;
        } else if (other.node != null) {
            if (deepCopy) {
                this.node = other.node.deepCopy();
            } else {
                this.node = other.node;
            }
        }
        return this;
    }

    public String toStringCompact() {
        if (str != null && strFormatting == Formatting.Compact) {
            return str;
        }

        if (node != null) {
            return node.toString();
        } else {
            if (str == null) {
                return "null";
            } else {
                return str;
            }
        }
    }

    public String toStringPretty() {
        if (str != null && strFormatting == Formatting.Pretty) {
            return str;
        }

        try {
            return getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(node());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T deserialize(Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz == null");
        }

        if (isNull()) {
            return null;
        }

        if (str != null) {
            try {
                return getObjectMapper().readValue(str, clazz);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (node != null) {
            try {
                getObjectMapper().readValue(node.traverse(), clazz);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

    public <T> T deserialize(T defaultValue) {
        if (defaultValue == null) {
            throw new NullPointerException("defaultValue == null");
        }
        return deserialize((Class<T>) defaultValue.getClass(), defaultValue);
    }

    public <T> T deserialize(Class<T> clazz, T defaultValue) {
        T o = deserialize(clazz);
        if (o == null) {
            return defaultValue;
        }
        return null;
    }

    public boolean isNull() {
        return (node == null && (str == null || str.isEmpty() || str.equals("null"))) || (node != null && node.isNull());
    }

    public boolean isObject() {
        if (node != null) {
            return node.isObject();
        }
        else {
            return str != null && str.length() > 0 && str.charAt(0) == '{';
        }
    }

    public boolean isArray() {
        if (node != null) {
            return node.isArray();
        }
        else {
            return str != null && str.length() > 0 && str.charAt(0) == '[';
        }
    }

    public boolean isPrimitive() {
        if (node != null) {
            return !node.isArray() && !node.isObject() && !node.isNull();
        }
        else {
            if (str != null && str.length() > 0) {
                if (str.equals("null")) {
                    return false;
                }
                char c = str.charAt(0);
                return c != '{' && c != '[';
            }
            return false;
        }
    }

    public JsonParser parse() throws IOException {
        String json = str;
        if (json == null) {
            json = toStringCompact();
        }
        return getObjectMapper().getFactory().createParser(json);
    }

    public boolean isValid() {
        if (node != null) {
            if (str == null || str.isEmpty()) {
                return false;
            }

            try {
                JsonParser parser = parse();
                while (parser.nextToken() != null) {
                }
            } catch (JsonParseException jpe) {
                return false;
            } catch (IOException ioe) {
                return false;
            }

            return true;
        } else {
            try {
                node();
                return true;
            } catch(Exception e) {
                return false;
            }
        }
    }

    public JSON validate() {
        if (!isValid()) {
           throw new IllegalStateException("Provided JSON is invalid");
        }
        return this;
    }

    public JSON clear() {
        str = null;
        strFormatting = null;
        node = null;
        return this;
    }

    @Override
    public String toString() {
        if (str != null) {
            return str;
        }

        return toStringPretty();
    }

    public String toString(Formatting formatting) {
        if (formatting == null || formatting == Formatting.Unknown) {
            return toString();
        }
        if (formatting == Formatting.Compact) {
            return toStringCompact();
        }
        if (formatting == Formatting.Pretty) {
            return toStringPretty();
        }

        throw new IllegalArgumentException("Unsupported formatting value");
    }


    public static class Serializer extends JsonSerializer<JSON> {
        @Override
        public void serialize(JSON json, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.getCodec().writeTree(jsonGenerator, json.node());
        }
    }

    public static class Deserializer extends JsonDeserializer<JSON> {
        @Override
        public JSON deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
            return new JSON(jsonNode);
        }
    }


    public interface BuilderObject<TParent> {
        BuilderObject<TParent> fieldNull(String fieldName);
        BuilderObject<TParent> field(String fieldName, byte v);
        BuilderObject<TParent> field(String fieldName, short v);
        BuilderObject<TParent> field(String fieldName, int v);
        BuilderObject<TParent> field(String fieldName, long v);
        BuilderObject<TParent> field(String fieldName, float v);
        BuilderObject<TParent> field(String fieldName, double v);
        BuilderObject<TParent> field(String fieldName, BigDecimal v);
        BuilderObject<TParent> field(String fieldName, String v);
        BuilderObject<TParent> field(String fieldName, boolean v);
        BuilderObject<TParent> field(String fieldName, byte[] data);
        BuilderObject<TParent> field(String fieldName, byte[] data, int offset, int length);
        BuilderObject<TParent> field(String fieldName, Object o);
        BuilderObject<TParent> field(String fieldName, TreeNode node);
        BuilderObject<TParent> field(String fieldName, JSON json);
        BuilderObject<BuilderObject<TParent>> startObject(String fieldName);
        BuilderArray<BuilderObject<TParent>> startArray(String fieldName);
        TParent endObject();
    }

    public interface BuilderArray<TParent> {
        BuilderArray<TParent> valueNull();
        BuilderArray<TParent> value(byte v);
        BuilderArray<TParent> value(short v);
        BuilderArray<TParent> value(int v);
        BuilderArray<TParent> value(long v);
        BuilderArray<TParent> value(float v);
        BuilderArray<TParent> value(double v);
        BuilderArray<TParent> value(BigInteger v);
        BuilderArray<TParent> value(BigDecimal v);
        BuilderArray<TParent> value(String v);
        BuilderArray<TParent> value(boolean v);
        BuilderArray<TParent> value(byte[] data);
        BuilderArray<TParent> value(byte[] data, int offset, int length);
        BuilderArray<TParent> value(TreeNode node);
        BuilderArray<TParent> value(JSON json);
        BuilderArray<TParent> value(Object o);
        BuilderObject<BuilderArray<TParent>> startObject();
        BuilderArray<BuilderArray<TParent>> startArray();
        TParent endArray();
    }

    public interface BuilderTerminate {
        JSON build();
    }

    public interface BuilderRoot {
        BuilderObject<BuilderTerminate> startObject();
        BuilderArray<BuilderTerminate> startArray();
        BuilderTerminate valueNull();
        BuilderTerminate value(byte v);
        BuilderTerminate value(short v);
        BuilderTerminate value(int v);
        BuilderTerminate value(long v);
        BuilderTerminate value(float v);
        BuilderTerminate value(double v);
        BuilderTerminate value(BigInteger v);
        BuilderTerminate value(BigDecimal v);
        BuilderTerminate value(String v);
        BuilderTerminate value(boolean v);
        BuilderTerminate value(byte[] data);
        BuilderTerminate value(byte[] data, int offset, int length);
        BuilderTerminate value(TreeNode node);
        BuilderTerminate value(JSON json);
        BuilderTerminate value(Object o);
    }

    public static final class Builder implements BuilderRoot, BuilderObject, BuilderArray, BuilderTerminate {

        private ByteArrayOutputStream out = new ByteArrayOutputStream();
        private JsonGenerator generator = generator(out);
        private Formatting formatting;

        public Builder() {
            this(Formatting.Compact);
        }

        public Builder(Formatting formatting) {
            if (formatting == Formatting.Pretty) {
                this.formatting = Formatting.Pretty;
                generator.useDefaultPrettyPrinter();
            } else {
                this.formatting = Formatting.Compact;
            }
        }

        public Builder startObject() {
            try {
                generator.writeStartObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder endObject() {
            try {
                generator.writeEndObject();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder startArray() {
            try {
                generator.writeStartArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder startArray(String fieldName) {
            try {
                generator.writeArrayFieldStart(fieldName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder endArray() {
            try {
                generator.writeEndArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder fieldNull(String fieldName) {
            try {
                generator.writeNullField(fieldName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder field(String fieldName, byte v) {
            try {
                generator.writeNumberField(fieldName, v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder field(String fieldName, short v) {
            try {
                generator.writeNumberField(fieldName, v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder field(String fieldName, int v) {
            try {
                generator.writeNumberField(fieldName, v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder field(String fieldName, long v) {
            try {
                generator.writeNumberField(fieldName, v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }


        public Builder field(String fieldName, float v) {
            try {
                generator.writeNumberField(fieldName, v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder field(String fieldName, double v) {
            try {
                generator.writeNumberField(fieldName, v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder field(String fieldName, BigDecimal v) {
            try {
                if (v == null) {
                    generator.writeNullField(fieldName);
                } else {
                    generator.writeNumberField(fieldName, v);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder field(String fieldName, String v) {
            try {
                if (v == null) {
                    generator.writeNullField(fieldName);
                } else {
                    generator.writeStringField(fieldName, v);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder field(String fieldName, boolean v) {
            try {
                generator.writeBooleanField(fieldName, v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public BuilderObject field(String fieldName, byte[] data) {
            try {
                generator.writeBinaryField(fieldName, data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public BuilderObject field(String fieldName, byte[] data, int offset, int length) {
            try {
                generator.writeFieldName(fieldName);
                generator.writeBinary(data, offset, length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder field(String fieldName, Object o) {
            try {
                if (o == null) {
                    generator.writeNullField(fieldName);
                } else {
                    generator.writeObjectField(fieldName, o);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder startObject(String fieldName) {
            try {
                generator.writeObjectFieldStart(fieldName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder field(String fieldName, TreeNode node) {
            try {
                generator.writeFieldName(fieldName);
                if (node == null) {
                    generator.writeNull();
                } else {
                    generator.writeTree(node);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public BuilderObject field(String fieldName, JSON json) {
            try {
                generator.writeFieldName(fieldName);
                if (json == null) {
                    generator.writeNull();
                } else {
                    if (json.node != null) {
                        generator.writeTree(json.node);
                    } else {
                        generator.writeRaw(json.toString());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder valueNull() {
            try {
                generator.writeNull();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(byte v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(short v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(int v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(long v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(float v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(double v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(BigDecimal v) {
            try {
                if (v == null) {
                    generator.writeNull();
                } else {
                    generator.writeNumber(v);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(BigInteger v) {
            try {
                if (v == null) {
                    generator.writeNull();
                } else {
                    generator.writeNumber(v);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(String v) {
            try {
                if (v == null) {
                    generator.writeNull();
                } else {
                    generator.writeString(v);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(boolean v) {
            try {
                generator.writeBoolean(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(byte[] data) {
            try {
                generator.writeBinary(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(byte[] data, int offset, int length) {
            try {
                generator.writeBinary(data, offset, length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(Object o) {
            try {
                if (o == null) {
                    generator.writeNull();
                }
                else {
                    generator.writeObject(o);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(TreeNode node) {
            try {
                generator.writeTree(node);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder value(JSON json) {
            try {
                if (json == null) {
                    generator.writeNull();
                } else {
                    if (json.node != null) {
                        generator.writeTree(json.node);
                    } else {
                        generator.writeRaw(json.toString());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public JSON build() {
            try {
                generator.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return new JSON(new String(out.toByteArray(), DEFAULT_CHARSET), formatting);
        }
    }
}
