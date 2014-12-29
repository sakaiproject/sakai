/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/branches/SAK-18678/api/src/main/java/org/sakaiproject/site/api/Site.java $
 * $Id: Site.java 81275 2010-08-14 09:24:56Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jan 7, 2010
 * Time: 11:49:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceLoaderMessageSource extends AbstractMessageSource {
    ResourceLoader resourceLoader;

    public void setBasename(String baseName) {
        if (baseName.startsWith("classpath:")) {
            baseName = baseName.replaceFirst("classpath:", "");
            baseName = baseName.replaceAll("/",".");
        }
        this.resourceLoader= new ResourceLoader(baseName);
    }

    protected MessageFormat resolveCode(String code, Locale locale) {
        resourceLoader.setContextLocale(locale);
        String msg = resourceLoader.getString(code);
        return createMessageFormat(msg, locale);
    }

    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        resourceLoader.setContextLocale(locale);
        return resourceLoader.getString(code);
    }

    public void setCacheSeconds(int secs) {
        
    }
}