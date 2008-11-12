package org.sakaiproject.sitestats.api.report;

import java.util.Date;
import java.util.List;


public interface ReportParams {
	
	public String getSiteId();
	
	public void setSiteId(String siteId);

	public String getWhat();

	public void setWhat(String what);
	
	public String getWhatEventSelType();

	public void setWhatEventSelType(String whatEventSelType);

	public List<String> getWhatEventIds();

	public void setWhatEventIds(List<String> whatEventIds);

	public List<String> getWhatResourceIds();

	public void setWhatResourceIds(List<String> whatResourceIds);

	public String getWhatResourceAction();

	public void setWhatResourceAction(String whatResourceAction);

	public List<String> getWhatToolIds();

	public void setWhatToolIds(List<String> whatToolIds);

	public String getWhen();

	public void setWhen(String when);

	public Date getWhenTo();

	public void setWhenTo(Date whenTo);

	public Date getWhenFrom();

	public void setWhenFrom(Date whenFrom);

	public String getWho();

	public void setWho(String who);

	public String getWhoGroupId();

	public void setWhoGroupId(String whoGroupId);

	public String getWhoRoleId();

	public void setWhoRoleId(String whoRoleId);

	public List<String> getWhoUserIds();

	public void setWhoUserIds(List<String> whoUserIds);

}