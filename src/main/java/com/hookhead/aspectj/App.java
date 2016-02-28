package com.hookhead.aspectj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        LOGGER.info("Main");

        outer("main1", "main2", "main3");
    }

    @TracyLogger
    protected static String outer(String param1, String param2, String param3) {
        inner("outer1");

        return "outer";
    }

    @TracyLogger
    private static String inner(String param) {
        return "inner";
    }
}
