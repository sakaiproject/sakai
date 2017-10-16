/**
 * Copyright (c) 2005-2014 The Apereo Foundation
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
package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Bb55StaffInfoTranslator implements IMSResourceTranslator{

	public String getTypeName() {
		return "resource/x-bb-staffinfo";
	}

	public Importable translate(Node archiveResource, Document descriptor, String contextPath, String archiveBasePath) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean processResourceChildren() {
		return true;
	}

}
