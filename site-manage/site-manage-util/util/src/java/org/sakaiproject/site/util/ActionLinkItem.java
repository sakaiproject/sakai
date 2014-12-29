
package org.sakaiproject.site.util;

/**
 * Inner class to put certain type of link into UI, that could be enabled or disabled by user actions
 * @author zqian
 *
 */
public class ActionLinkItem
{
	private String id = "";
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	private String label="";
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	private String link="";
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	
	private boolean toggleable = true;
	public boolean getToggleable() {
		return toggleable;
	}
	public void setToggleable(boolean toggleable) {
		this.toggleable = toggleable;
	}
	
	private boolean disabled = true;
	public boolean getDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public ActionLinkItem ()
	{
	}
	public ActionLinkItem(String id, String label, String link, boolean toggleable, boolean disabled)
	{
		this.id = id;
		this.label = label;
		this.link = link;
		this.toggleable = toggleable;
		this.disabled = disabled;
	}
}	// ActionLinkItem