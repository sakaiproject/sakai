package org.sakaiproject.component.section.facade.impl.standalone;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.sakaiproject.api.section.facade.manager.ResourceLoader;

public class ResourceLoaderStandaloneImpl implements ResourceLoader {

	public Locale getLocale() {
        FacesContext context = FacesContext.getCurrentInstance();
        if(context == null) {
        	return Locale.US;
        } else {
        	return context.getApplication().getDefaultLocale();
        }
	}

	public String getString(String str) {
        FacesContext context = FacesContext.getCurrentInstance();
        String bundleName = context.getApplication().getMessageBundle();
        return ResourceBundle.getBundle(bundleName).getString(str);
	}

}
