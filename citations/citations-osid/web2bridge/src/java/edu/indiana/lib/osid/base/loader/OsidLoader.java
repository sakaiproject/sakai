package edu.indiana.lib.osid.base.loader;

import lombok.extern.slf4j.Slf4j;

import org.osid.OsidContext;
import org.osid.OsidException;
import org.osid.OsidManager;

/**
 * OsidLoader loads a specific implementation of an Open Service Interface
 * Definition (OSID) with its getManager method. The getManager method loads
 * an instance of the OSID's org.osid.OsidManager, assigns the manager's  OsidContext,
 * assigns any configuration information, and returns the instance of the OSID
 * implementation.  This usage of the getManager method in the OsidLoader is
 * how applications should bind a particular implementation to an OSID.  The
 * value of this approach is that an application can defer which specific OSID
 * implementation is used until runtime. The specific implementation package
 * name can then be part of the configuration information rather than being
 * hard coded.  Changing implementations is simplified with this approach.
 *
 * <p>
 * As an example, in order to create a new Hierarchy, an application does not
 * use the new operator.  It uses the OsidLoader getManager method to get an
 * instance of a class that implements HierarchyManager (a subclass of
 * org.osid.OsidManager). The application uses the HierarchyManager instance to create
 * the Hierarchy.  It is the createHierarchy() method in some package (e.g.
 * org.osid.hierarchy.impl.HierarchyManager) which uses the new operator on
 * org.osid.hierarchy.impl.Hierarchy, casts it as
 * org.osid.hierarchy.Hierarchy, and returns it to the application.  This
 * indirection offers the significant value of being able to change
 * implementations in one spot with one modification, namely by using a
 * implementation package name argument for the OsidLoader getManager method.
 * </p>
 *
 * <p>
 * Sample:
 * <blockquote>
 * org.osid.OsidContext myContext = new org.osid.OsidContext();<br/
 * >String key = "myKey";<br/
 * >myContext.assignContext(key, "I want to save this string as context");<br/
 * >String whatWasMyContext = myContext.getContext(key);<br/
 * >org.osid.hierarchy.HierarchyManager hierarchyManager =
 * <blockquote>
 * org.osid.OsidLoader.getManager("org.osid.hierarchy.HierarchyManager","org.osid.shared.impl",myContext,null);
 * </blockquote>
 * org.osid.hierarchy.Hierarchy myHierarchy =
 * hierarchyManager.createHierarchy(...);<br/>
 * </blockquote>
 * </p>
 *
 * <p>
 * A similar technique can be used for creating other objects.  OSIDs that have
 * org.osid.OsidManager implementations loaded by OsidLoader, will define an
 * appropriate interface to create these objects.
 * </p>
 *
 * <p>
 * The arguments to OsidLoader.getManager method are the OSID org.osid.OsidManager
 * interface name, the implementing package name, the OsidContext, and any
 * additional configuration information.
 * </p>
 *
 * <p>
 * OSID Version: 2.0
 * </p>
 *
 * <p>
 * Licensed under the {@link org.osid.SidImplementationLicenseMIT MIT
 * O.K.I&#46; OSID Definition License}.
 * </p>
 */
