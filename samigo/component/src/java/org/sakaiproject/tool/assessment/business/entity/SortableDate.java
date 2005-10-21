/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.business.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class SortableDate
{
  private static Log log = LogFactory.getLog(SortableDate.class);

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

  /**
   * Unit test only
   *
   * @param args not used
   */
  public static void main(String[] args)
  {
    Date d = new Date();
    SortableDate sd = new SortableDate(d);
    log.debug("debug: " + sd);
  }
}
