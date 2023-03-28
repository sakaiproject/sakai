/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.messaging.impl;

import org.sakaiproject.messaging.api.UserNotificationHandler;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.SelfRegisteringUserNotificationHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * Spring-aware post-processor to catch beans that implement the {@link UserNotificationHandler} interface but do not
 * self-register (as indicated by the marker interface {@link SelfRegisteringUserNotificationHandler}). There are likely no such
 * beans, and everything should move to explicit registration since the {@link UserMessagingService} now supports runtime
 * registration. The {@link org.sakaiproject.messaging.api.AbstractUserNotificationHandler} handles it
 * automatically, so there is no extra effort on behalf of handler implementors.
 */
@Deprecated
public class UserNotificationAutoRegistrar implements DestructionAwareBeanPostProcessor {

    /**
     * The user messaging service we will register with.
     */
    final UserMessagingService userMessagingService;

    @Autowired
    public UserNotificationAutoRegistrar(UserMessagingService userMessagingService) {
        this.userMessagingService = userMessagingService;
    }

    /**
     * Passthrough because we do not need to modify or wrap the bean.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * Register the bean with the UserMessagingService.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (needsAutoRegistration(bean)) {
            userMessagingService.registerHandler((UserNotificationHandler) bean);
        }

        return bean;
    }

    /**
     * Unregister the handler while being destroyed.
     */
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {

        if (needsAutoRegistration(bean)) {
            userMessagingService.unregisterHandler((UserNotificationHandler) bean);
        }
    }

    /**
     * We unregister any handlers that are not "self registering".
     *
     * @param bean the bean instance to check
     * @return true if the bean needs to be auto-registered
     */
    @Override
    public boolean requiresDestruction(Object bean) {
        return needsAutoRegistration(bean);
    }

    /**
     * Determine whether this bean needs implicit auto-registration.
     *
     * @param bean a bean; we only care if it's a UserNotificationHandler
     * @return whether the bean implements the bare interface, indicating that it will not register itself
     */
    boolean needsAutoRegistration(Object bean) {
        return (bean instanceof UserNotificationHandler) && !(bean instanceof SelfRegisteringUserNotificationHandler);
    }
}
