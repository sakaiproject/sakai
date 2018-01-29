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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import net.htmlparser.jericho.Source;
import org.sakaiproject.tool.assessment.ui.bean.author.SearchQuestionBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: SearchQuestionByText</p>
 * <p>Description: Samigo Search Questions using text Listener</p>
 * @version $Id$
 */

public class SearchQuestionByText implements ActionListener
{

    /**
     * Simply cancel and return
     * @param ae ActionEvent
     * @throws AbortProcessingException
     */
    public void processAction(ActionEvent ae) throws AbortProcessingException
    {

        SearchQuestionBean searchQuestionBean= (SearchQuestionBean) ContextUtil.lookupBean("searchQuestionBean");
        searchQuestionBean.setLastSearchType("text");
        String searchText="";

        if (searchQuestionBean.getTextToSearch()!=null){
            searchText=searchQuestionBean.getTextToSearch();
            Source parseSearchTerms = new Source(searchText);
            searchText = parseSearchTerms.getTextExtractor().toString();
            if ((searchText.trim()).length()<3){
                String publish_error=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","tag_text_error");
                FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(publish_error));
                searchQuestionBean.setOutcome("searchQuestion");
                return;
            }
        }else {
            String publish_error = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "tag_text_error");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(publish_error));
            searchQuestionBean.setOutcome("searchQuestion");
            return;
        }

        if (ae.getComponent().getId().equals("searchByTextAND")){
            searchQuestionBean.searchQuestionsByText(true);
        }
        if (ae.getComponent().getId().equals("searchByTextOR")){
            searchQuestionBean.searchQuestionsByText(false);
        }

        searchQuestionBean.setOutcome("searchQuestion");
    }

}
