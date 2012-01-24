package org.sakaiproject.delegatedaccess.dao;

import java.util.List;

public interface DelegatedAccessDao {

	public List<String> getDistinctSiteTerms(String termField);
	
	public String getSiteProperty(String propertyName, String siteId);
	
	public void updateSiteProperty(String siteId, String propertyName, String propertyValue);
	
	public void addSiteProperty(String siteId, String propertyName, String propertyValue);
	
	public void removeSiteProperty(String siteId, String propertyName);
	
	public List<String> getNodesBySiteRef(String siteRef, String hierarchyId);
}
