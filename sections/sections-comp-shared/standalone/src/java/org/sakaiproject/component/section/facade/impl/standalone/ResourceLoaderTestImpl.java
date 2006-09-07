package org.sakaiproject.component.section.facade.impl.standalone;

import java.util.Locale;
import java.util.ResourceBundle;

import org.sakaiproject.api.section.facade.manager.ResourceLoader;

public class ResourceLoaderTestImpl implements ResourceLoader {

	public Locale getLocale() {
		return Locale.US;
	}

	public String getString(String str) {
        return ResourceBundle.getBundle("sections").getString(str);
	}

}
