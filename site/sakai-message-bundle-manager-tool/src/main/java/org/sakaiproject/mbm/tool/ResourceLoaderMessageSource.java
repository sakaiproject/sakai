/**
 * Copyright 2008 Sakaiproject Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.mbm.tool;

import java.text.MessageFormat;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.AbstractMessageSource;

import org.sakaiproject.util.ResourceLoader;

/**
 * Simple spring message source that uses the Sakai resource loader as the source
 * and avoids too many refreshes of the Sakai resource loader.
 * The actual resource loader will only be loaded one time
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
@Slf4j
public class ResourceLoaderMessageSource extends AbstractMessageSource {

    ResourceLoader resourceLoader;

    public void setBasename(String baseName) {
        if (baseName.startsWith("classpath:")) {
            baseName = baseName.replaceFirst("classpath:", "");
            baseName = baseName.replaceAll("/",".");
        }
        this.resourceLoader = new ResourceLoader(baseName, ResourceLoaderMessageSource.class.getClassLoader());
        log.info("Sakai-Spring i18n MSG basename init: "+baseName);
    }

    protected MessageFormat resolveCode(String code, Locale locale) {
        if (locale != null && resourceLoader.getLocale() == null) {
            resourceLoader.setContextLocale(locale);
        } else {
            locale = resourceLoader.getLocale();
        }
        String msg;
        if ("DEBUG".equals(locale.getVariant()) || "XX".equals(locale.getCountry())) {
            msg = "** "+code+" **";
        } else {
            msg = resourceLoader.getString(code);
        }
        if (log.isTraceEnabled()) log.trace("MSG resolveCode: "+code+" ("+locale.getLanguage() + "_" + locale.getCountry()+(locale.getVariant() == null ? "" : " -"+locale.getVariant())+") into msg: "+msg);
        return createMessageFormat(msg, locale);
    }

    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        if (locale != null && resourceLoader.getLocale() == null) {
            resourceLoader.setContextLocale(locale);
        } else {
            locale = resourceLoader.getLocale();
        }
        String msg;
        if ("DEBUG".equals(locale.getVariant()) || "XX".equals(locale.getCountry())) {
            msg = "** "+code+" **";
        } else {
            msg = resourceLoader.getString(code);
        }
        if (log.isTraceEnabled()) log.trace("MSG resolveCode (noargs): "+code+" ("+locale.getLanguage() + "_" + locale.getCountry()+(locale.getVariant() == null ? "" : " -"+locale.getVariant())+") into msg: "+msg);
        return msg;
    }

}
