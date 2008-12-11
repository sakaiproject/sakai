/**
 * 
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.model.IModel;
import org.sakaiproject.util.Web;

public class CHResourceModel implements IModel {
	private static final long	serialVersionUID	= 1L;
	
	String resourceId = null;
	String resourceName = null;
	boolean isCollection = false;
	
	public CHResourceModel(String resourceId, String resourceName) {
		this.resourceId = resourceId;
		this.resourceName = resourceName;
	}
	
	public CHResourceModel(String resourceId, String resourceName, boolean isCollection) {
		this.resourceId = resourceId;
		this.resourceName = resourceName;
		this.isCollection = isCollection;
	}

	public Object getObject() {
		return resourceId;
	}

	public void setObject(Object object) {
		this.resourceId = (String) object;
	}
	
	public String getResourceId() {
		return resourceId;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public String getResourceNameEscaped() {
		return Web.encodeUrlsAsHtml(getResourceName());
	}
	
	public boolean isCollection() {
		return isCollection;
	}
	
	public String getResourceExtension() {
		String[] parts = getResourceId().split("\\.");
		if(parts.length > 1) {
			return parts[parts.length - 1].toLowerCase();
		}else{
			return "";
		}
	}

	public void detach() {
		resourceId = null;
		resourceName = null;
	}
	
}