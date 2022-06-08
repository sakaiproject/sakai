package org.sakaiproject.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.modi.GlobalApplicationContext;
import org.sakaiproject.modi.SharedApplicationContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.Properties;
import java.util.Set;

/**
 * An adapter to stand in for SpringCompMgr in the static cover as well as be registered
 * in the ApplicationContext. It drops ineffectual lifecycle calls and forwards the meaningful
 * calls to the ApplicationContext. The cover will instantiate with the global context, analogous
 * to the previous behavior, but a specific context can be supplied to accommodate refactoring of
 * some existing tests.
 *
 * @deprecated This class is intended to be a transitional adapter so existing code can migrate
 *             toward using the native Java/Spring mechanisms. Ideally, all of the ComponentManager
 *             machinery will be retired and replaced with more direct injection, or failing that,
 *             service location directly through the ApplicationContext.
 */
@Slf4j
@Deprecated
public class ComponentManagerShim extends SpringCompMgr {
    private final SharedApplicationContext applicationContext;

    /**
     * Create a ComponentManager shim that forwards all meaningful methods to the ApplicationContext.
     *
     * @param applicationContext The delegate context; typically global
     */
    public ComponentManagerShim(SharedApplicationContext applicationContext) {
        super(null);
        this.applicationContext = applicationContext;
    }
    /**
     * Required constructor for shimming -- parent is ignored at SpringCompMgr, and so it goes here.
     *
     * Any nesting support that was intended is replaced by hierarchical application contexts.
     *
     * If this signature is called, the GlobalApplicationContext singleton will be retrieved.
     */
    @Deprecated
    public ComponentManagerShim(ComponentManager parent) {
        this(GlobalApplicationContext.getContext());
    }

    /**
     * In strict mode, we are looking for straggling uses of the ComponentManager.
     *
     * Set warnOnAllCalls to WARN on every method, which will start very noisy, but decreasingly so,
     * as we transition code to using Spring directly.
     */
    @Getter @Setter private boolean warnOnAllCalls = false;

    /**
     * In strict mode, we are looking for straggling uses of the ComponentManager.
     *
     * Set throwOnAllCalls to refuse to forward any calls, but throw an exception. This will
     * break everything, but give us a blunt instrument in tracking down any obscured uses.
     */
    @Getter @Setter private boolean throwOnAllCalls = false;

    @Override
    public void init(boolean lateRefresh) {
        trace("init(" + lateRefresh + ")");
    }

    @Override
    public <T> T get(Class<T> iface) {
        trace("get(" + iface.getName() + ".class)");
        try {
            return applicationContext.getBean(iface);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Could not locate bean requested by class, nullifying: {}", iface.getName());
            return null;
        }
    }

    @Override
    public Object get(String ifaceName) {
        trace("get(\"" + ifaceName + "\")");
        try {
            return applicationContext.getBean(ifaceName);
        } catch (NoSuchBeanDefinitionException e) {
            log.error("Could not locate bean requested by name, nullifying: {}", ifaceName);
            return null;
        }
    }

    @Override
    public boolean contains(Class iface) {
        trace("contains(" + iface.getName() + ".class)");
        return applicationContext.containsBean(iface.getName());
    }

    @Override
    public boolean contains(String ifaceName) {
        trace("contain(\"" + ifaceName + "\")");
        return applicationContext.containsBean(ifaceName);
    }

    @Override
    public Set getRegisteredInterfaces() {
        trace("getRegisteredInterfaces()");
        return Set.of(applicationContext.getBeanDefinitionNames());
    }

    @Override
    public void close() {
        trace("close()");
    }

    @Override
    public void loadComponent(Class iface, Object component) {
        trace("loadComponent(" + iface.getName() + ".class, [Object])");
    }

    @Override
    public void loadComponent(String ifaceName, Object component) {
        trace("loadComponent(\"" + ifaceName + "\", [Object])");
    }

    /**
     * loadComponents() is peculiar, since someone would have had to cast/extend SpringCompMgr,
     * rather than using the interface. We know that has happened in attempts to support tests
     * or specialized wiring. In any case, we will have already fully loaded, so this is a no-op.
     */
    @Override
    protected void loadComponents() {
        trace("loadComponents()");
    }

    @Override
    public synchronized void addChildAc() {
        trace("addChildAc()");
    }

    @Override
    public synchronized void removeChildAc() {
        trace("removeChildAc()");
    }

    /**
     * getConfig() is strange because it's been a deprecated no-op for years. This is the one
     * method we send up to super, since it has existing log behavior.
     */
    @Override
    public Properties getConfig() {
        trace("getConfig()");
        return super.getConfig();
    }

    @Override
    public void waitTillConfigured() {
        trace("waitTillConfigured()");
    }

    @Override
    public boolean hasBeenClosed() {
        trace("hasBeenClosed()");
        return !applicationContext.isActive();
    }

    private void trace(String method) throws RuntimeException {
        if (warnOnAllCalls) {
            log.warn("ComponentManager#{} called in strict warning mode. "
                    + "This is discouraged. Please use Spring injection or the ApplicationContext directly.", method);
        }

        if (throwOnAllCalls) {
            throw new RuntimeException("ComponentManager#" + method + " called in strict error mode. "
                    + "This is prohibited. Please use Spring injection or the ApplicationContext directly.");
        }
    }
}