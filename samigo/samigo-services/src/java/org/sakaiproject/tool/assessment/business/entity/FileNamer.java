/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

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
@Slf4j
 public class FileNamer
{
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

    // hold in a StringBuilder
    StringBuilder sb = new StringBuilder();

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
