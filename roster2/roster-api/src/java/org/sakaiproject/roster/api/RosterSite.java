/**
 * 
 */
package org.sakaiproject.roster.api;

import java.util.List;

/**
 * 
 * @author d.b.robinson@lancaster.ac.uk
 *
 */
public class RosterSite {

	private String id;
	private String title;
	private List<RosterGroup> siteGroups;
	
	public RosterSite() {
		
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<RosterGroup> getSiteGroups() {
		return siteGroups;
	}
	public void setSiteGroups(List<RosterGroup> siteGroups) {
		this.siteGroups = siteGroups;
	}
}
