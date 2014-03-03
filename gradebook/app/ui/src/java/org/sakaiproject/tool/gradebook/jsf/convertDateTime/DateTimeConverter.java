/*******************************************************************************
 * Copyright (c) 2006, 2008 The Sakai Foundation, The MIT Corporation
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
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.jsf.convertDateTime;

import java.util.TimeZone;
import java.util.Locale;
 
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.util.ResourceLoader;
 
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


/**
 * Simple converter that overrides the spec DateTimeConverter and uses TimeZone.getDefault() as the
 * base timezone, rather than GMT.
 *
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Sep 15, 2006
 * Time: 12:16:10 PM
 */
public class DateTimeConverter extends javax.faces.convert.DateTimeConverter {

    public static final String CONVERTER_ID =  org.sakaiproject.tool.gradebook.jsf.convertDateTime.DateTimeConverter.class.getName();

    public DateTimeConverter()
    {
    	super();
    	String userId = SessionManager.getCurrentSession().getUserId();
		Preferences prefs =  PreferencesService.getPreferences(userId);
		ResourceProperties props = prefs.getProperties(TimeService.APPLICATION_ID);
		String timeZone = props.getProperty(TimeService.TIMEZONE_KEY);
		TimeZone m_timeZone = TimeService.getLocalTimeZone();
		Locale m_locale = new ResourceLoader().getLocale();

		//TODO: Probably would be useful to do something here relating to a pattern for i18n display too
		//setPattern("");
		setTimeZone(m_timeZone);
    	setLocale(m_locale);
     }
    public Object getAsObject(FacesContext context, UIComponent component, String value){
    	return super.getAsObject(context, component, value);
    }
 
    public String getAsString(FacesContext context, UIComponent component, Object value){
    	return super.getAsString(context, component, value);
    }
    
	// Copied from UserPrefsTool.java
	/**
	 * Check String has value, not null
	 * 
	 * @return boolean
	 */
	protected boolean hasValue(String eval)
	{
		if (eval != null && !eval.trim().equals(""))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	private Locale getLocaleFromString(String localeString)
	{
		String[] locValues = localeString.trim().split("_");
		if (locValues.length >= 3)
			return new Locale(locValues[0], locValues[1], locValues[2]); // language, country, variant
		else if (locValues.length == 2)
			return new Locale(locValues[0], locValues[1]); // language, country
		else if (locValues.length == 1)
			return new Locale(locValues[0]); // language
		else
			return Locale.getDefault();
	}

	public void setDateStyle(String dateStyle) {
		super.setDateStyle(dateStyle);
	}
	public void setTimeStyle(String timeStyle) {
		super.setTimeStyle(timeStyle);
	}
	public void setType(String type) {
		super.setType(type);
	}


}
