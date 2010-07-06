package org.sakaiproject.jsf.roster;

import javax.faces.convert.Converter;
import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;

public class GroupTextTruncateConverter implements Converter {

    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String string) {
        if(string.length() > 80) return string.substring(0,80) + "...";
        return string;
    }

    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object object) {
        if(((String)object).length() > 80) return ((String)object).substring(0,80) +"...";
        return (String)object;
    }
}
