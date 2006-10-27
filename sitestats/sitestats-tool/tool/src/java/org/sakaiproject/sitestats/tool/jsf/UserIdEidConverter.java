package org.sakaiproject.sitestats.tool.jsf;

import java.util.ResourceBundle;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.CharacterConverter;

import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;


public class UserIdEidConverter extends CharacterConverter {

	/** Resource bundle */
	protected static ResourceBundle	msgs		= ResourceBundle.getBundle("org.sakaiproject.sitestats.tool.bundle.Messages");

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String userEid = null;
		if(value == null){
			userEid = "";
		}else{
			if(value instanceof String){
				String userId = (String) value;
				try{
					userEid = UserDirectoryService.getUserEid(userId);
				}catch(UserNotDefinedException e1){
					userEid = userId;
				}
			}
			userEid = super.getAsString(context, component, (Object) userEid);
		}

		return userEid;
	}

}
