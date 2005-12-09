/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.MessageForumsUserManager;
import org.sakaiproject.api.kernel.id.IdManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageForumsUserImpl;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.user.UserDirectoryService;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class MessageForumsUserManagerImpl extends HibernateDaoSupport implements MessageForumsUserManager {

  private static final Log LOG = LogFactory.getLog(MessageForumsUserManagerImpl.class);
  private static final String QUERY_BY_USER_ID = "findUserByUserId";    
  
  /** sakai dependencies */
  protected IdManager idManager;  
  protected UserDirectoryService userDirectoryService;
  
  public void init(){}

  
  /**
   * @see org.sakaiproject.api.app.messageforums.MessageForumsUserManager#getForumUser(java.lang.String)
   */
  public MessageForumsUser getForumUser(final String userId) throws IdUnusedException{
    
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
      userDirectoryService.getUser(userId);
      MessageForumsUser newUser = new MessageForumsUserImpl();
      newUser.setUuid(getNextUuid());
      newUser.setUserId(userId);
      saveForumUser(newUser);
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
    
}