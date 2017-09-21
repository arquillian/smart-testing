package org.arquillian.smart.testing.configuration;

import static org.arquillian.smart.testing.scm.ScmRunnerProperties.HEAD;

public class Scm {
    private String head;
    private String tail;

    Scm(Builder builder) {
        this.head = builder.head;
        this.tail = builder.tail;
    }

    Scm() {
    }

    public void setHead(String head) {
        this.head = head;
    }

    public void setTail(String tail) {
        this.tail = tail;
    }

    public String getHead() {
        return head;
    }

    public String getTail() {
        return tail;
    }

    public static Scm.Builder builder() {
        return new Scm.Builder();
    }

    public static class Builder {
        private String head;
        private String tail;

        public Builder head(String head) {
            this.head = head;
            return this;
        }

        public Builder tail(String tail) {
            this.tail = tail;
            return this;
        }

        public Builder lastChanges(String n) {
            this.head = HEAD;
            this.tail = String.join("~", HEAD, n);
            return this;
        }

        public Scm build() {
            return new Scm(this);
        }
    }
}
