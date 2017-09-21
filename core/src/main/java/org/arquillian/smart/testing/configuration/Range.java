package org.arquillian.smart.testing.configuration;

public class Range {

    private String head;
    private String tail;

    private Range(Builder builder) {
        this.head = builder.head;
        this.tail = builder.tail;
    }

    public Range() {
    }

    public void setTail(String tail) {
        this.tail = tail;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getHead() {
        return head;
    }

    public String getTail() {
        return tail;
    }

    public static Builder builder() {
        return new Range.Builder();
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

        public Range build() {
            return new Range(this);
        }
    }
}
