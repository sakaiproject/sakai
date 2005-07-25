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
import java.util.Iterator;
import java.util.Map;


import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SectionBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class SavePartListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(SavePartListener.class);
  private static ContextUtil cu;

  public SavePartListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();

    AssessmentBean assessmentBean = (AssessmentBean) cu.lookupBean(
								   "assessmentBean");
    String assessmentId = assessmentBean.getAssessmentId();

    SectionBean sectionBean= (SectionBean) cu.lookupBean(
                         "sectionBean");
    // create an assessment based on the title entered and the assessment
    // template selected
    // #1 - read from form editpart.jsp
    String title = (sectionBean.getSectionTitle()).trim();
    String description = sectionBean.getSectionDescription();
    String sectionId = sectionBean.getSectionId();
    AssessmentService assessmentService = new AssessmentService();
    SectionFacade section;
    if (sectionId.equals("")){
      section = addPart(assessmentId);
      log.debug("**** section="+section);
      sectionBean.setSection(section);
      sectionId = section.getSectionId().toString();
    }
    else
      section = assessmentService.getSection(sectionId);

    //Long assessmentId = section.getAssessmentId();

    boolean addItemsFromPool = false;

    sectionBean.setOutcome("editAssessment");

    if (!("".equals(sectionBean.getType()))  && ((SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()).equals(sectionBean.getType()))) {

      addItemsFromPool = true;

      if (validateItemsDrawn(sectionBean)) {
      log.debug("**** lydiatest validated true" );
      // if the author type was random draw type,  and the new type is random draw , then we need to disassociate sectionid with each items. Cannot delete items, 'cuz these items are linked in the pool

        if( (section !=null) && (section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE)!=null) && (section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE).equals(SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()))) {

          assessmentService.removeAllItems(sectionId);
        // need to reload
          section = assessmentService.getSection(sectionId);
        }
      }
      else {
      log.debug("**** lydiatest validated false " );
        sectionBean.setOutcome("editPart");
        return;
      }

    }

    log.debug("**** section title ="+section.getTitle());
    log.debug("**** title ="+title);
    if (title != null & !title.equals(""))
      section.setTitle(title);
    section.setDescription(description);

    // TODO: Need to save Type, Question Ordering, and Metadata

    if (!("".equals(sectionBean.getKeyword())))
    section.addSectionMetaData(SectionMetaDataIfc.KEYWORDS, sectionBean.getKeyword());

    if (!("".equals(sectionBean.getObjective())))
    section.addSectionMetaData(SectionMetaDataIfc.OBJECTIVES, sectionBean.getObjective());

    if (!("".equals(sectionBean.getRubric())))
    section.addSectionMetaData(SectionMetaDataIfc.RUBRICS, sectionBean.getRubric());

    if (!("".equals(sectionBean.getQuestionOrdering())))
    section.addSectionMetaData(SectionDataIfc.QUESTIONS_ORDERING, sectionBean.getQuestionOrdering());


    if (!("".equals(sectionBean.getType())))  {
    section.addSectionMetaData(SectionDataIfc.AUTHOR_TYPE, sectionBean.getType());
      if ((SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString()).equals(sectionBean.getType()))  {
        if ((sectionBean.getNumberSelected()!=null) && !("".equals(sectionBean.getNumberSelected())))
        {
          section.addSectionMetaData(SectionDataIfc.NUM_QUESTIONS_DRAWN, sectionBean.getNumberSelected());
        }

        if (!("".equals(sectionBean.getSelectedPool())))
        {
          section.addSectionMetaData(SectionDataIfc.POOLID_FOR_RANDOM_DRAW, sectionBean.getSelectedPool());
        }
      }
    }


    // if author-type is random draw from pool, add all items from pool now
    // Note: a pool can only be randomly drawn by one part.  if part A is created to randomly draw from pool 1, and you create part B, and select  the same pool 1, all items from part A will be removed.  (item.sectionId will be set to sectionId of part B.

    if (addItemsFromPool)
    {
      QuestionPoolService qpservice = new QuestionPoolService();
      ItemService itemservice = new ItemService();

    ArrayList itemlist = qpservice.getAllItems(new Long(sectionBean.getSelectedPool()) );
    int i = 0;
    Iterator iter = itemlist.iterator();
    while(iter.hasNext())
    {
      ItemFacade item= (ItemFacade) iter.next();
      item.setSection(section);
      item.setSequence(new Integer(i+1));
      section.addItem(item);
      i= i+1;
    }
    }




    assessmentService.saveOrUpdateSection(section);

    // #2 - goto editAssessment.jsp, so reset assessmentBean
    AssessmentFacade assessment = assessmentService.getAssessment(
        assessmentBean.getAssessmentId());
    assessmentBean.setAssessment(assessment);

  }

  public SectionFacade addPart(String assessmentId){
    AssessmentService assessmentService = new AssessmentService();
    SectionFacade section = assessmentService.addSection(
			     assessmentId);
    return section;
  }



  public boolean validateItemsDrawn(SectionBean sectionBean){
     String numberDrawn = sectionBean.getNumberSelected();
     int numberDrawnInt = (new Integer(numberDrawn)).intValue();

     QuestionPoolService qpservice = new QuestionPoolService();

     ArrayList itemlist = qpservice.getAllItems(new Long(sectionBean.getSelectedPool()) );
     int itemcount = itemlist.size();

     FacesContext context=FacesContext.getCurrentInstance();

     ResourceBundle rb=ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.AuthorMessages", context.getViewRoot().getLocale());
     String err;
      log.debug("lydiatest validate " + itemcount  + " and number drawn = " + numberDrawnInt);
     if(itemcount< numberDrawnInt ) {
         err=(String)rb.getObject("overdrawn_error");
         context.addMessage("modifyPartForm:numSelected",new FacesMessage(err));
            return false;
     }
     else {
            return true;
            }
    }

}
