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
package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.*;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.SectionService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.SearchQuestionBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolDataBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

@Slf4j
public class ImportQuestionsToAuthoringFromSearch implements ActionListener
{
  private static final ServerConfigurationService serverConfigurationService= (ServerConfigurationService) ComponentManager.get( ServerConfigurationService.class );


  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    SearchQuestionBean  searchQuestionBean= (SearchQuestionBean) ContextUtil.lookupBean("searchQuestionBean");
    if (!importItems(searchQuestionBean))
    {
      throw new RuntimeException("failed to populateItemBean.");
    }
    searchQuestionBean.setLastSearchType("");
  }


  public boolean importItems(SearchQuestionBean searchQuestionBean) {
    try {
      ArrayList destItems = ContextUtil.paramArrayValueLike("importCheckbox");

      if (destItems.size() > 0) {
        if (searchQuestionBean.isComesFromPool()) {

          ItemService delegate = new ItemService();
          ItemAuthorBean itemauthor = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
          ItemFacade itemfacade = null;
          ArrayList<ItemFacade> sortedQuestions = new ArrayList<ItemFacade>();
          QuestionPoolBean qpoolbean = (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
          QuestionPoolDataBean contextCurrentPool = qpoolbean.getCurrentPool();
            String protocol= serverConfigurationService.getServerUrl();
            String toContext=AgentFacade.getCurrentSiteId();

          for (Object itemID : destItems) {
            ItemFacade resultItemFacade = delegate.getItem(Long.valueOf((String) itemID), AgentFacade.getAgentString());
            ItemData clonedItem = delegate.cloneItem(resultItemFacade.getData());
            clonedItem.setItemId(Long.valueOf(0));
            clonedItem.setItemIdString("0");
              Set newItemTextSet = PersistenceService.getInstance().getAssessmentFacadeQueries().prepareItemTextSet(clonedItem, clonedItem
                      .getItemTextSet(), protocol, toContext);
              Set newItemAttachmentSet = PersistenceService.getInstance().getAssessmentFacadeQueries().prepareItemAttachmentSet(clonedItem, clonedItem
                      .getItemAttachmentSet(), protocol, toContext);
              clonedItem.setItemTextSet(newItemTextSet);
              clonedItem.setItemAttachmentSet(newItemAttachmentSet);
            itemfacade = new ItemFacade(clonedItem);
            sortedQuestions.add(itemfacade);
          }

          Collections.sort(sortedQuestions, new Comparator<ItemFacade>() {
            @Override
            public int compare(ItemFacade obj1, ItemFacade obj2) {
              return obj1.getText().compareTo(obj2.getText());
            }
          });

          Iterator iter = sortedQuestions.iterator();
          while (iter.hasNext()) {
            // path instead. so we will fix it here
            itemfacade = (ItemFacade) iter.next();
            setRelativePathInAttachment(itemfacade.getItemAttachmentList());
            itemfacade.setSequence(null);
            itemfacade.setSection(null);
            delegate.saveItem(itemfacade);

            EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SAVEITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/saved  itemId=" + itemfacade.getItemId().toString(), true));

            QuestionPoolService qpdelegate = new QuestionPoolService();

            //Save the question in the pool
            if (!qpdelegate.hasItem(itemfacade.getItemIdString(),
                    Long.valueOf(searchQuestionBean.getSelectedQuestionPool()))) {
              qpdelegate.addItemToPool(itemfacade.getItemId(),
                      Long.valueOf(searchQuestionBean.getSelectedQuestionPool()));

            }

            // update POOLID metadata if any and update PARTIID to 0,
            delegate.deleteItemMetaData(itemfacade.getItemId(), ItemMetaData.POOLID, AgentFacade.getAgentString());
            delegate.deleteItemMetaData(itemfacade.getItemId(), ItemMetaData.PARTID, AgentFacade.getAgentString());
            delegate.addItemMetaData(itemfacade.getItemId(), ItemMetaData.PARTID, "0", AgentFacade.getAgentString());

          }

          // reset InsertPosition
          itemauthor.setInsertPosition("");
          //reset the select
          itemauthor.setItemTypeString("");



          //And now we need to update the poolList page with the new questions
          qpoolbean.buildTree();

          qpoolbean.startEditPoolAgain(searchQuestionBean.getSelectedQuestionPool());
          QuestionPoolDataBean currentPool = qpoolbean.getCurrentPool();
          currentPool.setDisplayName(contextCurrentPool.getDisplayName());
          currentPool.setOrganizationName(contextCurrentPool.getOrganizationName());
          currentPool.setDescription(contextCurrentPool.getDescription());
          currentPool.setObjectives(contextCurrentPool.getObjectives());
          currentPool.setKeywords(contextCurrentPool.getKeywords());

          searchQuestionBean.setOutcome("editPool");

        } else {


          AssessmentService assessdelegate = new AssessmentService();
          ItemService delegate = new ItemService();
          SectionService sectiondelegate = new SectionService();
          AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
          ItemAuthorBean itemauthor = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
          int itempos = 0;
          SectionFacade section = null;
          ItemFacade itemfacade = null;
          boolean newSectionCreated = false;
          ArrayList<ItemFacade> sortedQuestions = new ArrayList<ItemFacade>();
          String protocol= serverConfigurationService.getServerUrl();
          String toContext=AgentFacade.getCurrentSiteId();

          for (Object itemID : destItems) {
            ItemFacade resultItemFacade = delegate.getItem(Long.valueOf((String) itemID), AgentFacade.getAgentString());
            ItemData clonedItem = delegate.cloneItem(resultItemFacade.getData());
            clonedItem.setItemId(Long.valueOf(0));
            clonedItem.setItemIdString("0");
            Set newItemTextSet = PersistenceService.getInstance().getAssessmentFacadeQueries().prepareItemTextSet(clonedItem, clonedItem
                    .getItemTextSet(), protocol, toContext);
            Set newItemAttachmentSet = PersistenceService.getInstance().getAssessmentFacadeQueries().prepareItemAttachmentSet(clonedItem, clonedItem
                    .getItemAttachmentSet(), protocol, toContext);
            clonedItem.setItemTextSet(newItemTextSet);
            clonedItem.setItemAttachmentSet(newItemAttachmentSet);
            itemfacade = new ItemFacade(clonedItem);
            sortedQuestions.add(itemfacade);
          }

          Collections.sort(sortedQuestions, new Comparator<ItemFacade>() {
            @Override
            public int compare(ItemFacade obj1, ItemFacade obj2) {
              return obj1.getText().compareTo(obj2.getText());
            }
          });


          Iterator iter = sortedQuestions.iterator();
          while (iter.hasNext()) {
            // path instead. so we will fix it here
            itemfacade = (ItemFacade) iter.next();
            setRelativePathInAttachment(itemfacade.getItemAttachmentList());
            if ("-1".equals(searchQuestionBean.getSelectedSection())) {
              if (!newSectionCreated) {
                // add a new section
                section = assessdelegate.addSection(assessmentBean.getAssessmentId());
                newSectionCreated = true;
              }
            } else {
              section = sectiondelegate.getSection(Long.valueOf(searchQuestionBean.getSelectedSection()), AgentFacade.getAgentString());
            }

            if (section != null) {
              itemfacade.setSection(section);
              if ((itemauthor.getInsertPosition() == null) || ("".equals(itemauthor.getInsertPosition()))) {
                if (newSectionCreated) {
                  itemfacade.setSequence(itempos + 1);
                } else {
                  // if adding to the end
                  if (section.getItemSet() != null) {
                    itemfacade.setSequence(section.getItemSet().size() + 1);
                  } else {
                    // this is a new part
                    itemfacade.setSequence(1);
                  }
                }
              } else {
                // if inserting or a question
                ItemAddListener itemAddListener = new ItemAddListener();
                int insertPosIntvalue = Integer.valueOf(itemauthor.getInsertPosition()) + itempos;
                itemAddListener.shiftSequences(delegate, section, insertPosIntvalue);
                itemfacade.setSequence(insertPosIntvalue + 1);
              }

              delegate.saveItem(itemfacade);

              EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_SAVEITEM, "/sam/" + AgentFacade.getCurrentSiteId() + "/saved  itemId=" + itemfacade.getItemId().toString(), true));

              // remove POOLID metadata if any,
              delegate.deleteItemMetaData(itemfacade.getItemId(), ItemMetaData.POOLID, AgentFacade.getAgentString());
              delegate.deleteItemMetaData(itemfacade.getItemId(), ItemMetaData.PARTID, AgentFacade.getAgentString());
              delegate.addItemMetaData(itemfacade.getItemId(), ItemMetaData.PARTID, section.getSectionId().toString(), AgentFacade.getAgentString());
            }

            itempos++;   // for next item in the destItem.
          }

          // reset InsertPosition
          itemauthor.setInsertPosition("");
          itemauthor.setItemTypeString("");


          AssessmentFacade assessment = assessdelegate.getAssessment(assessmentBean.getAssessmentId());
          assessmentBean.setAssessment(assessment);

          searchQuestionBean.setOutcome("editAssessment");
        }
        //reset the list of results
        String[] emptyArr = {};
        searchQuestionBean.setDestItems(emptyArr);
        searchQuestionBean.setTextToSearch("");
        searchQuestionBean.setTagToSearch(null);
        searchQuestionBean.setTagToSearchLabel("");
        searchQuestionBean.setResults(null);
        searchQuestionBean.setResultsSize(0);

      } else {
        // nothing is checked
        searchQuestionBean.setOutcome("searchQuestion");
      }
    } catch (RuntimeException e) {
      log.error(e.getMessage(), e);
      return false;
    }
    return true;
  }

  private void setRelativePathInAttachment(List attachmentList){
    for (int i=0; i<attachmentList.size();i++){
      AttachmentIfc attach = (AttachmentIfc) attachmentList.get(i);
      String url = ContextUtil.getRelativePath(attach.getLocation());
      attach.setLocation(url);
    }
  }
}
