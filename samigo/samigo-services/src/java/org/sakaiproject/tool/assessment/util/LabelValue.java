/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.util;


/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
public class LabelValue
{
  private String label = "";
  private String value = "";

  /**
   * Creates a new LabelValue object.
   *
   * @param label DOCUMENTATION PENDING
   * @param value DOCUMENTATION PENDING
   */
  public LabelValue(String label, String value)
  {
    this.label = label;
    this.value = value;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param label DOCUMENTATION PENDING
   */
  public void setLabel(String label)
  {
    this.label = label;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getLabel()
  {
    return label;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param value DOCUMENTATION PENDING
   */
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String getValue()
  {
    return value;
  }
}
