package org.arquillian.smart.testing.strategies.categorized;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;

class TagsParser extends AbstractParser {

    private static final String TAG = "org.junit.jupiter.api.Tag";
    private static final String TAGS = "org.junit.jupiter.api.Tags";
    private final Logger logger = Log.getLogger();

    TagsParser(CategorizedConfiguration strategyConfig) {
        super(strategyConfig);
    }

    @Override
    protected List<String> findCategories(Class<?> clazz) {

        return Arrays.stream(clazz.getAnnotations())
            .map(this::findJUnit5TagAnnotation)
            .filter(Objects::nonNull)
            .flatMap(this::retrieveTagFromAnnotation)
            .map(this::changeIfNonCaseSensitive)
            .collect(Collectors.toList());
    }

    @Override
    protected boolean isSpecified(String category, List<String> specifiedCategories) {
        return specifiedCategories.contains(category);
    }

    private Stream<String> retrieveTagFromAnnotation(Annotation tagsAnnotation) {
        final List<String> tags = new ArrayList<>();
        if (TAG.equals(tagsAnnotation.annotationType().getName())) {
            final String tagName = getTagName(tagsAnnotation);
            if (tagName != null) {
                tags.add(tagName);
            }
        } else {
            tags.addAll(getMultipleTagNames(tagsAnnotation));
        }
        return tags.stream();
    }

    private List<String> getMultipleTagNames(Annotation tagsAnnotation) {
        final List<String> tags = new ArrayList<>();
        try {
            Method valueMethod = tagsAnnotation.getClass().getMethod("value");
            final Annotation[] tagAnnotations = (Annotation[]) valueMethod.invoke(tagsAnnotation);
            tags.addAll(
                Arrays.stream(tagAnnotations)
                .filter(Objects::nonNull)
                .map(this::getTagName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
            );
        } catch (Exception e) {
            logger.warn("Something wrong happened when the annotation [%s] was being parsed: %s", tagsAnnotation,
                e.getMessage());
        }

        return tags;
    }

    private String getTagName(Annotation tagAnnotation) {
        try {
            Method valueMethod = tagAnnotation.getClass().getMethod("value");
            return (String) valueMethod.invoke(tagAnnotation);

        } catch (Exception e) {
            logger.warn("Something wrong happened when the annotation [%s] was being parsed: %s", tagAnnotation,
                e.getMessage());
        }
        return null;
    }

    private Annotation findJUnit5TagAnnotation(Annotation annotation) {
        String annotationName = annotation.annotationType().getName();
        boolean isDirectlyTagged = TAG.equals(annotationName) || TAGS.equals(annotationName);

        if (!isDirectlyTagged) {
            final Annotation[] metaAnnotations = annotation.annotationType().getAnnotations();

            for (Annotation metaAnnotation : metaAnnotations) {
                if (!metaAnnotation.annotationType().getName().startsWith("java.lang")) {
                    Annotation jUnit5Tag = findJUnit5TagAnnotation(metaAnnotation);

                    if (jUnit5Tag != null) {
                        return jUnit5Tag;
                    }
                }

            }

            return null;
        }

        return annotation;

    }
}

