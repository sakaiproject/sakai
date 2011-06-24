package org.sakaiproject.profile2.entity;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.io.Reader;

import lombok.Setter;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.search.api.EntityContentProducer;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.user.api.User;

import org.apache.log4j.Logger;

public class ProfileContentProducer implements EntityContentProducer {
	
	@Setter
	private ProfileLogic profileLogic = null;
	
	@Setter
	private SearchService searchService = null;
	
	@Setter
	private SearchIndexBuilder searchIndexBuilder = null;
	
	@Setter
	private SakaiProxy sakaiProxy = null;
	
	@Setter
	private SakaiPersonManager sakaiPersonManager = null;
	

	private Logger logger = Logger.getLogger(ProfileContentProducer.class);

	public void init() {
		searchService.registerFunction(ProfileConstants.EVENT_PROFILE_INFO_UPDATE);
		searchIndexBuilder.registerEntityContentProducer(this);
	}

	public boolean canRead(String reference) {
		if (logger.isDebugEnabled())
			logger.debug("canRead()");

		// TODO: sort this !
		return true;
	}

	public Integer getAction(Event event) {
		if (logger.isDebugEnabled())
			logger.debug("getAction()");

		String eventName = event.getEvent();

		if (ProfileConstants.EVENT_PROFILE_INFO_UPDATE.equals(eventName)) {
			return SearchBuilderItem.ACTION_ADD;
		} else
			return SearchBuilderItem.ACTION_UNKNOWN;
	}

	public String getContainer(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("getContainer()");

		return null;
	}

	public String getContent(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("getContent(" + ref + ")");

		String[] parts = ref.split(Entity.SEPARATOR);

		String type = parts[1];
		String id = parts[2];

		if ("profile".equals(type)) {
			try {
				SakaiPerson sp = sakaiPersonManager.getSakaiPerson(id, sakaiPersonManager.getUserMutableType());
				String notes = "";
				if(sp != null)
					notes = sp.getNotes();
				return notes;
			} catch (Exception e) {
				logger.error(
						"Caught exception whilst getting content for profile '"
								+ id + "'", e);
			}
		}

		return null;
	}

	public Reader getContentReader(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("getContentReader(" + ref + ")");

		return null;
	}

	public Map getCustomProperties(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("getCustomProperties(" + ref + ")");
		// TODO Auto-generated method stub
		return null;
	}

	public String getCustomRDF(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("getCustomRDF(" + ref + ")");

		// TODO Auto-generated method stub
		return null;
	}

	public String getId(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("getId(" + ref + ")");

		String[] parts = ref.split(Entity.SEPARATOR);

		if (parts.length == 3) {
			return parts[2];
		}

		return "unknown";
	}

	public List getSiteContent(String siteId) {
		if (logger.isDebugEnabled())
			logger.debug("getSiteContent(" + siteId + ")");
		
		List refs = new ArrayList();
		
		if(siteId.startsWith("~") && siteId.length() == 37) {
			// This is a user workspace
			String uuid = siteId.substring(1);
			
			if(uuid != null) {
				refs.add("/profile/" + uuid);
			}
		}

		return refs;
	}

	public Iterator getSiteContentIterator(String siteId) {
		if (logger.isDebugEnabled())
			logger.debug("getSiteContentIterator(" + siteId + ")");

		return getSiteContent(siteId).iterator();
	}

	public String getSiteId(String eventRef) {
		if (logger.isDebugEnabled())
			logger.debug("getSiteId(" + eventRef + ")");

		String[] parts = eventRef.split(Entity.SEPARATOR);
		
		if (parts.length == 3) {
			String id = "~" + parts[2];
			return id;
		}

		return null;
	}

	public String getSubType(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("getSubType(" + ref + ")");

		return null;
	}

	public String getTitle(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("getTitle(" + ref + ")");

		String[] parts = ref.split(Entity.SEPARATOR);
		String type = parts[1];
		String id = parts[2];

		if ("profile".equals(type)) {
			try {
				User user = sakaiProxy.getUserById(id);
				return user.getDisplayName();
			} catch (Exception e) {
				logger.error("Caught exception whilst getting user display name for id '"
						+ id + "'", e);
			}
		}

		return "Unrecognised";
	}

	public String getTool() {
		return "Profile";
	}

	public String getType(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("getType(" + ref + ")");

		return "profile";
	}

	public String getUrl(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("getUrl(" + ref + ")");

		String[] parts = ref.split(Entity.SEPARATOR);
		String type = parts[1];
		String id = parts[2];

		/*
		if ("profile".equals(type)) {
			try {
				UserProfile profile = profileLogic.getUserProfile(id);
				return sakaiProxy.getDirectUrlToUserProfile(id, null);
			} catch (Exception e) {
				logger.error("Caught exception whilst getting url for profile '"
						+ id + "'", e);
			}
		}
		*/

		return null;
	}

	public boolean isContentFromReader(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("isContentFromReader(" + ref + ")");

		return false;
	}

	public boolean isForIndex(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("isForIndex(" + ref + ")");

		return true;
	}

	public boolean matches(String ref) {
		if (logger.isDebugEnabled())
			logger.debug("matches(" + ref + ")");

		String[] parts = ref.split(Entity.SEPARATOR);

		if ("profile".equals(parts[1]))
			return true;

		return false;
	}

	public boolean matches(Event event) {
		String eventName = event.getEvent();

		if (ProfileConstants.EVENT_PROFILE_INFO_UPDATE.equals(eventName)) {
			return true;
		}

		return false;
	}
}
