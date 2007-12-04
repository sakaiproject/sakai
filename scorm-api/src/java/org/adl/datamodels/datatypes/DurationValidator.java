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

import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMTypeValidator;
import java.io.Serializable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * <strong>Filename:</strong> DurationValidator.java <br><br>
 * 
 * <strong>Description:</strong> The DurationValidator provides support for 
 * the Duration data type, as defined in the SCORM 2004 RTE Book
 *  
 * @author ADL Technical Team
 */
public class DurationValidator extends DMTypeValidator implements Serializable
{

   /**
    * Default constructor required for serialization support.
    */
   public DurationValidator() 
   {
      // The default constructor has no explicit functionallity defined.   
   }

   /**
    * Compares two valid data model elements for equality.
    * 
    * @param iFirst  The first value being compared.
    * 
    * @param iSecond The second value being compared.
    * 
    * @param iDelimiters The common set of delimiters associated with the
    *        values being compared.
    * 
    * @return <code>true</code> if the two values are equal, otherwise
    *         <code>false</code>.
    */
   public boolean compare(String iFirst, String iSecond, Vector iDelimiters)
   {
      boolean equal = true;

      // Cannot compare nothing
      if ( iFirst == null || iSecond == null )
      {
         // The first string is an invalid URI
         equal = false;
      }
      else if ( iDelimiters != null)
      {
         // There should not be any delimiters for this type
         equal = false;
      }
      else
      {
         Double secs1 = numSec(iFirst);
         Double secs2 = numSec(iSecond);

         if ( secs1 != null && secs2 != null )
         {
            equal =                                                             
               Double.compare(secs1.doubleValue(), secs2.doubleValue()) == 0;
         }
         else
         {
            equal = false;
         }
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
      int valid = DMErrorCodes.TYPE_MISMATCH;

      if ( iValue != null )
      {
         String durExp = 
            "P(([0-9]+Y)?([0-9]+M)?([0-9]+D)?)?" +
            "(T([0-9]+H)?([0-9]+M)?([0-9]+(\\.[0-9]{1,2})?S)?)?";

         Pattern pattern = Pattern.compile(durExp);
         Matcher matcher = pattern.matcher(iValue);

         if ( matcher.matches() )
         {
            if ( !(iValue.endsWith("P")) && !(iValue.endsWith("T")) &&
                 iValue.length() != 1 )
            {
               valid = DMErrorCodes.NO_ERROR;
            }
         }
      }
      else
      {
         // A null value can never be valid
         valid = DMErrorCodes.UNKNOWN_EXCEPTION;
      }

      return valid;
   }

   /**
    * Provides the number of seconds represented by the given duration.
    * The number of seconds does not include leap years.
    * 
    * @param iValue  Describes the duration being considered
    * 
    * @return The number of seconds represented by the duration.
    */
   private Double numSec(String iValue)
   {
      long secs = 0;
      Double total = null;

      // The current index must be 1 -- immediatly following the 'P'
      int curIdx = 1;

      // Look for the 'T' seperator
      int tIdx = iValue.indexOf('T');

      int nextIdx = -1;
      String val = null;
      boolean done = false;

      // Find the number of years
      nextIdx = iValue.indexOf('Y');

      if ( nextIdx != -1 )
      {
         // Get the value to be added
         val = iValue.substring(curIdx, nextIdx);

         // Increment past the seperator
         curIdx = nextIdx + 1;

         // 31536000 seconds in each year
         try
         {
            secs = secs + Long.parseLong (val, 10) * 31536000L;
         }
         catch ( NumberFormatException nfe )
         {
            done = true;
         }
      }

      // Find the number of Months if they are included
      nextIdx = iValue.indexOf('M');

      if ( tIdx > -1 && nextIdx >= tIdx )
      {
         nextIdx = -1;
      }

      if ( !done && nextIdx != -1 )
      {
         // Get the value to be added
         val = iValue.substring(curIdx, nextIdx);

         // Increment past the seperator
         curIdx = nextIdx + 1;

         // 2628029 seconds in each month (assumes 30.417 days / month)
         try
         {
            secs = secs + Long.parseLong (val, 10) * 2628029L;
         }
         catch ( NumberFormatException nfe )
         {
            done = true;
         }
      }

      // Find the number of Days
      nextIdx = iValue.indexOf('D');

      if ( !done && nextIdx != -1 )
      {
         // Get the value to be added
         val = iValue.substring(curIdx, nextIdx);

         // Increment past the seperator
         curIdx = nextIdx + 1;

         // 86400 seconds in each day
         try
         {
            secs = secs + Long.parseLong (val, 10) * 86400L;
         }
         catch ( NumberFormatException nfe )
         {
            done = true;
         }
      }

      // Find the number of Hours
      nextIdx = iValue.indexOf('H');

      if ( !done && nextIdx != -1 )
      {
         // Get the value to be added
         val = iValue.substring(curIdx, nextIdx);

         // Make sure we removed the 'T'
         if ( val.startsWith("T") )
         {
            val = val.substring(1);
         }
         // Increment past the seperator
         curIdx = nextIdx + 1;

         // 3600 seconds in each hour
         try
         {
            secs = secs + Long.parseLong (val, 10) * 3600L;
         }
         catch ( NumberFormatException e )
         {
            done = true;
         }
      }

      // Find the number of Minutes if they are included
      nextIdx = -1;
      if ( tIdx != -1 )
      {
         nextIdx = iValue.indexOf('M', tIdx);
      }

      if ( !done && nextIdx != -1 )
      {
         // Get the value to be added
         val = iValue.substring(curIdx, nextIdx);

         // Make sure we removed the 'T'
         if ( val.startsWith("T") )
         {
            val = val.substring(1);
         }

         // Increment past the seperator
         curIdx = nextIdx + 1;

         // 60 seconds in each Minute
         try
         {
            secs = secs + Long.parseLong (val, 10) * 60L;
         }
         catch ( NumberFormatException nfe )
         {
            done = true;
         }
      }

      double subSec = Double.NaN;
      nextIdx = iValue.indexOf('S');

      if ( !done && nextIdx != -1 )
      {
         // Get the value to be added
         val = iValue.substring(curIdx, nextIdx);

         // Make sure we removed the 'T'
         if ( val.startsWith("T") )
         {
            val = val.substring(1);
         }

         try
         {
            subSec = Double.parseDouble(val);
         }
         catch ( NumberFormatException nfe )
         {
            subSec = Double.NaN;
            done = true;
         }
      }

      // Add all of the seconds together
      if ( !done && secs >= 0 )
      {
         double sec = Double.parseDouble(Long.toString(secs, 10));

         if ( Double.compare(Double.NaN, subSec) != 0 )
         {
            subSec = Math.floor(subSec * 100.0) / 100.0;

            total = new Double(subSec + sec);
         }
         else
         {
            total = new Double(sec);
         }
      }
      return total;
   }                           

}