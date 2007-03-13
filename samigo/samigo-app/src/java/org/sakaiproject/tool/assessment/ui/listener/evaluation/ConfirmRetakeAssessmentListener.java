/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/listener/author/ConfirmRemovePartListener.java $
 * $Id: ConfirmRemovePartListener.java 16897 2006-10-09 00:28:33Z ktsao@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.ui.bean.evaluation.RetakeAssessmentBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2007 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: ConfirmRemovePartListener.java 16897 2006-10-09 00:28:33Z ktsao@stanford.edu $
 */

public class ConfirmRetakeAssessmentListener implements ActionListener {
	//private static Log log = LogFactory.getLog(ConfirmRemovePartListener.class);

	public ConfirmRetakeAssessmentListener() {
	}

	public void processAction(ActionEvent ae) throws AbortProcessingException {
		RetakeAssessmentBean retakeAssessment = (RetakeAssessmentBean) ContextUtil.lookupBean("retakeAssessment");
		String publishedAssessmentId = ContextUtil.lookupParam("publishedAssessmentId");
		String agentIdString = ContextUtil.lookupParam("agentIdString");
		String studentName = ContextUtil.lookupParam("studentName");
		//String assessmentGradingId = ContextUtil.lookupParam("assessmentGradingId");
		retakeAssessment.setPublishedAssessmentId(Long.valueOf(publishedAssessmentId));
		retakeAssessment.setAgentId(agentIdString);
		retakeAssessment.setStudentName(studentName);
	}

}
