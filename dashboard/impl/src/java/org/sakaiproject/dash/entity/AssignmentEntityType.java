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
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

/**
 * 
 *
 */
public class AssignmentEntityType implements EntityType {
	
	private Log logger = LogFactory.getLog(AssignmentEntityType.class);
	
	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}

	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}

	public static final String IDENTIFIER = "assignment";
	
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
		Assignment assn = (Assignment) this.sakaiProxy.getEntity(entityReference);
		ResourceLoader rl = new ResourceLoader("dash_entity");
		if(assn != null) {
			ResourceProperties props = assn.getProperties();
			// "entity-type": "assignment"
			values.put(EntityType.VALUE_ENTITY_TYPE, IDENTIFIER);
			//    "assessment-type": ""
			//values.put(VALUE_ASSESSMENT_TYPE, assn.);
			// "grade-type": ""
			values.put(VALUE_GRADE_TYPE, assn.getContent().getTypeOfGradeString(assn.getContent().getTypeOfGrade()));
			// "submission-type": ""
			values.put(VALUE_SUBMISSION_TYPE, Integer.toString(assn.getContent().getTypeOfSubmission()));
			if(Assignment.SCORE_GRADE_TYPE == assn.getContent().getTypeOfGrade()) {
				values.put(VALUE_MAX_SCORE, assn.getContent().getMaxGradePointDisplay());
			}
			// "calendar-time": 1234567890
			values.put(VALUE_CALENDAR_TIME, assn.getDueTimeString());
			try {
				values.put(VALUE_NEWS_TIME, new Date(props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE).getTime()));
			} catch (EntityPropertyNotDefinedException e) {
				logger.warn("getValues(" + entityReference + "," + localeCode + ") EntityPropertyNotDefinedException: " + e);
			} catch (EntityPropertyTypeException e) {
				logger.warn("getValues(" + entityReference + "," + localeCode + ") EntityPropertyTypeException: " + e);
			}
			// "description": "Long thing, markup, escaped",
			values.put(VALUE_DESCRIPTION, assn.getContent().getInstructions());
			// "title": "Assignment hoedown"
			values.put(VALUE_TITLE, assn.getTitle());
			// "user-name": "Creator's Name"
			User user = sakaiProxy.getUser(assn.getCreator());
			if(user != null) {
				values.put(VALUE_USER_NAME, user.getDisplayName());
			}
			
			// "more-info"
			List<Map<String,String>> infoList = new ArrayList<Map<String,String>>();
			Map<String,String> infoItem = new HashMap<String,String>();
			infoItem.put(VALUE_INFO_LINK_URL, assn.getUrl());
			infoItem.put(VALUE_INFO_LINK_TITLE, rl.getString("assignment.info.link"));
			infoList.add(infoItem);
			values.put(VALUE_MORE_INFO, infoList);
			
			// "attachments": [ ... ]
			List<Reference> attachments = assn.getContent().getAttachments();
			if(attachments != null && ! attachments.isEmpty()) {
				List<Map<String,String>> attList = new ArrayList<Map<String,String>>();
				for(Reference ref : attachments) {
					ContentResource resource = (ContentResource) ref.getEntity();
					Map<String, String> attInfo = new HashMap<String, String>();
					attInfo.put(VALUE_ATTACHMENT_TITLE, resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
					attInfo.put(VALUE_ATTACHMENT_URL, resource.getUrl());
					attInfo.put(VALUE_ATTACHMENT_MIMETYPE, resource.getContentType());
					attInfo.put(VALUE_ATTACHMENT_SIZE, Long.toString(resource.getContentLength()));
					attInfo.put(VALUE_ATTACHMENT_TARGET, this.sakaiProxy.getTargetForMimetype(resource.getContentType()));
					attList.add(attInfo );
				}
				values.put(VALUE_ATTACHMENTS, attList);
			}
		}
		
		return values;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.dash.entity.EntityType#getProperties(java.lang.String, java.lang.String)
	 */
	public Map<String, String> getProperties(String entityReference,
			String localeCode) {
		// TODO: create language bundle here or have SakaiProxy get the language bundle from assn??
		ResourceLoader rl = new ResourceLoader("dash_entity");
		Map<String, String> props = new HashMap<String, String>();
		props.put(LABEL_CALENDAR_TIME, rl.getString("assignment.calendar.time"));
		props.put(LABEL_USER_NAME, rl.getString("assignment.user.name"));
		props.put(LABEL_ATTACHMENTS, rl.getString("assignment.attachments"));
		return props;
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
		section1.add(VALUE_CALENDAR_TIME);
		section1.add(VALUE_USER_NAME);
		order.add(section1);
		List<String> section2 = new ArrayList<String>();
		section2.add(VALUE_ASSESSMENT_TYPE);
		section2.add(VALUE_GRADE_TYPE);
		section2.add(VALUE_SUBMISSION_TYPE);
		order.add(section2);
		List<String> section3 = new ArrayList<String>();
		section3.add(VALUE_DESCRIPTION);
		order.add(section3);
		List<String> section4 = new ArrayList<String>();
		section4.add(VALUE_ATTACHMENTS);
		order.add(section4);
		List<String> section5 = new ArrayList<String>();
		section5.add(VALUE_MORE_INFO);
		order.add(section5);
		return order;
	}

	public void init() {
		logger.info("init()");
		dashboardLogic.registerEntityType(this);
	}
}
