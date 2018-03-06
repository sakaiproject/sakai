/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.jsf.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

/**
 * Instead of using standard methods of deciding which locale should be used
 * when loading a resource, Sakai tools are requested to go through the Sakai-specific
 * ResourceLoader class, which lets Sakai-specific settings such as site
 * preferences be taken into account.
 * 
 * Currently the ResourceLoader functionality is not behind an
 * interface, and so direct references to the class will drag in dependencies
 * on framework features. To keep the Sakai JSF tag library self-sufficient,
 * this utility class uses JavaBean-style introspection to load and use
 * the ResourceLoader class if it's available, or to fall back to the standard
 * UIViewRoot  "getLocale()" method.
 * 
 * Note that we only use the "getLocale()" method in ResourceLoader.
 * For details, see the comments to SAK-6886.
 */
@Slf4j
public class LocaleUtil {
	private static Object sakaiResourceLoader = null;
	private static Method sakaiResourceLoaderGetLocale;
	private static Method sakaiResourceLoaderGetOrientation;
	private static boolean isInitialized = false;
	
	private static void init() {
		if (!isInitialized) {
			// Try to load the Sakai localization class.
			try {
				Class sakaiResourceLoaderClass = Class.forName("org.sakaiproject.util.ResourceLoader");
				if (log.isDebugEnabled()) log.debug("Found Sakai ResourceLoader class");
				Constructor sakaiResourceLoaderConstructor = sakaiResourceLoaderClass.getConstructor();
				sakaiResourceLoaderGetLocale = sakaiResourceLoaderClass.getMethod("getLocale");
				sakaiResourceLoaderGetOrientation = sakaiResourceLoaderClass.getMethod("getOrientation",new Class[]{Locale.class});
				sakaiResourceLoader = sakaiResourceLoaderConstructor.newInstance();
			} catch (ClassNotFoundException e) {
				log.debug("Did not find Sakai ResourceLoader class; will use standard JSF localization");
			} catch (SecurityException | NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				log.error("Will use standard JSF localization", e);
			}
			isInitialized = true;
		}
	}
	
	public static Locale getLocale(FacesContext context) {
		Locale locale = null;
		init();	
		if (sakaiResourceLoader != null) {
			try {
				locale = (Locale)sakaiResourceLoaderGetLocale.invoke(sakaiResourceLoader);
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				log.error(e.getMessage());
			}
		} else {
			// Use standard JSF approach.
			locale = context.getViewRoot().getLocale();
		}
		return locale;
	}
	
	public static String getLocalizedString(FacesContext context, String bundleName, String key) {
		String localized = null;
		Locale locale = getLocale(context);
		ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
		if (log.isDebugEnabled()) log.debug("getLocalizedString; locale=" + locale.getDisplayName() + ", bundleName=" + bundleName + ", rb=" + rb.getLocale() + ", rb getCountry()=" + rb.getLocale().getCountry());
		localized = rb.getString(key);
		return localized;
	}

	public static String getOrientation(Locale loc) {
		String orientation = "ltr";
		init();	
		if (sakaiResourceLoader != null) {
			try {
				orientation = (String) sakaiResourceLoaderGetOrientation.invoke(sakaiResourceLoader, new Object[]{loc});
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
				log.error(e.getMessage());
			}
		}
		return orientation;
	}
	
}
