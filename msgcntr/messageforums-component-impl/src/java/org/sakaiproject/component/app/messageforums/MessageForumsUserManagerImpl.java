/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/MessageForumsUserManagerImpl.java $
 * $Id: MessageForumsUserManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.MessageForumsUserManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageForumsUserImpl;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class MessageForumsUserManagerImpl extends HibernateDaoSupport implements MessageForumsUserManager {

  private static final Log LOG = LogFactory.getLog(MessageForumsUserManagerImpl.class);
  private static final String QUERY_BY_USER_ID = "findUserByUserId";    
  
  /** sakai dependencies */
  protected IdManager idManager;  
  protected UserDirectoryService userDirectoryService;
  
  private EventTrackingService eventTrackingService;

  public void init() {
     LOG.info("init()");
      ;
  }

  public EventTrackingService getEventTrackingService() {
      return eventTrackingService;
  }

  public void setEventTrackingService(EventTrackingService eventTrackingService) {
      this.eventTrackingService = eventTrackingService;
  }
  
  /**
   * @throws UserNotDefinedException 
 * @see org.sakaiproject.api.app.messageforums.MessageForumsUserManager#getForumUser(java.lang.String)
   */
  public MessageForumsUser getForumUser(final String userId)
  {
    if (LOG.isDebugEnabled()){
      LOG.debug("getForumUser(userId: " + userId + ")");
    }
    
    if (userId == null){
      throw new IllegalArgumentException("Null Argument");
    }
    
    if (TestUtil.isRunningTests()){
      MessageForumsUser newUser = new MessageForumsUserImpl();
      newUser.setUuid(getNextUuid());
      newUser.setUserId(userId);
      saveForumUser(newUser);
      return newUser;
    }
    
    HibernateCallback hcb = new HibernateCallback() {
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Query q = session.getNamedQuery(QUERY_BY_USER_ID);
        q.setParameter("userId", userId, Hibernate.STRING);          
        return q.uniqueResult();
      }
    };
  
    MessageForumsUser user = (MessageForumsUser) getHibernateTemplate().execute(hcb);
    
    if (user == null){
      
      /** ensure user exists from user provider */
      try {
		userDirectoryService.getUser(userId);
	} catch (UserNotDefinedException e) {
		e.printStackTrace();
	}
      MessageForumsUser newUser = new MessageForumsUserImpl();
      newUser.setUuid(getNextUuid());
      newUser.setUserId(userId);
      saveForumUser(newUser);
 // commented out when splitting events between Messages tool and Forums tool
 //     eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_RESOURCE_ADD, getEventMessage(newUser), false));
      return newUser;
    }
    else{
      return user;
    }          
  }
  
 
  /**
   * @see org.sakaiproject.api.app.messageforums.MessageForumsUserManager#saveForumUser(org.sakaiproject.api.app.messageforums.MessageForumsUser)
   */
  public void saveForumUser(MessageForumsUser user)
  {    
    getHibernateTemplate().saveOrUpdate(user);
    LOG.debug("saveDiscussionForumTopic executed with topicId: " + user.getUuid());    
  }

  private String getNextUuid() {        
    return idManager.createUuid();
  }
  
  public void setIdManager(IdManager idManager) {
    this.idManager = idManager;
  }

  public void setUserDirectoryService(UserDirectoryService userDirectoryService)
  {
    this.userDirectoryService = userDirectoryService;
  }
    
  private String getEventMessage(Object object) {
  	return "/MessageCenter/site/" + getContextId() + "/" + object.toString() ; 
      //return "MessageCenter::" + object.toString();
  }

  private String getContextId() {
    if (TestUtil.isRunningTests()) {
        return "test-context";
    }
    Placement placement = ToolManager.getCurrentPlacement();
    String presentSiteId = placement.getContext();
    return presentSiteId;
  }

}