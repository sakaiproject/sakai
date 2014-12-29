package org.sakaiproject.tool.section.jsf.backingbean;

import org.sakaiproject.util.ResourceLoader;
import java.text.DateFormatSymbols;

public class ConfigurationBean {
	private String capAM;
	private String capPM;

	public ConfigurationBean(){
	}
	public String getCapAM(){
		localizeParameters();
		return this.capAM;
	}
	public String getCapPM(){
		localizeParameters();
		return this.capPM;
	}
	private void localizeParameters(){
		DateFormatSymbols dsf = new DateFormatSymbols(new ResourceLoader().getLocale());
		String[] amPmString = dsf.getAmPmStrings();
		capAM = amPmString[0];
		capPM = amPmString[1];
	}
}
