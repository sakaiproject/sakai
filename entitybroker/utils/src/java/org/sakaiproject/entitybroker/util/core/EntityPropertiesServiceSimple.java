/**
 * $Id$
 * $URL$
 * EntityPropertiesSimple.java - entity-broker - Jan 13, 2009 7:42:36 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.util.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.sakaiproject.entitybroker.providers.EntityPropertiesService;


/**
 * This is a simple implementation of the properties loader service which uses standard java resource bundles
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityPropertiesServiceSimple extends AbstractEntityPropertiesService implements EntityPropertiesService {

    /**
     * Message source based on the standard resource bundle
     */
    protected class ResourceBundleMessageSource implements MessageBundle {
        private ResourceBundle bundle;
        public ResourceBundleMessageSource(ResourceBundle bundle) {
            this.bundle = bundle;
        }
        public String getPropertyMessage(String code, Object[] args, Locale locale) {
            if (code == null) {
                throw new IllegalArgumentException("code (key) cannot be null when looking up messages");
            }
            String message = null;
            String template = bundle.getString(code);
            if (template != null) {
                if (args != null && args.length > 0) {
                    MessageFormat formatter = new MessageFormat("");
                    if (locale == null) {
                        locale = Locale.getDefault();
                    }
                    formatter.setLocale(locale);
                    formatter.applyPattern(template);
                    message = formatter.format(args);
                } else {
                    message = template;
                }
            }
            return message;
        }
        public List<String> getPropertyKeys(Locale locale) {
            ArrayList<String> keys = new ArrayList<String>();
            Enumeration<String> enumKeys = bundle.getKeys();
            while (enumKeys.hasMoreElements()) {
                String key = enumKeys.nextElement();
                keys.add(key);
            }
            return keys;
        }
        public Locale getLocale() {
            return bundle.getLocale();
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.util.core.AbstractEntityPropertiesService#registerLocaleMessages(java.lang.String, java.lang.String, java.util.Locale, java.lang.ClassLoader)
     */
    public List<String> registerLocaleMessages(String prefix, String baseName, Locale locale, ClassLoader classLoader) {
        ArrayList<String> keys = new ArrayList<String>();
        try {
            ResourceBundle bundle = PropertyResourceBundle.getBundle(baseName, locale, classLoader);
            Enumeration<String> enumKeys = bundle.getKeys();
            while (enumKeys.hasMoreElements()) {
                String key = enumKeys.nextElement();
                keys.add(key);
            }
            if (keys.size() > 0) {
                prefixMap.put(prefix, new ResourceBundleMessageSource(bundle));
            }
        } catch (MissingResourceException e) {
            // looks like no properties files for this prefix
        }
        return keys;
    }

}
