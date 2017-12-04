/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.bean.shared;



import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p> </p>
 * <p>Description: Person Bean with some properties</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $id: $
 */
@Slf4j
public class PersonBean implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1884634498046475698L;
  private String anonymousId;
  private String previewFromPage;
  
  public PersonBean(){}
  {
  }

  public String getAgentString()
  {
    return AgentFacade.getAgentString();
  }

  public String getAnonymousId()
  {
    return anonymousId;
  }

  public String getId()
  {
    DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
    if (delivery.getAnonymousLogin())
      return getAnonymousId();
    else
      return getAgentString();
  }

  public String getEid()
  {
    return AgentFacade.getEid();
  }

  public String getDisplayId()
  {
      return AgentFacade.getDisplayId();
  }

  public void setAnonymousId(String anonymousId)
  {
    this.anonymousId=anonymousId;
  }


  public boolean getIsAdmin()
  {
    String context = "!admin";
    return SecurityService.unlock("site.upd", "/site/"+context);
  }

  private Map totalSubmissionPerAssessmentHash = new HashMap();
  public Map getTotalSubmissionPerAssessmentHash(){
    return totalSubmissionPerAssessmentHash;
  }

  public void setTotalSubmissionPerAssessmentHash(Map totalSubmissionPerAssessmentHash){
    this.totalSubmissionPerAssessmentHash = totalSubmissionPerAssessmentHash;
  }

  private List resourceIdList;
  public void setResourceIdListInPreview(List resourceIdList){
    if (this.resourceIdList !=null && this.resourceIdList.size()>0){
      // they maybe left over from last preview, delete them
      AssessmentService service = new AssessmentService();
      service.deleteResources(this.resourceIdList); 
    }
    this.resourceIdList = resourceIdList;
  }

  public List getResourceIdListInPreview(){
    return resourceIdList;
  }

  public String cleanResourceIdListInPreview(){
    AssessmentService service = new AssessmentService();
    service.deleteResources(resourceIdList);
    if (previewFromPage != null && previewFromPage.equals("author")) {
    	previewFromPage = null;
    	return "author";
    }
    return "editAssessment";
  }  

  public String getLocaleLanguage(){
	  ResourceLoader r = new ResourceLoader();
	  Locale locale = r.getLocale();
	  return locale.getLanguage();
  }
  
  public String getLocaleCountry(){
	  ResourceLoader r = new ResourceLoader();
	  Locale locale = r.getLocale();
	  return locale.getCountry();
  }
  
  public void setPreviewFromPage(String previewFromPage){
      this.previewFromPage = previewFromPage;
  }  

  public String getPreviewFromPage(){
    return previewFromPage;
  }
}
