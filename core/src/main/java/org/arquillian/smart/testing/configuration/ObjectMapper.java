package org.arquillian.smart.testing.configuration;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class ObjectMapper {

    static <T extends ConfigurationSection> T mapToObject(Class<T> aClass, Map<String, Object> map) {
        T instance;
        try {
            instance = aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create new instance of class " + aClass, e);
        }

        List<ConfigurationItem> configItems = instance.registerConfigurationItems();

        Arrays.stream(aClass.getMethods()).filter(ObjectMapper::isSetter)
            .forEach(method -> invokeMethodWithMappedValue(configItems, method, instance, map));

        return instance;
    }

    private static <T> void invokeMethodWithMappedValue(List<ConfigurationItem> configItems, Method method, T instance,
        Map<String, Object> map) {
        method.setAccessible(true);
        if (method.getParameterTypes().length != 1) {
            return;
        }
        final String field = method.getName().substring(3);
        final String property = Character.toLowerCase(field.charAt(0)) + field.substring(1);
        Object configFileValue = map.get(property);

        Object converted = getConvertedObject(method, configFileValue, property, configItems);

        try {
            if (converted != null) {
                method.invoke(instance, converted);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method " + method, e);
        }
    }

    private static Object getConvertedObject(Method method, Object configFileValue,
        String property, List<ConfigurationItem> configItems) {
        Optional<ConfigurationItem> foundConfigItem =
            configItems.stream().filter(item -> property.equals(item.getParamName())).findFirst();

        if (!foundConfigItem.isPresent()) {
            Class<?> parameterType = method.getParameterTypes()[0];
            if (!ConfigurationSection.class.isAssignableFrom(parameterType)) {
                return null;
            } else if (configFileValue == null) {
                return mapToObject((Class<ConfigurationSection>) parameterType, Collections.emptyMap());
            } else {
                return mapToObject((Class<ConfigurationSection>) parameterType, (Map<String, Object>) configFileValue);
            }
        } else {
            ConfigurationItem configItem = foundConfigItem.get();
            Object mappedValue = getUserSetProperty(method, configItem, configFileValue);

            if (mappedValue == null && configItem.getDefaultValue() != null) {
                mappedValue = configItem.getDefaultValue();
            }
            if (mappedValue != null) {
                return convert(method, mappedValue);
            }
        }
        return null;
    }

    private static Object getUserSetProperty(Method method, ConfigurationItem configItem, Object configFileValue) {
        if (configItem.getSystemProperty() != null) {
            if (!configItem.getSystemProperty().endsWith(".*")) {
                String sysPropertyValue = System.getProperty(configItem.getSystemProperty());
                return sysPropertyValue != null ? sysPropertyValue : configFileValue;
            } else {
                return createMultipleOccurrenceProperty(method, configItem, configFileValue);
            }
        }
        return configFileValue;
    }

    private static List<Object> createMultipleOccurrenceProperty(Method method, ConfigurationItem configItem,
        Object configFileValue) {

        String sysPropKey = configItem.getSystemProperty().substring(0, configItem.getSystemProperty().lastIndexOf('.'));
        Map<Object, Object> systemProperties =
            System.getProperties().entrySet()
                .stream()
                .filter(prop -> prop.getKey().toString().startsWith(sysPropKey))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<Object> multipleValue = new ArrayList<>();
        if (configFileValue != null) {
            multipleValue.addAll(getValuesFromFile(method, sysPropKey, configFileValue, systemProperties));
        }

        multipleValue.addAll(
            systemProperties.entrySet()
                .stream()
                .map(e -> e.getKey().toString() + "=" + e.getValue().toString())
                .collect(Collectors.toList()));

        if (!multipleValue.isEmpty()) {
            return multipleValue;
        }
        return null;
    }

    private static List<Object> getValuesFromFile(Method method, String sysPropKey, Object configFileValue,
        Map<Object, Object> systemProperties) {
        Class<?> parameterType = method.getParameterTypes()[0];
        ArrayList<Object> fromFileParam = new ArrayList<>();
        if (parameterType.isAssignableFrom(List.class)) {
            fromFileParam.addAll((Collection<?>) handleList(method, configFileValue));
        } else if (parameterType.isArray()) {
            fromFileParam.addAll(Arrays.asList(handleArray(parameterType.getComponentType(), configFileValue)));
        } else {
            fromFileParam.add(configFileValue);
        }
        return fromFileParam
            .stream()
            .filter(param -> !isSetBySysProperty(param, sysPropKey, systemProperties))
            .collect(Collectors.toList());
    }

    private static boolean isSetBySysProperty(Object param, String sysPropKey, Map<Object, Object> systemProperties) {
        String[] paramSplit = String.valueOf(param).split("=");
        if (paramSplit.length == 2) {
            String key = paramSplit[0];
            return systemProperties.containsKey(key) || systemProperties.containsKey(String.join(".", sysPropKey, key));
        }
        return false;
    }

    private static Object convert(Method method, Object mappedValue) {
        Class<?> parameterType = method.getParameterTypes()[0];
        if (parameterType.isArray()) {
            return handleArray(parameterType.getComponentType(), mappedValue);
        } else if (parameterType.isEnum()) {
            return handleEnum(method, mappedValue);
        } else if (parameterType.isAssignableFrom(List.class)) {
            return handleList(method, mappedValue);
        } else if (parameterType.isAssignableFrom(mappedValue.getClass())) {
            return mappedValue;
        } else if (ConfigurationSection.class.isAssignableFrom(parameterType)) {
            return mapToObject((Class<ConfigurationSection>) parameterType, (Map<String, Object>) mappedValue);
        } else {
            return convertToType(parameterType, mappedValue.toString());
        }
    }

    private static Enum handleEnum(Method method, Object mapValue) {
        if (mapValue.getClass().isEnum()) {
            return (Enum) mapValue;
        }
        final Class<?>[] parameterTypes = method.getParameterTypes();
        String value = (String) mapValue;
        return Enum.valueOf((Class<Enum>) parameterTypes[0], value.toUpperCase());
    }

    private static <T> T[] handleArray(Class<T> parameterType, Object mapValue) {
        if (mapValue != null && mapValue.getClass().isArray() && ((Object[]) mapValue).length == 0) {
            return (T[]) mapValue;
        }
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

