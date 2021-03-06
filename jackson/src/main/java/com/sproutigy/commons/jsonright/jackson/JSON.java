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
import com.fasterxml.jackson.databind.util.RawValue;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Iterator;


@JsonSerialize(using = JSON.Serializer.class)
@JsonDeserialize(using = JSON.Deserializer.class)
public final class JSON implements Serializable, Cloneable {

    public static final String MIME_TYPE = "application/json";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_ENCODING);
    public static final String CONTENT_TYPE_WITH_DEFAULT_ENCODING = MIME_TYPE + "; charset=" + DEFAULT_ENCODING.toLowerCase();

    private static ObjectMapper DEFAULT_OBJECT_MAPPER = null;

    private enum Operation {
        GET, SET, REMOVE
    }

    private ObjectMapper localObjectMapper;
    private String str;
    private Formatting strFormatting = null;
    private transient JsonNode node;

    public enum Formatting {
        Unknown, Compact, Pretty
    }

    public enum StorageType {
        Unknown, String, NodeTree
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
                    if (data[offset + 1] == 0 && data[offset + 2] == 0 && data[offset + 3] != 0) {
                        charset = Charset.forName("UTF-32BE");
                    } else {
                        charset = Charset.forName("UTF-16BE");
                    }
                }
            } else {
                if (data.length > 1) {
                    if (data[offset + 1] == 0) {
                        if (data.length >= 4) {
                            if (data[offset + 2] == 0 && data[offset + 3] == 0) {
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

    public static ObjectMapper getDefaultObjectMapper() {
        if (DEFAULT_OBJECT_MAPPER == null) {
            synchronized (JSON.class) {
                if (DEFAULT_OBJECT_MAPPER == null) {
                    DEFAULT_OBJECT_MAPPER = new ObjectMapper();
                    DEFAULT_OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    DEFAULT_OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                    DEFAULT_OBJECT_MAPPER.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
                    DEFAULT_OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
                    DEFAULT_OBJECT_MAPPER.getFactory().setCodec(DEFAULT_OBJECT_MAPPER);
                }
            }
        }
        return DEFAULT_OBJECT_MAPPER;
    }

    public ObjectMapper getLocalObjectMapper() {
        return localObjectMapper;
    }

    public JSON setLocalObjectMapper(ObjectMapper localObjectMapper) {
        this.localObjectMapper = localObjectMapper;
        return this;
    }

    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = localObjectMapper;
        if (objectMapper == null) {
            objectMapper = getDefaultObjectMapper();
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
        return new JSON(nullNode());
    }

    public static ObjectNode newObjectNode() {
        return getDefaultObjectMapper().createObjectNode();
    }

    public static ArrayNode newArrayNode() {
        return getDefaultObjectMapper().createArrayNode();
    }

    public static JsonNode nullNode() {
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

        return new JSON(stringify(o));
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

    public static BuilderRoot builder(ObjectMapper objectMapper) {
        return new Builder(objectMapper, Formatting.Compact);
    }

    public static BuilderRoot builder(ObjectMapper objectMapper, Formatting formatting) {
        return new Builder(objectMapper, formatting);
    }

    public static BuilderRoot builderCompact() {
        return builder(Formatting.Compact);
    }

    public static BuilderRoot builderPretty() {
        return builder(Formatting.Pretty);
    }

    public static String stringify(Object o) {
        if (o == null) {
            return "null";
        }

        try {
            return getDefaultObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String stringifyPretty(Object o) {
        if (o == null) {
            return "null";
        }

        try {
            return getDefaultObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String stringify(Object o, Formatting formatting) {
        if (o == null) {
            return "null";
        }

        return JSON.serialize(o).toString(formatting);
    }

    public static <T> T parse(String jsonString, Class<? extends T> clazz) {
        return parse(jsonString, clazz, null);
    }

    public static <T> T parse(String jsonString, Class<? extends T> clazz, T defaultValue) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        return JSON.fromString(jsonString).deserialize(clazz, defaultValue);
    }

    public static JsonNode parse(String json) {
        return new JSON(json).node();
    }

    public static JsonGenerator generator(OutputStream out) {
        return generator(getDefaultObjectMapper(), out);
    }

    public static JsonGenerator generator(ObjectMapper objectMapper, OutputStream out) {
        try {
            return objectMapper.getFactory().createGenerator(out, JsonEncoding.UTF8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonGenerator generatorPretty(OutputStream out) {
        return generator(getDefaultObjectMapper(), out);
    }

    public static JsonGenerator generatorPretty(ObjectMapper objectMapper, OutputStream out) {
        return generator(objectMapper, out).setPrettyPrinter(new DefaultPrettyPrinter());
    }

    public static String compactString(String jsonString) {
        try {
            ObjectMapper objectMapper = getDefaultObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return objectMapper.writeValueAsString(jsonNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettyString(String jsonString) {
        try {
            ObjectMapper objectMapper = getDefaultObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonNode get(String path) {
        if (path == null || path.isEmpty()) {
            return get();
        }

        return resolvePath(node(), path, Operation.GET, null);
    }

    public <T> T get(String path, Class<? extends T> clazz) {
        JsonNode node = get(path);
        try {
            if (node == null) {
                return null;
            } else {
                return getObjectMapper().treeToValue(node, clazz);
            }
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path, T defaultValue) {
        if (defaultValue == null) {
            throw new NullPointerException("defaultValue == null");
        }

        Class<? extends T> clazz = (Class<? extends T>)defaultValue.getClass();
        return get(path, clazz, defaultValue);
    }

    public <T> T get(String path, Class<? extends T> clazz, T defaultValue) {
        T value = get(path, clazz);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public boolean has(String path) {
        JsonNode node = get(path);
        return (node != null && !node.isNull());
    }

    public boolean has(String path, Object value) {
        if (value == null) {
            throw new NullPointerException("value == null");
        }
        JsonNode valueNode = convertToNode(value);
        JsonNode n = get(path);

        if (n != null) {
            if (n.isArray()) {
                for (int i = 0; i < n.size(); i++) {
                    JsonNode arrayItemNode = n.get(i);
                    if (valueNode.equals(arrayItemNode)) {
                        return true;
                    }
                }
            } else if (n.isObject()) {
                return n.has(value.toString());
            } else {
                return valueNode.equals(n);
            }
        }
        return false;
    }

    public int indexOf(String path, Object value) {
        JsonNode node = get(path);
        if (node != null) {
            if (node.isArray()) {
                return findIndexOf((ArrayNode) node, value);
            }
            if (node.isObject()) {
                int i = 0;
                Iterator<String> fieldNamesIterator = node.fieldNames();
                while (fieldNamesIterator.hasNext()) {
                    String fieldName = fieldNamesIterator.next();
                    if (value.toString().equals(fieldName)) {
                        return i;
                    }
                    i++;
                }
            }
        }
        return -1;
    }

    public JSON set(String path, Object value) {
        if (path == null || path.isEmpty()) {
            return set(value);
        }

        if (path.charAt(0) == '[') {
            if (!isArray()) {
                setRaw(newArrayNode());
            }
        }
        else if (!isObject()) {
            setRaw(newObjectNode());
        }

        resolvePath(node(), path, Operation.SET, value);
        return this;
    }

    public JSON set(Object value) {
        return setRaw(convertToNode(value));
    }

    public JSON remove(String path) {
        return remove(path, null);
    }

    public JSON remove(String path, Object value) {
        resolvePath(node(), path, Operation.REMOVE, value);
        return this;
    }

    private JsonNode resolvePath(JsonNode node, String path, Operation operation, Object value) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (path == null || path.isEmpty()) {
            return node;
        }
        if (path.startsWith(".") || path.endsWith(".") || path.startsWith("]") || path.endsWith("[")) {
            throw new IllegalArgumentException("Malformed JSON path");
        }

        JsonNode result = null;
        int nextDot = path.indexOf('.', 1);
        int nextIndexer = path.indexOf('[', 1);
        int next;
        if (nextDot != -1 && nextIndexer != -1) {
            next = Math.min(nextDot, nextIndexer);
        } else if (nextDot != -1) {
            next = nextDot;
        } else {
            next = nextIndexer;
        }

        boolean isObject = (next > -1 && next == nextDot);
        boolean isArray = (next > -1 && next == nextIndexer);
        boolean isTarget = (next == -1);

        if (next == -1) {
            next = path.length();
        }

        if (path.charAt(0) == '[') {
            next = path.indexOf(']', 1) + 1;
            if (next == 0) {
                throw new IllegalArgumentException("Malformed JSON path");
            }
            String indexer = path.substring(1, next - 1);
            isTarget = (next == path.length());

            try {
                int arrayIndex;
                if (indexer.isEmpty()) {
                    arrayIndex = node.size();
                } else {
                    arrayIndex = Integer.parseInt(indexer);
                }

                if (node.isArray()) {
                    if (operation != Operation.GET) {
                        if (operation == Operation.REMOVE) {
                            boolean remove = true;
                            if (value != null) {
                                JsonNode valueNode = convertToNode(value);
                                if (indexer.isEmpty()) {
                                    arrayIndex = findIndexOf((ArrayNode)node, value);
                                    remove = arrayIndex != -1;
                                } else {
                                    JsonNode arrayItemNode = node.get(arrayIndex);
                                    remove = valueNode.equals(arrayItemNode);
                                }
                            }

                            if (remove) {
                                ((ArrayNode) node).remove(arrayIndex);
                            }
                        } else {
                            while (node.size() < arrayIndex) {
                                ((ArrayNode) node).add(JSON.nullNode());
                            }
                            if (isTarget) {
                                result = convertToNode(value);
                            } else if (isObject) {
                                result = node.get(arrayIndex);
                                if (result == null || !result.isObject()) {
                                    result = newObjectNode();
                                }
                            } else if (isArray) {
                                result = node.get(arrayIndex);
                                if (result == null || !result.isArray()) {
                                    result = newArrayNode();
                                }
                            }
                            if (node.size() == arrayIndex) {
                                ((ArrayNode) node).add(result);
                            } else {
                                ((ArrayNode) node).set(arrayIndex, result);
                            }
                        }
                    } else {
                        if (node.size() >= arrayIndex) {
                            result = node.get(arrayIndex);
                        }
                    }
                }
            } catch (NumberFormatException notInteger) {
                throw new IllegalArgumentException("Malformed JSON path");
            }
        } else {
            String fieldName = path.substring(0, next);
            if (node.isObject()) {
                result = node.get(fieldName);
                if (result == null && operation != Operation.GET) {
                    if (isObject) {
                        result = JSON.newObjectNode();
                    } else if (isArray) {
                        result = JSON.newArrayNode();
                    }
                    if (operation != Operation.REMOVE) {
                        ((ObjectNode) node).set(fieldName, result);
                    }
                }

                if (isTarget && operation != Operation.GET) {
                    if (operation == Operation.REMOVE) {
                        boolean remove = true;
                        if (value != null) {
                            JsonNode valueNode = convertToNode(value);
                            JsonNode objectFieldNode = node.get(fieldName);
                            remove = valueNode.equals(objectFieldNode);
                        }
                        if (remove) {
                            ((ObjectNode) node).remove(fieldName);
                        }
                        result = node;
                    } else {
                        result = convertToNode(value);
                        ((ObjectNode) node).set(fieldName, result);
                    }
                }
            } else {
                throw new IllegalStateException("Cannot change type of a root node");
            }
        }

        if (next == nextDot) {
            next++;
        }

        if (!isTarget) {
            return resolvePath(result, path.substring(next), operation, value);
        }

        return result;
    }

    private JsonNode convertToNode(Object value) {
        if (value == null) {
            return nullNode();
        }
        if (value instanceof JsonNode) {
            return (JsonNode)value;
        }
        if (value instanceof JSON) {
            return ((JSON) value).node();
        }
        return getObjectMapper().valueToTree(value);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        if (str != null) {
            return new JSON(str);
        } else {
            return new JSON(toStringCompact());
        }
    }

    public JsonNode node() {
        if (node == null) {
            if (str != null && !str.isEmpty()) {
                if (str.equals("{}")) {
                    node = getObjectMapper().createObjectNode();
                } else if (str.equals("[]")) {
                    node = getObjectMapper().createArrayNode();
                } else {
                    try {
                        node = getObjectMapper().readTree(str);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                node = nullNode();
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

    public StorageType getCurrentStorageType() {
        if (str != null) {
            return StorageType.String;
        }
        if (node != null) {
            return StorageType.NodeTree;
        }

        return StorageType.Unknown;
    }

    public void appendTo(ObjectNode objectNode, String fieldName) {
        if (getCurrentStorageType() == StorageType.String) {
            objectNode.putRawValue(fieldName, new RawValue(toString()));
        } else {
            objectNode.set(fieldName, node());
        }
    }

    public void appendTo(ArrayNode arrayNode) {
        if (getCurrentStorageType() == StorageType.String) {
            arrayNode.addRawValue(new RawValue(toString()));
        } else {
            arrayNode.add(node());
        }
    }

    public JSON setRaw(String json) {
        return setRaw(json, Formatting.Unknown);
    }

    public JSON setRaw(String json, Formatting formatting) {
        clear();

        this.str = json;
        this.strFormatting = formatting;
        return this;
    }

    public JsonNode get() {
        return node();
    }

    public <T> T get(Class<? extends T> clazz) {
        return deserialize(clazz);
    }

    public <T> T get(Class<? extends T> clazz, T defaultValue) {
        return deserialize(clazz, defaultValue);
    }

    public JSON setRaw(JsonNode json) {
        clear();

        this.node = json;
        return this;
    }

    public JSON setRaw(JSON other) {
        return setRaw(other, true);
    }

    public JSON setRaw(JSON other, boolean deepCopy) {
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

    public <T> T deserialize(Class<? extends T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz == null");
        }

        if (isNull()) {
            return null;
        }

        if (str != null) {
            try {
                return getObjectMapper().readValue(parser(), clazz);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (node != null) {
            try {
                return getObjectMapper().treeToValue(node, clazz);
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
        return deserialize((Class<? extends T>) defaultValue.getClass(), defaultValue);
    }

    public <T> T deserialize(Class<? extends T> clazz, T defaultValue) {
        T o = deserialize(clazz);
        if (o == null) {
            return defaultValue;
        }
        return o;
    }

    public boolean isNull() {
        return (node == null && (str == null || str.isEmpty() || str.equals("null"))) || (node != null && node.isNull());
    }

    public boolean isObject() {
        if (node != null) {
            return node.isObject();
        } else {
            return str != null && str.length() > 0 && str.charAt(0) == '{';
        }
    }

    public boolean isArray() {
        if (node != null) {
            return node.isArray();
        } else {
            return str != null && str.length() > 0 && str.charAt(0) == '[';
        }
    }

    public boolean isPrimitive() {
        if (node != null) {
            return !node.isArray() && !node.isObject() && !node.isNull();
        } else {
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

    public JsonParser parser() throws IOException {
        String json = str;
        if (json == null) {
            json = toStringCompact();
        }

        JsonParser parser = getObjectMapper().getFactory().createParser(json);
        parser.setCodec(getObjectMapper());
        return parser;
    }

    public boolean isValid() {
        if (node != null) {
            if (str == null || str.isEmpty()) {
                return false;
            }

            try {
                JsonParser parser = parser();
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
            } catch (Exception e) {
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

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof JSON)) {
            return false;
        }
        return ((JSON) other).toStringCompact().equals(((JSON) other).toStringCompact());
    }

    @Override
    public int hashCode() {
        return toStringCompact().hashCode();
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


    private int findIndexOf(ArrayNode arrayNode, Object value) {
        JsonNode valueNode = convertToNode(value);
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode arrayItemNode = arrayNode.get(i);
            if (valueNode.equals(arrayItemNode)) {
                return i;
            }
        }
        return -1;
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        int formattingOrdinal = Formatting.Unknown.ordinal();

        String jsonString = str;
        if (jsonString == null) {
            formattingOrdinal = Formatting.Compact.ordinal();
            jsonString = toStringCompact();
        } else {
            if (strFormatting != null) {
                formattingOrdinal = strFormatting.ordinal();
            }
        }

        //formatting mark
        stream.writeByte((byte) formattingOrdinal);

        //JSON string
        stream.writeUTF(jsonString);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        //formatting mark
        int ordinal = stream.readByte();
        this.strFormatting = Formatting.values()[ordinal];

        //JSON string
        this.str = stream.readUTF();
    }


    public static class Serializer extends JsonSerializer<JSON> {
        @Override
        public void serialize(JSON json, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            if (jsonGenerator.getCodec() == null) {
                jsonGenerator.setCodec(getDefaultObjectMapper());
            }
            jsonGenerator.getCodec().writeTree(jsonGenerator, json.node());
        }
    }

    public static class Deserializer extends JsonDeserializer<JSON> {
        @Override
        public JSON deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            if (jsonParser.getCodec() == null) {
                jsonParser.setCodec(getDefaultObjectMapper());
            }
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

    @JsonSerialize(using = Builder.Serializer.class)
    @JsonDeserialize(using = Builder.Deserializer.class)
    public static final class Builder implements BuilderRoot, BuilderObject, BuilderArray, BuilderTerminate {

        private ByteArrayOutputStream out = new ByteArrayOutputStream();
        private JsonGenerator generator;
        private Formatting formatting;

        public Builder() {
            this(Formatting.Compact);
        }

        public Builder(Formatting formatting) {
            this(null, formatting);
        }

        public Builder(ObjectMapper objectMapper, Formatting formatting) {
            if (objectMapper != null) {
                generator = generator(objectMapper, out);
            } else {
                generator = generator(out);
            }

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
                        generator.writeRawValue(json.toString());

                        if (this.formatting == Formatting.Pretty || this.formatting != json.strFormatting) {
                            this.formatting = Formatting.Unknown;
                        }
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
                } else {
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
                        generator.writeRawValue(json.toString());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return this;
        }

        public JSON build() {
            try {
                generator.flush();
                generator.close();
                generator = null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return new JSON(new String(out.toByteArray(), DEFAULT_CHARSET), formatting);
        }


        public static class Serializer extends JsonSerializer<Builder> {
            @Override
            public void serialize(Builder that, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
                jsonGenerator.getCodec().writeTree(jsonGenerator, that.build().node());
            }
        }

        public static class Deserializer extends JsonDeserializer<Builder> {
            @Override
            public Builder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
                JSON.Builder builder = new JSON.Builder();
                JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
                builder.generator.writeRaw(jsonNode.toString());
                return builder;
            }
        }

        @Override
        public String toString() {
            if (generator != null) {
                try {
                    generator.flush();
                } catch (IOException ignore) {
                }
            }

            return new String(out.toByteArray(), DEFAULT_CHARSET);
        }
    }
}
