package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class AddImageToQuestionValueChangeListener implements ValueChangeListener {
    @Override
    public void processValueChange(ValueChangeEvent event) {
        ItemAuthorBean bean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
        bean.addImageToQuestion(event);
    }
}

