/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/AreaManagerImpl.java $
 * $Id: AreaManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums;

import java.util.Date;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.type.StringType;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class AreaManagerImpl extends HibernateDaoSupport implements AreaManager {

    private static final String QUERY_AREA_BY_CONTEXT_AND_TYPE_ID = "findAreaByContextIdAndTypeId";
    private static final String QUERY_AREA_BY_TYPE = "findAreaByType";

    // TODO: pull titles from bundle
    private static final String MESSAGECENTER_BUNDLE = "org.sakaiproject.api.app.messagecenter.bundle.Messages";
    private static final String MESSAGES_TITLE = "cdfm_message_pvtarea";
    private static final String FORUMS_TITLE = "cdfm_discussion_forums";

    private IdManager idManager;

    private MessageForumsForumManager forumManager;

    private SessionManager sessionManager;

    private MessageForumsTypeManager typeManager;

    private ServerConfigurationService serverConfigurationService;
    private Boolean DEFAULT_AUTO_MARK_READ = false;    

    private SiteService siteService;
    private ToolManager toolManager;
    
    /**
     * sakai.property for setting the default Messages tool option for sending a copy of a message
     * to recipient email addresses. Options are {@link Area#EMAIL_COPY_NEVER}, {@link Area#EMAIL_COPY_OPTIONAL},
     * and {@link Area#EMAIL_COPY_ALWAYS}
     */
    private static final String DEFAULT_SEND_TO_EMAIL_PROP = "msgcntr.defaultSendToEmailSetting";
    
    public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public void init() {
       log.info("init()");
       DEFAULT_AUTO_MARK_READ = serverConfigurationService.getBoolean("msgcntr.forums.default.auto.mark.threads.read", false);
    }



    public MessageForumsTypeManager getTypeManager() {
        return typeManager;
    }

    public void setTypeManager(MessageForumsTypeManager typeManager) {
        this.typeManager = typeManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public IdManager getIdManager() {
        return idManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    public void setForumManager(MessageForumsForumManager forumManager) {
        this.forumManager = forumManager;
    }

    public Area getPrivateArea() {
    	return getPrivateArea(getContextId());
    }
    
    public Area getPrivateArea(String siteId){
        Area area = getAreaByContextIdAndTypeId(siteId, typeManager.getPrivateMessageAreaType());
        if (area == null) {
            area = createArea(typeManager.getPrivateMessageAreaType(), siteId);
            area.setContextId(siteId);
            area.setName(getResourceBundleString(MESSAGES_TITLE));
            area.setEnabled(Boolean.FALSE);
            area.setHidden(Boolean.TRUE);
            area.setLocked(Boolean.FALSE);
            area.setModerated(Boolean.FALSE);
            area.setPostFirst(Boolean.FALSE);
	    area.setAutoMarkThreadsRead(DEFAULT_AUTO_MARK_READ);
            area.setSendToEmail(serverConfigurationService.getInt(DEFAULT_SEND_TO_EMAIL_PROP, Area.EMAIL_COPY_OPTIONAL));
            saveArea(area);
        }

        return area;
    }

    public Area getDiscusionArea() {
        return getDiscussionArea(this.getContextId());
    }
    
    public Area getDiscussionArea(String contextId) {
    	return getDiscussionArea(contextId, true);
    }
    
    public Area getDiscussionArea(String contextId, boolean createDefaultForum) {
    	log.debug("getDiscussionArea(" + contextId +")");
    	if (contextId == null) {
    		return getDiscusionArea();
    	}
    	Area area = this.getAreaByContextIdAndTypeId(contextId, typeManager.getDiscussionForumType());
    	
    	if (area == null) {
    		log.info("setting up a new Discussion Area for " + contextId);
    		area = createArea(typeManager.getDiscussionForumType(), contextId);
    		area.setName(getResourceBundleString(FORUMS_TITLE));
            area.setEnabled(Boolean.TRUE);
            area.setHidden(Boolean.TRUE);
            area.setLocked(Boolean.FALSE);
            area.setModerated(Boolean.FALSE);
            area.setPostFirst(Boolean.FALSE);
            area.setAutoMarkThreadsRead(DEFAULT_AUTO_MARK_READ);
            // this is a Messages tool option
	    area.setSendToEmail(serverConfigurationService.getInt(DEFAULT_SEND_TO_EMAIL_PROP, Area.EMAIL_COPY_OPTIONAL));
	    area.setAvailabilityRestricted(Boolean.FALSE);
            saveArea(area);
            //if set populate the default Forum and topic
            if  (createDefaultForum && serverConfigurationService.getBoolean("forums.setDefault.forum", true)) {
            	setAreaDefaultElements(area);
            }
            
    	}
    	
    	return area;
	}
    private void setAreaDefaultElements(Area area) {
    	log.info("setAreaDefaultElements(" + area.getId() + ")");
    	DiscussionForum forum = forumManager.createDiscussionForum();
    	forum.setArea(area);
    	String siteTitle = null;
    	try {
			Site site = siteService.getSite(area.getContextId());
			siteTitle = site.getTitle();
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		}
		//MSGCNTR-453
    	forum.setCreatedBy("admin");
    	forum.setTitle(getResourceBundleString("default_forum", new Object[]{(Object)siteTitle}));
    	forum.setDraft(false);
    	forum.setModerated(area.getModerated());
    	forum.setPostFirst(area.getPostFirst());
        forumManager.saveDiscussionForum(forum);
    	DiscussionTopic topic = forumManager.createDiscussionForumTopic(forum);
    	topic.setTitle(getResourceBundleString("default_topic"));
    	//MSGCNTR-453
    	topic.setCreatedBy("admin");
    	forumManager.saveDiscussionForumTopic(topic, false);
    	
    }
    
    public boolean isPrivateAreaEnabled() {
        return getPrivateArea().getEnabled().booleanValue();
    }

    public Area createArea(String typeId, String contextParam) {
    	
    	  if (log.isDebugEnabled())
        {
          log.debug("createArea(" + typeId + "," + contextParam + ")");
        }
    	      	      	  
        Area area = new AreaImpl();
        area.setUuid(getNextUuid());
        area.setTypeUuid(typeId);
        area.setCreated(new Date());
        area.setCreatedBy(getCurrentUser());
        
        /** compatibility with web services*/
        if (contextParam == null){
        	String contextId = getContextId();
        	if (contextId == null){
        		throw new IllegalStateException("Cannot retrive current context");
        	}        	
        	area.setContextId(contextId);
        }
        else{
        	area.setContextId(contextParam);
        }                      
                                                                         
        log.debug("createArea executed with areaId: " + area.getUuid());
        return area;
    }

    /**
     * This method sets the modified user and date.  It then checks all the open forums for a 
     * sort index of 0.  (if a sort index on a forum is 0 then it is new). If there is a 
     * zero sort index then it increments all the sort indices by one so the new sort index
     * becomes the first without having to rely on the creation date for the sorting.
     * 
     * @param area Area to save
     */
    public void saveArea(Area area) {
    	String currentUser = getCurrentUser();
    	saveArea( area, currentUser);
    }
    
    public void saveArea(Area area, String currentUser){
        boolean isNew = area.getId() == null;

        area.setModified(new Date());
        area.setModifiedBy(currentUser);
        
        boolean someForumHasZeroSortIndex = false;

        // If the open forums were not loaded then there is no need to redo the sort index
        //     thus if it's a hibernate persistentset and initialized
        if( area.getOpenForumsSet() != null &&
              ((area.getOpenForumsSet() instanceof PersistentSet &&
              ((PersistentSet)area.getOpenForumsSet()).wasInitialized()) || !(area.getOpenForumsSet() instanceof PersistentSet) )) {
           for(Iterator i = area.getOpenForums().iterator(); i.hasNext(); ) {
              BaseForum forum = (BaseForum)i.next();
              if(forum.getSortIndex().intValue() == 0) {
                 someForumHasZeroSortIndex = true;
                 break;
              }
           }
           if(someForumHasZeroSortIndex) {
              for(Iterator i = area.getOpenForums().iterator(); i.hasNext(); ) {
                 BaseForum forum = (BaseForum)i.next();
                 forum.setSortIndex(Integer.valueOf(forum.getSortIndex().intValue() + 1));
              }
           }
        }

        // until we have a settings screen to allow the user to hide all forums,
        // the area will always be available. 
        area.setAvailability(true); 
        
        getHibernateTemplate().saveOrUpdate(area);

        log.debug("saveArea executed with areaId: " + area.getId());
    }

    public void deleteArea(Area area) {
        getHibernateTemplate().delete(area);
        log.debug("deleteArea executed with areaId: " + area.getId());
    }

    /**
     * ContextId is present site id for now.
     */
    private String getContextId() {
        if (TestUtil.isRunningTests()) {
            return "test-context";
        }
        Placement placement = toolManager.getCurrentPlacement();
        String presentSiteId = placement.getContext();
        return presentSiteId;
    }

    public Area getAreaByContextIdAndTypeId(final String typeId) {
        log.debug("getAreaByContextIdAndTypeId executing for current user: " + getCurrentUser());
        return this.getAreaByContextIdAndTypeId(getContextId(), typeId);
    }
    
    public Area getAreaByContextIdAndTypeId(final String contextId, final String typeId) {
        log.debug("getAreaByContextIdAndTypeId executing for current user: " + getCurrentUser());
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query q = session.getNamedQuery(QUERY_AREA_BY_CONTEXT_AND_TYPE_ID);
                q.setParameter("contextId", contextId, StringType.INSTANCE);
                q.setParameter("typeId", typeId, StringType.INSTANCE);
                return q.uniqueResult();
            }
        };

        return (Area) getHibernateTemplate().execute(hcb);
    }
    
    

    public Area getAreaByType(final String typeId) {
      final String currentUser = getCurrentUser();
      log.debug("getAreaByType executing for current user: " + currentUser);
      HibernateCallback hcb = new HibernateCallback() {
          public Object doInHibernate(Session session) throws HibernateException {
              Query q = session.getNamedQuery(QUERY_AREA_BY_TYPE);              
              q.setParameter("typeId", typeId, StringType.INSTANCE);
              return q.uniqueResult();
          }
      };        
      return (Area) getHibernateTemplate().execute(hcb);
    }
       
    // helpers

    private String getNextUuid() {
        return idManager.createUuid();
    }

    private String getCurrentUser() {
    	String user = sessionManager.getCurrentSessionUserId();
  		return (user == null) ? "test-user" : user;
    }

    private String getEventMessage(Object object) {
    	  return "/MessageCenter/site/" + getContextId() + "/" + object.toString() + "/" + getCurrentUser(); 
        //return "MessageCenter::" + getCurrentUser() + "::" + object.toString();
    }

    /**
     * Gets Strings from Message Bundle (specifically for titles)
     * 
     * @param key
     * 			Message bundle key for String wanted
     * 
     * @return
     * 			String requested or "[missing key: key]" if not found
     */
    public String getResourceBundleString(String key) 
    {
    	final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);
    	return rb.getString(key);
    }
    
    private String getResourceBundleString(String key, Object[] replacementValues) 
    {
    	final ResourceLoader rb = new ResourceLoader(MESSAGECENTER_BUNDLE);
    	return rb.getFormattedMessage(key, replacementValues);
    	
    	
    }

}
