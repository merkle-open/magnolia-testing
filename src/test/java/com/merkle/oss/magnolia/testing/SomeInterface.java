package com.merkle.oss.magnolia.testing;

import info.magnolia.jcr.util.PropertyUtil;

import javax.jcr.Node;

public interface SomeInterface {
    String someMethod(Node node);

    class SomeImplementation implements SomeInterface {
        @Override
        public String someMethod(final Node node) {
            return PropertyUtil.getString(node, "someProperty");
        }
    }

    class SomeOtherImplementation implements SomeInterface {
        @Override
        public String someMethod(final Node node) {
            return "some other implementation!";
        }
    }
}
