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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class ClasspathScanner {

    private static final Log LOG = LogFactory.getLog(ClasspathScanner.class);

    /**
     * Retrieve a lit of all urls matching the specified
     * path.
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static List scan(String path) throws IOException {
        List list = scan(path, ClasspathScanner.class.getClassLoader());
        list.addAll(scan(path, Thread.currentThread().getContextClassLoader()));
        list.add(ClasspathScanner.class.getResource(path));

        if(LOG.isInfoEnabled()) {
            LOG.info("Found "+list.size()+" resources for path '"+path+"'.");
        }

        return list;
    }

    /**
     * Retrieve a list of all urls massing the specified path
     * for the specified classloader.
     *
     * @param path
     * @param loader
     * @return
     * @throws IOException
     */
    public static List scan(String path, ClassLoader loader) throws IOException {
        ArrayList list = new ArrayList();
        if (loader == null) {
            return list;
        }

        Enumeration enumeration = loader.getResources(path);
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }

    /**
     * Mechanism for finding all implementations of the specified
     * interface.  This method is used for resolving low level
     * implementations of interfaces needed by static and/or non
     * services.  These implementations are not bound to their
     * container, but instead, are bound to the global application
     * environment.
     *
     * @param implemented interface implemnted by configured impls
     * @return list of classes
     * @throws java.io.IOException if an error occurs during classpath scanning.
     */
    public static List findConfiguredImplementations(Class implemented)
        throws IOException {
        List classes = new ArrayList();
        List resources = scan("/META-INF/pluto.properties");
        Iterator i = resources.iterator();

        Properties p = new Properties();
        while (i.hasNext()) {
            URL url = (URL) i.next();
            p.load(url.openStream());
            String impl = p.getProperty(implemented.getName());
            if (impl != null) {
                StringTokenizer st = new StringTokenizer(impl, ",", false);
                while (st.hasMoreTokens()) {
                    String token = st.nextToken().trim();
                    if (token.length() > 0) {
                        try {
                            classes.add(Class.forName(token));
                        } catch (ClassNotFoundException cnfe) {
                            LOG.warn("Unable to find configured implementation " + impl + " of interface " + implemented.getName());
                        }
                    }
                }
            }
            p.clear();
        }
        return classes;
    }
}
