package org.arquillian.smart.testing.ftest.testbed.configuration.builder;

import org.arquillian.smart.testing.configuration.Range;

public class RangeBuilder {
        private String head;
        private String tail;

        private final ScmBuilder builder;

        RangeBuilder(ScmBuilder builder) {
            this.builder = builder;
        }

        public RangeBuilder head(String head) {
            this.head = head;
            return this;
        }

        public RangeBuilder tail(String tail) {
            this.tail = tail;
            return this;
        }

        public ScmBuilder build() {
            final Range range = new Range();
            range.setHead(this.head);
            range.setTail(this.tail);

            return this.builder.setRange(range);
        }
    }
