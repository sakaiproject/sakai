/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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

package org.sakaiproject.lessonbuildertool.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;

import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentEdit;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.cover.AssignmentService;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;

import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.impl.DBLTIService; // HACK

import org.sakaiproject.util.foorm.SakaiFoorm;

/**
 * Interface to Assignment
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */

// NOTE: almost no other class should import this. We want to be able
// to support both forums and jforum. So typically there will be a 
// forumEntity, but it's injected, and it can be either forum and jforum.
// Hence it has to be declared LessonEntity. That leads to a lot of
// declarations like LessonEntity forumEntity.  In this case forumEntity
// means either a ForumEntity or a JForumEntity. We can't just call the
// variables lessonEntity because the same module will probably have an
// injected class to handle tests and quizes as well. That will eventually
// be converted to be a LessonEntity.

public class BltiEntity implements LessonEntity {

    private static Log log = LogFactory.getLog(BltiEntity.class);

    private static Cache bltiCache = null;
    protected static final int DEFAULT_EXPIRATION = 10 * 60;

    private SimplePageBean simplePageBean;

    protected static LTIService ltiService = null; 
    protected static SakaiFoorm foorm = new SakaiFoorm();


    public void setSimplePageBean(SimplePageBean simplePageBean) {
	this.simplePageBean = simplePageBean;
    }

    private LessonEntity nextEntity = null;
    public void setNextEntity(LessonEntity e) {
	nextEntity = e;
    }
    public LessonEntity getNextEntity() {
	return nextEntity;
    }
    
    static MemoryService memoryService = null;
    public void setMemoryService(MemoryService m) {
	memoryService = m;
    }

    static MessageLocator messageLocator = null;
    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }

    public void init () {
	bltiCache = memoryService
	    .newCache("org.sakaiproject.lessonbuildertool.service.BltiEntity.cache");

	log.info("init()");
	if ( ltiService == null ) { 
		ltiService = (LTIService) new DBLTIService(); 
		((org.sakaiproject.lti.impl.DBLTIService) ltiService).setAutoDdl("true"); 
		((org.sakaiproject.lti.impl.DBLTIService) ltiService).init(); 
	} 

    }

    public void destroy()
    {
	bltiCache.destroy();
	bltiCache = null;

	log.info("destroy()");
    }


    // to create bean. the bean is used only to call the pseudo-static
    // methods such as getEntitiesInSite. So type, id, etc are left uninitialized

    protected BltiEntity() {
    }

    protected BltiEntity(int type, String id) {
	this.type = type;
	this.id = id;
    }

    public String getToolId() {
	return "sakai.blti";
    }

    // the underlying object, something Sakaiish
    protected String id;
    protected int type;
    // not required fields. If we need to look up
    // the actual objects, lets us cache them

    protected Map<String,Object> content;

/*
    public Blti getBlti(String ref, boolean nocache) {
	Blti ret = (Blti)bltiCache.get(ref);
	if (!nocache && ret != null)
	    return ret;

	try {
	    //  ret = BltiService.getBlti(ref);
	} catch (Exception e) {
	    ret = null;
	}
	
	if (ret != null)
	    bltiCache.put(ref, ret, DEFAULT_EXPIRATION);
	return ret;
    }
*/

    // type of the underlying object
    public int getType() {
	return type;
    }

    public int getLevel() {
	return 0;
    }

    public int getTypeOfGrade() {
	return 1;
    }

  // hack for forums. not used for assessments, so always ok
    public boolean isUsable() {
	return true;
    }

    public String getReference() {
	return "/" + BLTI + "/" + id;
    }

    // find topics in site, but organized by forum
    public List<LessonEntity> getEntitiesInSite() {
	List<LessonEntity> ret = new ArrayList<LessonEntity>();
	List<Map<String,Object>> contents = ltiService.getContents(null,null,0,0);
	for (Map<String, Object> content : contents ) {
	    Long id = foorm.getLong(content.get("id"));
	    if ( id == -1 ) continue;
	    BltiEntity entity = new BltiEntity(TYPE_BLTI, id.toString());
	    entity.content = content;
	    ret.add(entity);
	}
	return ret;
    }

    public LessonEntity getEntity(String ref) {
	int i = ref.indexOf("/",1);

	String typeString = ref.substring(1, i);
	String idString = ref.substring(i+1);
	String id = "";
	try {
	    id = idString;
	} catch (Exception ignore) {
	    return null;
	}

	if (typeString.equals(BLTI)) {
	    return new BltiEntity(TYPE_BLTI, id);
	} else if (nextEntity != null) {
	    // in case we chain to a different implementation. Not likely for BLTI
	    return nextEntity.getEntity(ref);
	} else
	    return null;
    }

    protected void loadContent() {
	if ( content != null ) return;
	if ( id == null ) return; // Likely a failure
	Long key = foorm.getLong(id);
	content = ltiService.getContent(key);
	System.out.println("Key="+key+" Retrived content="+content);
    }	

    // properties of entities
    public String getTitle() {
	loadContent();
	if ( content == null ) return null;
	return (String) content.get("title");
    }

    public String getUrl() {
	loadContent();
	if ( content == null ) return null;
	return (String) content.get("launch_url");
    }

    public Date getDueDate() {
	return null;
    }

    // the following methods all take references. So they're in effect static.
    // They ignore the entity from which they're called.
    // The reason for not making them a normal method is that many of the
    // implementations seem to let you set access control and find submissions
    // from a reference, without needing the actual object. So doing it this
    // way could save some database activity

    // access control
    public boolean addEntityControl(String siteId, String groupId) throws IOException {
	// not used for BLTI, control is done entirely within LB
	return false;
    }

    public boolean removeEntityControl(String siteId, String groupId) throws IOException {
	return false;
    }

    // submission
    // do we need the data from submission?
    public boolean needSubmission(){
	return false;
    }

    public LessonSubmission getSubmission(String userId) {
	// students don't have submissions to BLTI
	return null;
    }

// we can do this for real, but the API will cause us to get all the submissions in full, not just a count.
// I think it's cheaper to get the best assessment, since we don't actually care whether it's 1 or >= 1.
    public int getSubmissionCount(String user) {
	return 0;
    }

    // URL to create a new item. Normally called from the generic entity, not a specific one                                                 
    // can't be null                                                                                                                         
    public List<UrlItem> createNewUrls(SimplePageBean bean) {
	return null;
    }


    // URL to edit an existing entity.                                                                                                       
    // Can be null if we can't get one or it isn't needed                                                                                    
    public String editItemUrl(SimplePageBean bean) {
	return null;
    }


    // for most entities editItem is enough, however tests allow separate editing of                                                         
    // contents and settings. This will be null except in that situation                                                                     
    public String editItemSettingsUrl(SimplePageBean bean) {
	return null;
    }

    // return the list of groups if the item is only accessible to specific groups
    // null if it's accessible to the whole site.
    public Collection<String> getGroups(boolean nocache) {
	// done entirely witnin LB, this item type is not group-aware
	return null;
    }
  
    // set the item to be accessible only to the specific groups.
    // null to make it accessible to the whole site
    public void setGroups(Collection<String> groups) {
	// not group aware
    }

}
