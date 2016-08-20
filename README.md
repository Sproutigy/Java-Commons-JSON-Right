# Sproutigy Java Commons JSON Right
Simplifies usage of JSON serialization format in JVM applications.

## json-right-jackson

Currently JSON Right uses [Jackson 2](http://wiki.fasterxml.com/JacksonHome) library.
Dependencies:
- jackson-core
- jackson-annotations
- jackson-databind

### Jackson version
Tested with Jackson version **2.8.1**. May work with other versions.

If you want to use different version, add this to your POM:
```
    <properties>
        <jackson.version>2.8.1</jackson.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-annotations</artifactId>
            <version>${jackson.version}</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>
    </dependencies>
```

### Features

#### String or parsed nodes tree
It does not matter how you want to keep your data. JSON Right dynamically converts between string and JsonNode object when there's a need:
```java
String jsonStr = "{\"hello\":\"world\"}";
JSON json = new JSON(jsonStr);
System.out.println(json.node().get("hello"));
```

It also reflects changes in JsonNode objects when generating strings:
```java
json.nodeObject().put("hello", "universe");
System.out.println(json.toStringCompact());
System.out.println(json.toStringPretty());
```


#### New JSON creation
```java
JSON jsonObj = JSON.newObject();
jsonObj.nodeObject().set("version", 1);

JSON jsonArr = JSON.newArray();
jsonArr.nodeArray().add("first");

JSON jsonPrimitive = JSON.primitive(42);
```


#### Validation
JSON Right supports validation to boolean value:
```java
boolean ok = json.isValid();
```

or inline check that may throw `IllegalStateException`:
```
boolean ok = json.validate().isObject();
System.out.println(json.validate().node().get("hello"));
```


#### Serialization
Serialization is possible using static method and deserialization using instance method:
```java
MyClass myPOJO = ...;
JSON json = JSON.serialize(myPOJO);
MyClass deserializedPOJO = json.deserialize(MyClass.class);
```


#### Encoding detection
When raw data (byte array) is specified as input, JSON Right will automatically detect charset used to encode JSON and convert to proper string:
```java
String json = "{\"hello\":\"world\"}";
assert json.equals(new JSON(json.getBytes("UTF-8")).toString());
assert json.equals(new JSON(json.getBytes("UTF-16BE")).toString());
assert json.equals(new JSON(json.getBytes("UTF-16LE")).toString());
assert json.equals(new JSON(json.getBytes("UTF-32BE")).toString());
assert json.equals(new JSON(json.getBytes("UTF-32LE")).toString());
```


#### Builder
JSON Right introduces it's own JSON builder with semantic interfaces that validates the state at compilation time as also helps in IDE to prevent invalid JSON creation from code:
```java
String json = JSON.builder()
    .startObject()
        .field("hello", "world")
        .startArray("test")
            .value(1)
            .value("OK")
            .value(true)
        .endArray()
    .endObject()
    .build()
    .toString();
```


#### Useful static constants
```java
System.out.println(JSON.MIME_TYPE); // application/json
System.out.println(JSON.DEFAULT_ENCODING); // UTF-8
System.out.println(JSON.CONTENT_TYPE_WITH_DEFAULT_ENCODING); // application/json; charset=utf-8

byte[] data = "{\"hello\":\"world\"}".getBytes(JSON.DEFAULT_CHARSET);
```


#### Self-serialization
JSON instance is self-serializable and may be safely used as a serializable object class field.
```java
class MyClass {
    private int version;
    private JSON data;

    //TODO: getters and setter
}
```


#### Jackson's ObjectMapper
JSON uses it's own `ObjectMapper` singleton, which may be retrieved and used:
```java
ObjectMapper objectMapper = JSON.getObjectMapper();
Integer num = objectMapper.convertValue("42", Integer.class);
```


### Maven

To use as a dependency add to your `pom.xml` into `<dependencies>` section:
```xml
<dependency>
    <groupId>com.sproutigy.commons</groupId>
    <artifactId>json-right-jackson</artifactId>
    <version>RELEASE</version>
</dependency>
```


## More
For more information and commercial support visit [Sproutigy](http://www.sproutigy.com/opensource)
