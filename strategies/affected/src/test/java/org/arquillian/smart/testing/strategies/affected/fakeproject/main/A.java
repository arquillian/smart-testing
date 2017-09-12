package org.arquillian.smart.testing.strategies.affected.fakeproject.main;

public class A {

    private final B b;

    public A() {
        b = new B();
    }

    public void doTask() {
        b.doTask();
    }

}
