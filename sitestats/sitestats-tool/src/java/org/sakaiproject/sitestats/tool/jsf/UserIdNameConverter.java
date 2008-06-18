package org.sakaiproject.sitestats.tool.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.CharacterConverter;

import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

public class UserIdNameConverter extends CharacterConverter {
	
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String name = null;
		if (value == null) {
			name = "-";
		} else {
			if (value instanceof String) {
				try{
					name = UserDirectoryService.getUser((String)value).getDisplayName();
				}catch(UserNotDefinedException e1){
					name = "-";
				}
			}
			name = super.getAsString(context, component, (Object)name);
		}

		return name;
	}

}
