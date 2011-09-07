/**
 * 
 */
package org.sakaiproject.dash.entity;

import java.text.DateFormat;
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
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

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
		ResourceLoader rl = new ResourceLoader("dash_entity");
		if(resource != null) {
			ResourceProperties props = resource.getProperties();
			values.put(VALUE_TITLE, props.getProperty(ResourceProperties.PROP_DISPLAY_NAME));

			String descr = props.getProperty(ResourceProperties.PROP_DESCRIPTION);
			if(descr != null && !descr.trim().equals("")) {
				values.put(VALUE_DESCRIPTION, descr);
			}

			try {
				DateFormat df = DateFormat.getDateTimeInstance();
				values.put(VALUE_NEWS_TIME, df.format(new Date(props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE).getTime())));
			} catch (EntityPropertyNotDefinedException e) {
				logger.warn("getValues(" + entityReference + "," + localeCode + ") EntityPropertyNotDefinedException: " + e);
			} catch (EntityPropertyTypeException e) {
				logger.warn("getValues(" + entityReference + "," + localeCode + ") EntityPropertyTypeException: " + e);
			}
			
			values.put(VALUE_ENTITY_TYPE, IDENTIFIER);
			
			User user = sakaiProxy.getUser(props.getProperty(ResourceProperties.PROP_CREATOR));
			if(user != null) {
				values.put(VALUE_USER_NAME, user.getDisplayName());
			}
			
			// "more-info"
			List<Map<String,String>> infoList = new ArrayList<Map<String,String>>();
			Map<String,String> infoItem = new HashMap<String,String>();
			infoItem.put(VALUE_INFO_LINK_URL, resource.getUrl());
			infoItem.put(VALUE_INFO_LINK_SIZE, Long.toString(resource.getContentLength()));
			infoItem.put(VALUE_INFO_LINK_MIMETYPE, resource.getContentType());
			infoItem.put(VALUE_INFO_LINK_TARGET, this.sakaiProxy.getTargetForMimetype(resource.getContentType()));
			// TODO: VALUE_INFO_LINK_TITLE depends on VALUE_INFO_LINK_TARGET. If new window, title might be "View the damned item". Otherwise "Download the stupid thing"? 
			infoItem.put(VALUE_INFO_LINK_TITLE, rl.getString("resource.info.link"));
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
		ResourceLoader rl = new ResourceLoader("dash_entity");
		
		
		Map<String, String> props = new HashMap<String, String>();
		
		props.put(LABEL_NEWS_TIME, rl.getString("resource.news.time"));
		props.put(LABEL_USER_NAME, rl.getString("resource.user.name"));
		
		return props ;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.entity.EntityType#getAccessUrlTarget(java.lang.String)
	 */
	public String getAccessUrlTarget(String entityReference) {
		// ignored
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.entity.EntityType#getOrder(java.lang.String, java.lang.String)
	 */
	public List<List<String>> getOrder(String entityReference, String localeCode) {
		List<List<String>> order = new ArrayList<List<String>>();
		List<String> section1 = new ArrayList<String>();
		section1.add(VALUE_TITLE);
		section1.add(VALUE_NEWS_TIME);
		section1.add(VALUE_USER_NAME);
		order.add(section1);
		List<String> section2 = new ArrayList<String>();
		section2.add(VALUE_DESCRIPTION);
		order.add(section2);
		List<String> section3 = new ArrayList<String>();
		section3.add(VALUE_MORE_INFO);
		order.add(section3);

		return order;
	}

	public void init() {
		logger.info("init()");
		
		this.dashboardLogic.registerEntityType(this);
	}
}
