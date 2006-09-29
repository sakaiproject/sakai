/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/qti/constants/AuthoringConstantStrings.java $
 * $Id: AuthoringConstantStrings.java 9274 2006-05-10 22:50:48Z daisyf@stanford.edu $
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
  public static final String UNLIMITED_SUBMISSIONS = "9999";
  
  public static final String ANONYMOUS = rb.getString("anonymous"); // Anonymous Users
  public static final String AUTHENTICATED = rb.getString("authenticated"); // Authenticated Users
  
  //Item Types
  public static final String MATCHING = rb.getString("matching"); //"Matching"
  public static final String FIB = rb.getString("fib"); //"Fill In the Blank"
  public static final String FIN = rb.getString("fin"); //"Numeric Response"
  public static final String MCMC = rb.getString("mcmc"); //"Multiple Correct Answer"
  public static final String MCSC = rb.getString("mcsc"); // "Multiple Choice"
  public static final String TF = rb.getString("tf"); // "True False"
  public static final String SURVEY = rb.getString("survey"); // "Multiple Choice Survey"
  public static final String ESSAY = rb.getString("essay"); // "Short Answers/Essay"
  public static final String ESSAY_ALT = rb.getString("essay_alt"); // "Essay"
  public static final String AUDIO = rb.getString("audio"); // "Audio Recording"
  public static final String FILE = rb.getString("file"); // "File Upload"

  // "Unknown Type" is a placeholder for the invalid '0' , "Unused Type" is an alternate MCMC
  
  // Lydia 9/29/2006 : added "" before FIN, because Diego used 11 as the type.  Rather than changing the conversion script for SAM_TYPE_D table, 
  // I'm just adding an "" to make FIN the itemTypes[11].  This is used in ItemTypeExtractionStrategy.getValidType()
  
  public static String[] itemTypes =
  { "Unknown Type", MCSC, MCMC, SURVEY, TF, ESSAY, FILE, AUDIO, FIB, MATCHING,"", FIN };
  

  // Feedback Type
  public static final String FEEDBACKTYPE_IMMEDIATE = "IMMEDIATE";
  public static final String FEEDBACKTYPE_DATED = "DATED";
  public static final String FEEDBACKTYPE_NONE = "NONE";
}


