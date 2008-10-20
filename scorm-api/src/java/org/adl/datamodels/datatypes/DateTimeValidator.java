/******************************************************************************
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

package org.adl.datamodels.datatypes;

import java.io.Serializable;

import org.adl.datamodels.DMTypeValidator;

/**
 * <br><br>
 * 
 * <strong>Filename:</strong> DateTimeValidator.java<br><br>
 * 
 * <strong>Description:</strong><br><br>
 * 
 * <strong>Design Issues:</strong><br><br>
 * 
 * <strong>Implementation Issues:</strong><br><br>
 * 
 * <strong>Known Problems:</strong><br><br>
 * 
 * <strong>Side Effects:</strong><br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>SCORM 2004
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class DateTimeValidator extends DMTypeValidator implements Serializable
{

   /**
    * A constant holding the second field upper bound: 59
    */
   protected static final int SECOND_UPPER_BOUND = 59;

   /**
    * A constant holding the minute field upper bound: 59
    */
   protected static final int MIN_UPPER_BOUND = 59;

   /**
    * A constant holding the hour field upper bound: 23
    */
   protected static final int HOUR_UPPER_BOUND = 23;

   /**
    * A constant holding the month field upper bound: 11
    */
   protected static final int MONTH_UPPER_BOUND = 11;

   /**
    * A constant holding the year field lower bound: 1970 
    */
   protected static final int YEAR_LOWER_BOUND = 1970;

   /**
    * A constant holding the year field upper bound: 2038
    */
   protected static final int YEAR_UPPER_BOUND = 2038;


   /**
    * Indicates if subseconds should be allowed -- and tested
    */
   protected boolean mIncludeSubSecs = true;

   
   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
   Constructors
  
  -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

  // For hibernate
  public DateTimeValidator() { }
  
  /**
   * Constructor for this type
   * 
   * @param iInclude Indicates if subseconds should be tested for in this 
   * validator.
   */
  public DateTimeValidator(boolean iInclude)
  {
     mIncludeSubSecs = iInclude; 
  }


  public int validate(String value) {
	  return -1;
  }   

} // end DateTimeValidator
