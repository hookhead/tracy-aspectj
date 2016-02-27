package com.hookhead.aspectj;

public class App 
{
    public static void main(String[] args) {
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
