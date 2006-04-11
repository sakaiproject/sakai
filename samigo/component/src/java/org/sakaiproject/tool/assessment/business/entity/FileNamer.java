/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.assessment.util.StringParseUtils;

/**
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 *
 * <p>
 * Organization: Stanford University
 * </p>
 *
 * <p>
 * This class implements common methods for describing data needed to
 * record/save and retrieve audio recordings.
 * </p>
 *
 * <p>
 * Usage (hypothetical example): <code><br>
 * <br>
 * RecordingData rd = new RecordingData( agent_name,  agent_id, course_name,
 * course_id); log.debug("file" + rd.getFileName() + "." +
 * rd.getFileExtension()); </code>
 * </p>
 *
 * @author Ed Smiley
 * @version $Id $
 */
public class FileNamer
{
  private static Log log = LogFactory.getLog(RecordingData.class);

  // internals
  private static final int maxAgentName = 20;
  private static final int maxAgentId = 10;
  private static final int maxcourseAssignmentContext = 15;
  private static final int maxCourseId = 10;
  private static final int maxFileName = 64;

  /**
   * Makes a unique file name from the data supplied.
   *
   * @param agent_name The name of the person uploading the file
   * @param agent_id The id code of the person uploading the file
   * @param course_assignment_context The name of the course, assignment, part,
   *        quetion etc.
   *
   * @return a meaningful file name
   */
  public static String make(
    String agent_name, String agent_id, String course_assignment_context)
  {
    // we can still create a unique file if one or more values are null
    agent_name = "" + agent_name;
    agent_id = "" + agent_id;
    course_assignment_context = "" + course_assignment_context;

    //timestamp this access
    Date date = new Date();

    // hold in a StringBuffer
    StringBuffer sb = new StringBuffer();

    // make a unique random signature
    String rand = "" + Math.random() + "" + Math.random();

    sb.append(
      StringParseUtils.simplifyString("" + agent_name, maxAgentName, agent_name.length()));
    sb.append(
      StringParseUtils.simplifyString("" + agent_id, maxAgentId, agent_id.length()));
    sb.append(
      StringParseUtils.simplifyString(
        "" + course_assignment_context, maxcourseAssignmentContext,
        course_assignment_context.length()));
    sb.append(new SortableDate(date));
    sb.append("__");
    sb.append(StringParseUtils.simplifyString(rand, 99, 99));

    // return as a string
    if(sb.length() > maxFileName)
    {
      return sb.substring(0, maxFileName - 1);
    }

    return sb.toString();
  }

  /**
   * unit test for use with jUnit etc.
   */
  public static void unitTest()
  {
    String s;
    s = make("Ed Smiley", "esmiley", "Intro to Wombats 101");
    log.debug("esmiley file: " + s);

    s = make(
        "Rachel Gollub", "rgollub", "Intro to Wolverines and Aardvarks 221B");
    log.debug("rgollub file: " + s);

    s = make("Ed Smiley", "esmiley", "Intro to Wombats 101");
    log.debug("esmiley file: " + s);

    s = make(
        "Rachel Gollub", "rgollub", "Intro to Wolverines and Aardvarks 221B");
    log.debug("rgollub file: " + s);

    s = make(null, null, null);
    log.debug("NULL file: " + s);
    s = make(null, null, null);
    log.debug("NULL file: " + s);
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @param args DOCUMENTATION PENDING
   */
  public static void main(String[] args)
  {
    unitTest();
  }
}


