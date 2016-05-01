package com.hookhead.aspectj;

import com.apm4all.tracy.Tracy;
import com.apm4all.tracy.extensions.annotations.Profiled;
import com.apm4all.tracy.extensions.annotations.Qualify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static final String TASK_ID_VALUE = "taskId";
    private static final String PARENT_OPT_ID_VALUE = "parentOptId";
    private static final String COMPONENT_VALUE = "component";

    public static void main(String[] args) {

        Tracy.setContext(TASK_ID_VALUE, PARENT_OPT_ID_VALUE, COMPONENT_VALUE);

        outer("main1", "main2", "main3");

        List<String> events = Tracy.getEventsAsJson();
        for (String event : events) {
            LOGGER.info(event);
        }

        Tracy.clearContext();
    }

    @Profiled(annotations = {"param1", "param2", "param3"} ,name = "outer", qualify = Qualify.PACKAGE)
    protected static String outer(String param1, String param2, String param3) {
        inner("outer1");

        return "outer";
    }

    @Profiled(annotations = "..")
    private static String inner(String param) {
        return "inner";
    }
}
