package org.arquillian.smart.testing.configuration;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.arquillian.smart.testing.configuration.Configuration.DISABLE;
import static org.arquillian.smart.testing.configuration.Configuration.INHERIT;
import static org.arquillian.smart.testing.configuration.ConfigurationLoader.getConfigParametersFromFile;

class ObjectMapper {

    private boolean userSetProperty = true;

    static <T extends ConfigurationSection> T mapToObject(Class<T> aClass, Map<String, Object> map) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(aClass, map);
    }

    Configuration overWriteDefaultPropertiesFromParent(Configuration configuration, Path currentDir) {
        final String inherit = configuration.getInherit();
        if (inherit == null) {
            return configuration;
        } else {
            List<String> fieldNamesWithDefaultValue = fieldNamesWithDefaultValues(configuration);

            final Path inheritPath = currentDir.resolve(inherit);
            final Map<String, Object> parameters = getConfigParametersFromFile(inheritPath);
            if (parameters.isEmpty()) {
                configuration.setInherit(null);
                return configuration;
            }
            final Map<String, Object> map = parameters.entrySet().stream()
                .filter(entry -> fieldNamesWithDefaultValue.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            List<ConfigurationItem> configItems = configuration.registerConfigurationItems();

            Arrays.stream(configuration.getClass().getMethods())
                .filter(method -> fieldNamesWithDefaultValue.contains(fieldName(method)) && isSetter(method))
                .forEach(method -> invokeMethodWithMappedValue(configItems, method, configuration, map));

            configuration.setInherit((String) parameters.get(INHERIT));

            return overWriteDefaultPropertiesFromParent(configuration, inheritPath.getParent());
        }
    }

    <T extends ConfigurationSection> T readValue(Class<T> aClass, Map<String, Object> map) {
        T instance;
        try {
            instance = aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to create new instance of class " + aClass, e);
        }

        List<ConfigurationItem> configItems = instance.registerConfigurationItems();

        Arrays.stream(aClass.getMethods()).filter(this::isSetter)
            .forEach(method -> invokeMethodWithMappedValue(configItems, method, instance, map));

        return instance;
    }

    private List<String> fieldNamesWithDefaultValues(Configuration configuration) {
        List<Field> fieldsWithDefaultValue = new ArrayList<>();
        Arrays.stream(configuration.getClass().getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .forEach(field -> addConfigurationFieldsWithDefaultValue(field, configuration, fieldsWithDefaultValue));

        return fieldsWithDefaultValue.stream().map(Field::getName).collect(Collectors.toList());
    }

    private void addConfigurationFieldsWithDefaultValue(Field field, Configuration configuration,
        List<Field> defaultValues) {
        if (field.getName().equals(DISABLE) || field.getName().equals(INHERIT)) {
            return;
        }
        final ConfigurationSection defaultConfiguration = mapToDefaultObject(Configuration.class, new HashMap<>(0));
        field.setAccessible(true);
        try {
            final Object actualValue = field.get(configuration);
            final Object defaultValue = field.get(defaultConfiguration);
            if (actualValue == null && defaultValue == null) {
                defaultValues.add(field);
                return;
            }
            if (field.getType().isArray() && Arrays.equals(((Object[]) actualValue), ((Object[]) defaultValue))) {
                defaultValues.add(field);
                return;
            }
            if (actualValue != null && actualValue.equals(defaultValue)) {
                defaultValues.add(field);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to access fieldName: " + field, e);
        }
    }

    private String fieldName(Method method) {
        final String field = method.getName().substring(3);
        return Character.toLowerCase(field.charAt(0)) + field.substring(1);
    }

    private <T extends ConfigurationSection> T mapToDefaultObject(Class<T> aClass, Map<String, Object> map) {
        this.userSetProperty = false;
        final T defaultObject = readValue(aClass, map);
        this.userSetProperty = true;
        return defaultObject;
    }

    private <T> void invokeMethodWithMappedValue(List<ConfigurationItem> configItems, Method method, T instance,
        Map<String, Object> map) {
        method.setAccessible(true);
        if (method.getParameterTypes().length != 1) {
            return;
        }
        final String field = method.getName().substring(3);
        final String property = Character.toLowerCase(field.charAt(0)) + field.substring(1);
        Object configFileValue = map.get(property);

        Optional<ConfigurationItem> foundConfigItem =
            configItems.stream().filter(item -> property.equals(item.getParamName())).findFirst();

        Object converted = getConvertedObject(method, configFileValue, foundConfigItem);

        try {
            if (converted != null) {
                method.invoke(instance, converted);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to invoke method " + method, e);
        }
    }

    private Object getConvertedObject(Method method, Object configFileValue,
        Optional<ConfigurationItem> foundConfigItem) {
        if (!foundConfigItem.isPresent()) {
            Class<?> parameterType = method.getParameterTypes()[0];
            if (!ConfigurationSection.class.isAssignableFrom(parameterType)) {
                return null;
            } else if (configFileValue == null) {
                return readValue((Class<ConfigurationSection>) parameterType, new HashMap<>(0));
            } else {
                return readValue((Class<ConfigurationSection>) parameterType, (Map<String, Object>) configFileValue);
            }
        } else {
            Object mappedValue = null;
            ConfigurationItem configItem = foundConfigItem.get();
            if (this.userSetProperty) {
                mappedValue = getUserSetProperty(method, configItem, configFileValue);
            }
            if (mappedValue == null && configItem.getDefaultValue() != null) {
                mappedValue = configItem.getDefaultValue();
            }
            if (mappedValue != null) {
                return convert(method, mappedValue);
            }
        }
        return null;
    }

    private Object getUserSetProperty(Method method, ConfigurationItem configItem, Object configFileValue) {
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

    private List<Object> createMultipleOccurrenceProperty(Method method, ConfigurationItem configItem,
        Object configFileValue) {

        String sysPropKey = configItem.getSystemProperty().substring(0, configItem.getSystemProperty().lastIndexOf('.'));
        Map<Object, Object> systemProperties =
            System.getProperties().entrySet()
                .stream()
                .filter(prop -> prop.getKey().toString().startsWith(sysPropKey))
                .collect(Collectors.toMap(prop -> prop.getKey(), prop -> prop.getValue()));

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

    private List<Object> getValuesFromFile(Method method, String sysPropKey, Object configFileValue,
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

    private boolean isSetBySysProperty(Object param, String sysPropKey, Map<Object, Object> systemProperties) {
        String[] paramSplit = String.valueOf(param).split("=");
        if (paramSplit.length == 2) {
            String key = paramSplit[0];
            return systemProperties.containsKey(key) || systemProperties.containsKey(String.join(".", sysPropKey, key));
        }
        return false;
    }

    private Object convert(Method method, Object mappedValue) {
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
            return readValue((Class<ConfigurationSection>) parameterType, (Map<String, Object>) mappedValue);
        } else {
            return convertToType(parameterType, mappedValue.toString());
        }
    }

    private Enum handleEnum(Method method, Object mapValue) {
        if (mapValue.getClass().isEnum()) {
            return (Enum) mapValue;
        }
        final Class<?>[] parameterTypes = method.getParameterTypes();
        String value = (String) mapValue;
        return Enum.valueOf((Class<Enum>) parameterTypes[0], value.toUpperCase());
    }

    private <T> T[] handleArray(Class<T> parameterType, Object mapValue) {
        if (mapValue != null && mapValue.getClass().isArray() && ((Object[]) mapValue).length == 0) {
            return (T[]) mapValue;
        }
        List<T> convertedList = getConvertedList(parameterType, mapValue);
        T[] array = (T[]) Array.newInstance(parameterType, convertedList.size());
        return convertedList.toArray(array);
    }

    private Object handleList(Method method, Object mappedValue) {
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

    private <T> Object convertToType(Class<T> clazz, String mappedValue) {
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

    private <T> List<T> getConvertedList(Class<T> parameterType, Object mappedValue) {
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

    private boolean isSetter(Method candidate) {
        return candidate.getName().matches("^(set|add)[A-Z].*")
            && (candidate.getReturnType().equals(Void.TYPE) || candidate.getReturnType()
            .equals(candidate.getDeclaringClass()))
            && candidate.getParameterTypes().length > 0;
    }
}

