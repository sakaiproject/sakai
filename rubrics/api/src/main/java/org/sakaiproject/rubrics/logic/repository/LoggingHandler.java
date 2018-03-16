package org.sakaiproject.rubrics.logic.repository;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingHandler {

    Logger log = LoggerFactory.getLogger(LoggingHandler.class);

    @Pointcut("this(org.springframework.data.repository.Repository)")
    public void repository() {
    }

    @Pointcut("execution(* *.*(..))")
    protected void allMethod() {
    }

    //before -> Any resource annotated with @Repository annotation and all method and function
    @Before("repository() && allMethod()")
    public void logBefore(JoinPoint joinPoint) {
        log.debug("Entering in Method :  " + joinPoint.getSignature().getName() + " at " + System.currentTimeMillis());
        Object[] args = joinPoint.getArgs();
        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            log.debug("  " + argIndex + " = " + args[argIndex]);
        }
    }

    //After -> All method within resource annotated with @Repository annotation
    @AfterReturning(pointcut = "repository() && allMethod()")
    public void logAfter(JoinPoint joinPoint) {
        log.debug("Method Returned at : " + System.currentTimeMillis());
    }
}
