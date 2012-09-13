package org.sakaiproject.delegatedaccess.dao;

import java.util.List;
import java.util.Map;

public interface DelegatedAccessDao {

	public List<String> getDistinctSiteTerms(String termField);
	
	public String getSiteProperty(String propertyName, String siteId);
	
	public void updateSiteProperty(String siteId, String propertyName, String propertyValue);
	
	public void addSiteProperty(String siteId, String propertyName, String propertyValue);
	
	public void removeSiteProperty(String siteId, String propertyName);
	
	/**
	 * returns a Map of -> {siteRef, {nodeId, nodeId ...}}
	 * 
	 * @param siteRef
	 * @param hierarchyId
	 * @return
	 */
	public Map<String, List<String>> getNodesBySiteRef(String[] siteRef, String hierarchyId);
	
	public List<String> getEmptyNonSiteNodes(String hierarchyId);
	
	/**
	 * returns a list of {siteId, title} for sites returned in search
	 * if you search for instructorsIds as well, then the results will be {siteId, title, userId}
	 * 
	 * @param titleSearch
	 * @param propsMap
	 * @param instructorIds
	 * @return
	 */
	public List<String[]> searchSites(String titleSearch, Map<String, String> propsMap, String[] instructorIds);
	
	/**
	 * returns a list of {siteId, map{name->value}} for the site ids and properties searched
	 * @param props
	 * @param siteIds
	 * @return
	 */
	public Map<String, Map<String, String>> searchSitesForProp(String[] props, String[] siteIds);
}
