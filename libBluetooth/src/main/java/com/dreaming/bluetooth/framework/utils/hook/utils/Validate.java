package com.dreaming.bluetooth.framework.utils.hook.utils;

public class Validate {
    public static void isTrue(final boolean expression, final String message, final Object... values) {
        if (expression == false) {
            throw new IllegalArgumentException(String.format(message, values));
        }
    }
}
