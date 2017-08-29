/**
 * Copyright (c) 2005-2013 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.entity.impl;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.tool.assessment.entity.api.PublishedAssessmentEntityProvider;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.BrowseSearchable;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.entityprovider.extension.TemplateMap;
import org.sakaiproject.entitybroker.DeveloperHelperService;





import java.lang.IllegalArgumentException;
import java.lang.SecurityException;
import java.lang.IllegalStateException;



/**
 * Entity Provider impl for samigo PublishedAssessments
 * 
 * Implements PropertyProvideable to allow things to browse the entities provided by site
 * 
 * @author Joshua Ryan  josh@asu.edu  alt^I
 *
 */
public class PublishedAssessmentEntityProviderImpl implements PublishedAssessmentEntityProvider,
      CoreEntityProvider, AutoRegisterEntityProvider, PropertyProvideable,  BrowseSearchable, RESTful, Outputable, RedirectDefinable {

  private static final String CAN_TAKE = "assessment.takeAssessment";
  private static final String CAN_PUBLISH = "assessment.publishAssessment.any";
  private PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries;
  private SecurityService securityService;
  private GradingServiceAPI gradingService = null;
  
  public String getEntityPrefix() {
    return ENTITY_PREFIX;
  }

  public boolean entityExists(String id) {
    boolean rv = false;

    try {
    PublishedAssessmentService service = new PublishedAssessmentService();

    //TODO: Should we refrence Published Assessments via ID or via alias as they are now?
    PublishedAssessmentFacade pub = service.getPublishedAssessment(id);
    if (pub != null) {
      rv = true;
    }
    }
    catch (Exception e){
    	rv=false;
    }
    return rv;
  }

  public TemplateMap[] defineURLMappings() {
      return new TemplateMap[] {
              new TemplateMap("/{prefix}/context/{siteId}", "{prefix}{dot-extension}") // all assignments in a site
      };
  }


  /**
   * Browse for the list of published assessments for a given site for a user
   * 
   * @param siteId
   * @param userId
   * 
   * @return list of published assessments that a user has access to see in a site
   */
  private List<String> browseEntities(String siteId, String userId) {
    List refs = null;
    List assessments = null;
    boolean canPublish = false;
    Date currentDate = new Date();
    
    //TODO: support sorting by other attributes
    String orderBy = "title";

    // Check what the user can do
    if (securityService.unlock(CAN_PUBLISH, "/site/"+siteId)) {
      publishedAssessmentFacadeQueries.
        getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true);
      assessments = publishedAssessmentFacadeQueries
        .getBasicInfoOfAllInActivePublishedAssessments(orderBy, siteId, true);
      assessments.addAll(publishedAssessmentFacadeQueries
        .getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true));
      canPublish = true;
    }
    else if (securityService.unlock(CAN_TAKE, "/site/"+siteId)) {
      assessments = publishedAssessmentFacadeQueries
        .getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true);
    }

    if (assessments != null) {
      refs = new ArrayList();
      for (int i = 0; i < assessments.size(); i++) {
        PublishedAssessmentFacade pub = (PublishedAssessmentFacade) assessments.get(i);
        
        if (canPublish || pub.getStartDate() == null || currentDate.after(pub.getStartDate()))
          refs.add("/" + ENTITY_PREFIX + "/" + pub.getPublishedAssessmentId());
      }
    }
    return refs;
  }

  public List getEntities(EntityReference ref, Search search){
	  Vector results = new Vector();
	  

	  String siteId = null;
	  Restriction[] restrictions = search.getRestrictions();

      String userId = null;	  
	  
	  for(int r=0;r<restrictions.length;r++){
		  if(restrictions[r].property.equalsIgnoreCase("siteId")){
			  siteId = (String) restrictions[r].value;
		  }
		  if(restrictions[r].property.equalsIgnoreCase("userId")){
			  userId = (String) restrictions[r].value;
		  }
		  if(restrictions[r].property.equalsIgnoreCase("context")){
			  siteId = (String) restrictions[r].value;
		  }		  
	  }

	  if(userId==null){
		  userId = developerHelperService.getCurrentUserId();
	  }
      if (userId == null) {
          throw new SecurityException("No user is currently logged in so no data can be retrieved");
      }
	  
	  if(userId==null)return results;
	  if(siteId==null) return results;
	   String orderBy = "title";
	   List assessments = null;
	   boolean canPublish = false;
	   Date currentDate = new Date();

	    // Check what the user can do
	    if (securityService.unlock(CAN_PUBLISH, "/site/"+siteId)) {
	      publishedAssessmentFacadeQueries.
	        getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true);
	      assessments = publishedAssessmentFacadeQueries
	        .getBasicInfoOfAllInActivePublishedAssessments(orderBy, siteId, true);
	      assessments.addAll(publishedAssessmentFacadeQueries
	        .getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true));
	      canPublish = true;
	    }
	    else if (securityService.unlock(CAN_TAKE, "/site/"+siteId)) {
	      assessments = publishedAssessmentFacadeQueries
	        .getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true);
	    }

		if (assessments != null) {
			Iterator assessmentIterator = assessments.iterator();
			while(assessmentIterator.hasNext()){
				PublishedAssessmentFacade pub = (PublishedAssessmentFacade) assessmentIterator.next();
				results.add(pub);

			}
		}
	  return results;
  }
  
  public Object getEntity(EntityReference ref) {
	  
	  return new EntityData(new EntityReference("dummy reference"), "dummy"); 
  }
  
  public List<EntityData> browseEntities(Search search,
          String userReference,
          String associatedReference,
          Map<String,Object> params){
	  
	  Vector results = new Vector();
	  
	  String siteId = (String) params.get("context");
	  Restriction[] restrictions = search.getRestrictions();
	  String userId = null;
	  for(int r=0;r<restrictions.length;r++){
		  if(restrictions[r].property.equalsIgnoreCase("userId")){
			  userId = (String) restrictions[r].value;
		  }
		  if(restrictions[r].property.equalsIgnoreCase("context")){
			  siteId = (String) restrictions[r].value;
		  }
		  
	  }
	  if(userId==null)return results;
	  if(siteId==null) return results;
	   String orderBy = "title";
	   List assessments = null;
	   boolean canPublish = false;
	   Date currentDate = new Date();

	    // Check what the user can do
	    if (securityService.unlock(CAN_PUBLISH, "/site/"+siteId)) {
	      publishedAssessmentFacadeQueries.
	        getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true);
	      assessments = publishedAssessmentFacadeQueries
	        .getBasicInfoOfAllInActivePublishedAssessments(orderBy, siteId, true);
	      assessments.addAll(publishedAssessmentFacadeQueries
	        .getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true));
	      canPublish = true;
	    }
	    else if (securityService.unlock(CAN_TAKE, "/site/"+siteId)) {
	      assessments = publishedAssessmentFacadeQueries
	        .getBasicInfoOfAllActivePublishedAssessments(orderBy, siteId, true);
	    }
	    Iterator assessmentIterator = assessments.iterator();
	    while(assessmentIterator.hasNext()){
	    	PublishedAssessmentFacade pub = (PublishedAssessmentFacade) assessmentIterator.next();
	        if(canPublish || pub.getStartDate() == null || currentDate.after(pub.getStartDate())){
	            String thisEntityReference = "/" + ENTITY_PREFIX + "/" + pub.getPublishedAssessmentId();
	            String thisEntityTitle = pub.getTitle();
	            results.add(new EntityData(new EntityReference(thisEntityReference), thisEntityTitle));
	        }

	    	
	    }
	  return results;
	  
  }

  public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
      boolean exactMatch) {
    String siteId = null;
    String userId = null;

    if (ENTITY_PREFIX.equals(prefixes[0])) {

      for (int i = 0; i < name.length; i++) {
        if ("context".equalsIgnoreCase(name[i]) || "site".equalsIgnoreCase(name[i]))
          siteId = searchValue[i];
        else if ("user".equalsIgnoreCase(name[i]) || "userId".equalsIgnoreCase(name[i]))
          userId = searchValue[i];
      }

      if (siteId != null && userId != null) {
        return browseEntities(siteId, userId);
      }
    }

    return null;
  }

  public Map<String, String> getProperties(String reference) {
    Map<String, String> props = new HashMap<String, String>();
    PublishedAssessmentService service = new PublishedAssessmentService();
    PublishedAssessmentFacade pub =
      service.getPublishedAssessment(reference.substring(ENTITY_PREFIX.length() + 2));
    props.put("title", pub.getTitle());
    props.put("description", pub.getDescription());
    props.put("author", pub.getCreatedBy());
    if (pub.getCreatedDate() != null)
      props.put("created_date", DateFormat.getInstance().format(pub.getCreatedDate()));
    props.put("modified_by", pub.getLastModifiedBy());
    if (pub.getLastModifiedDate() != null)
      props.put("modified_date", DateFormat.getInstance().format(pub.getLastModifiedDate()));
    if (pub.getTotalScore() != null)
      props.put("totalScore", pub.getTotalScore().toString());
    if (pub.getStartDate() != null)
      props.put("start_date", DateFormat.getInstance().format(pub.getStartDate()));
    else if (pub.getAssessmentAccessControl().getStartDate() != null)
      props.put("start_date", DateFormat.getInstance().format(
          pub.getAssessmentAccessControl().getStartDate()));
    if (pub.getDueDate() != null)
      props.put("due_date", DateFormat.getInstance().format(pub.getDueDate()));
    else if (pub.getAssessmentAccessControl().getDueDate() != null)
      props.put("due_date", DateFormat.getInstance().format(
          pub.getAssessmentAccessControl().getDueDate()));
    if (pub.getRetractDate() != null)
      props.put("retract_date", DateFormat.getInstance().format(pub.getRetractDate()));
    else if (pub.getAssessmentAccessControl().getRetractDate() != null)
      props.put("retract_date", DateFormat.getInstance().format(
          pub.getAssessmentAccessControl().getRetractDate()));

    props.put("comments", pub.getComments());
    props.put("siteId", pub.getOwnerSiteId());
    return props;
  }

  
  
	public String[] getHandledOutputFormats() {
		return new String[] {Formats.XML, Formats.JSON};
	}
	
    public String[] getHandledInputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
    	return null;    	
    }

    public Object getSampleEntity() {
    	return null;    	
    	
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {

    }
    
    public void deleteEntity(EntityReference ref, Map<String, Object> params) {

    }


  public String getPropertyValue(String reference, String name) {
    Map<String, String> props = getProperties(reference);
    return props.get(name);
  }

  public void setPropertyValue(String reference, String name, String value) {
    // This does nothing for now... we could all the setting of many published assessment properties
    // here though... if you're feeling jumpy feel free.
  }

  public PublishedAssessmentFacadeQueriesAPI getPublishedAssessmentFacadeQueries() {
    return publishedAssessmentFacadeQueries;
  }

  public void setPublishedAssessmentFacadeQueries(
      PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries) {
    this.publishedAssessmentFacadeQueries = publishedAssessmentFacadeQueries;
  }

  public SecurityService getSecurityService() {
    return securityService;
  }

  public void setSecurityService(SecurityService security) {
    this.securityService = security;
  }
  
  private DeveloperHelperService developerHelperService;
  public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
     this.developerHelperService = developerHelperService;
}

}
