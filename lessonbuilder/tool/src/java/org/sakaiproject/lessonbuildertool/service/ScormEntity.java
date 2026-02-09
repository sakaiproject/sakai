/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2015 Rutgers, the State University of New Jersey
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

package org.sakaiproject.lessonbuildertool.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.jdom2.Element;
import org.jdom2.Namespace;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.scorm.dao.api.ContentPackageDao;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.Validator;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * Interface to Scorm
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */
@Slf4j
public class ScormEntity implements LessonEntity, AssignmentInterface {
    private SimplePageBean simplePageBean;

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
    
    public void setPrevEntity(LessonEntity e) {
	e.setNextEntity(this);
    }

    static MessageLocator messageLocator = null;
    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }


    static ContentPackageDao dao = null;
    static ScormResourceService scormResourceService = null;
    static ScormResultService scormResultService = null;

    public void init () {
	dao = (ContentPackageDao)ComponentManager.get("org.sakaiproject.scorm.dao.api.ContentPackageDao");
	scormResourceService = (ScormResourceService)ComponentManager.get("org.sakaiproject.scorm.service.api.ScormResourceService");
	scormResultService = (ScormResultService)ComponentManager.get("org.sakaiproject.scorm.service.api.ScormResultService");
	// don't use scorm unless all three are up. Code will only check dao
	if (scormResourceService == null ||
	    scormResultService == null)
	    dao = null;
	log.info("init() " + dao);
    }

    public void destroy()
    {
	log.info("destroy()");
    }


    // to create bean. the bean is used only to call the pseudo-static
    // methods such as getEntitiesInSite. So type, id, etc are left uninitialized

    protected ScormEntity() {
    }

    /* id is a string containing a long in this case */
    protected ScormEntity(int type, String id, int level) 
	throws NumberFormatException {
	this.type = type;
	this.id = Long.parseLong(id);
	this.level = level;
    }

    protected ScormEntity(int type, Long id, int level) 
	throws NumberFormatException {
	this.type = type;
	this.id = id;
	this.level = level;
    }

    public String getToolId() {
	return "sakai.scorm.helper";
    }

    // the underlying object, something Sakaiish
    protected Long id;
    protected int type;
    protected int level;
    // not required fields. If we need to look up
    // the actual objects, lets us cache them
    protected ContentPackage assignment;;

    // ref looks like /scorm/id
    public ContentPackage getAssignment(Long id) {
	return getAssignment(id, false);
    }

    public ContentPackage getAssignment(Long id, boolean nocache) {
	assignment = dao.load(id);
	return assignment;
    }

    // type of the underlying object
    public int getType() {
	return type;
    }

    public int getLevel() {
	return level;
    }

    public int getTypeOfGrade() {
	return 1;
    }

    public boolean showAdditionalLink() {
	return false;
    }

    public String getDescription() {
	return "";
    }

  // hack for forums. not used for assessments, so always ok
    public boolean isUsable() {
	return true;
    }

    public String getReference() {
	return "/" + SCORM + "/" + id;
    }

    public List<LessonEntity> getEntitiesInSite() {
	return getEntitiesInSite(null);
    }

    // find topics in site, but organized by forum
    public List<LessonEntity> getEntitiesInSite(final SimplePageBean bean) {

	if (dao == null) {
	    if (nextEntity != null) 
		return nextEntity.getEntitiesInSite();
	    else
		return new ArrayList<LessonEntity>();
	}

	List<LessonEntity> lessons = new ArrayList<LessonEntity>();

	String siteId = ToolManager.getCurrentPlacement().getContext();

	List<ContentPackage> packages = dao.find(siteId);
	log.info("pckages " + packages.size());

	for (ContentPackage contentPackage: packages) {
	    if (contentPackage.isDeleted())
		continue;
	    ScormEntity entity = new ScormEntity(TYPE_SCORM, contentPackage.getContentPackageId(), 1);
	    entity.assignment = contentPackage;
	    entity.simplePageBean = bean;
	    lessons.add(entity);
	}

	return lessons;
    }
			
    public LessonEntity getEntity(String ref) {
	return getEntity(ref, null);
    }

    public LessonEntity getEntity(String ref, SimplePageBean bean) {

	int i = ref.indexOf("/",1);

	String typeString = ref.substring(1, i);
	String idString = ref.substring(i+1);

	if (typeString.equals(SCORM)) {
	    ScormEntity entity = new ScormEntity(TYPE_SCORM, idString, 1); // 
	    entity.setSimplePageBean(bean);
	    return entity;
	} else if (nextEntity != null) {
	    return nextEntity.getEntity(ref, bean);
	} else
	    return null;
    }
	

    // properties of entities
    public String getTitle() {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return null;
	log.info("title " + assignment.getTitle());
	return assignment.getTitle();
    }

    // http://heidelberg.rutgers.edu/portal/tool/575de542-a928-41a8-aab0-348a67e2ccc1/student-submit/5
    public String getUrl() {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return null;

	log.info("simplepagebean " + simplePageBean);
	log.info(simplePageBean.getCurrentTool("sakai.scorm.tool"));
	log.info(assignment.getResourceId());
	log.info(assignment.getTitle());
	return ServerConfigurationService.getToolUrl() + "/" + simplePageBean.getCurrentTool("sakai.scorm.tool") + "/?wicket:bookmarkablePage=ScormPlayer:org.sakaiproject.scorm.ui.player.pages.PlayerPage&contentPackageId=" + id + "&resourceId=" + assignment.getResourceId() + "&title=" + assignment.getTitle();
    }

    public Date getDueDate() {
	return null;
    }

    public Double toDouble(Object f) {
	if (f instanceof Double)
	    return (Double)f;
	else if (f instanceof Float)
	    return ((Float)f).doubleValue();
        else
	    return null;
    }

    public LessonSubmission getSubmission(String userId) {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return null;
	if (scormResultService.getNewstAttempt(assignment.getContentPackageId(), userId) != null)
	    return new LessonSubmission(null);
	return null;
    }

