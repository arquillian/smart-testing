package org.arquillian.smart.testing;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ObjectMapper {

    public static <T> T mapToObject(Class<T> aClass, Map<String, Object> map) {
        if (!map.isEmpty()) {
            T instance;
            try {
                instance = aClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Failed to create new instance of class " + aClass, e);
            }

            Arrays.stream(aClass.getMethods()).filter(ObjectMapper::isSetter)
                .forEach(method -> invokeMethodWithMappedValue(method, instance, map));

            return instance;
        }

        return null;
    }

    private static <T> void invokeMethodWithMappedValue(Method method, T instance, Map<String, Object> map) {
        method.setAccessible(true);
        final String field = method.getName().substring(3);
        final String property = Character.toLowerCase(field.charAt(0)) + field.substring(1);
        final Object mappedValue = map.get(property);

        if (mappedValue != null && method.getParameterTypes().length == 1) {
            try {
                final Object converted = convert(method, mappedValue);
                if (converted != null) {
                    method.invoke(instance, converted);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to invoke method " + method, e);
            }
        }
    }

    private static Object convert(Method method, Object mappedValue) {
        Class<?> parameterType = method.getParameterTypes()[0];
        if (parameterType.isArray()) {
            return handleArray(parameterType.getComponentType(), mappedValue);
        } else if (parameterType.isEnum()) {
            return handleEnum(method, mappedValue);
        } else if (parameterType.isAssignableFrom(List.class)) {
            return handleList(method, mappedValue);
        } else if (!parameterType.isAssignableFrom(Map.class) && Map.class.isAssignableFrom(mappedValue.getClass())) {
            return mapToObject(parameterType, (Map<String, Object>) mappedValue);
        } else {
            return mappedValue;
        }
    }

    private static Enum handleEnum(Method method, Object mapValue) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        String value = (String) mapValue;
        return Enum.valueOf((Class<Enum>) parameterTypes[0], value.toUpperCase());
    }

    private static <T> T[] handleArray(Class<T> parameterType, Object mapValue) {
        List<T> convertedList = getConvertedList(parameterType, mapValue);
        T[] array = (T[]) Array.newInstance(parameterType, convertedList.size());
        return convertedList.toArray(array);
    }

    private static Object handleList(Method method, Object mappedValue) {
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        if (genericParameterTypes.length == 1) {
            Type type = genericParameterTypes[0];

            if (type instanceof ParameterizedType) {
                Type[] parameters = ((ParameterizedType) type).getActualTypeArguments();
                if (parameters.length == 1) {
                    return getConvertedList((Class<Object>) parameters[0], mappedValue);
                }
            }
        }
        return null;
    }

    private static <T> Object convertToType(Class<T> clazz, String mappedValue) {
        if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
            return Integer.valueOf(mappedValue);
        } else if (Double.class.equals(clazz) || double.class.equals(clazz)) {
            return Double.valueOf(mappedValue);
        } else if (Long.class.equals(clazz) || long.class.equals(clazz)) {
            return Long.valueOf(mappedValue);
        } else if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
            return Boolean.valueOf(mappedValue);
        } else if (String.class.equals(clazz)) {
            return mappedValue;
        }
        return null;
    }

    private static <T> List<T> getConvertedList(Class<T> parameterType, Object mappedValue) {
        final Class<?> aClass = mappedValue.getClass();
        if (List.class.isAssignableFrom(aClass)) {
            return (List<T>) mappedValue;
        } else if (String.class.isAssignableFrom(aClass)) {
            final String value = (String) mappedValue;
            final List<String> values = Arrays.stream(value.split("\\s*,\\s*")).collect(Collectors.toList());
            List<T> convertedList = new ArrayList<>(values.size());
            for (String v : values) {
                convertedList.add((T) convertToType(parameterType, v));
            }
            return convertedList;
        }

        return null;
    }

    private static boolean isSetter(Method candidate) {
        return candidate.getName().matches("^(set|add)[A-Z].*")
            && (candidate.getReturnType().equals(Void.TYPE) || candidate.getReturnType()
            .equals(candidate.getDeclaringClass()))
            && candidate.getParameterTypes().length > 0;
    }
}

