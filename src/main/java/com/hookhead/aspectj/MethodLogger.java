package com.hookhead.aspectj;

import com.apm4all.tracy.Tracy;
import com.apm4all.tracy.extensions.annotations.Constants;
import com.apm4all.tracy.extensions.annotations.Profiled;
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

    @Around("execution(* *(..)) && @annotation(Profiled)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        //
        // Get method name
        //

        MethodSignature methodSignature = MethodSignature.class.cast(point.getSignature());
        Profiled profiled = methodSignature.getMethod().getAnnotation(Profiled.class);

        StringBuilder methodNameBuilder = new StringBuilder();
        switch(profiled.qualify()) {
            case NO:
                break;
            case CLASS:
                methodNameBuilder.append(methodSignature.getDeclaringType().getSimpleName());
                methodNameBuilder.append(".");
                break;
            case PACKAGE:
                methodNameBuilder.append(methodSignature.getDeclaringType().getCanonicalName());
                methodNameBuilder.append(".");
                break;
        }

        String methodName = profiled.name();
        if (methodName.equals("")) {
            methodName = methodSignature.getName();
        }
        methodNameBuilder.append(methodName);
        String qualifiedMethodName = methodNameBuilder.toString();

        LOGGER.info("Qualified Method Name = " + qualifiedMethodName);

        //
        // Get the annotations
        //

        boolean traceAll = false;
        String[] parameterNames = profiled.annotations();
        if (parameterNames.length == 1 && parameterNames[0].equals(Constants.ALL_PARAMETERS)) {
            traceAll = true;
        }

        LOGGER.info("TraceAll = " + traceAll);

        int count = 0;
        List<String> annotations = new ArrayList<>(Arrays.asList(profiled.annotations()));

        //
        // Tracy.before
        //

        Tracy.before(qualifiedMethodName);

        //
        // Tracy.annotate
        //
        for (String paramName : methodSignature.getParameterNames()) {
            if (traceAll || annotations.contains(paramName)) {
                Object argValue = point.getArgs()[count];
                Tracy.annotate(paramName, argValue.toString());
                LOGGER.info("\tParameter:" + paramName + "=" + argValue);
            }
            count++;
        }

        Object result;
        try {
            result = point.proceed();
        } catch(Throwable t) {
            List<Class> includeExceptions = new ArrayList<>(Arrays.asList(profiled.include()));
            if (includeExceptions.size() == 0 || includeExceptions.contains(t.getClass())) {
                List<Class> excludeExceptions = new ArrayList<>(Arrays.asList(profiled.exclude()));
                if (!excludeExceptions.contains(t.getClass())) {
                    switch (profiled.onException()) {
                        case DO_NOTHING:
                            break;
                        case POP:
                            // FIXME
                            break;
                        case MARK_FRAME:
                            // FIXME
                            break;
                        case MARK_UP_TO_THE_ROOT_FRAME:
                            // FIXME
                            break;
                        default:
                            LOGGER.error("Unhandled case in switch " + profiled.onException());
                            break;
                    }
                }
            }

            throw t;
        }

        if (profiled.captureOutput()) {
            LOGGER.info("Adding '" + Constants.METHOD_RESULT + "' attribute " + result);
            Tracy.annotate(Constants.METHOD_RESULT, result.toString());
        }

        Tracy.after(qualifiedMethodName);

        LOGGER.info("\tReturn:" + result);

        return result;
    }
}
