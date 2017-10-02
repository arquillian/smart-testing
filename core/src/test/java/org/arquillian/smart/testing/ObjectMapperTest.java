package org.arquillian.smart.testing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.arquillian.smart.testing.configuration.ConfigurationItem;
import org.arquillian.smart.testing.configuration.ConfigurationSection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.arquillian.smart.testing.ObjectMapper.mapToObject;
import static org.arquillian.smart.testing.ObjectMapperTest.TestEnum.FOO;
import static org.assertj.core.api.Assertions.assertThat;

public class ObjectMapperTest {

    private Map<String, Object> map;

    @Before
    public void initMap() {
        map = new HashMap<>();
    }

    @Test
    public void should_set_integer_to_object() {
        // given
        map.put("i", 10);

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("i", 10);
    }

    @Test
    public void should_set_double_to_object() {
        // given
        map.put("d", 10.0);

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("d", 10.0);
    }

    @Test
    public void should_set_boolean_to_object() {
        // given
        map.put("b", true);

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("b", true);
    }

    @Test
    public void should_set_string_to_object() {
        // given
        map.put("s", "Hello");

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("s", "Hello");
    }

    @Test
    public void should_set_comma_separated_string_as_array_to_object() {
        // given
        map.put("as", "hello, bar");

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("as", new String[] {"hello", "bar"});
    }

    @Test
    public void should_set_list_to_object() {
        // given
        map.put("l", Arrays.asList("foo", "bar"));

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        assertThat(testObject).hasFieldOrPropertyWithValue("l", Arrays.asList("foo", "bar"));
    }

    @Test
    public void should_set_enum_to_object() {
        // given
        map.put("e", "foo");

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("e", FOO);
    }

    @Test
    public void should_set_nested_object_to_object() {
        // given
        Map<String, Object> innerObjectMap = new HashMap<>();
        innerObjectMap.put("b", true);
        innerObjectMap.put("as", Arrays.asList("foo", "bar"));
        innerObjectMap.put("l", Arrays.asList("foo", "bar"));
        map.put("dummyObject", innerObjectMap);

        final DummyObject dummyObject = new DummyObject();
        dummyObject.setB(true);
        dummyObject.setAs(new String[] {"foo", "bar"});
        dummyObject.setL(Arrays.asList("foo", "bar"));

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).isNotNull();
        assertThat(testObject.dummyObject).hasNoNullFieldsOrProperties();
        assertThat(testObject.dummyObject).isEqualToComparingFieldByField(dummyObject);
    }

    @Test
    public void should_set_map_to_object() {
        // given
        final Map<String, String> stringMap = Stream.of("foo", "bar")
            .collect(Collectors.toMap(Function.identity(), Function.identity()));
        map.put("m", stringMap);

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("m", stringMap);
    }

    @Test
    public void should_return_null_for_empty_map() {
        // when
        final TestObject testObject = mapToObject(TestObject.class, Collections.emptyMap());

        // then
        assertThat(testObject).isNull();
    }

    @After
    public void clearMap() {
        map.clear();
    }
    
    static class TestObject implements ConfigurationSection {
        private String s;
        private int i;
        private double d;
        private boolean b;
        private String[] as;
        private List<String> l;
        private Map<String, String> m;
        private TestEnum e;

        private DummyObject dummyObject;

        public void setS(String s) {
            this.s = s;
        }

        public void setI(Integer i) {
            this.i = i;
        }

        public void setD(Double d) {
            this.d = d;
        }

        public void setB(Boolean b) {
            this.b = b;
        }

        public void setAs(String[] as) {
            this.as = as;
        }

        public void setL(List<String> l) {
            this.l = l;
        }

        public void setE(TestEnum e) {
            this.e = e;
        }

        public void setM(Map<String, String> m) {
            this.m = m;
        }

        public void setDummyObject(DummyObject dummyObject) {
            this.dummyObject = dummyObject;
        }

        @Override
        public List<ConfigurationItem> registerConfigurationItems() {
            return null;
        }
    }

    static class DummyObject {
        private boolean b;
        private String[] as;
        private List<String> l;

        public void setB(boolean b) {
            this.b = b;
        }

        public void setAs(String[] as) {
            this.as = as;
        }

        public void setL(List<String> l) {
            this.l = l;
        }
    }

    enum TestEnum {
        FOO, BAR;
    }
}
