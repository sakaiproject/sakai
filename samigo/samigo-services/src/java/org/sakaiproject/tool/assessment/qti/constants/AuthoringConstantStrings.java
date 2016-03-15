/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
 * A set of strings for QTI XML item characteristics
 *
 * @author rshastri
 */

/**
 * This class contains authoring strings and names frequently used
 * in the Java code for QTI authoring.
 */
public class AuthoringConstantStrings
{
	
	/*
org.sakaiproject.tool.assessment.bundle.Messages:


anonymous = Anonymous Users
authenticated = Authenticated Users

# AssessmentFacadeQueries.java
new_section = this section is added when a new assessment is created

# AuthoringConstantStrings.java
matching = Matching
fib = Fill In the Blank
fin= Numeric Response
mcmc = Multiple Correct Answer
mcsc = Multiple Choice
tf = True False
survey = Multiple Choice Survey
essay = Short Answers/Essay
essay_alt = Essay
audio = Audio Recording
file = File Upload
*/

  //private static ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.Messages");
  //BUT ALL OF THEM ARE STATIC AND FINAL...  THIS IS NOT A GOOD COMBINATION WITH RESOURCELOADES...
  //
  public static final String UNLIMITED_SUBMISSIONS = "9999";
  
  // use static final string instead of rb.getString() because samigo-service is deployed in shared/lib and has no access to sakai-util, which contains the REsourceLoader
  // These are not strings displayed in UI. 
  
  public static final String ANONYMOUS = "Anonymous Users";
  public static final String AUTHENTICATED = "Authenticated Users";
  
  //Item Types
  public static final String MATCHING =  "Matching";
  public static final String FIB =  "Fill In the Blank";
  public static final String FIN = "Numeric Response";
  public static final String MCMC = "Multiple Correct Answer";
  public static final String MCMCSS = "Multiple Correct Single Selection";
  public static final String MCSC = "Multiple Choice";
  public static final String TF = "True False";
  public static final String SURVEY = "Multiple Choice Survey";
  public static final String ESSAY = "Short Answers/Essay";
  public static final String ESSAY_ALT = "Essay";
  public static final String AUDIO = "Audio Recording";
  public static final String FILE = "File Upload";
  public static final String EMI = "Extended Matching Items";
  public static final String MATRIX = "Survey Matrix";
  public static final String CALCQ =  "Calculated Question"; // CALCULATED_QUESTION - 15
  public static final String IMAGMQ =  "Image Map Question"; // IMAGEMAP_QUESTION - 16

  /*
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
*/
  
  
  // "Unknown Type" is a placeholder for the invalid '0' , "Unused Type" is an alternate MCMC
  
  // Lydia 9/29/2006 : added "" before FIN, because Diego used 11 as the type.  Rather than changing the conversion script for SAM_TYPE_D table, 
  // I'm just adding an "" to make FIN the itemTypes[11].  This is used in ItemTypeExtractionStrategy.getValidType()
  
  public static final String[] itemTypes =
	  { "Unknown Type", MCSC, MCMC, SURVEY, TF, ESSAY, FILE, AUDIO, FIB, MATCHING,"", FIN, MCMCSS, MATRIX, EMI, CALCQ, IMAGMQ};
  

  // Feedback Type
  public static final String FEEDBACKTYPE_IMMEDIATE = "IMMEDIATE";
  public static final String FEEDBACKTYPE_DATED = "DATED";
  public static final String FEEDBACKTYPE_NONE = "NONE";
}


