/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/common/branches/sakai-10.x/import-impl/src/java/org/sakaiproject/importer/impl/importables/FileResource.java $
 * $Id: FileResource.java 106351 2012-03-28 20:21:21Z matthew@longsight.com $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.importer.impl.importables;

import java.io.InputStream;

public class LBCCResource extends FileResource {

	public String getTypeName() {
		return "lessonbuilder-cc-file";
	}

}
