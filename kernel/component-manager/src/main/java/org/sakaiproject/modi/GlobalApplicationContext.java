package org.sakaiproject.modi;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * A basic singleton / service locator for a global application context.
 * <p>
 * This is used by the {@link Launcher} to ensure that there is a root context available to all of the components and
 * webapps.
 * <p>
 * It can also be used in integration tests to set up a shared context for classes that use the
 * {@link org.sakaiproject.component.api.ComponentManager} directly.
 */
public class GlobalApplicationContext {
    /** The constructor is private because this serves as a singleton service locator. */
    private GlobalApplicationContext() {
    }

    /** The underlying context that serves as the singleton. */
    private static ConfigurableApplicationContext context = null;

    /** Simple lock for synchronized blocks when changing the underlying context. */
    private static final Object lock = new Object();

    /**
     * Get the global context. Will always be already configured/refreshed.
     *
     * Because beans access the ComponentManager cover during their init (and hence through the
     * shim to this context), it must be set before starting.
     *
     * @return the configured {@link ConfigurableApplicationContext} to be used globally
     * @throws IllegalStateException if there is no context established
     */
    public static ConfigurableApplicationContext getContext() throws IllegalStateException {
        if (context == null)
            throw new IllegalStateException("Global Application Context accessed, but not registered. It must be set"
                    + "before refresh because certain beans must be initialized during refresh AND use this.");

        return context;
    }

    public static void setContext(ConfigurableApplicationContext context) {
        synchronized (lock) {
            GlobalApplicationContext.context = context;
        }
    }

    /**
     * Destroy and unset the global context. Synchronized for thread safety.
     * <p>
     * This will not typically be called during normal operation, except at shutdown. However, there is no need to
     * protect this instance. If someone is working to refactor code to use injection directly, resetting the global
     * context may be useful for testing.
     */
    public static void destroyContext() {
        synchronized (lock) {
            if (context != null) {
                context.stop();
                context = null;
            }
        }
    }
}
