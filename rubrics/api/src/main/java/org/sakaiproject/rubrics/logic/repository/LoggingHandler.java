/**
 * Copyright (c) 2003-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.rubrics.logic.repository;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingHandler {

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
