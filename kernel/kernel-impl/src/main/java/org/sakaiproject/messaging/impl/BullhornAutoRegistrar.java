package org.sakaiproject.messaging.impl;

import org.sakaiproject.messaging.api.BullhornHandler;
import org.sakaiproject.messaging.api.MessagingService;
import org.sakaiproject.messaging.api.SelfRegisteringBullhornHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * Spring-aware post-processor to catch beans that implement the {@link BullhornHandler} interface but do not
 * self-register (as indicated by the marker interface {@link SelfRegisteringBullhornHandler}). There are likely no such
 * beans, and everything should move to explicit registration since the {@link MessagingService} now supports runtime
 * registration. The {@link org.sakaiproject.messaging.api.bullhornhandlers.AbstractBullhornHandler} handles it
 * automatically, so there is no extra effort on behalf of handler implementors.
 */
@Deprecated
public class BullhornAutoRegistrar implements DestructionAwareBeanPostProcessor {
    /**
     * The messaging service we will register with.
     */
    final MessagingService messagingService;

    @Autowired
    public BullhornAutoRegistrar(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    /**
     * Passthrough because we do not need to modify or wrap the bean.
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * Register the bean with the MessagingService.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (needsAutoRegistration(bean))
            messagingService.registerHandler((BullhornHandler) bean);

        return bean;
    }

    /**
     * Unregister the handler while being destroyed.
     */
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (needsAutoRegistration(bean))
            messagingService.unregisterHandler((BullhornHandler) bean);
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
     * @param bean a bean; we only care if it's a BullhornHandler
     * @return whether the bean implements the bare interface, indicating that it will not register itself
     */
    boolean needsAutoRegistration(Object bean) {
        return (bean instanceof BullhornHandler) && !(bean instanceof SelfRegisteringBullhornHandler);
    }
}
