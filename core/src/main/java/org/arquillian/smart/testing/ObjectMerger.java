package org.arquillian.smart.testing;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjectMerger<T> {

    private T first;
    private T second;
    private T third;

    public ObjectMerger(T first, T second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T merge() {
        final T object = first != null ? first : (second != null ? second : third);
        if (object == null) {
            return  null;
        }

        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            Object newValue = clazz.newInstance();
            Arrays.stream(fields)
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .forEach(field -> setFieldValueToObject(field, newValue));

            return (T) newValue;
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge & create new instance of" + clazz.getName(), e);
        }
    }

    private void setFieldValueToObject(Field field, Object newObject) {
        field.setAccessible(true);
        final Class<?> type = field.getType();
        Object value1;
        Object value2;
        Object value3;
        try {
            value1 = first != null ? field.get(first) : null;
            value2 = second != null ? field.get(second) : null;
            value3 = third != null ? field.get(third) : null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get field " + field, e);
        }

        Object newValue;
        if (type.isPrimitive()) {
            final Object defaultValue = getDefaultValue(type.getTypeName());
            newValue = getFirstNonNullOrNonDefaultValue(defaultValue, value1, value2, value3);
        } else {
            newValue = getFirstNonNullOrNonDefaultValue(null, value1, value2, value3);
        }

        try {
            field.set(newObject, newValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set field " + field, e);
        }
    }

    private Object getFirstNonNullOrNonDefaultValue(Object defaultValue, Object value1, Object value2, Object value3) {
        if (defaultValue == null) {
            if (value1 != null && value1.getClass().isArray()) {
                final Object[] objectArray1 = convertToObjectArray(value1);
                Object newValue = objectArray1.length > 0 ? value1 : value2;
                if (newValue == null) {
                    return value3;
                }
                final Object[] firstPrecedence1 = convertToObjectArray(newValue);
                return firstPrecedence1.length > 0 ? newValue : value3;
            }
            Object newValue = value1 != defaultValue ? value1 : value2;
            return newValue != defaultValue ? newValue : value3;
        } else {
            Object newValue = !value1.toString().equals(defaultValue.toString()) ? value1 : value2;
            return !newValue.toString().equals(defaultValue.toString()) ? newValue : value3;
        }
    }

    private Object[] convertToObjectArray(Object array) {
        Class ofArray = array.getClass().getComponentType();
        if (ofArray.isPrimitive()) {
            List ar = new ArrayList();
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                ar.add(Array.get(array, i));
            }
            return ar.toArray();
        } else {
            return (Object[]) array;
        }
    }

    private Object getDefaultValue(String typeName) {
        switch (typeName) {
            case "byte":
                return 0;
            case "short":
                return 0;
            case "int":
                return 0;
            case "long":
                return 0L;
            case "char":
                return '\u0000';
            case "float":
                return 0.0F;
            case "double":
                return 0.0;
            case "boolean":
                return false;
        }

        return null;
    }
}

