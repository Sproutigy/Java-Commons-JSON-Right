package com.sproutiyg.commons.jsonright;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sproutigy.commons.jsonright.jackson.ClassedJSON;
import com.sproutigy.commons.jsonright.jackson.JSON;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class JSONTest {

    @Test
    public void testAccessors() {
        JSON json = JSON.builder()
            .startObject()
                .field("name", "John")
                .field("age", 25)
                .startArray("knows")
                    .value("Java")
                    .value("C++")
                .endArray()
                .startObject("address")
                    .field("city", "New York")
                .endObject()
            .endObject()
        .build();

        String name = json.get("name", "");
        assertEquals("John", name);

        int age = json.get("age", 0);
        assertEquals(25, age);

        String city = json.get("address.city", "");
        assertEquals("New York", city);

        String knowsBest = json.get("knows[0]", "");
        assertEquals("Java", knowsBest);

        json.set("age", 26);
        assertEquals(26, json.nodeObject().get("age").asInt());

        json.set("address.city", "Boston");
        assertEquals("Boston", json.nodeObject().get("address").get("city").asText());

        json.set("family.mother", "Julie");
        assertEquals("Julie", json.nodeObject().get("family").get("mother").asText());

        json.remove("age");
        assertFalse(json.nodeObject().has("age"));

        json.remove("address");
        assertFalse(json.nodeObject().has("address"));

        json.remove("knows[0]");
        assertEquals(1, json.nodeObject().get("knows").size());
        assertEquals("C++", json.nodeObject().get("knows").get(0).asText());

        json.set("interests[0]", "sport");
        json.set("interests[1]", "jazz");
        json.set("interests[1]", "hiphop");
        json.set("interests[]", "bicycle");
        assertEquals(3, json.nodeObject().get("interests").size());
        assertEquals("sport", json.nodeObject().get("interests").get(0).asText());
        assertEquals("hiphop", json.nodeObject().get("interests").get(1).asText());
        assertEquals("bicycle", json.nodeObject().get("interests").get(2).asText());
    }

    @Test
    public void testAccessorsMultiDimensionalArray() {
        JSON json = new JSON();

        json.set("playboard[0][0]", "O");
        json.set("playboard[0][1]", "O");
        json.set("playboard[0][2]", "O");
        json.set("playboard[1][0]", null);
        json.set("playboard[1][1]", "X");
        json.set("playboard[1][2]", null);
        json.set("playboard[2][0]", "X");
        json.set("playboard[2][1]", null);
        json.set("playboard[2][2]", "X");
        assertEquals(3, json.nodeObject().get("playboard").size());
        assertEquals(3, json.nodeObject().get("playboard").get(0).size());
        assertEquals(3, json.nodeObject().get("playboard").get(1).size());
        assertEquals(3, json.nodeObject().get("playboard").get(2).size());
        assertEquals("O", json.nodeObject().get("playboard").get(0).get(0).asText());
        assertEquals("O", json.nodeObject().get("playboard").get(0).get(1).asText());
        assertEquals("O", json.nodeObject().get("playboard").get(0).get(2).asText());
        assertTrue(json.nodeObject().get("playboard").get(1).get(0).isNull());
        assertEquals("X", json.nodeObject().get("playboard").get(1).get(1).asText());
        assertTrue(json.nodeObject().get("playboard").get(1).get(2).isNull());
        assertEquals("X", json.nodeObject().get("playboard").get(2).get(0).asText());
        assertTrue(json.nodeObject().get("playboard").get(2).get(1).isNull());
        assertEquals("X", json.nodeObject().get("playboard").get(2).get(2).asText());
    }

    @Test
    public void testAccessorsPrimitives() {
        JSON json = new JSON();

        json.set("hello");
        assertEquals("hello", json.get(String.class));
        assertTrue(json.node().isTextual());

        json.set(123);
        assertEquals(123, (int)json.get(Integer.class));
        assertTrue(json.node().isInt());

        json.set(null);
        assertTrue(json.get().isNull());
    }

    @Test
    public void testAccessorsArray() {
        JSON json = new JSON();
        json.set("[]", "first");
        json.set("[]", "second");
        assertTrue(json.isArray());
        assertEquals(2, json.nodeArray().size());
        assertEquals("first", json.nodeArray().get(0).asText());
        assertEquals("second", json.nodeArray().get(1).asText());
    }

    @Test
    public void testEncodingDetection() throws UnsupportedEncodingException {
        String json = "{\"hello\":\"world\"}";
        assertEquals(json, new JSON(json.getBytes("UTF-8")).toString());
        assertEquals(json, new JSON(json.getBytes("UTF-16BE")).toString());
        assertEquals(json, new JSON(json.getBytes("UTF-16LE")).toString());
        assertEquals(json, new JSON(json.getBytes("UTF-32BE")).toString());
        assertEquals(json, new JSON(json.getBytes("UTF-32LE")).toString());
    }

    @Test
    public void testStringToNodeModifyToString() {
        JSON json = JSON.fromString("{\"hello\":\"world\"}");
        assertEquals("world", json.node().get("hello").asText());
        json.nodeObject().put("hello", "universe");
        assertEquals("{\"hello\":\"universe\"}", json.toStringCompact());
    }

    @Test
    public void testValidation() {
        String good = "{\"hello\":\"world\"}";
        String bad = "{bad:string}";
        assertTrue(JSON.fromString(good).isValid());
        assertFalse(JSON.fromString(bad).isValid());
    }

    @Test
    public void testSelfSerialization() throws IOException {
        String jsonString = "{\"hello\":\"world\"}";
        JSON json = JSON.fromString(jsonString);
        assertEquals(jsonString, JSON.getDefaultObjectMapper().writeValueAsString(json));
        json = JSON.getDefaultObjectMapper().readValue(jsonString, JSON.class);
        assertEquals(jsonString, json.toStringCompact());
    }

    @Test
    public void testPrettyString() {
        String jsonString = "{\"hello\":\"world\",\"whazzup\":\"okay\"}";
        String jsonStringPretty = JSON.fromString(jsonString).toStringPretty();
        assertEquals("{\r\n  \"hello\" : \"world\",\r\n  \"whazzup\" : \"okay\"\r\n}", jsonStringPretty);
    }

    @Test
    public void testJavaSerialization() throws IOException, ClassNotFoundException {
        JSON src = new JSON();
        src.nodeObject().put("v", 1);

        //serialize
        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(serialized);
        out.writeObject(src);

        //deserialize
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serialized.toByteArray()));
        JSON deserialized = (JSON) in.readObject();

        //check
        assertEquals("{\"v\":1}", deserialized.toStringCompact());
        assertEquals(src, deserialized);
    }

    @Test
    public void testBuilder() {

        String json1 = JSON.builder()
                .startObject()
                .field("hello", "world")
                .endObject()
                .build().toString();

        assertEquals("{\"hello\":\"world\"}", json1);


        String json2 = JSON.builder()
                .startArray()
                .value(true)
                .startObject()
                .field("x", 5)
                .endObject()
                .endArray()
                .build().toString();

        assertEquals("[true,{\"x\":5}]", json2);


        String json3 = JSON.builder().value("OK").build().toString();

        assertEquals("\"OK\"", json3);
    }

    @Test
    public void testBuilderSerialization() {
        JSON.Builder builder = new JSON.Builder().startObject().field("hello", "world").endObject();
        assertEquals("{\"hello\":\"world\"}", builder.toString());
        String json = JSON.stringify(builder);
        assertEquals("{\"hello\":\"world\"}", json);
        builder = JSON.fromString(json).deserialize(JSON.Builder.class);
        assertEquals("{\"hello\":\"world\"}", builder.build().toStringCompact());
    }

    @Test
    public void testBuildUsingOtherJSON() {
        String json1str = "{\"hello\":\"world\"}";
        JSON json1 = new JSON(json1str);

        JSON json2 = JSON.builder().startObject().field("test", json1).endObject().build();
        assertEquals("{\"test\":" + json1str + "}", json2.toString());

        JSON json3 = JSON.builder().startArray().value("test").value(json1).endArray().build();
        assertEquals("[\"test\"," + json1str + "]", json3.toString());

        JSON json4 = JSON.builder().startArray().value(json1).value("test").endArray().build();
        assertEquals("[" + json1str + ",\"test\"]", json4.toString());

        JSON json5 = JSON.builder().startObject().field("test", json1).field("check", true).endObject().build();
        assertEquals("{\"test\":" + json1str + ",\"check\":true}", json5.toString());
    }

    @Test
    public void testObjectMapper() {
        ObjectMapper objectMapper = JSON.getDefaultObjectMapper();
        Integer num = objectMapper.convertValue("42", Integer.class);
        assertEquals(42, num.intValue());
    }

    @Test
    public void testJSONTypeCheck() {
        JSON jsonObj = new JSON(" {\"hello\":\"world\"} ");
        assertTrue(jsonObj.isObject());
        assertFalse(jsonObj.isArray());
        assertFalse(jsonObj.isPrimitive());
        assertFalse(jsonObj.isNull());

        JSON jsonArr = new JSON(" [\"hello\",\"world\"] ");
        assertFalse(jsonArr.isObject());
        assertTrue(jsonArr.isArray());
        assertFalse(jsonArr.isPrimitive());
        assertFalse(jsonArr.isNull());

        JSON jsonPrimitiveNum = new JSON(" 5 ");
        assertFalse(jsonPrimitiveNum.isObject());
        assertFalse(jsonPrimitiveNum.isArray());
        assertTrue(jsonPrimitiveNum.isPrimitive());
        assertFalse(jsonPrimitiveNum.isNull());

        JSON jsonNull = new JSON(" null ");
        assertFalse(jsonNull.isObject());
        assertFalse(jsonNull.isArray());
        assertFalse(jsonNull.isPrimitive());
        assertTrue(jsonNull.isNull());

    }

    @Test
    public void testClassed() {
        TestPOJO pojo = new TestPOJO("hello");

        String json = ClassedJSON.serialize(pojo).toString();
        assertTrue(json.contains(TestPOJO.class.getName()));

        String clazz = ClassedJSON.fetchClassName(json);
        assertEquals(TestPOJO.class.getName(), clazz);

        TestPOJO deserialized = ClassedJSON.deserialize(json);
        assertEquals("hello", deserialized.getName());
    }

    @Test
    public void testComplexObjectSerialization() {
        Complex complex = new Complex();
        complex.setMyClassedJSON(new ClassedJSON(new TestPOJO("hello")));
        complex.setMyJSON(JSON.fromString("{\"x\":7}"));

        String json = JSON.serialize(complex).toString();
        Complex deserialized = new JSON(json).deserialize(Complex.class);
        assertEquals(7, deserialized.getMyJSON().nodeObject().get("x").asInt());
        TestPOJO deserializedPOJO = deserialized.getMyClassedJSON().get();
        assertEquals("hello", deserializedPOJO.getName());
    }

    @Test
    public void testDeserializeSimpleTypes() {
        JSON json = JSON.builder().startObject().field("hello", "world").field("counter", 1).endObject().build();
        TextNode textNode = (TextNode) json.nodeObject().get("hello");
        assertEquals("world", JSON.fromNode(textNode).deserialize(String.class));
        assertEquals(1, (int) JSON.fromNode(json.nodeObject().get("counter")).deserialize(Integer.class));
    }

    @Test
    public void testCustomObjectMapper() {
        TestPOJO testPOJO = new TestPOJO();

        JSON json = new JSON();

        json.set(testPOJO);
        assertEquals("{}", json.toStringCompact());


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        json.setLocalObjectMapper(objectMapper);

        json.set(testPOJO);
        assertEquals("{\"name\":null}", json.toStringCompact());
    }

    public static class TestPOJO {
        String name;

        public TestPOJO() {
        }

        public TestPOJO(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Complex {
        private JSON myJSON;
        private ClassedJSON myClassedJSON;

        public JSON getMyJSON() {
            return myJSON;
        }

        public void setMyJSON(JSON myJSON) {
            this.myJSON = myJSON;
        }

        public ClassedJSON getMyClassedJSON() {
            return myClassedJSON;
        }

        public void setMyClassedJSON(ClassedJSON myClassedJSON) {
            this.myClassedJSON = myClassedJSON;
        }
    }


}
