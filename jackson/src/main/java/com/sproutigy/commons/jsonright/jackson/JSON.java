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
        this.str = jsonString;
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

            this.str = new String(data, offset, length, charset);
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

        return new JSON(serializeToCompactString(o));
    }

    public static JSON primitive(byte v) {
        return JSON.builder().element(v).build();
    }

    public static JSON primitive(short v) {
        return JSON.builder().element(v).build();
    }

    public static JSON primitive(int v) {
        return JSON.builder().element(v).build();
    }

    public static JSON primitive(long v) {
        return JSON.builder().element(v).build();
    }

    public static JSON primitive(float v) {
        return JSON.builder().element(v).build();
    }

    public static JSON primitive(double v) {
        return JSON.builder().element(v).build();
    }

    public static JSON primitive(BigInteger v) {
        return JSON.builder().element(v).build();
    }

    public static JSON primitive(BigDecimal v) {
        return JSON.builder().element(v).build();
    }

    public static JSON primitive(boolean v) {
        return JSON.builder().element(v).build();
    }

    public static JSON primitive(String v) {
        return JSON.builder().element(v).build();
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

    public static String serializeToCompactString(Object o) {
        if (o == null) {
            return null;
        }

        try {
            return getObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String serializeToPrettyString(Object o) {
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

    public void set(String json) {
        this.node = null;
        this.str = json;
    }

    public void set(JsonNode json) {
        this.node = json;
        this.str = null;
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
        return (node == null && (str == null || str.isEmpty())) || (node != null && node.isNull());
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
        BuilderArray<BuilderObject<TParent>> fieldArray(String fieldName);
        BuilderObject<TParent> field(String fieldName, byte v);
        BuilderObject<TParent> field(String fieldName, short v);
        BuilderObject<TParent> field(String fieldName, int v);
        BuilderObject<TParent> field(String fieldName, long v);
        BuilderObject<TParent> field(String fieldName, float v);
        BuilderObject<TParent> field(String fieldName, double v);
        BuilderObject<TParent> field(String fieldName, BigDecimal v);
        BuilderObject<TParent> field(String fieldName, String v);
        BuilderObject<TParent> field(String fieldName, boolean v);
        BuilderObject<TParent> fieldSerialize(String fieldName, Object o);
        BuilderObject<BuilderObject<TParent>> startObject(String fieldName);
        TParent endObject();
    }

    public interface BuilderArray<TParent> {
        BuilderArray<TParent> elementNull();
        BuilderArray<TParent> element(byte v);
        BuilderArray<TParent> element(short v);
        BuilderArray<TParent> element(int v);
        BuilderArray<TParent> element(long v);
        BuilderArray<TParent> element(float v);
        BuilderArray<TParent> element(double v);
        BuilderArray<TParent> element(BigInteger v);
        BuilderArray<TParent> element(BigDecimal v);
        BuilderArray<TParent> element(String v);
        BuilderArray<TParent> element(boolean v);
        BuilderArray<TParent> elementSerialize(Object o);
        BuilderObject<BuilderArray<TParent>> startObject();
        TParent endArray();
    }

    public interface BuilderTerminate {
        JSON build();
    }

    public interface BuilderRoot {
        BuilderObject<BuilderTerminate> startObject();
        BuilderArray<BuilderTerminate> startArray();
        BuilderTerminate elementNull();
        BuilderTerminate element(byte v);
        BuilderTerminate element(short v);
        BuilderTerminate element(int v);
        BuilderTerminate element(long v);
        BuilderTerminate element(float v);
        BuilderTerminate element(double v);
        BuilderTerminate element(BigInteger v);
        BuilderTerminate element(BigDecimal v);
        BuilderTerminate element(String v);
        BuilderTerminate element(boolean v);
        BuilderTerminate elementSerialize(Object o);
    }

    public static class Builder implements BuilderRoot, BuilderObject, BuilderArray, BuilderTerminate {

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

        public Builder fieldArray(String fieldName) {
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

        public Builder fieldSerialize(String fieldName, Object o) {
            try {
                generator.writeObjectField(fieldName, o);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        @Override
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
                generator.writeTree(node);
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

        public Builder element(byte v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder element(short v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder element(int v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder element(long v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder element(float v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder element(double v) {
            try {
                generator.writeNumber(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder element(BigDecimal v) {
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

        public Builder element(BigInteger v) {
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

        public Builder element(String v) {
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

        public Builder element(boolean v) {
            try {
                generator.writeBoolean(v);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder elementSerialize(Object o) {
            try {
                generator.writeObject(o);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder element(TreeNode node) {
            try {
                generator.writeTree(node);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public Builder elementNull() {
            try {
                generator.writeNull();
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
