/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.importer.api;
import java.io.InputStream;

/**
 * A parser is a class that understands how to read an archive file and extract from
 * it a vendor-neutral collection of content objects.
 * This is the interface you should implement if you have an importer which is going
 * to parse a new type of archive.
 *
 */
public interface ImportFileParser {
	boolean isValidArchive(InputStream fileData);
	ImportDataSource parse(InputStream fileData, String unArchiveLocation);
	
	/**
	 * Factory method to create a new instance of this parser.
	 * @return A new instance of this parser.
	 */
	ImportFileParser newParser();
}