@Slf4j
public class OsidLoader implements java.io.Serializable {
    /**
     * Returns an instance of the org.osid.OsidManager of the OSID specified by the OSID
     * package org.osid.OsidManager interface name and the implementation package name.
     * The implementation class name is constructed from the SID package
     * Manager interface name. A configuration file name is constructed in a
     * similar manner and if the file exists it is loaded into the
     * implementation's org.osid.OsidManager's configuration.
     *
     * <p>
     * Example:  To load an implementation of the org.osid.Filing OSID
     * implemented in a package "xyz", one would use:
     * </p>
     *
     * <p>
     * org.osid.filing.FilingManager fm =
     * (org.osid.filing.FilingManager)org.osid.OsidLoader.getManager(
     * </p>
     *
     * <p>
     * "org.osid.filing.FilingManager" ,
     * </p>
     *
     * <p>
     * "xyz" ,
     * </p>
     *
     * <p>
     * new org.osid.OsidContext());
     * </p>
     *
     * @param osidPackageManagerName osidPackageManagerName is a fully
     *        qualified org.osid.OsidManager interface name
     * @param implPackageName implPackageName is a fully qualified
     *        implementation package name
     * @param context
     * @param additionalConfiguration
     *
     * @return org.osid.OsidManager
     *
     * @throws org.osid.OsidException An exception with one of the following
     *         messages defined in org.osid.OsidException:  {@link
     *         org.osid.OsidException#OPERATION_FAILED OPERATION_FAILED},
     *         {@link org.osid.OsidException#NULL_ARGUMENT NULL_ARGUMENT},
     *         {@link org.osid.OsidException#VERSION_ERROR VERSION_ERROR},
     *         ={@link org.osid.OsidException#INTERFACE_NOT_FOUND
     *         INTERFACE_NOT_FOUND}, ={@link
     *         org.osid.OsidException#MANAGER_NOT_FOUND MANAGER_NOT_FOUND},
     *         ={@link org.osid.OsidException#MANAGER_INSTANTIATION_ERROR
     *         MANAGER_INSTANTIATION_ERROR}, ={@link
     *         org.osid.OsidException#ERROR_ASSIGNING_CONTEXT
     *         ERROR_ASSIGNING_CONTEXT}, ={@link
     *         org.osid.OsidException#ERROR_ASSIGNING_CONFIGURATION
     *         ERROR_ASSIGNING_CONFIGURATION}
     */
    public static OsidManager getManager(
        String osidPackageManagerName, String implPackageName,
        OsidContext context,
        java.util.Properties additionalConfiguration)
        throws OsidException {


        try {
            if ((null != context) && (null != osidPackageManagerName) &&
                    (null != implPackageName)) {

                String osidInterfaceName = osidPackageManagerName;
                String className = makeClassName(osidPackageManagerName);
                String managerClassName = makeFullyQualifiedClassName(implPackageName,
                        className);

                Class osidInterface = Class.forName(osidInterfaceName);

                if (null != osidInterface) {
                    Class managerClass = Class.forName(managerClassName);

                    if (null != managerClass) {
                        if (osidInterface.isAssignableFrom(managerClass)) {
                            OsidManager manager = (OsidManager) managerClass.newInstance();

                            if (null != manager) {
                                try {
                                    manager.osidVersion_2_0();
                                } catch (Throwable ex) {
                                    throw new OsidException(OsidException.VERSION_ERROR);
                                }

                                try {
                                    manager.assignOsidContext(context);
                                } catch (Exception ex) {
                                    throw new OsidException(OsidException.ERROR_ASSIGNING_CONTEXT);
                                }

                                try {
		                               java.util.Properties configuration = getConfiguration(manager);

                                    if (null == configuration) {
                                        configuration = new java.util.Properties();
                                    }

                                    if (null != additionalConfiguration) {
                                        java.util.Enumeration enumer = additionalConfiguration.propertyNames();

                                        while (enumer.hasMoreElements()) {
                                            java.io.Serializable key = (java.io.Serializable) enumer.nextElement();

                                            if (null != key) {
                                                java.io.Serializable value = (java.io.Serializable) additionalConfiguration.get(key);

                                                if (null != value) {
                                                    configuration.put(key, value);
                                                }
                                            }
                                        }
                                    }

                                    manager.assignConfiguration(configuration);

                                    return manager;
                                } catch (Exception ex) {
                                    throw new OsidException(OsidException.ERROR_ASSIGNING_CONFIGURATION);
                                }
                            }

                            throw new OsidException(OsidException.MANAGER_INSTANTIATION_ERROR);
                        }

                        throw new OsidException(OsidException.MANAGER_NOT_OSID_IMPLEMENTATION);
                    }

                    throw new OsidException(OsidException.MANAGER_NOT_FOUND);
                }

                throw new OsidException(OsidException.INTERFACE_NOT_FOUND);
            }

            throw new OsidException(OsidException.NULL_ARGUMENT);
        } catch (OsidException oex) {
            log.error(oex.getMessage(), oex);
            throw new OsidException(oex.getMessage());
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
            throw new OsidException(OsidException.OPERATION_FAILED);
        }
    }

    private static String makeClassName(String packageManagerName)
        throws OsidException {
        String className = packageManagerName;

        if (null != className) {
            className = (className.endsWith(".")
                ? className.substring(0, className.length() - 1) : className);

            int lastdot = className.lastIndexOf(".");

            if (-1 != lastdot) {
                className = className.substring(lastdot + 1);
            }
        }

        return className;
    }

    private static String makeFullyQualifiedClassName(String packageName,
        String className) throws OsidException {
        String cName = className;

        if (null != packageName) {
            String pName = (packageName.endsWith(".") ? packageName
                                                      : new String(packageName +
                    "."));
            cName = pName + className;
        }

        return cName;
    }

	/******* Utility Methods For Sakai Implementations ********/

        /**
         * Get an InputStream for a particular file name - first check the sakai.home area and then
         * revert to the classpath.
	 *
	 * This is a utility method used several places.
         */
        public static java.io.InputStream getConfigStream(String fileName, Class curClass)
        {
                String sakaiHome = System.getProperty("sakai.home");
                String filePath = sakaiHome + fileName;

                try
                {
                        java.io.File f = new java.io.File(filePath);
                        if (f.exists())
                        {
                                return new java.io.FileInputStream(f);
                        }
                }
                catch (Throwable t)
                {
                        // Not found in the sakai.home area
                }

		if ( curClass == null ) return null;

		// If there is a class context, load from the class context...
                java.io.InputStream istream = null;

                // Load from the class loader
                istream = curClass.getClassLoader().getResourceAsStream(fileName);
                if ( istream != null ) return istream;

                // Load from the class relative
                istream = curClass.getResourceAsStream(fileName);
                if ( istream != null ) return istream;

                // Loading from the class at the root
                istream = curClass.getResourceAsStream("/"+fileName);
                return istream;
        }