// we can do this for real, but the API will cause us to get all the submissions in full, not just a count.
// I think it's cheaper to get the best assessment, since we don't actually care whether it's 1 or >= 1.
    public int getSubmissionCount(String user) {
	if (assignment == null)
	    assignment = getAssignment(id);
	if (assignment == null)
	    return 0;
	return scormResultService.countAttempts(assignment.getContentPackageId(), user);
    }

    // URL to create a new item. Normally called from the generic entity, not a specific one                                                 
    // can't be null                                                                                                                         
    public List<UrlItem> createNewUrls(SimplePageBean bean) {
	ArrayList<UrlItem> list = new ArrayList<UrlItem>();
	if (dao == null)
	    return list;
	String tool = bean.getCurrentTool("sakai.scorm.tool");
	if (tool != null) {
	    String url = ServerConfigurationService.getToolUrl() + "/" + tool;
	    list.add(new UrlItem(url, messageLocator.getMessage("simplepage.create_scorm")));
	}
	return list;
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

   public boolean objectExists() {
       if (dao == null)
	   return false;
       return true;
   }

    public boolean notPublished(String ref) {
	return false;
    }

    public boolean notPublished() {
	return false;
    }

    // return the list of groups if the item is only accessible to specific groups
    // null if it's accessible to the whole site.
    public Collection<String> getGroups(boolean nocache) {
	List<String>ret = new ArrayList<String>();
	return ret;
    }
  
    // set the item to be accessible only to the specific groups.
    // null to make it accessible to the whole site
    //
    // We can't access the A2 support code, because it's inside the component,with no
    // external API. This code was written before I figured out how to use the DAO.
    // I may be able to rewrite it that way. Note that Oracle needs slightly diffferent
    // code, so we use a loadable class for some of the SQL statements
    //
    public void setGroups(Collection<String> tgroups) {

    }

    // currently assignment 2 does not participate in the fixup. However I'm going to include
    // the ID anyway, just in case it happens in the future
    public String getObjectId(){
	String title = getTitle();
	if (title == null)
	    return null;
	return "scorm/" + id + "/" + title;
    }

    public String findObject(String objectid, Map<String,String>objectMap, String siteid) {
	if (!objectid.startsWith("scorm/")) {
	    if (nextEntity != null)
		return nextEntity.findObject(objectid, objectMap, siteid);
	    return null;
	}

	return null;

    }

    public String importObject(String title, String href, String mime, boolean hide){

	return null;
    }


    public String importObject(Element resource, Namespace ns, String base, String baseDir, List<String>attachments, boolean hide) {
	return null;
    }

    public String removeDotDot(String s) {
	while (true) {
	    int i = s.indexOf("/../");
	    if (i < 1)
		return s;
	    int j = s.lastIndexOf("/", i-1);
	    if (j < 0)
		j = 0;
	    else
		j = j + 1;
	    s = s.substring(0, j) + s.substring(i+4);
	}
    }

    public String getSiteId() {
	return null;
    }

    @Override
    public void preShowItem(SimplePageItem simplePageItem) {
    }

}
