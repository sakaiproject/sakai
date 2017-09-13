/**
 * Copyright (c) 2005-2016 The Apereo Foundation
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
import org.sakaiproject.tool.assessment.entity.api.CoreAssessmentEntityProvider;
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
 * Entity Provider impl for samigo core Assessments.
 * 
 * References to /direct/sam_core/NNN are mapped into the most recent published
 * assessment based on the specified core assessment. The mapping is done by
 * PublishedAssessmentVPInferrer. This is a minimal implementation. It is not browsable.
 *
 * @author Charles Hedrick hedrick@rutgers.edu
 *
 */
public class CoreAssessmentEntityProviderImpl implements CoreAssessmentEntityProvider,
      CoreEntityProvider, AutoRegisterEntityProvider, RESTful {

  private static final String CAN_TAKE = "assessment.takeAssessment";
  private static final String CAN_PUBLISH = "assessment.publishAssessment.any";
  private PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries;
  private SecurityService securityService;
  private GradingServiceAPI gradingService = null;
  public final static String ENTITY_PREFIX = CoreAssessmentEntityProvider.ENTITY_PREFIX;
  
  public String getEntityPrefix() {
      return ENTITY_PREFIX;
  }

    // doesn't look like this actualy gets used
  public boolean entityExists(String id) {
	Long publishedId = null;
	Long coreId = null;
	if (id != null) {
	    try {
		coreId = new Long(id);
	    } catch (Exception e) {
		return false;
	    }
	}
	if (coreId != null)
	    publishedId = publishedAssessmentFacadeQueries.getPublishedAssessmentId(coreId);
	if (publishedId != 0)
	    return true;
	return false;
  }

  public Object getEntity(EntityReference ref) {
	  
	  return new EntityData(new EntityReference("dummy reference"), "dummy"); 
  }

  public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
      boolean exactMatch) {

    return null;
  }

  public List getEntities(EntityReference ref, Search search){
      return new ArrayList();
  }	  

  public Map<String, String> getProperties(String reference) {
    Map<String, String> props = new HashMap<String, String>();
    return props;
  }

  private List<String> browseEntities(String siteId, String userId) {
      return new ArrayList<String>();
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