    private static java.util.Properties getConfiguration(
        OsidManager manager) throws OsidException {
        java.util.Properties properties = null;

        if (null != manager) {
            Class managerClass = manager.getClass();

            try {
                String managerClassName = managerClass.getName();
                int index = managerClassName.lastIndexOf(".");

                if (-1 != index) {
                    managerClassName = managerClassName.substring(index + 1);
                }

								String propertyName = managerClassName + ".properties";

                // java.io.InputStream is = managerClass.getResourceAsStream(managerClassName + ".properties");
                java.io.InputStream is = getConfigStream(propertyName,managerClass);

                if (null != is) {
                    properties = new java.util.Properties();
                    properties.load(is);
                }
            } catch (Throwable ex) {
            }
        }

        return properties;
    }

    /**
     * <p>
     * MIT O.K.I&#46; SID Definition License.
     * </p>
     *
     * <p>
     * Copyright &copy; 2003 Massachusetts Institute of     Technology &lt;or
     * copyright holder&gt;
     * </p>
     *
     * <p>
     * This work is being provided by the copyright holder(s)     subject to
     * the terms of the O.K.I&#46; SID Definition     License. By obtaining,
     * using and/or copying this Work,     you agree that you have read,
     * understand, and will comply     with the O.K.I&#46; SID Definition
     * License.
     * </p>
     *
     * <p>
     * THE WORK IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY     KIND, EXPRESS
     * OR IMPLIED, INCLUDING BUT NOT LIMITED TO     THE WARRANTIES OF
     * MERCHANTABILITY, FITNESS FOR A     PARTICULAR PURPOSE AND
     * NONINFRINGEMENT. IN NO EVENT SHALL     MASSACHUSETTS INSTITUTE OF
     * TECHNOLOGY, THE AUTHORS, OR     COPYRIGHT HOLDERS BE LIABLE FOR ANY
     * CLAIM, DAMAGES OR     OTHER LIABILITY, WHETHER IN AN ACTION OF
     * CONTRACT, TORT     OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
     * WITH     THE WORK OR THE USE OR OTHER DEALINGS IN THE WORK.
     * </p>
     *
     * <p>
     * <b>O.K.I&#46; SID Definition License</b>
     * </p>
     *
     * <p>
     * This work (the &ldquo;Work&rdquo;), including any     software,
     * documents, or other items related to O.K.I&#46;     SID definitions, is
     * being provided by the copyright     holder(s) subject to the terms of
     * the O.K.I&#46; SID     Definition License. By obtaining, using and/or
     * copying     this Work, you agree that you have read, understand, and
     * will comply with the following terms and conditions of     the
     * O.K.I&#46; SID Definition License:
     * </p>
     *
     * <p>
     * You may use, copy, and distribute unmodified versions of     this Work
     * for any purpose, without fee or royalty,     provided that you include
     * the following on ALL copies of     the Work that you make or
     * distribute:
     * </p>
     *
     * <ul>
     * <li>
     * The full text of the O.K.I&#46; SID Definition License in a location
     * viewable to users of the redistributed Work.
     * </li>
     * </ul>
     *
     *
     * <ul>
     * <li>
     * Any pre-existing intellectual property disclaimers, notices, or terms
     * and conditions. If none exist, a short notice similar to the following
     * should be used within the body of any redistributed Work:
     * &ldquo;Copyright &copy; 2003 Massachusetts Institute of Technology. All
     * Rights Reserved.&rdquo;
     * </li>
     * </ul>
     *
     * <p>
     * You may modify or create Derivatives of this Work only     for your
     * internal purposes. You shall not distribute or     transfer any such
     * Derivative of this Work to any location     or any other third party.
     * For purposes of this license,     &ldquo;Derivative&rdquo; shall mean
     * any derivative of the     Work as defined in the United States
     * Copyright Act of     1976, such as a translation or modification.
     * </p>
     *
     * <p>
     * THE WORK PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,     EXPRESS OR
     * IMPLIED, INCLUDING BUT NOT LIMITED TO THE     WARRANTIES OF
     * MERCHANTABILITY, FITNESS FOR A PARTICULAR     PURPOSE AND
     * NONINFRINGEMENT. IN NO EVENT SHALL     MASSACHUSETTS INSTITUTE OF
     * TECHNOLOGY, THE AUTHORS, OR     COPYRIGHT HOLDERS BE LIABLE FOR ANY
     * CLAIM, DAMAGES OR     OTHER LIABILITY, WHETHER IN AN ACTION OF
     * CONTRACT, TORT     OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
     * WITH     THE WORK OR THE USE OR OTHER DEALINGS IN THE WORK.
     * </p>
     *
     * <p>
     * The name and trademarks of copyright holder(s) and/or     O.K.I&#46; may
     * NOT be used in advertising or publicity     pertaining to the Work
     * without specific, written prior     permission. Title to copyright in
     * the Work and any     associated documentation will at all times remain
     * with     the copyright holders.
     * </p>
     *
     * <p>
     * The export of software employing encryption technology     may require a
     * specific license from the United States     Government. It is the
     * responsibility of any person or     organization contemplating export
     * to obtain such a     license before exporting this Work.
     * </p>
     */
}
