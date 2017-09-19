package org.arquillian.smart.testing.configuration;

public class ScmConfiguration {
    private String head;
    private String tail;

    ScmConfiguration(Builder builder) {
        this.head = builder.head;
        this.tail = builder.tail;
    }

    ScmConfiguration() {
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

    public static ScmConfiguration.Builder builder() {
        return new ScmConfiguration.Builder();
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

        public ScmConfiguration build() {
            return new ScmConfiguration(this);
        }
    }
}
