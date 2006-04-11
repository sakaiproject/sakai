/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class ConfirmRemovePartListener implements ActionListener
{
  private static Log log = LogFactory.getLog(ConfirmRemovePartListener.class);
  private static ContextUtil cu;

  public ConfirmRemovePartListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    SectionBean sectionBean = (SectionBean) cu.lookupBean(
        "sectionBean");
    String sectionId = (String) FacesContext.getCurrentInstance().
        getExternalContext().getRequestParameterMap().get("sectionId");

    sectionBean.setSectionId(sectionId);

    AssessmentBean assessmentBean = (AssessmentBean) cu.lookupBean(
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
