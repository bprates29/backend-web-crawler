package com.axreng.backend.util;

import java.util.UUID;

public class IdGenerator {

    private IdGenerator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String generateId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
