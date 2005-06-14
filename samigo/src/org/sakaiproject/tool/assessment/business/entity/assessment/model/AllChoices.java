/*
 *                       Navigo Software License
 *
 * Copyright 2003, Trustees of Indiana University, The Regents of the University
 * of Michigan, and Stanford University, all rights reserved.
 *
 * This work, including software, documents, or other related items (the
 * "Software"), is being provided by the copyright holder(s) subject to the
 * terms of the Navigo Software License. By obtaining, using and/or copying this
 * Software, you agree that you have read, understand, and will comply with the
 * following terms and conditions of the Navigo Software License:
 *
 * Permission to use, copy, modify, and distribute this Software and its
 * documentation, with or without modification, for any purpose and without fee
 * or royalty is hereby granted, provided that you include the following on ALL
 * copies of the Software or portions thereof, including modifications or
 * derivatives, that you make:
 *
 *    The full text of the Navigo Software License in a location viewable to
 *    users of the redistributed or derivative work.
 *
 *    Any pre-existing intellectual property disclaimers, notices, or terms and
 *    conditions. If none exist, a short notice similar to the following should
 *    be used within the body of any redistributed or derivative Software:
 *    "Copyright 2003, Trustees of Indiana University, The Regents of the
 *    University of Michigan and Stanford University, all rights reserved."
 *
 *    Notice of any changes or modifications to the Navigo Software, including
 *    the date the changes were made.
 *
 *    Any modified software must be distributed in such as manner as to avoid
 *    any confusion with the original Navigo Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * The name and trademarks of copyright holder(s) and/or Indiana University,
 * The University of Michigan, Stanford University, or Navigo may NOT be used
 * in advertising or publicity pertaining to the Software without specific,
 * written prior permission. Title to copyright in the Software and any
 * associated documentation will at all times remain with the copyright holders.
 * The export of software employing encryption technology may require a specific
 * license from the United States Government. It is the responsibility of any
 * person or organization contemplating export to obtain such a license before
 * exporting this Software.
 */

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
