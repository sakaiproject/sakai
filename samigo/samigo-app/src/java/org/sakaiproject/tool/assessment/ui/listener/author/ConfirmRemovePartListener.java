/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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



package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ConfirmRemovePartListener implements ActionListener
{
  //private static Log log = LogFactory.getLog(ConfirmRemovePartListener.class);

  public ConfirmRemovePartListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  { 
    SectionBean sectionBean = (SectionBean) ContextUtil.lookupBean(
        "sectionBean");
    String sectionId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("sectionId");

    sectionBean.setSectionId(sectionId);

    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean(
        "assessmentBean");
    AssessmentService assessdelegate = new AssessmentService();
    List sectionList = assessmentBean.getSectionList();
    ArrayList otherSectionList = new ArrayList();
    for (int i=0; i<sectionList.size();i++){
      SelectItem s = (SelectItem) sectionList.get(i);
      
      // need to filter out all the random draw parts

      SectionDataIfc section= assessdelegate.getSection(s.getValue().toString());
  if( (section !=null) && (section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE)!=null) && (section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE).equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()))) {

        // skip random draw parts, cannot add items to this part manually
      }

      else {	
        if (! (sectionId).equals((String)s.getValue())) {
	  otherSectionList.add(s);
        }
      }
    }
    assessmentBean.setOtherSectionList(otherSectionList);
  }

}
