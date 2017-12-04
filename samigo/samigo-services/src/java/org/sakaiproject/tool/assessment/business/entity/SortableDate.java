/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.business.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Title: Navigo Project: AAM
 * </p>
 *
 * <p>
 * Description: Standard Date
 * </p>
 *
 * <p>
 * Purpose: Encapsulate the standard date format used in AAM
 * </p>
 *
 * <p></p>
 *
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 *
 * <p>
 * Company: Stanford University
 * </p>
 *
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class SortableDate
{

  /**
   * standard date format string used in AAM
   *
   * @todo would be nice to convert to use properties later on
   */
  private static final String SORT_FORMAT = "yyyyMMddkkmm";
  private Date date;

  /**
   * Constructor.
   *
   * @param pdate a date object ot be formatted
   */
  public SortableDate(Date pdate)
  {
    date = pdate;
  }

  /**
   * Overides Object.toString().
   *
   * @return the formatted date in yyyMMddkkmm order
   */
  public String toString()
  {
    try
    {
      SimpleDateFormat dateFormatter = new SimpleDateFormat(SORT_FORMAT);

      return dateFormatter.format(date);
    }
    catch(Exception e)
    {
      log.debug("Date Exception " + e);
    }

    return "unknown date";
  }
}
