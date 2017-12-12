package org.arquillian.smart.testing.ftest.testbed.configuration;

public class Strategy {
    public static final Strategy AFFECTED = new Strategy("affected");
    public static final Strategy NEW = new Strategy("new");
    public static final Strategy CHANGED = new Strategy("changed");
    public static final Strategy FAILED = new Strategy("failed");
    public static final Strategy CATEGORIZED = new Strategy("categorized");

    private final String name;

    public Strategy(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name.toLowerCase();
    }

}
