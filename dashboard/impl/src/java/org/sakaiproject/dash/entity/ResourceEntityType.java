/**
 * 
 */
package org.sakaiproject.dash.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * @author jimeng
 *
 */
public class ResourceEntityType implements EntityType {
	
	private static Log logger = LogFactory.getLog(ResourceEntityType.class); 

	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}

	public static final String IDENTIFIER = "resource";
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.entity.EntityType#getIdentifier()
	 */
	public String getIdentifier() {
		return IDENTIFIER;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.entity.EntityType#getEntityLinkStrategy(java.lang.String)
	 */
	public EntityLinkStrategy getEntityLinkStrategy(String entityReference) {
		
		return EntityLinkStrategy.SHOW_PROPERTIES;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.entity.EntityType#getValues(java.lang.String, java.lang.String)
	 */
	public Map<String, Object> getValues(String entityReference,
			String localeCode) {
		Map<String, Object> values = new HashMap<String, Object>();
		ContentResource resource = (ContentResource) this.sakaiProxy.getEntity(entityReference);
		if(resource != null) {
			ResourceProperties props = resource.getProperties();
			values.put(VALUE_ENTITY_TYPE, IDENTIFIER);
			try {
				values.put(VALUE_NEWS_TIME, new Date(props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE).getTime()));
			} catch (EntityPropertyNotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (EntityPropertyTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String descr = props.getProperty(ResourceProperties.PROP_DESCRIPTION);
			if(descr != null && !descr.trim().equals("")) {
					values.put(VALUE_DESCRIPTION, descr);
			}
			// "more-info"
			List<Map<String,String>> infoList = new ArrayList<Map<String,String>>();
			Map<String,String> infoItem = new HashMap<String,String>();
			infoItem.put(VALUE_INFO_LINK_URL, resource.getUrl());
			infoItem.put(VALUE_INFO_LINK_TITLE, "More Info");
			infoItem.put(VALUE_INFO_LINK_SIZE, Long.toString(resource.getContentLength()));
			infoItem.put(VALUE_INFO_LINK_MIMETYPE, resource.getContentType());
			infoItem.put(VALUE_INFO_LINK_TARGET, this.sakaiProxy.getTargetForMimetype(resource.getContentType()));
			infoList.add(infoItem);
			values.put(VALUE_MORE_INFO, infoList);
			

		}
		return values ;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.entity.EntityType#getProperties(java.lang.String, java.lang.String)
	 */
	public Map<String, String> getProperties(String entityReference,
			String localeCode) {
		Map<String, String> map = new HashMap<String, String>();
		
		return map ;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.entity.EntityType#getAccessUrlTarget(java.lang.String)
	 */
	public String getAccessUrlTarget(String entityReference) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.entity.EntityType#getOrder(java.lang.String, java.lang.String)
	 */
	public List<List<String>> getOrder(String entityReference, String localeCode) {
		List<List<String>> list = new ArrayList<List<String>>();
		return list;
	}

	public void init() {
		logger.info("init()");
		
		this.dashboardLogic.registerEntityType(this);
	}
}
