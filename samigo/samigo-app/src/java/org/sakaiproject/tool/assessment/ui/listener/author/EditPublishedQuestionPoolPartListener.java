package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class EditPublishedQuestionPoolPartListener    implements ActionListener
{
    private static Log log = LogFactory
            .getLog(EditPublishedQuestionPoolPartListener.class);

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
