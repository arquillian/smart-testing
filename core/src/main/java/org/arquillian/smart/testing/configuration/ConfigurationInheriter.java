package org.arquillian.smart.testing.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.arquillian.smart.testing.configuration.Configuration.DISABLE;
import static org.arquillian.smart.testing.configuration.Configuration.INHERIT;

class ConfigurationInheriter {

    Configuration overWriteNotDefinedValuesFromInherit(Configuration configuration, Path currentDir) {
        final String inherit = configuration.getInherit();
        if (inherit == null) {
            return configuration;
        } else {
            final List<String> fieldsToInherit = fieldNamesToInherit(configuration);
            final Path inheritPath = currentDir.resolve(inherit);
            final ConfigurationReader configurationReader = new ConfigurationReader();
            final Map<String, Object> inheritedParameters = configurationReader.getConfigParametersFromFile(inheritPath);
            if (inheritedParameters.isEmpty()) {
                configuration.setInherit(null);
                return configuration;
            }
            final Map<String, Object> parametersToInherit = inheritedParameters.entrySet().stream()
                .filter(entry -> fieldsToInherit.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            List<ConfigurationItem> configItems = configuration.registerConfigurationItems();

            final ObjectMapper objectMapper = new ObjectMapper();
            Arrays.stream(configuration.getClass().getMethods())
                .filter(method -> fieldsToInherit.contains(fieldName(method)) && objectMapper.isSetter(method))
                .forEach(method -> objectMapper.invokeMethodWithMappedValue(configItems, method, configuration,
                    parametersToInherit));

            configuration.setInherit((String) inheritedParameters.get(INHERIT));

            return overWriteNotDefinedValuesFromInherit(configuration, inheritPath.getParent());
        }
    }

    private List<String> fieldNamesToInherit(Configuration configuration) {
        return Arrays.stream(configuration.getClass().getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()) &&
                !hasFieldToIgnore(field) && hasValueNotDefined(field, configuration))
            .map(Field::getName)
            .collect(Collectors.toList());
    }

    private boolean hasFieldToIgnore(Field field) {
        return Arrays.asList(DISABLE, INHERIT).contains(field.getName());
    }

    private boolean hasValueNotDefined(Field field, Configuration configuration) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Configuration defaultConfiguration = objectMapper.readValue(Configuration.class, Collections.emptyMap());

        field.setAccessible(true);
        try {
            final Object actualValue = field.get(configuration);
            final Object defaultValue = field.get(defaultConfiguration);
            return actualValue == null || actualValue.equals(defaultValue) ||
                (field.getType().isArray() && Arrays.equals(((Object[]) actualValue), ((Object[]) defaultValue)));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access fieldName: " + field, e);
        }
    }

    private String fieldName(Method method) {
        final String field = method.getName().substring(3);
        return Character.toLowerCase(field.charAt(0)) + field.substring(1);
    }
}
