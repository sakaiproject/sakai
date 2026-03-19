/**********************************************************************************
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.jdom2.Element;
import org.jdom2.Namespace;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.scorm.api.ScormConstants;
import org.sakaiproject.scorm.model.api.ActivitySummary;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * Interface to Scorm
 *
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */
@Slf4j
public class ScormEntity implements LessonEntity {
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


    static ScormContentService scormContentService = null;
    static ScormResultService scormResultService = null;

    public void init () {
	scormContentService = (ScormContentService)ComponentManager.get("org.sakaiproject.scorm.service.api.ScormContentService");
	scormResultService = (ScormResultService)ComponentManager.get("org.sakaiproject.scorm.service.api.ScormResultService");
	if (scormResultService == null)
	    scormContentService = null;
	log.info("init() {}", scormContentService);
    }

    public boolean isServiceDeployed() {
	return scormContentService != null;
    }


    protected ScormEntity() {
    }

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
	return "sakai.scorm.tool";
    }

    protected Long id;
    protected int type;
    protected int level;
    protected ContentPackage contentPackage;

    public ContentPackage getContentPackage(Long id) {
	try {
	    return scormContentService.getContentPackage(id);
	} catch (Exception e) {
	    log.warn("Unable to load SCORM content package {}: {}", id, e.getMessage());
	    return null;
	}
    }

    public int getType() {
	return type;
    }

    public int getLevel() {
	return level;
    }

    public int getTypeOfGrade() {
	return 0;
    }

    public boolean showAdditionalLink() {
	return false;
    }

    public String getDescription() {
	return "";
    }

    public boolean isUsable() {
	return true;
    }

    public String getReference() {
	return "/" + SCORM + "/" + id;
    }

    public List<LessonEntity> getEntitiesInSite() {
	return getEntitiesInSite(null);
    }

    public List<LessonEntity> getEntitiesInSite(final SimplePageBean bean) {

	if (scormContentService == null) {
	    if (nextEntity != null)
		return nextEntity.getEntitiesInSite();
	    else
		return new ArrayList<>();
	}

	List<LessonEntity> lessons = new ArrayList<>();

	String siteId = ToolManager.getCurrentPlacement().getContext();

	List<ContentPackage> packages;
	try {
	    packages = scormContentService.getContentPackages(siteId);
	} catch (Exception e) {
	    log.warn("Unable to retrieve SCORM content packages for site {}: {}", siteId, e.getMessage());
	    return new ArrayList<>();
	}
	log.debug("packages {}", packages.size());

	for (ContentPackage contentPackage: packages) {
	    if (contentPackage.isDeleted())
		continue;
	    ScormEntity entity = new ScormEntity(TYPE_SCORM, contentPackage.getContentPackageId(), 1);
	    entity.contentPackage = contentPackage;
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
	    ScormEntity entity = new ScormEntity(TYPE_SCORM, idString, 1);
	    entity.setSimplePageBean(bean);
	    return entity;
	} else if (nextEntity != null) {
	    return nextEntity.getEntity(ref, bean);
	} else
	    return null;
    }
	

    public String getTitle() {
	if (scormContentService == null)
	    return null;
	if (contentPackage == null)
	    contentPackage = getContentPackage(id);
	if (contentPackage == null)
	    return null;
	return contentPackage.getTitle();
    }

    public String getUrl() {
	if (simplePageBean == null || scormContentService == null)
	    return null;
	if (contentPackage == null)
	    contentPackage = getContentPackage(id);
	if (contentPackage == null)
	    return null;
	String tool = simplePageBean.getCurrentTool("sakai.scorm.tool");
	if (tool == null)
	    return null;
	return ServerConfigurationService.getToolUrl() + "/" + tool + "/scormPlayerPage?contentPackageId=" + id;
    }

    public Date getDueDate() {
	return null;
    }

    public LessonSubmission getSubmission(String userId) {
	if (scormContentService == null)
	    return null;
	if (contentPackage == null)
	    contentPackage = getContentPackage(id);
	if (contentPackage == null)
	    return null;
	try {
	    Attempt latest = scormResultService.getNewstAttempt(contentPackage.getContentPackageId(), userId);
	    if (latest == null)
		return null;
	    // Check actual cmi.completion_status / cmi.success_status across all SCOs in this attempt
	    List<ActivitySummary> summaries = scormResultService.getActivitySummaries(
		contentPackage.getContentPackageId(), userId, latest.getAttemptNumber());
	    if (summaries.isEmpty()) return null;
	    for (ActivitySummary summary : summaries) {
		if (!"completed".equals(summary.getCompletionStatus()) && !"passed".equals(summary.getSuccessStatus()))
		    return null;
	    }
	    return new LessonSubmission(null);
	} catch (Exception e) {
	    log.warn("getSubmission: error querying SCORM result service for user {} package {}: {}", userId, id, e.getMessage());
	    return null;
	}
    }

    public int getSubmissionCount(String user) {
	if (scormContentService == null)
	    return 0;
	if (contentPackage == null)
	    contentPackage = getContentPackage(id);
	if (contentPackage == null)
	    return 0;
	try {
	    return scormResultService.countAttempts(contentPackage.getContentPackageId(), user);
	} catch (Exception e) {
	    log.warn("getSubmissionCount: error querying SCORM result service for user {} package {}: {}", user, id, e.getMessage());
	    return 0;
	}
    }

    public List<UrlItem> createNewUrls(SimplePageBean bean) {
	List<UrlItem> list = new ArrayList<>();
	if (scormContentService == null)
	    return list;
	String tool = bean.getCurrentTool("sakai.scorm.tool");
	if (tool != null) {
	    String url = ServerConfigurationService.getToolUrl() + "/" + tool;
	    list.add(new UrlItem(url, messageLocator.getMessage("simplepage.create_scorm")));
	}
	return list;
    }


    public String editItemUrl(SimplePageBean bean) {
	return null;
    }

    public String editItemSettingsUrl(SimplePageBean bean) {
	return null;
    }

   public boolean objectExists() {
       if (scormContentService == null)
	   return false;
       if (contentPackage == null)
	   contentPackage = getContentPackage(id);
       return contentPackage != null;
   }

    public boolean notPublished(String ref) {
	return notPublished();
    }

    public boolean notPublished() {
	if (scormContentService == null) return true;
	if (contentPackage == null) contentPackage = getContentPackage(id);
	if (contentPackage == null) return true;
	try {
	    int status = scormContentService.getContentPackageStatus(contentPackage);
	    // OPEN and OVERDUE packages are accessible to learners; CLOSED and NOTYETOPEN are not
	    return status != ScormConstants.CONTENT_PACKAGE_STATUS_OPEN
		&& status != ScormConstants.CONTENT_PACKAGE_STATUS_OVERDUE;
	} catch (Exception e) {
	    log.warn("notPublished: error retrieving status for SCORM package {}: {}", id, e.getMessage());
	    return true;
	}
    }

    public Collection<String> getGroups(boolean nocache) {
	return null;
    }
  
    // SCORM content packages are not access-controlled at the Sakai groups level;
    // prerequisite enforcement for SCORM items is handled at the Lessons UI layer only.
    public void setGroups(Collection<String> tgroups) {

    }

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

	if (scormContentService == null)
	    return null;

	// objectid format: "scorm/{id}/{title}" — check transversal map first
	int prefixLen = "scorm/".length();
	int titleSlash = objectid.indexOf("/", prefixLen);
	if (titleSlash <= 0)
	    return null;

	String realObjectId = objectid.substring(0, titleSlash);
	String title = objectid.substring(titleSlash + 1);

	// sakaiId is stored with a leading slash (e.g. "/scorm/{id}"), so prefix before lookup
	String mapped = objectMap.get("/" + realObjectId);
	if (mapped != null)
	    return "/" + mapped;

	// fall back to title-based lookup in the destination site
	try {
	    List<ContentPackage> packages = scormContentService.getContentPackages(siteid);
	    for (ContentPackage pkg : packages) {
		if (!pkg.isDeleted() && title.equals(pkg.getTitle()))
		    return "/" + SCORM + "/" + pkg.getContentPackageId();
	    }
	} catch (Exception e) {
	    log.warn("findObject: Unable to retrieve SCORM packages for site {}: {}", siteid, e.getMessage());
	}

	return null;
    }

    // SCORM packages cannot be imported from a Common Cartridge resource reference;
    // site-copy resolution is handled by findObject() using title-based matching.
    public String importObject(String title, String href, String mime, boolean hide){
	return null;
    }

    public String importObject(Element resource, Namespace ns, String base, String baseDir, List<String>attachments, boolean hide) {
	return null;
    }

    public String getSiteId() {
	return null;
    }

    @Override
    public void preShowItem(SimplePageItem simplePageItem) {
    }

}
