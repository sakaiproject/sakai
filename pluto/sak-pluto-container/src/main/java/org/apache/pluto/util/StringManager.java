/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.util;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * An internationalization / localization helper class which reduces the bother
 * of handling ResourceBundles and takes care of the common cases of message
 * formating which otherwise require the creation of Object arrays and such.
 * <p/>
 * <p>The StringManager operates on a package basis. One StringManager per
 * package can be created and accessed via the getManager method call.
 * <p/>
 * <p>The StringManager will look for a ResourceBundle named by the package name
 * given plus the suffix of "LocalStrings". In practice, this means that the
 * localized information will be contained in a LocalStrings.properties file
 * located in the package directory of the classpath.
 * <p/>
 * <p>Please see the documentation for java.util.ResourceBundle for more
 * information.
 */
public class StringManager {
    private static final Log LOG =  LogFactory.getLog(StringManager.class);

    /**
     * The ResourceBundle for this StringManager.
     */

    private ResourceBundle bundle;

    /**
     * The package this StringManager belongs to.
     */
    private final String packageName;

    /**
     * Creates a new StringManager for a given package. This is a private method
     * and all access to it is arbitrated by the static getManager method call
     * so that only one StringManager per package will be created.
     * @param packageName Name of package to create StringManager for.
     */

    private StringManager(String packageName) {
        if(LOG.isDebugEnabled()) {
            LOG.debug("String Manager Created for package: "+packageName);
        }
        this.packageName = packageName;
        String bundleName = packageName + ".LocalStrings";
        try {
            bundle = ResourceBundle.getBundle(bundleName);
            return;
        } catch (MissingResourceException ex) {
            // Try from the current loader ( that's the case for trusted apps )
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != null) {
                try {
                    bundle =
                    ResourceBundle.getBundle(bundleName, Locale.getDefault(),
                                             cl);
                    return;
                } catch (MissingResourceException ex2) {
                }
            }
            if (cl == null) {
                cl = this.getClass().getClassLoader();
            }

            System.out.println("Can't find resource " + bundleName +
                               " " + cl);
            if (cl instanceof URLClassLoader) {
                System.out.println(((URLClassLoader) cl).getURLs());
            }
        }
    }

    /**
     * Get a string from the underlying resource bundle.
     * @param key The resource name
     */
    public String getString(String key) {
        return MessageFormat.format(getStringInternal(key), null);
    }


    protected String getStringInternal(String key) {
        if (key == null) {
            String msg = "key is null";

            throw new NullPointerException(msg);
        }

        String str = null;

        if (bundle == null) {
            return key;
        }
        try {
            str = bundle.getString(key);
        } catch (MissingResourceException mre) {
            String name = null;
            if (packageName == null) {
                name = "unknown";
            } else {
                name = packageName;
            }
            str = "Cannot find message in the ResourceBundle associated with key '" + key + "' " +
                    "(package " + name + ")";
            LOG.warn(str, mre);
        }

        return str;
    }

    /**
     * Get a string from the underlying resource bundle and format it with the
     * given set of arguments.
     * @param key  The resource name
     * @param args Formatting directives
     */

    public String getString(String key, Object[] args) {
        String iString = null;
        String value = getStringInternal(key);

        // this check for the runtime exception is some pre 1.1.6
        // VM's don't do an automatic toString() on the passed in
        // objects and barf out

        try {
            // ensure the arguments are not null so pre 1.2 VM's don't barf
            Object nonNullArgs[] = args;
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    if (nonNullArgs == args) {
                        nonNullArgs =
                        (Object[]) args.clone();
                    }
                    nonNullArgs[i] = "null";
                }
            }

            iString = MessageFormat.format(value, nonNullArgs);
        } catch (IllegalArgumentException iae) {
            StringBuffer buf = new StringBuffer();
            buf.append(value);
            for (int i = 0; i < args.length; i++) {
                buf.append(" arg[").append(i).append("]=").append(args[i]);
            }
            iString = buf.toString();
        }
        return iString;
    }

    /**
     * Get a string from the underlying resource bundle and format it with the
     * given object argument. This argument can of course be a String object.
     * @param key The resource name
     * @param arg Formatting directive
     */

    public String getString(String key, Object arg) {
        Object[] args = new Object[]{arg};
        return getString(key, args);
    }

    /**
     * Get a string from the underlying resource bundle and format it with the
     * given object arguments. These arguments can of course be String objects.
     * @param key  The resource name
     * @param arg1 Formatting directive
     * @param arg2 Formatting directive
     */

    public String getString(String key, Object arg1, Object arg2) {
        Object[] args = new Object[]{arg1, arg2};
        return getString(key, args);
    }

    /**
     * Get a string from the underlying resource bundle and format it with the
     * given object arguments. These arguments can of course be String objects.
     * @param key  The resource name
     * @param arg1 Formatting directive
     * @param arg2 Formatting directive
     * @param arg3 Formatting directive
     */

    public String getString(String key, Object arg1, Object arg2,
                            Object arg3) {
        Object[] args = new Object[]{arg1, arg2, arg3};
        return getString(key, args);
    }

    /**
     * Get a string from the underlying resource bundle and format it with the
     * given object arguments. These arguments can of course be String objects.
     * @param key  The resource name
     * @param arg1 Formatting directive
     * @param arg2 Formatting directive
     * @param arg3 Formatting directive
     * @param arg4 Formatting directive
     */

    public String getString(String key, Object arg1, Object arg2,
                            Object arg3, Object arg4) {
        Object[] args = new Object[]{arg1, arg2, arg3, arg4};
        return getString(key, args);
    }
    // --------------------------------------------------------------
    // STATIC SUPPORT METHODS
    // --------------------------------------------------------------

    private static Hashtable managers = new Hashtable();

    /**
     * Get the StringManager for a particular package. If a manager for a
     * package already exists, it will be reused, else a new StringManager will
     * be created and returned.
     * @param packageName The package name
     */

    public synchronized static StringManager getManager(String packageName) {
        StringManager mgr = (StringManager) managers.get(packageName);

        if (mgr == null) {
            mgr = new StringManager(packageName);
            managers.put(packageName, mgr);
        }
        return mgr;
    }
}
