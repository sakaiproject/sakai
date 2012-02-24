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

package org.sakaiproject.importer.impl.importables;

import org.sakaiproject.importer.api.HasSequence;
import org.sakaiproject.importer.api.Importable;

public abstract class AbstractImportable implements Importable, HasSequence {
	
	protected String guid;
	protected String legacyGroup;
	protected String contextPath;
	protected Importable parent;
	protected int sequenceNum;
	
	public int getSequenceNum() {
		return sequenceNum;
	}
	public void setSequenceNum(int sequenceNum) {
		this.sequenceNum = sequenceNum;
	}
	public Importable getParent() {
		return parent;
	}
	public void setParent(Importable parent) {
		this.parent = parent;
	}
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getLegacyGroup() {
		return legacyGroup;
	}
	public void setLegacyGroup(String legacyGroup) {
		this.legacyGroup = legacyGroup;
	}
	public String getContextPath() {
		return contextPath;
	}
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}
}
