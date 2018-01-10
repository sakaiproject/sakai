/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/common/branches/sakai-10.x/import-handlers/content-handlers/src/java/org/sakaiproject/importer/impl/handlers/ResourcesHandler.java $
 * $Id: ResourcesHandler.java 106351 2012-03-28 20:21:21Z matthew@longsight.com $
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

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.importer.api.HandlesImportable;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.LBCCResource;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;

public class LBCCHandler implements HandlesImportable {

	public boolean canHandleType(String typeName) {
	    return "lessonbuilder-cc-file".equals(typeName);
	}

	public void handle(Importable thing, String siteId) {
	    if(canHandleType(thing.getTypeName())){
		LessonBuilderAccessAPI lessonBuilderApi = (LessonBuilderAccessAPI)
		    ComponentManager.get("org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI");    
		LBCCResource file = (LBCCResource)thing;
		lessonBuilderApi.loadCartridge(null, file.getFileName(), siteId);
	    }
	}

}
