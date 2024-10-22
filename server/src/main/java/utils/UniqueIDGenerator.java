package utils;

import java.util.concurrent.atomic.AtomicInteger;

public class UniqueIDGenerator {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static int generateUniqueId() {
        return COUNTER.incrementAndGet();
    }
}