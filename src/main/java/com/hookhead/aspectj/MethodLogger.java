package com.hookhead.aspectj;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


@Aspect
public class MethodLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodLogger.class);

    @Around("execution(* *(..)) && @annotation(TracyLogger)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        MethodSignature methodSignature = MethodSignature.class.cast(point.getSignature());
        String methodName = methodSignature.getName();

        LOGGER.info("Tracing method " + methodName);

        TracyLogger tracyLogger = methodSignature.getMethod().getAnnotation(TracyLogger.class);
        List<String> traceParameters = new ArrayList<>(Arrays.asList(tracyLogger.params()));

        boolean traceAll = false;
        String[] parameterNames = tracyLogger.params();
        if (parameterNames.length == 1 && parameterNames[0].equals("..")) {
            traceAll = true;
        }

        int count = 0;
        for (String paramName : methodSignature.getParameterNames()) {
            if (traceAll || traceParameters.contains(paramName)) {
                Object argValue = point.getArgs()[count];
                LOGGER.info("\tParameter:" + paramName + "=" + argValue);
            }
            count++;
        }

        Object result = point.proceed();

        LOGGER.info("\tReturn:" + result);

        return result;
    }
}
