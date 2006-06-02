/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/osid/shared/extension/TypeExtension.java $
 * $Id: TypeExtension.java 9276 2006-05-10 23:04:20Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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
  private String typeId;

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
