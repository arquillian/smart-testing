package org.arquillian.smart.testing;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class SchemaGenerator {

    static final String TYPE = "type";

    private Class<?> aClass;

    SchemaGenerator(Class<?> aClass) {
        this.aClass = aClass;
    }

    Map<String, Object> generateSchemaMap() {
        return getMetadata(aClass);
    }

    static Map<String, Object> getSchemaMap(Class<?> aClass) {
        final SchemaGenerator schemaGenerator = new SchemaGenerator(aClass);
        return schemaGenerator.generateSchemaMap();
    }

    private Map<String, Object> getMetadata(Class<?> aClass) {
        Map<String, Object> properties = new LinkedHashMap<>();
        if (isPrimitiveOrWrapper(aClass) || isString(aClass)) {
            addClassType(aClass, properties);
        } else if (aClass.isArray() || List.class.isAssignableFrom(aClass)) {
            properties.put(TYPE, "array");

            Map<String, Object> arrayProperties = new LinkedHashMap<>();
            final Class<?> componentType = aClass.getComponentType();
            if (isPrimitiveOrWrapper(componentType) || isString(componentType)) {
                addClassType(componentType, arrayProperties);
            } else {
                addClassDetails(componentType, arrayProperties);
            }
            properties.put("items", arrayProperties);
        } else if (aClass.isEnum()) {
            addClassType(String.class, properties);
            final List<String> enumValues = Arrays.stream(aClass.getEnumConstants())
                .map(cons -> cons.toString().toLowerCase())
                .collect(Collectors.toList());
            properties.put("enum", enumValues);
        } else {
            addClassDetails(aClass, properties);
        }

        return properties;
    }

    private void addClassType(Class<?> aClass, Map<String, Object> properties) {
        properties.put(TYPE, aClass.getSimpleName().toLowerCase());
    }

    private void addClassDetails(Class<?> aClass, Map<String, Object> map) {
        map.put(TYPE, "object");
        map.put("javatype", aClass.getName());

        final List<Field> list = Arrays.stream(aClass.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .collect(Collectors.toList());
        final Map<String, Map<String, Object>> properties = new LinkedHashMap<>();
        list.forEach(field -> properties.put(field.getName(), getMetadata(field.getType())));
        map.put("properties", properties);
    }

    private boolean isString(Class<?> type) {
        return String.class.equals(type);
    }

    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type != null && (type.isPrimitive() || isPrimitiveWrapper(type));
    }

    private boolean isPrimitiveWrapper(Class<?> type) {
        final List<Class<?>> classes = Arrays.asList(Byte.class, Character.class,
            Short.class, Integer.class, Long.class, Double.class, Float.class, Boolean.class);

        return classes.contains(type);
    }

}
