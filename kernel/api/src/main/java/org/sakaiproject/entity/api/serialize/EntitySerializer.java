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


/**
 * <pre>
 *  An EntitySerializer performs the work of serialization and requires a
 *  serializably Entity to work on, it used by an Entity Reader Handler, and
 *  should be a standalone class that can be unit tested or used outside Sakai to
 *  read serialzed data.
 *  
 *  PLEASE DO NOT, implement this interface as an internal interface, as the data will not
 *  be readable outside Sakai.
 * </pre>
 * 
 * @author ieb
 */
public interface EntitySerializer
{

	/**
	 * @param se
	 * @param buffer
	 * @throws EntityParseException
	 */
     void parse(SerializableEntity se, byte[] buffer) throws EntityParseException;

	/**
	 * @param se
	 * @return
	 * @throws EntityParseException
	 */
    byte[] serialize(SerializableEntity se) throws EntityParseException;

	/**
	 * Return true if this serializer can parse the data
	 * @param blob
	 * @return
	 */
	boolean accept(byte[] blob);


}
