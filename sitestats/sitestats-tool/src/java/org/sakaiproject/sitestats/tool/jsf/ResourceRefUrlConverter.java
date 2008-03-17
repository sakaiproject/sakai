package org.sakaiproject.sitestats.tool.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.CharacterConverter;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.sitestats.api.StatsManager;


public class ResourceRefUrlConverter extends CharacterConverter {
	/** Statistics Manager object */
	private StatsManager	sm	= (StatsManager) ComponentManager.get(StatsManager.class.getName());

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String url = null;
		if(value == null || value.equals("")){
			url = "";
		}else{
			if(value instanceof String)
				url = sm.getResourceURL((String) value);
			url = super.getAsString(context, component, (Object) url);
		}

		return url;
	}

}
