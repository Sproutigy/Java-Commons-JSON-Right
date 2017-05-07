# Sproutigy Java Commons JSON Right
Simplifies usage of JSON serialization format in JVM applications.

## json-right-jackson

Currently JSON Right uses [Jackson 2](http://wiki.fasterxml.com/JacksonHome) library.
Dependencies:
- jackson-core
- jackson-annotations
- jackson-databind

### Jackson version
Tested with Jackson version **2.8.8**, which is added as a default dependency. May work with other versions.

If you want to use different version, add this to your POM:
```
    <properties>
        <jackson.version>2.7.9</jackson.version>
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

#### JavaScript-like static methods

##### JSON.stringify
```java
String jsonStr1 = JSON.stringify(obj);
String jsonStr2 = JSON.stringifyPretty(obj);
```

##### JSON.parse
```java
String jsonStr = "{\"hello\":\"world\"}";
JsonNode node = JSON.parse(jsonStr);
MyClass instance = JSON.parse(jsonStr, MyClass.class);
```

#### JavaScript-like accessors
JSON supports accessing objects, arrays and values by path. Use dot character: `.` to access object's internals and brackets: `[0]` to specify array index. Use empty brackets `[]` to append value to an array.    

```java
String jsonStr = "{\"name\":\"John\"}";
JSON json = new JSON(jsonStr);

//fetch value
String name = json.get("name", ""); //empty string as a default value

//modify
json.set("name", "Josh"); //changes field value

//add new values and nodes
json.set("age", 27); //puts numeric field
json.set("address.city", "Cansas City"); //creates node "address" and puts field "city"

//remove values and nodes
json.remove("age"); //removes field
json.remove("address"); //removes node

//create, append to and remove from arrays
json.set("interests[0]", "sport"); //creates new array and adds a value
json.set("interests[1]", "jazz"); //sets value to existing array on specified index
json.set("interests[]", "bicycle"); //appends value to existing array at the end of it
json.remove("interests[1]"); //removes value from array, shortens the array
json.remove("interests[]", "sport") //removes by value
boolean likesSport = json.has("interests", "sport"); //checks whether array contains specific value
int idx = json.indexOf("interests", "bicycle"); //

//serialize/deserialize any classes
MyObj instance1 = json.get("data", MyObj.class); //may be null when not defined
MyObj instance2 = json.get("data", new MyObj()); //will be default when not defined
json.set("data", new MyObj()); //serializes object
```

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
##### Object
```java
JSON jsonObj = JSON.newObject();
jsonObj.nodeObject().set("version", 1);
```
or just:
```java
new JSON().set("version", 1);
```

##### Array
```java
JSON jsonArr = JSON.newArray();
jsonArr.nodeArray().add("first");
jsonArr.nodeArray().add("second");
```
or just:
```java
new JSON().set("[]", "first").set("[]", "second");
```

##### Primitives
```java
JSON jsonPrimitive = JSON.primitive(42);
```
or just:
```java
new JSON().set(42); 
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


#### ClassedJSON
When there's a need to serialize any class and keep class name for later deserialization without class knowledge, ClassedJSON may be used as a decorator for any object.

Assuming having class:
```java
    package test;

    public class TestPOJO {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
```

It can be serialized with class name information by:
```java
TestPOJO obj = new TestPOJO();
obj.setName("John Doe");

String json = ClassedJSON.serialize(obj).toString();
```

it will be serialized to JSON string representation:
```
{"test.TestPOJO":{"name":"John Doe"}}
```

To deserialize use:
```java
TestPOJO deserialized = ClassedJSON.deserialize(obj);
```

An instance of ClassedJSON may be used as an containing object:
```java
ClassedJSON x = new ClassedJSON(obj);
String json = JSON.serialize(x).toString();
```

Class name could be read before Java object deserialization:
```java
String className = ClassedJSON.fetchClassName(json);
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
