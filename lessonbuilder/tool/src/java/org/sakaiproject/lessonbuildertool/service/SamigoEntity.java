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
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;

import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.CacheRefresher;
import org.sakaiproject.memory.api.MemoryService;


/**
 * Interface to Message Forums, the forum that comes with Sakai
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

public class SamigoEntity implements LessonEntity {

    private static Log log = LogFactory.getLog(SamigoEntity.class);

    private static Cache assessmentCache = null;
    protected static final int DEFAULT_EXPIRATION = 10 * 60;

    static PublishedAssessmentService pService = new PublishedAssessmentService();

    private SimplePageBean simplePageBean;

    public void setSimplePageBean(SimplePageBean simplePageBean) {
	this.simplePageBean = simplePageBean;
    }

    static PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries;

    public void setPublishedAssessmentFacadeQueries(
        PublishedAssessmentFacadeQueriesAPI p) {
    	this.publishedAssessmentFacadeQueries = p;
    }

    private LessonEntity nextEntity = null;
    public void setNextEntity(LessonEntity e) {
	nextEntity = e;
    }
    
    static MemoryService memoryService = null;
    public void setMemoryService(MemoryService m) {
	memoryService = m;
    }

    public void init () {
	assessmentCache = memoryService
	    .newCache("org.sakaiproject.lessonbuildertool.service.SamigoEntity.cache");

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

    protected SamigoEntity() {
    }

    protected SamigoEntity(int type, Long id, int level) {
	this.type = type;
	this.id = id;
	this.level = level;
    }

    // the underlying object, something Sakaiish
    protected Long id;
    protected int type;
    protected int level;
    // not required fields. If we need to look up
    // the actual objects, lets us cache them
    protected PublishedAssessmentData assessment;

    public PublishedAssessmentData getPublishedAssessment(Long publishedId) {
	
	PublishedAssessmentData ret = (PublishedAssessmentData)assessmentCache.get(publishedId);

	if (ret != null) {
	    return ret;
	}

	try {
	    ret = publishedAssessmentFacadeQueries.loadPublishedAssessment(publishedId);
	} catch (Exception e) {
	    return null;
	}

	if (ret != null) 
	    assessmentCache.put(publishedId, ret, DEFAULT_EXPIRATION);

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
	return "/" + SAM_PUB + "/" + id;
    }

    // find topics in site, but organized by forum
    public List<LessonEntity> getEntitiesInSite() {

	Session ses = SessionManager.getCurrentSession();

	ArrayList<PublishedAssessmentFacade> plist = pService.getBasicInfoOfAllPublishedAssessments(ses.getUserId(), "title", true, ToolManager.getCurrentPlacement().getContext());

	List<LessonEntity> ret = new ArrayList<LessonEntity>();
	// security. assume this is only used in places where it's OK, so skip security checks
	for (PublishedAssessmentFacade assessment: plist) {
	    SamigoEntity entity = new SamigoEntity(TYPE_SAMIGO, assessment.getPublishedAssessmentId(), 1);
	    entity.assessment = (PublishedAssessmentData)assessment.getData();
	    ret.add(entity);
	}

	if (nextEntity != null) 
	    ret.addAll(nextEntity.getEntitiesInSite());

	return ret;
    }

    public LessonEntity getEntity(String ref) {
	int i = ref.indexOf("/",1);
	if (i < 0) {
	    // old format, just the number
	    try {
		return new SamigoEntity(TYPE_SAMIGO, Long.valueOf(ref), 1);
	    } catch (Exception ignore) {
		return null;
	    }
	}
	String typeString = ref.substring(1, i);
	String idString = ref.substring(i+1);
	Long id = 0L;
	try {
	    id = Long.parseLong(idString);
	} catch (Exception ignore) {
	    return null;
	}

	if (typeString.equals(SAM_PUB)) {
	    return new SamigoEntity(TYPE_SAMIGO, id, 1);
	} else if (nextEntity != null) {
	    return nextEntity.getEntity(ref);
	} else
	    return null;
    }
	
    // properties of entities
    public String getTitle() {
	if (assessment == null)
	    assessment = getPublishedAssessment(id);
	if (assessment == null)
	    return null;
	return assessment.getTitle();
    }

    public String getAssessmentAlias(Long publishedId) {
	try {
	    return getPublishedAssessment(publishedId).getAssessmentMetaDataByLabel("ALIAS");
	} catch (Exception ex) {
	    return null;
	}
    }

    public String getUrl() {
        return "/samigo-app/servlet/Login?id=" + getAssessmentAlias(id);
    }

    // I don't think they have this
    public Date getDueDate() {
	if (assessment == null)
	    assessment = getPublishedAssessment(id);
	if (assessment == null)
	    return null;
	return assessment.getDueDate();
    }

    // the following methods all take references. So they're in effect static.
    // They ignore the entity from which they're called.
    // The reason for not making them a normal method is that many of the
    // implementations seem to let you set access control and find submissions
    // from a reference, without needing the actual object. So doing it this
    // way could save some database activity

    // access control
    public boolean addEntityControl(String siteId, String groupId) throws IOException {
	PublishedAssessmentService assessmentService = new PublishedAssessmentService();
	// I don't want to do a full load of the facade most of the time. So we use
	// PublishedAssessmentData normally. Unfortunately here we need it
	PublishedAssessmentFacade assessment = null;
	AssessmentAccessControlIfc control = null;

	try {
	    assessment = assessmentService.getPublishedAssessment(Long.toString(id));
	    control = assessment.getAssessmentAccessControl();
	} catch (Exception e) {
	    log.warn("can't find published " + id, e);
	    return false;
	}

	AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();

	if (authz == null) {
	    log.warn("Null Authorization");
	    return false;
	}
	
	if (!control.getReleaseTo().equals(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS)) {
	    control.setReleaseTo(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS);
	    assessmentService.saveAssessment(assessment);
	    String qualifierIdString = assessment.getPublishedAssessmentId().toString();

	    // the original one lists the site. once we set release to groups, it will try to look
	    // up the site id as a group id. very bad, so remove all existing ones.
	    authz.removeAuthorizationByQualifierAndFunction(qualifierIdString, "TAKE_PUBLISHED_ASSESSMENT");

	    // and add our group
	    authz.createAuthorization(groupId, "TAKE_PUBLISHED_ASSESSMENT", Long.toString(id));
	} else {
	    // already release to groups. see if we need to add our group
	    List<AuthorizationData> authorizations = authz.getAuthorizationByFunctionAndQualifier("TAKE_PUBLISHED_ASSESSMENT", Long.toString(id));
	    boolean found = false;

	    for (AuthorizationData ad : authorizations) {
		if (ad.getAgentIdString().equals(groupId)) {
		    found = true;
		    break;
		}
	    }

	    // if not, add it; can't add it otherwise or we get duplicates
	    if (!found) {
		authz.createAuthorization(groupId, "TAKE_PUBLISHED_ASSESSMENT", Long.toString(id));
	    }
	}

	return true;
    }

    public boolean removeEntityControl(String siteId, String groupId) throws IOException {
	PublishedAssessmentService assessmentService = new PublishedAssessmentService();
	// I don't want to do a full load of the facade most of the time. So we use
	// PublishedAssessmentData normally. Unfortunately here we need it
	PublishedAssessmentFacade assessment = null;
	AssessmentAccessControlIfc control = null;

	try {
	    assessment = assessmentService.getPublishedAssessment(Long.toString(id));
	    control = assessment.getAssessmentAccessControl();
	} catch (Exception e) {
	    log.warn("can't find published " + id, e);
	    return false;
	}

	AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();

	if (!control.getReleaseTo().equals(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS)) {
	    // not release to groups, nothing to do
	    return true;
	} else {
	    // what do we do if it was originally released to groups, and then we added ours? I
	    // guess jsut remove ours?
	    List<AuthorizationData> authorizations = authz.getAuthorizationByFunctionAndQualifier("TAKE_PUBLISHED_ASSESSMENT", Long.toString(id));
	    boolean foundother = false;
	    for (AuthorizationData ad : authorizations) {
		if (ad.getAgentIdString().equals(groupId)) {} else {
		    foundother = true;
		}
	    }
	    
	    if (foundother) {
		// just remove our group
		authz.removeAuthorizationByAgentQualifierAndFunction(groupId, Long.toString(id), "TAKE_PUBLISHED_ASSESSMENT");
	    } else {
		Site site = null;
		
		try {
		    site = SiteService.getSite(siteId);
		} catch (Exception e) {
		    return false;
		}
		
		// otherwise remove all groups
		authz.removeAuthorizationByQualifierAndFunction(Long.toString(id), "TAKE_PUBLISHED_ASSESSMENT");
		
		// put back the site
		authz.createAuthorization(siteId, "TAKE_PUBLISHED_ASSESSMENT", Long.toString(id));
		
		// and put back the access control
		control.setReleaseTo(site.getTitle()); // what if it's too long?
		
		// and save the updated info
		assessmentService.saveAssessment(assessment);
	    }
	}
	return true;
    }

    // submission
    // do we need the data from submission?
    public boolean needSubmission(){
	return true;
    }

    public LessonSubmission getSubmission(String user) {
	if (assessment == null)
	    assessment = getPublishedAssessment(id);
	if (assessment == null) {
	    log.warn("can't find published " + id);
	    return null;
	}

	GradingService gradingService = new GradingService();

	Session ses = SessionManager.getCurrentSession();

	AssessmentGradingIfc grading = null;
	if (assessment.getEvaluationModel().getScoringType() == EvaluationModelIfc.LAST_SCORE) {
	    grading = gradingService.getLastSubmittedAssessmentGradingByAgentId(Long.toString(id), ses.getUserId(), null);
	} else {
	    grading = gradingService.getHighestSubmittedAssessmentGrading(Long.toString(id), ses.getUserId());
	}

	if (grading == null)
	    return null;

	return new LessonSubmission(grading.getFinalScore());
    }

// we can do this for real, but the API will cause us to get all the submissions in full, not just a count.
// I think it's cheaper to get the best assessment, since we don't actually care whether it's 1 or >= 1.
    public int getSubmissionCount(String user) {
	if (getSubmission(user) == null)
	    return 0;
	else
	    return 1;
    }

}
