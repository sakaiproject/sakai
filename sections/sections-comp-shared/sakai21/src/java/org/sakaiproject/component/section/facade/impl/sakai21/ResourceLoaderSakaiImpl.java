package org.sakaiproject.component.section.facade.impl.sakai21;

import java.util.Locale;

import org.sakaiproject.api.section.facade.manager.ResourceLoader;

public class ResourceLoaderSakaiImpl implements ResourceLoader {

	public Locale getLocale() {
		org.sakaiproject.util.ResourceLoader rl = new org.sakaiproject.util.ResourceLoader("sections");
		return rl.getLocale();
	}

	public String getString(String str) {
		org.sakaiproject.util.ResourceLoader rl = new org.sakaiproject.util.ResourceLoader("sections");
		return rl.getString(str);
	}

}
