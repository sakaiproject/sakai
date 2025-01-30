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
package org.sakaiproject.component.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.modi.BeanDefinitionSource;
import org.sakaiproject.modi.GlobalApplicationContext;
import org.sakaiproject.modi.SharedApplicationContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

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

    private Set<String> nonuniqueFailures = new java.util.HashSet<String> ();
    private Set<String> missingFailures = new java.util.HashSet<String> ();
    private Set<String> nameFailures = new java.util.HashSet<String> ();

    /**
     * Create a ComponentManager shim that forwards all meaningful methods to an ApplicationContext.
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
     * If this signature is called, the {@link GlobalApplicationContext} singleton will be retrieved.
     */
    @Deprecated
    public ComponentManagerShim(ComponentManager parent) {
        this(GlobalApplicationContext.getContext());
    }

    /**
     * In strict mode, we are looking for straggling uses of the ComponentManager.
     *
     * Set warnOnAllCalls to WARN on every method, which will start very noisy, but decreasingly so, as we transition
     * code to using Spring directly.
     */
    @Getter @Setter private boolean warnOnAllCalls = false;

    /**
     * In strict mode, we are looking for straggling uses of the ComponentManager.
     *
     * Set throwOnAllCalls to refuse to forward any calls, but throw an exception. This will
     * break everything, but give us a blunt instrument in tracking down any obscured uses.
     */
    @Getter @Setter private boolean throwOnAllCalls = false;

    /**
     * Lenient lookups allow missing beans to be returned as null and non-unique beans to be resolved if one of them is
     * registered under the full name of the requested class. Spring is normally strict, but the ComponentManager was
     * historically lenient.
     */
    @Getter @Setter private boolean lenientLookup = true;

    /**
     * Initialization is a no-op in the shim. Components will have already been loaded.
     */
    @Override
    public void init(boolean lateRefresh) {
        trace("init(" + lateRefresh + ")");
    }

    /**
     * Get a bean from the application context by class.
     *
     * If operating with lenient retrieval, allow non-unique results (where multiple beans of this type are registered)
     * to be retried by the class's name. Also, allow a missing bean to come back null.
     *
     * Note that a bean registered by another class's name may result in a casting error. This would always be against
     * our conventions, so is not handled specially.
     *
     * @see #isLenientLookup()
     * @throws NoUniqueBeanDefinitionException if operating in strict mode and there are multiple beans of this type
     * @throws NoSuchBeanDefinitionException if operating in strict mode and there is no such bean
     */
    @Override
    public <T> T get(Class<T> iface) {
        trace("get(" + iface.getName() + ".class)");
        try {
            return applicationContext.getBean(iface);
        } catch (NoUniqueBeanDefinitionException e) {
            if (lenientLookup) {
                logLenientLookup("No unique bean found for class: {} -- lenient retrieval enabled, retrying by name", iface.getName(), nonuniqueFailures);
                return (T) get(iface.getName());
            } else {
                throw e;
            }
        } catch (NoSuchBeanDefinitionException e) {
            if (lenientLookup) {
                logLenientLookup("Could not locate bean requested by class: {} -- lenient retrieval enabled, returning null", iface.getName(), missingFailures);
                return null;
            } else {
                throw e;
            }
        }
    }

    /**
     * Get a bean from the application context by name.
     *
     * If operating with lenient retrieval, allow missing beans to come back null.
     *
     * @see #isLenientLookup()
     * @throws NoSuchBeanDefinitionException if operating in strict mode and there is no such bean
     */
    @Override
    public Object get(String ifaceName) {
        trace("get(\"" + ifaceName + "\")");
        try {
            return applicationContext.getBean(ifaceName);
        } catch (NoSuchBeanDefinitionException e) {
            if (lenientLookup) {
                logLenientLookup("Could not locate bean requested by name: {} -- lenient retrieval enabled, returning null", ifaceName, nameFailures);
                return null;
            } else {
                throw e;
            }
        }
    }

    /**
     * Log lookup misses as warn once, then switch to debug
     *
     * @param msg The log message string
     * @param name The lookup that failed
     * @param alreadyLogged A set used to track once we have issued a warning for a lookup
     *
     */
    private void logLenientLookup(String msg, String name, Set<String> alreadyLogged) {
        if (alreadyLogged.contains(name)) {
            log.debug(msg, name);
        } else {
            log.warn(msg + " (further messages will be log.debug)", name);
            alreadyLogged.add(name);
        }
    }

    /**
     * Check whether a bean name is registered in the application context, as per a class's name.
     *
     * Note that this does not check whether there are actually beans of the class. This signature is based on the
     * conventional lookup in the ComponentManager.
     */
    @Override
    public boolean contains(Class iface) {
        trace("contains(" + iface.getName() + ".class)");
        return applicationContext.containsBean(iface.getName());
    }

    /**
     * Check whether a bean name is registered in the application context.
     */
    @Override
    public boolean contains(String ifaceName) {
        trace("contain(\"" + ifaceName + "\")");
        return applicationContext.containsBean(ifaceName);
    }

    /**
     * Get all of the application context's bean definition names.
     *
     * @return the unique names of the registered beans, as String
     */
    @Override
    public Set getRegisteredInterfaces() {
        trace("getRegisteredInterfaces()");
        return Set.of(applicationContext.getBeanDefinitionNames());
    }

    /**
     * Get the application context. This is a strange case because it would require downcasting from the
     * cover/interface to the SpringCompMgr. It's only used in the {@link SakaiContextLoader} and some tests, and
     * would be better done directly.
     *
     * @return the shared (typically global) context we are wrapping
     */
    @Override
    public SharedApplicationContext getApplicationContext() {
        trace("getApplicationContext()");
        return applicationContext;
    }

    /**
     * Closing the component manager is disabled in the shim. Use the application context instead.
     */
    @Override
    public void close() {
        trace("close()");
    }

    /**
     * Loading components is disabled in the shim.
     *
     * @see ComponentManagerShim#loadComponent(String, Object)
     */
    @Override
    public void loadComponent(Class iface, Object component) {
        trace("loadComponent(" + iface.getName() + ".class, [Object])");
    }

    /**
     * Loading components is disabled in the shim. Use a child context or set up the {@link SharedApplicationContext}
     * with a {@link BeanDefinitionSource} before starting it. There are no known uses, so this does not do any special
     * logging or exceptions.
     */
    @Override
    public void loadComponent(String ifaceName, Object component) {
        trace("loadComponent(\"" + ifaceName + "\", [Object])");
    }

    /**
     * Loading components is disabled in the shim.
     *
     * This method is peculiar, since someone would have had to cast/extend SpringCompMgr,
     * rather than using the interface. We know that has happened in attempts to support tests
     * or specialized wiring. In any case, we will have already fully loaded, so this is a no-op.
     */
    @Override
    protected void loadComponents() {
        trace("loadComponents()");
    }

    /**
     * Context hierarchy and cleanup is built into Spring, so addChildAc() is a no-op.
     */
    @Override
    public synchronized void addChildAc() {
        trace("addChildAc()");
    }

    /**
     * Context hierarchy and cleanup is built into Spring, so removeChildAc() is a no-op.
     */
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

    /**
     * Waiting for configuration is a no-op in the shim. If the shim is active, the components will have already been
     * loaded. If you have code that is running before the context may be loaded, Spring offers lifecycle events.
     *
     * @see org.springframework.context.ApplicationEvent
     * @see org.springframework.context.ApplicationListener
     * @see <a href="https://docs.spring.io/spring-framework/docs/5.3.18/reference/html/core.html#context-functionality-events">Spring event documentation<a>
     */
    @Override
    public void waitTillConfigured() {
        trace("waitTillConfigured()");
    }

    /**
     * Delegate to whether application context is considered "inactive".
     *
     * This is not an exact match because this would return true before the context is started. The original
     * {@link ComponentManager#hasBeenClosed()} would be false until started and then closed. If this difference is
     * material to some code, it should use lifecycle events.
     *
     * @see org.springframework.context.ConfigurableApplicationContext#isActive()
     * @see org.springframework.context.ApplicationEvent
     * @see org.springframework.context.ApplicationListener
     * @see <a href="https://docs.spring.io/spring-framework/docs/5.3.18/reference/html/core.html#context-functionality-events">Spring event documentation<a>
     * @return false while the application context is active; true before it starts and after it closes
     */
    @Override
    public boolean hasBeenClosed() {
        trace("hasBeenClosed()");
        return !applicationContext.isActive();
    }

    private void trace(String method) throws RuntimeException {
        if (throwOnAllCalls) {
            throw new RuntimeException("ComponentManager#" + method + " called in strict error mode. "
                    + "This is prohibited. Please use Spring injection or the ApplicationContext directly.");
        } else if (warnOnAllCalls) {
            log.warn("ComponentManager#{} called in strict warning mode. "
                    + "This is discouraged. Please use Spring injection or the ApplicationContext directly.", method);
        } else {
            log.debug("ComponentManager#{} called in unrestricted mode. "
                    + "This is discouraged. Please use Spring injection or the ApplicationContext directly.", method);
        }
    }
}
