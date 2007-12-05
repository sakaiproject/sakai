package org.sakaiproject.sitestats.tool.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.CharacterConverter;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.sitestats.api.StatsManager;


public class ResourceRefNameConverter extends CharacterConverter {
	/** Statistics Manager object */
	private StatsManager	sm	= (StatsManager) ComponentManager.get(StatsManager.class.getName());

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String img = null;
		if(value == null || value.equals("")){
			img = "";
		}else{
			if(value instanceof String)
				img = sm.getResourceName((String) value);
			img = super.getAsString(context, component, (Object) img);
		}

		return img;
	}

}
