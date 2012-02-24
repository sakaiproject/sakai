/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/SAXEntityReader.java $
 * $Id: SAXEntityReader.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
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

package org.sakaiproject.util;

import java.util.Map;

/**
 * 
 * StorageUsers that can read using SAX should implement this interface. 
 * Unfortunately the parse.parse method requires a DefaultHandler which is a concrete class,
 * so DefaultEntityHandler cant be an interface, and hence we need to extend. So the implementation 
 * has to be somewhere. To avoid forcing things that dont already depend on util-util to depend on 
 * util-util the interface is here rather than in db-util or elsewhere. 
 * 
 * @author ieb
 *
 */
public interface SAXEntityReader
{

	/**
	 * Get the Default Entity Handler.
	 * @return
	 */
	DefaultEntityHandler getDefaultHandler(Map<String, Object> services);

	/**
	 * @return
	 */
	Map<String, Object> getServices();

}
