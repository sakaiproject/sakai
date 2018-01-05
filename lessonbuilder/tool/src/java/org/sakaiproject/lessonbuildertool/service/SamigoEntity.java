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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedMetaData;
import org.sakaiproject.tool.assessment.data.dao.authz.AuthorizationData;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AuthzQueriesFacadeAPI;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FormattedText;

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
@Slf4j
public class SamigoEntity implements LessonEntity, QuizEntity {

    private static Cache assessmentCache = null;

    protected static final int DEFAULT_EXPIRATION = 10 * 60;
    private static boolean samigo_linked = false;

    PublishedAssessmentService pService = new PublishedAssessmentService();
    AssessmentService assessmentService = new AssessmentService();

    private SimplePageToolDao simplePageToolDao;

    public void setSimplePageToolDao(Object dao) {
	simplePageToolDao = (SimplePageToolDao) dao;
    }

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
	assessmentCache = memoryService
	    .getCache("org.sakaiproject.lessonbuildertool.service.SamigoEntity.cache");



	samigo_linked = ServerConfigurationService.getBoolean("lessonbuilder.samigo.editlink", true);
	log.info("SamigoEntity edit link " + samigo_linked);

	log.info("init()");

    }

    public void destroy()
    {
	//	assessmentCache.destroy();
	//	assessmentCache = null;

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

    public String getToolId() {
	return "sakai.samigo";
    }

    // the underlying object, something Sakaiish
    protected Long id;
    protected int type;
    protected int level;
    // not required fields. If we need to look up
    // the actual objects, lets us cache them
    protected PublishedAssessmentData assessment;

    public PublishedAssessmentData getPublishedAssessment(Long publishedId) {
	return getPublishedAssessment(publishedId, false);
    }

    public PublishedAssessmentData getPublishedAssessment(Long publishedId, boolean nocache) {
	
	PublishedAssessmentData ret = (PublishedAssessmentData)assessmentCache.get(publishedId.toString());

	if (!nocache && ret != null) {
	    return ret;
	}

	try {
	    ret = publishedAssessmentFacadeQueries.loadPublishedAssessment(publishedId);
	    // this will ignore retracted. I think that's right. Students
	    // we show dead and inactive, just not deleted
	    if (ret.getStatus().equals(PublishedAssessmentFacade.DEAD_STATUS)) {
		return null;
	    }
	} catch (Exception e) {
	    return null;
	}

	if (ret != null) {
	    ret.setComments(null);
	    assessmentCache.put(publishedId.toString(), ret);
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
	return "/" + SAM_PUB + "/" + id;
    }


    public List<LessonEntity> getEntitiesInSite() {
	return getEntitiesInSite(null);
    }

    // find topics in site, but organized by forum
    public List<LessonEntity> getEntitiesInSite(SimplePageBean bean) {

	Session ses = SessionManager.getCurrentSession();

	List<PublishedAssessmentFacade> plist = pService.getBasicInfoOfAllPublishedAssessments2("title", true, ToolManager.getCurrentPlacement().getContext());

	List<LessonEntity> ret = new ArrayList<LessonEntity>();
	// security. assume this is only used in places where it's OK, so skip security checks
	for (PublishedAssessmentFacade assessment: plist) {
	    SamigoEntity entity = null;
	    if (assessment.getStatus().equals(AssessmentIfc.ACTIVE_STATUS)) {
		entity = new SamigoEntity(TYPE_SAMIGO, assessment.getPublishedAssessmentId(), 1);
		entity.assessment = (PublishedAssessmentData)assessment.getData();
		ret.add(entity);
	    }

	    if (false) {  // testing
		log.info(entity.getGroups(true).toString());
		List<String> oldGroups = entity.getGroups(true);
		//5c51c1fb-bf48-475f-99a6-a303f5ad9520
		//d579a252-204e-46cd-9720-7eca7bd47630
		entity.setGroups(null);
		log.info("null " + entity.getGroups(true));
		entity.setGroups(null);
		log.info("null " + entity.getGroups(true));
		entity.setGroups(Arrays.asList("5c51c1fb-bf48-475f-99a6-a303f5ad9520"));
		log.info("5c51 " + entity.getGroups(true));
		entity.setGroups(Arrays.asList("5c51c1fb-bf48-475f-99a6-a303f5ad9520","d579a252-204e-46cd-9720-7eca7bd47630"));
		log.info("5c51,d579 " + entity.getGroups(true));
		entity.setGroups(null);
		log.info("null " + entity.getGroups(true));
		entity.setGroups(oldGroups);
		log.info(oldGroups + " " + entity.getGroups(true));
	    }

	}

	if (nextEntity != null) 
	    ret.addAll(nextEntity.getEntitiesInSite(bean));

	return ret;
    }

    public LessonEntity getEntity(String ref) {
	return getEntity(ref, null);
    }

    public LessonEntity getEntity(String ref, SimplePageBean o) {
	// if the site was copied, all sakaiids for tests are set to something like /sam_core/NNN
	// the problem is that published assessments aren't copied. So all we can do is poitn to
	// the core assessment. Of course you can't really take that, so we try to find a published
	// assessment based on that core. If we find one, we fix up the sakaiids, and we're ok.
	if (o != null && ref.startsWith("/sam_core/")) {
	    Object fields[] = new Object[1];
	    fields[0] = new Long(ref.substring("/sam_core/".length()));

	    // assessmentid is indexed, so this is a pretty lightweight query
	    List <String> publishedIds = SqlService.dbRead("select ID from SAM_PUBLISHEDASSESSMENT_T where ASSESSMENTID=?", fields, null);
	    if (publishedIds != null && publishedIds.size() > 0) {
		// found it, update all sakaiids to use the published assessment
		String newid = publishedIds.get(0);
		List <SimplePageItem> items = simplePageToolDao.findItemsBySakaiId(ref);
		ref = "/sam_pub/" + newid;
		for (SimplePageItem item: items) {
		    item.setSakaiId(ref);
		    simplePageToolDao.quickUpdate(item);
		    o.checkControlGroup(item, item.isPrerequisite());
		}
	    } else
		return null;

	    // ref has now been updated, so the rest of the code should work as is
	}

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
	return FormattedText.convertFormattedTextToPlaintext(assessment.getTitle());
    }

    public String getAssessmentAlias(Long publishedId) {
	try {
	    PublishedAssessmentData a = getPublishedAssessment(publishedId);
	    if (a == null)
		return null;
	    else 
		return a.getAssessmentMetaDataByLabel("ALIAS");
	} catch (Exception ex) {
	    log.info("exception " + ex);
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

	// I don't want to do a full load of the facade most of the time. So we use
	// PublishedAssessmentData normally. Unfortunately here we need it
	PublishedAssessmentFacade assessment = null;
	AssessmentAccessControlIfc control = null;

	try {
	    assessment = pService.getPublishedAssessment(Long.toString(id));
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
	    pService.saveAssessment(assessment);
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
	// I don't want to do a full load of the facade most of the time. So we use
	// PublishedAssessmentData normally. Unfortunately here we need it
	PublishedAssessmentFacade assessment = null;
	AssessmentAccessControlIfc control = null;

	try {
	    assessment = pService.getPublishedAssessment(Long.toString(id));
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
		// otherwise remove all groups
		authz.removeAuthorizationByQualifierAndFunction(Long.toString(id), "TAKE_PUBLISHED_ASSESSMENT");
	    }

	    Site site = null;
		
	    try {
		site = SiteService.getSite(siteId);
	    } catch (Exception e) {
		return false;
	    }
		
	    // put back the site
	    authz.createAuthorization(siteId, "TAKE_PUBLISHED_ASSESSMENT", Long.toString(id));
	    
	    // and put back the access control
	    control.setReleaseTo(site.getTitle()); // what if it's too long?
		
	    // and save the updated info
	    pService.saveAssessment(assessment);

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

	AssessmentGradingData grading = null;
	try {
	if (assessment.getEvaluationModel().getScoringType() == EvaluationModelIfc.LAST_SCORE) {
	    grading = gradingService.getLastSubmittedAssessmentGradingByAgentId(Long.toString(id), ses.getUserId(), null);
	} else {
	    // the declared return type changed from AssessmentGradingIfc to Data. But the actual
	    // underlying object is Data. In the old code Data implemented Ifc, but that no longer
	    // seems to be true. I believe this cast will work either way.
	    grading = (AssessmentGradingData)gradingService.getHighestSubmittedAssessmentGrading(Long.toString(id), ses.getUserId());
	}
	} catch (Exception e) {
	    log.info("unable to find submission for samigo item " + id);
	    grading = null;
	}
	if (grading == null)
	    return null;

	return new LessonSubmission(toDouble(grading.getFinalScore()));

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
	String tool = bean.getCurrentTool("sakai.samigo");
	if (tool != null) {
	    tool = ServerConfigurationService.getToolUrl() + "/" + tool + "/jsf/index/mainIndex";
	    list.add(new UrlItem(tool, messageLocator.getMessage("simplepage.create_samigo")));
	}
	if (nextEntity != null)
	    list.addAll(nextEntity.createNewUrls(bean));
	return list;
    }

    // URL to edit an existing entity.                                                                                                       
    // Can be null if we can't get one or it isn't needed                                                                                    
    public String editItemUrl(SimplePageBean bean) {
	String tool = bean.getCurrentTool("sakai.samigo");
	if (tool == null)
	    return null;
    
	if (false) {
	    // code to verify that exportObject actually works
	    if (assessment == null)
		assessment = getPublishedAssessment(id);
	    String aid = assessment.getAssessmentId().toString();
	    
	    Document doc = exportObject(aid);
	    log.info("foo " + doc.getElementsByTagName("questestinterop"));
	}

	if (samigo_linked)
	    return ServerConfigurationService.getToolUrl() + "/" + tool + "/jsf/author/editLink?publishedAssessmentId=" + id;
	else
	    return ServerConfigurationService.getToolUrl() + "/" + tool + "/jsf/index/mainIndex";
    }


    // for most entities editItem is enough, however tests allow separate editing of                                                         
    // contents and settings. This will be null except in that situation                                                                     
    public String editItemSettingsUrl(SimplePageBean bean) {
	String tool = bean.getCurrentTool("sakai.samigo");
	if (tool == null)
	    return null;
    
	if (samigo_linked)
	    return ServerConfigurationService.getToolUrl() + "/" + tool + "/jsf/author/editLink?publishedAssessmentId=" + id + "&settings=true";
	else
	    return ServerConfigurationService.getToolUrl() + "/" + tool + "/jsf/index/mainIndex";

    }

    // export an assessment as an XML document. This is in Samigo's version of QTI
    public Document exportObject(String assessmentId) {
	try {
	    QTIService qtiService = new QTIService();
	    return qtiService.getExportedAssessment(assessmentId, QTIVersion.VERSION_1_2);
	} catch (Exception e) {
	    log.info("exception in exportobject " + e);
	    return null;
	}
    }
	

    public String importObject(Document document, boolean isBank, String siteId, boolean hide) {

	QTIService qtiService = new QTIService();
	AssessmentFacade assessment = null;
	PublishedAssessmentFacade publishedAssessment = null;
	if (isBank) {
	    qtiService.createImportedQuestionPool(document, QTIVersion.VERSION_1_2);
	    return null;
	} else {
	    assessment = qtiService.createImportedAssessment(document, QTIVersion.VERSION_1_2);
	    try {
		// can't find a way to do this in the metadata, and in fact the real import
		// code does hack on this stuff
		AssessmentAccessControl control = new AssessmentAccessControl();
		control.setAssessmentBase(assessment.getData());
		assessment.setSecuredIPAddressSet(new HashSet());
		assessment.setAssessmentAttachmentSet(new HashSet());
		assessmentService.saveAssessment(assessment);

		// what we just did gives us empty access control. we need the real thing
		// this code is based on setgroups.  It's kind of reverse engineered
		AssessmentAccessControlIfc controlIfc = null;
		Long id = assessment.getAssessmentId();
		try {
		    assessment = assessmentService.getAssessment(Long.toString(id));
		    controlIfc = assessment.getAssessmentAccessControl();
		} catch (Exception e) {
		    log.warn("can't find assessment we just loaded " + id, e);
		    return null;
		}
		// set proper access control
		AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();
		if (authz == null) {
		    log.warn("Null Authorization");
		    return null;
		}
		authz.removeAuthorizationByQualifierAndFunction(Long.toString(id), "TAKE_PUBLISHED_ASSESSMENT");
		Site site;
		try {
		    site = SiteService.getSite(siteId);
		} catch (Exception e) {
		    return null;
		}
		// put back the site
		//		authz.createAuthorization(siteId, "TAKE_PUBLISHED_ASSESSMENT", Long.toString(id));
		// and put back the access control
		controlIfc.setReleaseTo(site.getTitle()); // what if it's too long?
		// and save the updated info
		assessmentService.saveAssessment(assessment);

		// get it again
		try {
		    assessment = assessmentService.getAssessment(Long.toString(id));
		    controlIfc = assessment.getAssessmentAccessControl();
		} catch (Exception e) {
		    log.warn("can't find assessment we just loaded " + id, e);
		    return null;
		}

		if (!hide) {
		    publishedAssessment = pService.publishAssessment(assessment);

		    String alias = SessionManager.getCurrentSessionUserId() + (new Date()).getTime();
		    PublishedMetaData meta = new PublishedMetaData(publishedAssessment.getData(), "ALIAS", alias);
		    pService.saveOrUpdateMetaData(meta);
		}

	    } catch (Exception e) {
		log.warn("can't publish assessment after import " + e);
	    }
	    if (publishedAssessment != null) {
		return 	"/" + SAM_PUB + "/" + publishedAssessment.getPublishedAssessmentId();
	    } else
		return null;
	}	
    }

    public boolean objectExists() {
	if (assessment == null)
	    assessment = getPublishedAssessment(id);
	return assessment != null;
    }

    public boolean notPublished(String ref) {
	if (ref.startsWith("/sam_core/"))
	    return true;
	else
	    return false;
    }

    public boolean notPublished() {
	return false;
    }

    // return the list of groups if the item is only accessible to specific groups
    // null if it's accessible to the whole site.  Update the data in the cache
    // use the comments field, since there's no place to put a list and we don't use
    // that field
    public List<String> getGroups(boolean nocache) {
	if (nocache)
	    assessment = getPublishedAssessment(id, true);
	else if (assessment == null)
	    assessment = getPublishedAssessment(id);
	if (assessment == null)
	    return null;

	// our model doens't include anonymous. Treat as no groups
	String releaseTo = assessment.getAssessmentAccessControl().getReleaseTo();
	if (releaseTo != null && releaseTo.indexOf("Anonymous Users")> -1)
	    return null;

	// cached value?
	String groupString = assessment.getComments();
	if (groupString != null) {
	    if (groupString.equals(""))  // release to site
		return null;
	    else
		return Arrays.asList(groupString.split(","));
	}

	// no, get the value
	String siteId = ToolManager.getCurrentPlacement().getContext();
	List<String> groups = publishedAssessmentFacadeQueries.getReleaseToGroupIdsForPublishedAssessment(assessment.getPublishedAssessmentId()+"");
	if (groups == null)
	    return null;

	// if it's released to site we get a list with the site id in it
	if (groups.size() == 1 && groups.get(0).equals(siteId))
	    groupString = "";   // released to site
	else 
	    for (String group:groups) {
		if (groupString == null)
		    groupString = group;
		else
		    groupString = groupString + "," + group;
	    }
	// cache it
	assessment.setComments(groupString);

	// return it
	if (groupString == null || groupString.equals(""))
	    return null;
	else
	    return groups;
    }

    // set the item to be accessible only to the specific groups.
    // null to make it accessible to the whole site
    public void setGroups(Collection<String> groups) {

	if (groups != null && groups.size() == 0)
	    groups = null;

	// kill cached value. not perfect, as other systems
	// will still have the old value
	if (assessment != null)
	    assessment.setComments(null);
	PublishedAssessmentData cached = (PublishedAssessmentData)assessmentCache.get(id.toString());
	if (cached != null)
	    cached.setComments(null);

	String siteId = ToolManager.getCurrentPlacement().getContext();

	// I don't want to do a full load of the facade most of the time. So we use
	// PublishedAssessmentData normally. Unfortunately here we need it
	PublishedAssessmentFacade assessment = null;
	AssessmentAccessControlIfc control = null;

	try {
	    assessment = pService.getPublishedAssessment(Long.toString(id));
	    control = assessment.getAssessmentAccessControl();
	} catch (Exception e) {
	    log.warn("can't find published " + id, e);
	    return;
	}

	// no groups for anonymous assessments
	String releaseTo = control.getReleaseTo();
	if (releaseTo != null && releaseTo.indexOf("Anonymous Users")> -1)
	    return;

	AuthzQueriesFacadeAPI authz = PersistenceService.getInstance().getAuthzQueriesFacade();

	if (authz == null) {
	    log.warn("Null Authorization");
	    return;
	}
	
	//
	// got the info, now have the 4 possibilities

	// 1. asked for release to site, already is.

	if (groups == null && !control.getReleaseTo().equals(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS))
	    return;

	// 2. asked for release to site but it's now release to groups

	if (groups == null) {
	    // remove all groups
	    authz.removeAuthorizationByQualifierAndFunction(Long.toString(id), "TAKE_PUBLISHED_ASSESSMENT");
	    
	    Site site;
	    try {
		site = SiteService.getSite(siteId);
	    } catch (Exception e) {
		return;
	    }

	    // put back the site
	    authz.createAuthorization(siteId, "TAKE_PUBLISHED_ASSESSMENT", Long.toString(id));
		
	    // and put back the access control
	    control.setReleaseTo(site.getTitle()); // what if it's too long?
		
	    // and save the updated info
	    pService.saveAssessment(assessment);

	    return;
	}

	// 3 and 4 asked for release to groups 
	
	// 3. it's currently release to site, update to groups

	if (!control.getReleaseTo().equals(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS)) {

	    control.setReleaseTo(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS);
	    pService.saveAssessment(assessment);
	}

	// 3 and 4. recreate the group list

	String qualifierIdString = assessment.getPublishedAssessmentId().toString();

	// remove existing
	authz.removeAuthorizationByQualifierAndFunction(qualifierIdString, "TAKE_PUBLISHED_ASSESSMENT");

	// and add new list
	for (String groupId: groups) {
	    authz.createAuthorization(groupId, "TAKE_PUBLISHED_ASSESSMENT", Long.toString(id));
	}

    }

    // this is what goes into the XML file. Samigo doesn't support RefMigrator, so the only way
    // we can connect tests in the new site with the old one is by title. Thus what we save is
    // use sam_core/TITLE
    // this is the title of the core assessment, since that's what gets copied.
    public String getObjectId(){
	if (assessment == null)
	    assessment = getPublishedAssessment(id);
	if (assessment == null)
	    return null;
	Long coreId = assessment.getAssessmentId();
	AssessmentFacade facade = assessmentService.getAssessment(coreId.toString());
	if (facade == null)
	    return null;
	String title = facade.getTitle();
	return "sam_core/" + title;
    }

    // normally this will look up the object ID and find the corresponding sakaiid in the
    // new site. Unfortunately, the assessment hasn't been published, so the best we
    // can do is return the corresponding core assessment. When we try to refer to it,
    // getEntity will see if it's been published yet.
    // returns sam_core/NNNN
    public String findObject(String objectid, Map<String,String>objectMap, String siteid) {

        if (!objectid.startsWith("sam_core/")) {
            if (nextEntity != null) {
                return nextEntity.findObject(objectid, objectMap, siteid);
            }
	    return null;
	}

	String title = objectid.substring("sam_core/".length());

	// this is an expensive query, but this is called pretty rarely, so it's probably better to do this than
	// got to the database ourselves. We'd be a lot better with a getBasicInfo version
	List<AssessmentData> list = assessmentService.getAllActiveAssessmentsbyAgent(siteid);
	for (AssessmentData data: list) {
	    if (data.getTitle().equals(title)) {
		return "/sam_core/" + data.getAssessmentBaseId();
	    }
	}
	return null;
    }

    public String getSiteId() {
	return publishedAssessmentFacadeQueries.getPublishedAssessmentSiteId(Long.toString(id));
    }

}
