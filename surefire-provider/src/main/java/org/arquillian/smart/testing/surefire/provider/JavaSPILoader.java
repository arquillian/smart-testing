package org.arquillian.smart.testing.surefire.provider;

public interface JavaSPILoader {

    <S> Iterable<S> load(Class<S> service);
}
