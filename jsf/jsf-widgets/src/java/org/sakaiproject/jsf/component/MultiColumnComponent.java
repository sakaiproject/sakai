/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
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

package org.sakaiproject.jsf.component;

import javax.faces.component.UIData;

/**
 *
 * <p>Thin "marker component" for a column that handles several columns at once.
 * Extends UIDAta so that it can iterate.</p>
 * <p>Copyright: Copyright  Sakai (c) 2005</p>
 * @author Ed Smiley
 * @version $Id$
 */
public class MultiColumnComponent extends UIData
{
  public MultiColumnComponent()
  {
      super();
  }

  /**
  *
  * @return "org.sakaiproject.MultiColumn"
  */
  public String getFamily()
  {
      return "org.sakaiproject.MultiColumn";
  }

}
