/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.business.entity.constants;

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
 * This class contains qti tag names and attribute names frequently used
 * in the Java code.
 */
public class AuthoringConstantStrings
{

  //Item Types
  public static String MATCHING = "Matching";
  public static String FIB = "Fill In the Blank";
  public static String MCMC = "Multiple Correct Answer";
  public static String MCSC = "Multiple Choice";
  public static String TF = "True False";
  public static String SURVEY = "Multiple Choice Survey";
  public static String ESSAY = "Short Answers/Essay";
  public static String ESSAY_ALT = "Essay";
  public static String AUDIO = "Audio Recording";
  public static String FILE = "File Upload";

  // "Unknown Type" is a placeholder for the invalid '0' , "Unused Type" is an alternate MCMC
  public static String[] itemTypes =
  { "Unknown Type", "Unused Type", MCMC, SURVEY, TF, ESSAY, FILE, AUDIO, FIB, MATCHING };


	// Feedback Type
	public static String FEEDBACKTYPE_IMMEDIATE = "IMMEDIATE";
	public static String FEEDBACKTYPE_DATED = "DATED";
	public static String FEEDBACKTYPE_NONE = "NONE";
}


