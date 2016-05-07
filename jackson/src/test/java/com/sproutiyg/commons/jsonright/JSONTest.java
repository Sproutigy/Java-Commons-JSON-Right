package com.sproutiyg.commons.jsonright;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sproutigy.commons.jsonright.jackson.JSON;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

public class JSONTest {

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
        assertEquals("{\"hello\":\"universe\"}", json.toString());
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
        assertEquals(jsonString, JSON.getObjectMapper().writeValueAsString(json));
        json = JSON.getObjectMapper().readValue(jsonString, JSON.class);
        assertEquals(jsonString, json.toString());
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
                .build().toString()
                ;

        assertEquals("[true,{\"x\":5}]", json2);


        String json3 = JSON.builder().value("OK").build().toString();

        assertEquals("\"OK\"", json3);
    }

    @Test
    public void testObjectMapper() {
        ObjectMapper objectMapper = JSON.getObjectMapper();
        Integer num = objectMapper.convertValue("42", Integer.class);
        assertEquals(42, num.intValue());
    }
}
