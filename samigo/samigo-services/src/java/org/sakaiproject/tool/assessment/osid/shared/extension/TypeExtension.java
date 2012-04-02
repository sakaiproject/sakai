/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.osid.shared.extension;

import org.osid.shared.Type;

public class TypeExtension extends Type {
  //private String typeId;

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public TypeExtension(String authority, String domain, String keyword)
  {
    super(authority, domain, keyword);
  }

  public TypeExtension(
    String authority, String domain, String keyword, String description)
  {
    // cannot log due to the way super() works
    super(authority, domain, keyword, description);
  }

}
