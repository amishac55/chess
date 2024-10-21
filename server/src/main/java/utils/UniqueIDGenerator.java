package utils;

import java.util.concurrent.atomic.AtomicInteger;

public class UniqueIDGenerator {

    private static final AtomicInteger counter = new AtomicInteger(0);

    public static int generateUniqueId() {
        return counter.incrementAndGet();
    }
}