/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.entity.api.serialize;

import org.sakaiproject.entity.api.Entity;

/**
 *  An Entity Reader handler provides methods to read(parse) resources and containers
 *  from storage and to write(serialise) them back out to storage.
 * 
 * @author ieb
 */
public interface EntityReaderHandler
{

	/**
	 * @param entry
	 * @return
	 * @throws EntityParseException
	 */
	byte[] serialize(Entity entry) throws EntityParseException;

	/**
	 * @param xml
	 * @return
	 * @throws EntityParseException
	 */
	Entity parse(String xml, byte[] blob) throws EntityParseException;

	/**
	 * @param container
	 * @param xml
	 * @return
	 * @throws EntityParseException
	 */
	Entity parse(Entity container, String xml, byte[] blob) throws EntityParseException;

	/**
	 * returns true if the implementation will parse the target
	 * 
	 * @param blob xml
	 * @return
	 */
	boolean accept(byte[] blob);

}
