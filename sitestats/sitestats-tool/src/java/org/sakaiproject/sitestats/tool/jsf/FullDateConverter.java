package org.sakaiproject.sitestats.tool.jsf;

import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.CharacterConverter;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;


public class FullDateConverter extends CharacterConverter {
	/** Statistics Manager object */
	private TimeService		ts	= (TimeService) ComponentManager.get(TimeService.class.getName());

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String dateStr = null;
		if(value == null){
			dateStr = "";
		}else{
			if(value instanceof Date){
				Date date = (Date) value;
				Time t = ts.newTime(date.getTime());
				dateStr =  t.toStringLocalFull();
			}
			dateStr = super.getAsString(context, component, (Object) dateStr);
		}

		return dateStr;
	}

}
