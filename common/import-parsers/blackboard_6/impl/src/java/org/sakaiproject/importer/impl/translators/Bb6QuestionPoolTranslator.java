/**********************************************************************************
 * $URL: https://newtools.oirt.rutgers.edu:8443/repos/sakai2.x/sakai/trunk/archive/import-parsers/blackboard_6/impl/src/java/org/sakaiproject/importer/impl/translators/Bb6QuestionPoolTranslator.java $
 * $Id: Bb6QuestionPoolTranslator.java 1314 2009-04-08 19:09:09Z weresow $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
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

package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.impl.importables.Assessment;
import org.sakaiproject.importer.impl.importables.QuestionPool;

public class Bb6QuestionPoolTranslator extends Bb6AssessmentTranslator{

	public String getTypeName() {
		return "assessment/x-bb-qti-pool";
	}
	
	protected Assessment newImportable() {
		return new QuestionPool();
	}
}
