package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;

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
        add(new SimpleAttributeModifier("lang", language)); 
        add(new SimpleAttributeModifier("xml:lang", language)); 
    } 

    @Override 
    public boolean isTransparentResolver() { 
        return true; 
    } 
    
}
