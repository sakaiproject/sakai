/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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


package org.sakaiproject.tool.assessment.business.entity.assessment.model;


/**
 * This holds all the choices for the various options.  It seems more portable
 * to include the strings here than to try to build them into either the front
 * end or the database. In every case of a boolean, the false answer goes
 * first (0=false) and the true answer goes second (1=true).
 *
 * @author Rachel Gollub
 * @author Ed Smiley
 *
 * @todo we should move the AllChoices into a resource after Struts 1.1 --Ed
 */
public class AllChoices
{
  public static String[] itemAccess =
  {
    "Random access to each question from a Table of Contents page listing all questions.",
    "Random access to each part from a Table of Contents page listing all parts.",
    "Sequential Access to questions with no return and no Table of Contents page."
  };
  public static String[] itemBookmarking =
  {
    "There is no bookmarking.",
    "Students can bookmark questions and return to these questions later from a Bookmark page."
  };
  public static String[] displayChunking =
  {
    "Each question is on a separate Web page.",
    "Each Part is on a separate Web page.",
    "Each Part can be divided to be displayed on multiple web pages.",
    "The complete assessment is displayed on one Web page."
  };
  public static String[] multiPartAllowed =
  {
    "The assessment can have only one Part.",
    "The assessment can contain more than one Part."
  };
  public static String[] lateHandlingLong =
  {
    "Late submissions will be accepted, but will be marked LATE.",
    "Late submissions will not be accepted.",
    "Late submissions will be accepted and will not be marked."
  };
  public static String[] lateHandlingShort =
  { "Allowed-Tagged.", "Not Allowed.", "Allowed-Not Tagged." };
  public static String[] submissionsSavedLong =
  {
    "Save first submission by student.", "Save last submission by student.",
    "Save all submissions by student."
  };
  public static String[] submissionsSavedShort = { "First.", "Last.", "All." };
  public static String[] numberSubmissionsShort =
  { "Unlimited", "Only ONE.", "Multiple submissions." };
  public static String[] numberSubmissionsLong =
  {
    "Unlimited", "Only ONE submission is allowed.",
    "Multiple submissions are allowed:"
  };
  public static String[] releaseType =
  {
    "Immediate", "On date:",
    "On completion of a given assignment with a given score:"
  };
  public static String[] retractType =
  { "Immediate", "Never", "On date:", "Upon Submission of this Assessment" };
  public static String[] passwordAccess =
  {
    "No secondary password is needed to take the assessment.",
    "A secondary password is needed. Enter Password:"
  };
  public static String[] ipAccessType =
  {
    "Students at any IP Address can access the assessment.",
    "Only students at these IP addresses and ranges can take assessment:",
    "Students at these IP addresses and ranges CANNOT take assessment:"
  };
  public static String[] distributionGroup =
  { "Instructor", "TA", "Section Leader", "Testee", "Gradebook", "Admin" }; // Should this be based on roles instead?
  public static String[] distributionType =
  {
    "Anonymous responses, scores, and comments for each testee",
    "Individual responses, scores, and/or comments for each testee",
    "Responses, scores, and/or comments for the testee",
    "Anonymous aggregate statistics"
  };
}
