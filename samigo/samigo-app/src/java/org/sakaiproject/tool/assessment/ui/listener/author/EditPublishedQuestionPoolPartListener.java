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
package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class EditPublishedQuestionPoolPartListener    implements ActionListener
{

    public void processAction(ActionEvent ae) throws AbortProcessingException
    {
        AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
        SectionContentsBean sectionBean= (SectionContentsBean) ContextUtil.lookupBean(
                "partBean");
        
        String sectionId;
        String poolName;
        String sectionTitle;

        if (sectionBean != null) {
            sectionId = sectionBean.getSectionId();
            sectionTitle = sectionBean.getTitle();
            poolName = sectionBean.getPoolNameToBeDrawn();
        }
        else {
            sectionId = null;
            sectionTitle = null;
            poolName = null;
        }
        

        if (author != null) {
            if (author.getIsEditPoolFlow()) {
                 author.setIsEditPoolFlow(false);
            }
            else {
                author.setIsEditPoolFlow(true);
            }
        
            author.setEditPoolSectionId(sectionId);
            author.setEditPoolName(poolName);
            author.setEditPoolSectionName(sectionTitle);
        }
    }

}
