package org.sakaiproject.sitestats.tool.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.CharacterConverter;

import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;


public class UserIdEidConverter extends CharacterConverter {

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String userEid = null;
		if(value == null){
			userEid = "";
		}else{
			if(value instanceof String){
				String userId = (String) value;
				try{
					userEid = UserDirectoryService.getUser(userId).getDisplayId();
				}catch(UserNotDefinedException e1){
					userEid = userId;
				}
			}
			userEid = super.getAsString(context, component, (Object) userEid);
		}

		return userEid;
	}

}
