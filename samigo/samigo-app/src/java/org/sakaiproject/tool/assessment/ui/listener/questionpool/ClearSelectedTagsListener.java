package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class ClearSelectedTagsListener implements ActionListener {

    @Override
    public void processAction(ActionEvent actionEvent) throws AbortProcessingException {
        QuestionPoolBean questionpoolBean = (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
        questionpoolBean.clearFilters();
    }
}
