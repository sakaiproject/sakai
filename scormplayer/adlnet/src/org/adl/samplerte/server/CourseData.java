/*******************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you 
** ("Licensee") a non-exclusive, royalty free, license to use, modify and 
** redistribute this software in source and binary code form, provided that 
** i) this copyright notice and license appear on all copies of the software; 
** and ii) Licensee does not utilize the software in a manner which is 
** disparaging to ADL Co-Lab Hub.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL 
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING 
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE 
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS 
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO 
** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, 
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE 
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE 
** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH 
** DAMAGES.
**
*******************************************************************************/

package org.adl.samplerte.server;

/**
 * Encapsulation of information required for launch.<br><br>
 * 
 * <strong>Filename:</strong> CourseData.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * The <code>CourseData</code> encapsulates the information about a specific
 * course returned from the from the Sample RTE Database.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM Sample RTE<br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * All fields are purposefully public to allow immediate access to known data
 * elements.<br><br>
 * 
 * <strong>Known Problems:</strong><br><br>
 * 
 * <strong>Side Effects:</strong><br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS 1.0
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 * 
 * @author ADL Technical Team
 */ 
public class CourseData
{
   /**
    * The unique course identifier.  This is the identifier used internally
    * by the Sample RTE.
    */ 
   public String mCourseID = null;

   /**
    * The course title.  This is the title as defined by the &lt;title&gt;
    * sub-element of the &lt;organization&gt; element in the imsmanifest.xml
    * file
    */ 
   public String mCourseTitle = null;

   /**
    * The course import date and time.  This is the date and time that the SCORM
    * 2004 3rd Edition Content Aggregation Package was imported into the Sample RTE    
    */ 
   public String mImportDateTime = null;

   /**
    * The satisfied value for the course.
    */ 
   public String mSatisfied = null;

   /**
    * The measure for the course.
    */ 
   public String mMeasure = null;

   /**
    * The completion status for the course.    
    */ 
   public String mCompleted = null;
   
   /**
    * The start indicator for the course.
    */
   public boolean mStart = false;
   
   /**
    * The view TOC indicator for the course.
    */
   public boolean mTOC = false;
   
   /**
    * The suspend indicator for the course.
    */
   public boolean mSuspend = false;
}


