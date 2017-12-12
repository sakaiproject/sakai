/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.lessonbuildertool.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentDates;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.ImportQtiService;
import org.etudes.mneme.api.SubmissionService;

import org.w3c.dom.Document;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;  
import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;         

/**
 * Interface to Mneme
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */
@Slf4j
public class MnemeEntity implements LessonEntity, QuizEntity {
    private static Cache assessmentCache = null;
    protected static final int DEFAULT_EXPIRATION = 10 * 60;


    static AssessmentService assessmentService = (AssessmentService)
	    ComponentManager.get("org.etudes.mneme.api.AssessmentService");


    static SubmissionService submissionService = (SubmissionService)
	    ComponentManager.get("org.etudes.mneme.api.SubmissionService");

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
    
    // stick ourselves after the last required entity
    public void setPrevEntity(LessonEntity e) {
	e.setNextEntity(this);
    }

    static MemoryService memoryService = null;
    public void setMemoryService(MemoryService m) {
	memoryService = m;
    }

    static MessageLocator messageLocator = null;
    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }

    static ImportQtiService importQtiService = (ImportQtiService)
	    ComponentManager.get("org.etudes.mneme.api.ImportQtiService");

    public void init () {
	assessmentCache = memoryService
	    .getCache("org.sakaiproject.lessonbuildertool.service.MnemeEntity.cache");

	log.info("init()");

    }

    public void destroy()
    {
	assessmentCache.destroy();
	assessmentCache = null;

	log.info("destroy()");
    }


    // to create bean. the bean is used only to call the pseudo-static
    // methods such as getEntitiesInSite. So type, id, etc are left uninitialized

    protected MnemeEntity() {
    }

    protected MnemeEntity(int type, String id, int level) {
	this.type = type;
	this.id = id;
	this.level = level;
    }

    public String getToolId() {
	return "sakai.mneme";
    }

    // the underlying object, something Sakaiish
    protected String id;
    protected int type;
    protected int level;
    // not required fields. If we need to look up
    // the actual objects, lets us cache them

    private Assessment assessment;

    private Assessment getAssessment(String id) {
	
	Assessment ret = (Assessment)assessmentCache.get(id);

	if (ret != null) {
	    // don't check for published. Faculty may temporarily unpublish
	    // to edit. We don't want it to disappear from students
            if (ret.getArchived() || ret.getMint())
		return null;
	    return ret;
	}

	if (assessmentService == null)
	    return null;
	ret = assessmentService.getAssessment(id);

	if (ret != null) {
	    // cache it if we find it, even if not acceptable
	    assessmentCache.put(id, ret, DEFAULT_EXPIRATION);

	    // don't check for published. Faculty may temporarily unpublish
	    // to edit. We don't want it to disappear from students
            if (ret.getArchived() || ret.getMint())
		return null;
	}

	return ret;
    }

    // type of the underlying object
    public int getType() {
	return type;
    }

    public int getTypeOfGrade() {
	return 1;
    }

    public int getLevel() {
	return level;
    }

  // hack for forums. not used for assessments, so always ok
    public boolean isUsable() {
	return true;
    }

    public String getReference() {
	return "/" + MNEME + "/" + id;
    }

    public List<LessonEntity> getEntitiesInSite() {
	return getEntitiesInSite(null);
    }

    // find topics in site, but organized by forum
    public List<LessonEntity> getEntitiesInSite(SimplePageBean bean) {

	Session ses = SessionManager.getCurrentSession();

	List<LessonEntity> ret = new ArrayList<LessonEntity>();

	if (assessmentService == null)
	    return ret;

	List<Assessment> plist = assessmentService.getContextAssessments(ToolManager.getCurrentPlacement().getContext(), AssessmentService.AssessmentsSort.title_a, true);

	// security. assume this is only used in places where it's OK, so skip security checks
	for (Assessment assessment: plist) {
	    MnemeEntity entity = new MnemeEntity(TYPE_MNEME, assessment.getId(), 1);
	    entity.assessment = assessment;
	    ret.add(entity);
	}

	if (nextEntity != null) 
	    ret.addAll(nextEntity.getEntitiesInSite(bean));

	return ret;
    }

    public LessonEntity getEntity(String ref, SimplePageBean o) {
	return getEntity(ref);
    }

    public LessonEntity getEntity(String ref) {
	int i = ref.indexOf("/",1);
	if (i < 1 && i < ref.length()) {
		// We didn't find it.
		return null;
	}
	String typeString = ref.substring(1, i);
	String id = ref.substring(i+1);

	if (typeString.equals(MNEME)) {
	    return new MnemeEntity(TYPE_MNEME, id, 1);
	} else if (nextEntity != null) {
	    return nextEntity.getEntity(ref);
	} else
	    return null;
    }
	
    // properties of entities
    public String getTitle() {
	if (assessment == null)
	    assessment = getAssessment(id);
	if (assessment == null)
	    return null;
	return assessment.getTitle();
    }

    public String getUrl() {
	Site site = null;
	ToolConfiguration siteTool = null;
	
	try {
	    site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
	    siteTool = site.getToolForCommonId("sakai.mneme");
	} catch (Exception e) {
	    return null;
	}

	if (siteTool == null)
	    return null;
	
	return ServerConfigurationService.getToolUrl()+ "/" + siteTool.getId() + "/enter/" + id;

    }


    // I don't think they have this
    public Date getDueDate() {
	if (assessment == null)
	    assessment = getAssessment(id);
	if (assessment == null)
	    return null;
	return assessment.getDates().getDueDate();
    }

    // the following methods all take references. So they're in effect static.
    // They ignore the entity from which they're called.
    // The reason for not making them a normal method is that many of the
    // implementations seem to let you set access control and find submissions
    // from a reference, without needing the actual object. So doing it this
    // way could save some database activity

    // access control
    public boolean addEntityControl(String siteId, String groupId) throws IOException {
	return true;
    }

    public boolean removeEntityControl(String siteId, String groupId) throws IOException {
	return true;
    }

    // submission
    // do we need the data from submission?
    public boolean needSubmission(){
	return true;
    }

    public LessonSubmission getSubmission(String user) {
	if (assessment == null)
	    assessment = getAssessment(id);
	if (assessment == null) {
	    log.warn("can't find published " + id);
	    return null;
	}

	if (submissionService == null)
	    return null;

	Session ses = SessionManager.getCurrentSession();

	Double score = toDouble(submissionService.getSubmissionOfficialScore(assessment, ses.getUserId()));

	if (score == null)
	    return null;

	return new LessonSubmission(score);
    }

    public Double toDouble(Object f) {
        if (f instanceof Double)
            return (Double)f;
        else if (f instanceof Float)
            return ((Float)f).doubleValue();
        else
            return null;
    }


