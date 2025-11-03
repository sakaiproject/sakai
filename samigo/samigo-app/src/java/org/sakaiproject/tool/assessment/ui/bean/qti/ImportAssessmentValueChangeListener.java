package org.sakaiproject.tool.assessment.ui.bean.qti;

import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class ImportAssessmentValueChangeListener implements ValueChangeListener {
    @Override
    public void processValueChange(ValueChangeEvent event) {
        XMLImportBean bean = (XMLImportBean) ContextUtil.lookupBean("xmlImport");
        bean.importAssessment(event);
    }
}

