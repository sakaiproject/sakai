package org.sakaiproject.sitestats.tool.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.CharacterConverter;

import org.sakaiproject.util.ResourceLoader;


public class ResourceActionConverter extends CharacterConverter {
	/** Resource Bundle */
	private String 			bundleName 	= FacesContext.getCurrentInstance().getApplication().getMessageBundle();
	private ResourceLoader	msgs		= new ResourceLoader(bundleName);
	

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String txt = null;
		if(value == null){
			txt = "";
		}else{
			if(value instanceof String) {
				String _txt = (String) value;
				if(!(_txt.trim().equals("")))
					txt = msgs.getString("action_"+value);
				else
					txt = "";
			}
			txt = super.getAsString(context, component, (Object) txt);
		}

		return txt;
	}

}
