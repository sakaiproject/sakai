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

import org.adl.datamodels.DMTypeValidator;
import org.adl.datamodels.DMErrorCodes;
import java.io.Serializable;
import java.util.Vector;
import java.util.GregorianCalendar;
import java.util.Calendar;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

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
public class DateTimeValidatorImpl extends DateTimeValidator implements Serializable
{

   /**
    * A constant holding the second field upper bound: 59
    */
   private static final int SECOND_UPPER_BOUND = 59;

   /**
    * A constant holding the minute field upper bound: 59
    */
   private static final int MIN_UPPER_BOUND = 59;

   /**
    * A constant holding the hour field upper bound: 23
    */
   private static final int HOUR_UPPER_BOUND = 23;

   /**
    * A constant holding the month field upper bound: 11
    */
   private static final int MONTH_UPPER_BOUND = 11;

   /**
    * A constant holding the year field lower bound: 1970 
    */
   private static final int YEAR_LOWER_BOUND = 1970;

   /**
    * A constant holding the year field upper bound: 2038
    */
   private static final int YEAR_UPPER_BOUND = 2038;


   /**
    * Indicates if subseconds should be allowed -- and tested
    */
   private boolean mIncludeSubSecs = true;


   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Constructors
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   // For hibernate
   public DateTimeValidatorImpl() { super(); }
   
   /**
    * Constructor for this type
    * 
    * @param iInclude Indicates if subseconds should be tested for in this 
    * validator.
    */
   public DateTimeValidatorImpl(boolean iInclude)
   {
	  super(iInclude);
      mIncludeSubSecs = iInclude; 
   }


   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Public Methods
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   /**
    * Compares two valid data model elements for equality.
    * 
    * @param iFirst  The first value being compared.
    * 
    * @param iSecond The second value being compared.
    * 
    * @param iDelimiters The common set of delimiters associated with the
    * values being compared.
    * 
    * @return Returns <code>true</code> if the two values are equal, otherwise
    *         <code>false</code>.
    */
   public boolean compare(String iFirst, String iSecond, Vector iDelimiters)
   {

      boolean equal = true;

      DateTimeFormatter dtp = ISODateTimeFormat.dateTimeParser();

      try
      {
         // Parse the first string and remove the sub-seconds
         DateTime dt1 = dtp.parseDateTime(iFirst);
         dt1 = new DateTime(dt1.getYear(), 
                            dt1.getMonthOfYear(),
                            dt1.getDayOfMonth(),
                            dt1.getHourOfDay(),
                            dt1.getMinuteOfHour(),
                            dt1.getSecondOfMinute(),
                            0);

         // Parse the second string and remove the sub-seconds
         DateTime dt2 = dtp.parseDateTime(iSecond);
         dt2 = new DateTime(dt2.getYear(), 
                            dt2.getMonthOfYear(),
                            dt2.getDayOfMonth(),
                            dt2.getHourOfDay(),
                            dt2.getMinuteOfHour(),
                            dt2.getSecondOfMinute(),
                            0);

         equal = dt1.equals(dt2);
      }
      catch (Exception e)
      {
         // String format error -- these cannot be equal
         equal = false;
      }

      return equal;
   }

