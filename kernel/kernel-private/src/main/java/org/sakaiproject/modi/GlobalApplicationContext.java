package org.sakaiproject.modi;

/**
 * A basic singleton / service locator for a global application context.
 *
 * This is used by the Tomcat {@link Bootstrap} listener to ensure that there is a root context available to all of
 * the components and webapps.
 */
public class GlobalApplicationContext {
    private static SharedApplicationContext context = null;

    private static final Object lock = new Object();

    private GlobalApplicationContext() {}

    public static SharedApplicationContext getContext() {
        synchronized (lock) {
            if (context == null) {
                context = new SharedApplicationContext();
            }
            return context;
        }
    }

    public static void destroyContext() {
        synchronized (lock) {
            if (context != null) {
                context.stop();
                context = null;
            }
        }
    }
}