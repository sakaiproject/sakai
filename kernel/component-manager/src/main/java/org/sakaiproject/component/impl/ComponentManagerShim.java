package org.sakaiproject.component.impl;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.api.ComponentManager;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class ComponentManagerShim extends SpringCompMgr {
    /**
     * Initialize.
     *
     * @param parent A ComponentManager in which this one gets nested, or NULL if
     *               this is this top one.
     */
    public ComponentManagerShim(ComponentManager parent) {
        super(parent);
    }

    @Override
    public void init(boolean lateRefresh) {
        log.info("init called -- doing nothing.");
    }

    @Override
    public void close() {
        log.info("close called -- doing nothing.");
    }

    @Override
    public void loadComponent(Class iface, Object component) {
        log.error("Attempted to load component in shim: {}", iface);
    }

    @Override
    protected void loadComponents() {
        log.error("Attempted to loadComponents, which is impl-internal.");
    }
}