/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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


package org.sakaiproject.tool.assessment.qti.constants;

import java.util.ResourceBundle;

/**
 * A set of strings for QTI XML item characteristics
 *
 * @author rshastri
 */
/*
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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
 */

/**
 * This class contains authoring strings and names frequently used
 * in the Java code for QTI authoring.
 */
public class AuthoringConstantStrings
{
  private static ResourceBundle rb = ResourceBundle.getBundle("org.sakaiproject.tool.assessment.bundle.Messages");

  //
  public static String UNLIMITED_SUBMISSIONS = "9999";
  
  public static String ANONYMOUS = rb.getString("anonymous"); // Anonymous Users
  public static String AUTHENTICATED = rb.getString("authenticated"); // Authenticated Users
  
  //Item Types
  public static String MATCHING = rb.getString("matching"); //"Matching"
  public static String FIB = rb.getString("fib"); //"Fill In the Blank"
  public static String MCMC = rb.getString("mcmc"); //"Multiple Correct Answer"
  public static String MCSC = rb.getString("mcsc"); // "Multiple Choice"
  public static String TF = rb.getString("tf"); // "True False"
  public static String SURVEY = rb.getString("survey"); // "Multiple Choice Survey"
  public static String ESSAY = rb.getString("essay"); // "Short Answers/Essay"
  public static String ESSAY_ALT = rb.getString("essay_alt"); // "Essay"
  public static String AUDIO = rb.getString("audio"); // "Audio Recording"
  public static String FILE = rb.getString("file"); // "File Upload"

  // "Unknown Type" is a placeholder for the invalid '0' , "Unused Type" is an alternate MCMC
  public static String[] itemTypes =
  { "Unknown Type", MCSC, MCMC, SURVEY, TF, ESSAY, FILE, AUDIO, FIB, MATCHING };


  // Feedback Type
  public static String FEEDBACKTYPE_IMMEDIATE = "IMMEDIATE";
  public static String FEEDBACKTYPE_DATED = "DATED";
  public static String FEEDBACKTYPE_NONE = "NONE";
}


