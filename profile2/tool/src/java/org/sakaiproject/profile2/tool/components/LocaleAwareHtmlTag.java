/**
 * Copyright (c) 2008-2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Tag that is Locale aware and will inject the correct language into the base HTML tag.
 * Note that the language comes from the session so this must already be configured
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * @see https://jira.sakaiproject.org/browse/PRFL-791
 *
 */
public class LocaleAwareHtmlTag extends WebMarkupContainer {

	public LocaleAwareHtmlTag(String id) { 
        super(id); 
        String language = getSession().getLocale().getLanguage();
        String orientation = ProfileUtils.getUserPreferredOrientation();
        add(AttributeModifier.replace("lang", language)); 
        add(AttributeModifier.replace("xml:lang", language)); 
        add(AttributeModifier.replace("dir", orientation));
    } 
 
    
}