// we can do this for real, but the API will cause us to get all the submissions in full, not just a count.
// I think it's cheaper to get the best assessment, since we don't actually care whether it's 1 or >= 1.
    public int getSubmissionCount(String user) {
	if (getSubmission(user) == null)
	    return 0;
	else
	    return 1;
    }

    // URL to create a new item. Normally called from the generic entity, not a specific one                                                 
    // can't be null                                                                                                                         
    public List<UrlItem> createNewUrls(SimplePageBean bean) {
	ArrayList<UrlItem> list = new ArrayList<UrlItem>();
	String tool = bean.getCurrentTool("sakai.mneme");
	if (tool != null) {
	    tool = ServerConfigurationService.getToolUrl()+ "/" + tool + "/assessments";
	    list.add(new UrlItem(tool, messageLocator.getMessage("simplepage.create_mneme")));
	}
	if (nextEntity != null)
	    list.addAll(nextEntity.createNewUrls(bean));
	return list;
    }

    // URL to edit an existing entity.                                                                                                       
    // Can be null if we can't get one or it isn't needed                                                                                    
    public String editItemUrl(SimplePageBean bean) {
	String tool = bean.getCurrentTool("sakai.mneme");
	if (tool == null)
	    return null;
    
	return ServerConfigurationService.getToolUrl()+ "/" + tool + "/assessment_edit/" + id + "/1";
    }


    // for most entities editItem is enough, however tests allow separate editing of                                                         
    // contents and settings. This will be null except in that situation                                                                     
    public String editItemSettingsUrl(SimplePageBean bean) {
	String tool = bean.getCurrentTool("sakai.mneme");
	if (tool == null)
	    return null;
    
	return ServerConfigurationService.getToolUrl()+ "/" + tool + "/assessment_settings/" + id + "/1";
    }

    public String importObject(Document document, boolean isBank, String siteId, boolean hide) {
	// ignore isbank because everything has to go into pool.
	// they have no way to import an assessment

	if (importQtiService == null)
	    return null;

	try {
	    importQtiService.importPool(document, siteId);
	} catch (Exception e) {
	    simplePageBean.setErrMessage("" + e);
	}
	return null; // for now. should be sakaiid

    }

    public boolean objectExists() {
	if (assessment == null)
	    assessment = getAssessment(id);
	return assessment != null;
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
	return null;
    }

    // set the item to be accessible only to the specific groups.
    // null to make it accessible to the whole site
    public void setGroups(Collection<String> groups) {
    }

    // currently Mneme does not participate in the fixup. However I'm going to include
    // the ID anyway, just in case it happens in the future. Doesn't currently support
    // direct, so it's just a guess what prefix it might use
    public String getObjectId(){
	String title = getTitle();
	if (title == null)
	    return null;
	return "mneme/" + id + "/" + title;
    }

    public String findObject(String objectid, Map<String,String>objectMap, String siteid) {
	if (!objectid.startsWith("mneme/")) {
	    if (nextEntity != null)
		return nextEntity.findObject(objectid, objectMap, siteid);
	    return null;
	}

	if (assessmentService == null)
	    return null;

	// isolate mneme/NNN from title
	int i = objectid.indexOf("/", "mneme/".length());
	if (i <= 0)
	    return null;
	String realobjectid = objectid.substring(0, i);

	// now see if it's in the map. not currently possible, but who knows
	String newAssessment = objectMap.get(realobjectid);
	if (newAssessment != null)
	    return "/" + newAssessment;  // sakaiid is /mneme/ID

	// Can't find the assessment in the map
	// i is start of title
	String title = objectid.substring(i+1);

	// this would be more efficient if I did my own query, but prefer to use API where possible
	List<Assessment> plist = assessmentService.getContextAssessments(ToolManager.getCurrentPlacement().getContext(), AssessmentService.AssessmentsSort.title_a, true);

	// security. assume this is only used in places where it's OK, so skip security checks
	for (Assessment assessment: plist) {
	    if (assessment.getTitle().equals(title))
		return "/mneme/" + assessment.getId();
	}

	return null;

    }

    public String getSiteId() {
	if (assessment == null)
	    assessment = getAssessment(id);
	if (assessment == null)
	    return null;
	return assessment.getContext();
    }

}
