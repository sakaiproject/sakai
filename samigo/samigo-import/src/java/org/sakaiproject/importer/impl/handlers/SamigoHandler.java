/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.importer.impl.handlers;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.importer.api.HandlesImportable;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.QuestionPool;
import org.sakaiproject.importer.impl.importables.Assessment;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.services.qti.QTIService;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.cover.SessionManager;

/**
*
* @author Joshua Ryan joshua.ryan@asu.edu
*
*/

@Slf4j
public class SamigoHandler implements HandlesImportable {

	//won't be needed if we can get a createImportedQuestionPool(Document, int)
	//added to QTIService
	private QuestionPoolService qps = new QuestionPoolService();

	public boolean canHandleType(String typeName) {
		return ("sakai-question-pool".equals(typeName) 
				|| "sakai-assessment".equals(typeName));
	}
	
	public void handle(Importable thing, String siteId) {

		if ("sakai-assessment".equals(thing.getTypeName())) {
			Assessment assessment = (Assessment) thing;
//			Document document = assessment.getQti();
//			String version = assessment.getVersion();

			QTIService qtiService = new QTIService();

			//default to qti 2.0, the latest version Samigo can handle currently
			int version = QTIVersion.VERSION_2_0;
			if ("1.2".equals(assessment.getVersion()))
					version = QTIVersion.VERSION_1_2;
			
			try {
				qtiService.createImportedAssessment(assessment.getQti(), version);
			}
			catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		else if ("sakai-question-pool".equals(thing.getTypeName())){
			QuestionPool sourcePool = (QuestionPool) thing;

			// plan 'A'
			// QTI question pool import support is being added to samigo
			// currently just use this.

			// plan 'B' is to use QTIService to import items then add them
			// to a pool like Zach did in the old brute force version... a 
			// rough version of which is seen below.

			
			QuestionPoolService qps = new QuestionPoolService();

			QuestionPoolFacade destinationPool = new QuestionPoolFacade();
			destinationPool.setOwnerId(SessionManager.getCurrentSessionUserId());
			destinationPool.setTitle(sourcePool.getTitle());
			destinationPool.setDescription(sourcePool.getDescription());

			QuestionPoolFacade savedPool = qps.savePool(destinationPool);
/*
			for (Iterator i = questionItems.iterator();i.hasNext();) {
				ItemFacade item = 
					QTIService.createImportedItem(Document document, int qtiVersion);
				qps.addItemToPool(item.getItemIdString(),savedPool.getQuestionPoolId());
			}
*/			
		}
	}
}
