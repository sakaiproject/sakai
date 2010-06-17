package org.sakaiproject.profile2.tool.entityprovider;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.profile2.logic.ProfileMessagingLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePreferences;

/**
 * This is the entity provider for messaging in Profile2
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfileMessagingEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, AutoRegisterEntityProvider, RESTful {
	
	public final static String ENTITY_PREFIX = "profile-message";
	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
		
	public boolean entityExists(String eid) {
		return true;
	}

	public Object getSampleEntity() {
		return new ProfilePreferences();
	}
	
	
	@EntityCustomAction(action="unread-count",viewKey=EntityView.VIEW_LIST)
	public Object getUnreadMessageCount(EntityView view) {
		
		//get current user
		String uuid = sakaiProxy.getCurrentUserId();
		
		//get count & return
		int count = messagingLogic.getAllUnreadMessagesCount(uuid);
		return new ActionReturn(count);
	}
	
	public Object getEntity(EntityReference ref) {
		return null;
	}
	
	
	
	
	public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		// TODO Auto-generated method stub
		
	}
	
	
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		// TODO Auto-generated method stub
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getHandledOutputFormats() {
		return new String[] {Formats.XML, Formats.JSON};
	}

	public String[] getHandledInputFormats() {
		return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
	}
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
		
	private ProfileMessagingLogic messagingLogic;
	public void setMessagingLogic(ProfileMessagingLogic messagingLogic) {
		this.messagingLogic = messagingLogic;
	}
}
