package org.arquillian.smart.testing.strategies.affected.fakeproject.main;

public class B {

    private C c;
    private D d;

    public B() {
        this.c = new C();
        this.d = new D();
    }

    public void doTask() {
       c.doTask();
       d.doTask();
    }

}
