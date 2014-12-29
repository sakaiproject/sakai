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
 *  An Entity reader should be implemented by any thing that wants to Read and
 *  Write entitied to Storage. The reader can be configured not to convert data
 *  into its format, only to read it if it is found in its native format by
 *  returning false in isMigrateData. 
 *  
 *  The EntityReaderHandler from getHandler
 *  does the bulk of the work. This seperated patern has been used to ensure that
 *  the Reader can be user in pthe DbBaseStorage and with internal classes in
 *  places like content hosting.
 * </pre>
 * 
 * @author ieb
 */
public interface EntityReader
{

	/**
	 * A handler to parse and serialize the data for single storage
	 * 
	 * @return
	 */
	EntityReaderHandler getHandler();



}
