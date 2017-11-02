package org.arquillian.smart.testing.strategies.affected;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to set which production classes are tested in current test.
 * By default it appends all classes defined in all attributes.
 *
 * If none of the attributes are set, then all production classes with same package as test and its subpackages are added.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(ComponentsUnderTest.class)
@Documented
public @interface ComponentUnderTest {

    /**
     * Packages of classes that needs to be added as tested classes in current test. You can set the package name "org.superbiz" which means only classes defined in this package,
     * or ending with start (*) operator "org.superbiz.*" which means all classes of current package and its subpackages.
     * @return Packages containing Java classes.
     */
    String[] packages() default {};

    /**
     * Packages of classes that needs to be added as tested classes in current test. It is used Class to get the package.
     * Notice that in this case subpackages are not scanned.
     * @return Packages containing Java classes.
     */
    Class[] packagesOf() default {};

    /**
     * Classes to be added as tested classes in current test.
     * @return Classes
     */
    Class[] classes() default {};

}
