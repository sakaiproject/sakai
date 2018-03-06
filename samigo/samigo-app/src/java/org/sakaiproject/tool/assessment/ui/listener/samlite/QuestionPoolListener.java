/**
 * Copyright (c) 2005-2010 The Apereo Foundation
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