   /**
    * Validates the provided string against a known format.
    * 
    * @param iValue The value being validated.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public int validate(String iValue)
   {
      // Assume the value is valid
      int valid = DMErrorCodes.NO_ERROR;

      int idx = -1;
      boolean done = false;

      int year = 0;
      int month = 0;
      int day = 0;

      int tempLength = 0;

      // Check for Null case
      if ( iValue == null )
      {
         // A null value can never be valid
         valid = DMErrorCodes.UNKNOWN_EXCEPTION;
         done = true;
      }

      if ( !done )
      {
         tempLength = iValue.length();

         // First ensure that the year is present
         if ( tempLength < 4 )
         {
            valid = DMErrorCodes.TYPE_MISMATCH;
            return valid;
         }

         try
         {
            year = Integer.parseInt( iValue.substring(0, 4) );

            if ( year < YEAR_LOWER_BOUND || year > YEAR_UPPER_BOUND )
            {
               valid = DMErrorCodes.TYPE_MISMATCH;
               return valid;
            }
         }
         catch ( Exception e )
         {
            valid = DMErrorCodes.TYPE_MISMATCH;
            done = true;
         }

         if ( !done )
         {
            idx += 5;

            // Make sure there is something else to look at
            if ( idx != iValue.length() )
            {
               // Check for a month
               if ( iValue.charAt(idx) == '-' )
               {
                  idx++;

                  // Found month, test range
                  month = 0;
                  try
                  {
                     month = 
                     Integer.parseInt( iValue.substring(idx, idx + 2) );

                     month -= 1;

                     if ( month < 0 || month > MONTH_UPPER_BOUND )
                     {
                        valid = DMErrorCodes.TYPE_MISMATCH;
                        done = true;
                     }
                  }
                  catch ( Exception e )
                  {
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     done = true;
                  }
               }
               else
               {
                  valid = DMErrorCodes.TYPE_MISMATCH;
                  done = true;
               }
            }
            else
            {
               // NO Error because only the year is valid
               valid = DMErrorCodes.NO_ERROR;
               done = true;
            }
         }

         // Check for day
         if ( !done )
         {
            idx += 2;

            if ( idx != iValue.length() )
            {
               if ( iValue.charAt(idx) == '-' )
               {
                  idx++;

                  // Found day, test range
                  try
                  {
                     day = Integer.parseInt( iValue.substring(idx, idx + 2) );

                     // Create a calendar for the indicated year and month
                     Calendar cal = new GregorianCalendar(year, month, 1);

                     // Get the number of days in that month
                     int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                     if ( day < 1 || day > days )
                     {
                        valid = DMErrorCodes.TYPE_MISMATCH;
                        done = true;
                     }
                     else
                     {
                        // Valid day, but need to check if we are at the end
                        idx += 2;
                        if ( idx == iValue.length() )
                        {
                           done = true;
                        }
                        else
                        {
                           // Make sure the next section is describing a time
                           if ( iValue.charAt(idx) != 'T' )
                           {
                              valid = DMErrorCodes.TYPE_MISMATCH;
                              done = true;
                           }
                        }
                     }
                  }
                  catch ( Exception e )
                  {
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     done = true;
                  }
               }
               else
               {
                  valid = DMErrorCodes.TYPE_MISMATCH;
                  done = true;
               }
            }
         }
      }

      // Look for Time
      if ( !done )
      {
         // Check for a hour
         idx = iValue.indexOf("T");

         if ( idx != -1 )
         {
            idx++;

            // Found hour, test range
            int hour = -1;
            try
            {
               hour = Integer.parseInt( iValue.substring(idx, idx + 2) );

               if ( hour < 0 || hour > HOUR_UPPER_BOUND )
               {
                  valid = DMErrorCodes.TYPE_MISMATCH;
                  done = true;
               }
            }
            catch ( Exception e )
            {
               valid = DMErrorCodes.TYPE_MISMATCH;
               done = true;
            }

            // Check for minutes
            if ( !done )
            {
               idx += 2;

               if ( idx != iValue.length() )
               {
                  if ( iValue.charAt(idx) == ':' )
                  {
                     idx++;

                     // Found minutes, test range
                     int minutes = -1;
                     try
                     {
                        minutes = 
                        Integer.parseInt( iValue.substring(idx, idx + 2) );

                        if ( minutes < 0 || minutes > MIN_UPPER_BOUND )
                        {
                           valid = DMErrorCodes.TYPE_MISMATCH;
                           done = true;
                        }
                     }
                     catch ( Exception e )
                     {
                        valid = DMErrorCodes.TYPE_MISMATCH;
                        done = true;
                     }
                  }
                  else
                  {
                     valid = DMErrorCodes.TYPE_MISMATCH;
                     done = true;
                  }
               }
               else
               {
                  done = true;
               }

               // Check for seconds
               if ( !done )
               {
                  // Move past the minutes
                  idx += 2;

                  if ( idx != iValue.length() )
                  {
                     if ( iValue.charAt(idx) == ':' )
                     {
                        idx++;

                        // Found seconds, test range
                        int seconds = -1;
                        try
                        {
                           seconds =
                           Integer.parseInt( iValue.substring(idx, idx + 2) );

                           if ( seconds < 0 || seconds > SECOND_UPPER_BOUND )
                           {
                              valid = DMErrorCodes.TYPE_MISMATCH;
                              done = true;
                           }
                        }
                        catch ( Exception e )
                        {
                           valid = DMErrorCodes.TYPE_MISMATCH;
                           done = true;
                        }
                     }
                     else
                     {
                        valid = DMErrorCodes.TYPE_MISMATCH;
                        done = true;
                     }
                  }
                  else
                  {
                     done = true;
                  }
               }

               // Move past the seconds
               idx += 2;

               int sub = -1;
               // Check for sub-seconds
               if ( !done )
               {
                  if ( mIncludeSubSecs )
                  {
                     if ( idx != iValue.length() )
                     {
                        if ( iValue.charAt(idx) == '.' )
                        {
                           // Move past the '.'
                           idx++;
                           sub = idx;

                           int lengthCounter = 0;

                           // Found start of subseconds, try to find the end
                           while ( sub < iValue.length() &&
                                   iValue.charAt(sub) != 'Z' &&
                                   iValue.charAt(sub) != 'z' &&
                                   iValue.charAt(sub) != '+' &&
                                   iValue.charAt(sub) != '-' )
                           {
                              sub++;
                              lengthCounter++;
                           }

                           // Check if the length is out of range
                           if ( lengthCounter == 0 || lengthCounter > 2 )
                           {
                              valid = DMErrorCodes.TYPE_MISMATCH;
                              done = true;
                           }
                           else
                           {
                              try
                              {
                                 // Try to parse the integer value, if exception
                                 // is thrown, then set appropriate error code.
                                 Integer.parseInt( iValue.substring(idx, sub) );

                                 // Move up to the TZD
                                 idx = sub;
                              }
                              catch ( Exception e )
                              {
                                 valid = DMErrorCodes.TYPE_MISMATCH;
                                 done = true;
                              }
                           }
                        }
                        else
                        {
                           valid = DMErrorCodes.TYPE_MISMATCH;
                        }
                     }
                  }
               }

               // Check for Time Zone Designator
               if ( !done )
               {
                  // Check if we have anything left to check
                  if ( idx < iValue.length() )
                  {
                     if ( iValue.charAt(idx) == 'Z' ||
                          iValue.charAt(idx) == 'z' )
                     {
                        // This has to be the last character
                        if ( idx != (iValue.length() - 1) )
                        {
                           valid = DMErrorCodes.TYPE_MISMATCH;
                           done = true;
                        }

                        // We're done
                        idx = -1;
                     }
                     else if ( iValue.charAt(idx) == '-'  ||
                               iValue.charAt(idx) == '+' )
                     {
                        idx++;
                     }
                     else
                     {
                        valid = DMErrorCodes.TYPE_MISMATCH;

                        done = true;
                        idx = -1;
                     }

                     if ( idx != -1 )
                     {
                        // Check if an hour is properly defined
                        if ( iValue.length() >= (idx + 2) )
                        {
                           // Found hour, test range
                           hour = -1;
                           try
                           {
                              hour =
                              Integer.
                              parseInt(iValue.substring(idx, idx + 2));

                              if ( hour < 0 ||
                                   hour > HOUR_UPPER_BOUND )
                              {
                                 valid = DMErrorCodes.TYPE_MISMATCH;
                                 done = true;
                              }
                           }
                           catch ( Exception e )
                           {
                              valid = DMErrorCodes.TYPE_MISMATCH;
                              done = true;
                           }
                        }
                        else
                        {
                           valid = DMErrorCodes.TYPE_MISMATCH;
                           done = true;
                        }

                        // Check for minutes
                        if ( !done )
                        {
                           // Move past the minutes
                           idx += 2;

                           if ( idx != iValue.length() )
                           {
                              if ( iValue.charAt(idx) == ':' )
                              {
                                 idx++;

                                 // Found minutes, test range
                                 int minutes = -1;
                                 try
                                 {
                                    minutes =
                                    Integer.
                                    parseInt( iValue.substring(idx, idx + 2) );

                                    // Make sure we are at the end
                                    idx += 2;

                                    if ( minutes < 0 ||
                                         minutes > MIN_UPPER_BOUND ||
                                         idx != iValue.length() )
                                    {
                                       valid = DMErrorCodes.TYPE_MISMATCH;
                                    }
                                 }
                                 catch ( Exception e )
                                 {
                                    valid = DMErrorCodes.TYPE_MISMATCH;
                                 }
                              }
                              else
                              {
                                 valid = DMErrorCodes.TYPE_MISMATCH;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return valid;
   }

} // end DateTimeValidator
