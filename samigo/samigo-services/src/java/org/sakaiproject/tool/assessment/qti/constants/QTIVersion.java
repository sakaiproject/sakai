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



package org.sakaiproject.tool.assessment.qti.constants;

/**
 * <p>Supported QTI Versions.</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005 Sakai</p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class QTIVersion
{
  public static final int VERSION_1_2 = 1;
  public static final int VERSION_2_0 = 2;

  public QTIVersion()
  {
  }

  /**
   * @param q
   * @return true if
   * OK (QTI_VERSION_1_2 or QTI_VERSION_2_0)
   */
  public static boolean isValid(int q)
  {
    return (q == VERSION_1_2 || q == VERSION_2_0) ? true : false;
  }

}


