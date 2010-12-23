package org.sakaiproject.tool.section.jsf.backingbean;

import org.sakaiproject.tool.section.jsf.JsfUtil;

public class ConfigurationBean {
	private String sectionRequired;
	private String capAM;
	private String capPM;


	public ConfigurationBean(){
	
		sectionRequired = JsfUtil.getConfigurationValue("section_required","*");
		capAM = JsfUtil.getConfigurationValue("time_of_day_am_cap","AM");
		capPM = JsfUtil.getConfigurationValue("time_of_day_pm_cap","PM");
	}
	
	public void setSectionRequired(String s){
		this.sectionRequired = s;
	}
	public String getSectionRequired(){
		return this.sectionRequired;
	}
	
	public void setCapAM(String s){
		this.capAM = s;
	}
	public String getCapAM(){
		return this.capAM;
	}
	public void setCapPM(String s){
		this.capPM = s;
	}
	public String getCapPM(){
		return this.capPM;
	}
}
