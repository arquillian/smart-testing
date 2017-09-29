package org.arquillian.smart.testing;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectMergerTest {

    @Test
    public void should_set_all_fields_from_first_object_as_available() {
        // given
        final TestObject testObject1 = new TestObject();
        testObject1.setString("system property");
        testObject1.setB(false);
        testObject1.setD(1000.0);
        testObject1.setDummyObject(DummyObject.build(true));
        testObject1.setI(1000);

        final TestObject testObject2 = new TestObject();
        testObject2.setString("test2");
        testObject2.setB(true);
        testObject2.setI(100);

        final TestObject testObject3 = new TestObject();

        // when
        final ObjectMerger<TestObject> objectMerger = new ObjectMerger<>(testObject1, testObject2, testObject3);
        final TestObject mergedObject = objectMerger.merge();

        // then
        assertThat(mergedObject).isEqualToComparingFieldByField(testObject1);
    }

    @Test
    public void should_set_available_fields_from_second_object_if_it_is_not_available_in_first() throws InstantiationException, IllegalAccessException {
        // given
        final TestObject testObject1 = new TestObject();
        testObject1.setD(1000.0);
        testObject1.setDummyObject(DummyObject.build(true));

        final TestObject testObject2 = new TestObject();
        testObject2.setString("configuration");
        testObject2.setStrings(new String[] {"configuration"});
        testObject2.setB(true);
        testObject2.setI(100);

        final TestObject testObject3 = new TestObject();

        TestObject expectedObject = new TestObject();
        expectedObject.setD(1000.0);
        expectedObject.setDummyObject(DummyObject.build(true));
        expectedObject.setString("configuration");
        expectedObject.setStrings(new String[] {"configuration"});
        expectedObject.setB(true);
        expectedObject.setI(100);

        // when
        final ObjectMerger<TestObject> objectMerger = new ObjectMerger<>(testObject1, testObject2, testObject3);
        final TestObject mergedObject = objectMerger.merge();

        // then
        assertThat(mergedObject).isEqualToComparingFieldByFieldRecursively(expectedObject);
    }

    @Test
    public void should_set_available_fields_from_third_object_if_it_is_not_available_in_first_two() throws InstantiationException, IllegalAccessException {
        // given
        final TestObject testObject1 = new TestObject();

        final TestObject testObject2 = new TestObject();
        testObject2.setString("configuration");
        testObject2.setB(true);

        TestObject expectedObject = new TestObject();
        expectedObject.setString("configuration");
        expectedObject.setB(true);
        expectedObject.setI(10);
        expectedObject.setD(10.0);
        expectedObject.setDummyObject(DummyObject.build(false));
        expectedObject.setStrings(new String[] {"default"});

        // when
        final ObjectMerger<TestObject> objectMerger = new ObjectMerger<>(testObject1, testObject2, TestObject.getDefault());
        final TestObject mergedObject = objectMerger.merge();

        // then
        assertThat(mergedObject).isEqualToComparingFieldByFieldRecursively(expectedObject);
    }


    @Test
    public void should_overwrite_empty_string_array_from_first_object_with_second_object() {
        final TestObject testObject1 = new TestObject();
        testObject1.setStrings(new String[0]);

        final TestObject testObject2 = new TestObject();
        testObject2.setStrings(new String[] {"hello"});

        // when
        final ObjectMerger<TestObject> objectMerger = new ObjectMerger<>(testObject1, testObject2, TestObject.getDefault());
        final TestObject mergedObject = objectMerger.merge();

        // then
        assertThat(mergedObject.strings).isEqualTo(new String[] {"hello"});
    }

    @Test
    public void should_return_null_for_all_null_objects() {
        final ObjectMerger<TestObject> objectMerger = new ObjectMerger<>(null, null, null);
        final Object merged = objectMerger.merge();

        assertThat(merged).isNull();
    }

    static class TestObject {
        private int i;
        private double d;
        private Boolean b;

        private String string;
        private String[] strings;
        private DummyObject dummyObject;

        void setI(int i) {
            this.i = i;
        }

        void setD(double d) {
            this.d = d;
        }

        void setB(boolean b) {
            this.b = b;
        }

        void setString(String s) {
            this.string = s;
        }

        void setStrings(String[] strings) {
            this.strings = strings;
        }

        void setDummyObject(DummyObject dummyObject) {
            this.dummyObject = dummyObject;
        }

        static TestObject getDefault() {
            final TestObject defaultObject = new TestObject();
            defaultObject.setB(false);
            defaultObject.setD(10.0);
            defaultObject.setI(10);
            defaultObject.setString("default");
            defaultObject.setStrings(new String[] {"default"});
            defaultObject.setDummyObject(DummyObject.build(false));

            return defaultObject;
        }
    }

    static class DummyObject {

        private Boolean dummy;

        DummyObject(boolean dummy) {
            this.dummy = dummy;
        }

        static DummyObject build(boolean b) {
            return new DummyObject(b);
        }

    }
}


