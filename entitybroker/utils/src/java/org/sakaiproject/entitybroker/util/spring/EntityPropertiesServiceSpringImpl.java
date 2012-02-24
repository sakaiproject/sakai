/**
 * $Id$
 * $URL$
 * EntityProviderProperties.java - entity-broker - Jul 18, 2008 6:20:19 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.util.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import org.sakaiproject.entitybroker.providers.EntityPropertiesService;
import org.sakaiproject.entitybroker.util.core.AbstractEntityPropertiesService;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.DefaultResourceLoader;

/**
 * This service allows us to track all the properties files related to describing the capabilities
 * of our entities and the entities themselves, it allows lookup of strings as well<br/>
 * NOTE: Depends on spring and extends the spring based message bundle
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityPropertiesServiceSpringImpl extends AbstractEntityPropertiesService implements EntityPropertiesService {

    protected class SpringMessageBundle extends ReloadableResourceBundleMessageSource implements MessageBundle {
        public String getPropertyMessage(String key, Object[] args, Locale locale) {
            String msg;
            try {
                msg = getMessage(key, args, locale);
            } catch (NoSuchMessageException e) {
                throw new MissingResourceException("Cannot find key ("+key+"): " + e.getMessage(), 
                        SpringMessageBundle.class.getName(), key);
            }
            return msg;
        }
        public List<String> getPropertyKeys(Locale locale) {
            ArrayList<String> keys = new ArrayList<String>();
            PropertiesHolder ph = this.getMergedProperties(locale);
            for (Object o : ph.getProperties().keySet()) {
                keys.add(o.toString());
            }
            return keys;
        }
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.util.core.AbstractEntityPropertiesService#registerLocaleMessages(java.lang.String, java.lang.String, java.util.Locale, java.lang.ClassLoader)
     */
    @Override
    public List<String> registerLocaleMessages(String prefix, String baseName, Locale locale,
            ClassLoader classLoader) {
        SpringMessageBundle messageBundle = new SpringMessageBundle();
        messageBundle.setResourceLoader( new DefaultResourceLoader(classLoader) );
        messageBundle.setBasename(baseName);
        messageBundle.setDefaultEncoding("UTF-8");
        List<String> keys = messageBundle.getPropertyKeys(locale);
        registerPrefixMessageBundle(prefix, messageBundle);
        return keys;
    }

}
