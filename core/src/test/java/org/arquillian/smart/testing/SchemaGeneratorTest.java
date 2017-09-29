package org.arquillian.smart.testing;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.bytebuddy.implementation.FieldAccessor;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;

import static org.arquillian.smart.testing.SchemaGenerator.TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class SchemaGeneratorTest {

    @Test
    public void verify_schema_for_string() {
        final Map<String, Object> schemaMap = SchemaGenerator.getSchemaMap(String.class);

        assertThat(schemaMap).containsExactly(entry(TYPE, "string"));
    }

    @Test
    public void verify_schema_for_boolean() {
        final Map<String, Object> schemaMap = SchemaGenerator.getSchemaMap(Boolean.class);

        assertThat(schemaMap).containsExactly(entry(TYPE, "boolean"));
    }

    @Test
    public void verify_schema_for_integer() {
        final Map<String, Object> schemaMap = SchemaGenerator.getSchemaMap(Integer.class);

        assertThat(schemaMap).containsExactly(entry(TYPE, "integer"));
    }

    @Test
    public void verify_schema_for_double() {
        final Map<String, Object> schemaMap = SchemaGenerator.getSchemaMap(Double.class);

        assertThat(schemaMap).containsExactly(entry(TYPE, "double"));
    }

    @Test
    public void verify_schema_for_enum() {
        final Map<String, Object> schemaMap = SchemaGenerator.getSchemaMap(DummyEnum.class);

        assertThat(schemaMap).containsExactly(
            entry(TYPE, "string"),
            entry("enum", Arrays.asList("foo", "bar")));
    }

    @Test
    public void verify_schema_for_object() {
        final Map<String, Object> schemaMap = SchemaGenerator.getSchemaMap(DummyObject.class);

        assertThat(schemaMap).containsAllEntriesOf(getSchemaForDummyObject());
    }

    @Test
    public void verify_schema_for_nestedObject() {
        final Map<String, Object> schemaMap = SchemaGenerator.getSchemaMap(TestObject.class);

        final Map<String, Object> properties = (Map<String, Object>) schemaMap.get("properties");

        assertThat(schemaMap).contains(
            entry(TYPE, "object"),
            entry("javatype", "org.arquillian.smart.testing.SchemaGeneratorTest$TestObject"));
        assertThat(properties).contains(entry("dummyObject", getSchemaForDummyObject()));
    }

    private static Map<String, Object> getSchemaForDummyObject() {
        Map<String, String> type = new LinkedHashMap<>();
        type.put(TYPE, "boolean");

        Map<String, Map<String, String>> dummyObject = new LinkedHashMap<>();
        dummyObject.put("dummy", type);


        Map<String, Object> dummyObjectSchema = new LinkedHashMap<>();
        dummyObjectSchema.put(TYPE, "object");
        dummyObjectSchema.put("javatype", "org.arquillian.smart.testing.SchemaGeneratorTest$DummyObject");
        dummyObjectSchema.put( "properties", dummyObject);

        return dummyObjectSchema;
    }

    private static class TestObject {
        private DummyObject dummyObject;
    }

    static class DummyObject {

        private Boolean dummy;

        DummyObject(boolean dummy) {
            this.dummy = dummy;
        }
    }

    enum DummyEnum {
        FOO, BAR;
    }

}
