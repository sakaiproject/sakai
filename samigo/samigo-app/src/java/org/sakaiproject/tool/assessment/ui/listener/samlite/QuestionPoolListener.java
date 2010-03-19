package org.sakaiproject.tool.assessment.ui.listener.samlite;

import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.ui.bean.samlite.SamLiteBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.w3c.dom.Document;

public class QuestionPoolListener implements ActionListener {

	public QuestionPoolListener() {}
	
	public void processAction(ActionEvent ae) {
		SamLiteBean samLiteBean = (SamLiteBean) ContextUtil.lookupBean("samLiteBean");
		
		//samLiteBean.parse();
		
		Document doc = samLiteBean.createDocument();

		createImportedQuestionPool(doc, QTIVersion.VERSION_1_2);
		samLiteBean.setData("");

	}

	public QuestionPoolFacade createImportedQuestionPool(Document document, int qti) {
	    QTIService qtiService = new QTIService();
	    return qtiService.createImportedQuestionPool(document, qti);
	}
	
}
