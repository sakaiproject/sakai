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
******************************************************************************/

package org.adl.datamodels;

/**
 * Defines run-time data model utilities.
 * <br><br>
 * 
 * <strong>Filename:</strong> DMTimeUtility.java<br><br>
 * 
 * <strong>Description:</strong>This file contains utility methods used
 * by the run-time datamodel.<br><br>
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
public class DMTimeUtility 
{
   /**
    * This method calls the timeStringParse method to convert a time interval 
    * string to integers for the year, month, day, hour, minute, second and 
    * decimal portion of second.  The returned integers are added and then 
    * converted back to a string which is returned.
    *
    * @param iTimeOne The String representation of a datamodel time interval.
    * 
    * @param iTimeTwo The String representation of a datamodel time interval.
    * 
    * @return A String representing the addition of the two input parameters.
    * 
    */
   public static String add(String iTimeOne, String iTimeTwo)
   {
      // Possible formats for input srings
      // P[yY][mM][dD][T[hH][mM][s[.s]S] 
      // P1Y3M2DT3H
      // PT3H5M

      String mTimeString = null;
      int multiple = 1;
      int[] mFirstTime = new int[7];
      int[] mSecondTime = new int[7];

      for (int i = 0; i < 7; i++)
      {
         mFirstTime[i] = 0;
         mSecondTime[i] = 0;
      }

      timeStringParse(iTimeOne, mFirstTime); 
      timeStringParse(iTimeTwo, mSecondTime);

      // add first and second time arrays  
      for (int i = 0; i < 7; i++)
      {
         mFirstTime[i] += mSecondTime[i];
      }

      // adjust seconds, minutes, hours, and days if addition
      // results in too large a number
      if ( mFirstTime[6] > 99 )
      {
         multiple = mFirstTime[6] / 100;
         mFirstTime[6] = mFirstTime[6] % 100;
         mFirstTime[5] += multiple;
      }

      if ( mFirstTime[5] > 59 )
      {
         multiple = mFirstTime[5] / 60;
         mFirstTime[5] = mFirstTime[5] % 60;
         mFirstTime[4] += multiple;
      }
      if ( mFirstTime[4] > 59 )
      {
         multiple = mFirstTime[4] / 60;
         mFirstTime[4] = mFirstTime[4] % 60;
         mFirstTime[3] += multiple;
      }

      if ( mFirstTime[3] > 23 )
      {
         multiple = mFirstTime[3] / 24;
         mFirstTime[3] = mFirstTime[3] % 24;
         mFirstTime[2] += multiple;
      }


      // create the new timeInterval string
      mTimeString = "P";
      if ( mFirstTime[0] != 0 )
      {
         Integer tempInt = new Integer(mFirstTime[0]);
         mTimeString +=  tempInt.toString();
         mTimeString += "Y";
      }
      if ( mFirstTime[1] != 0 )
      {
         Integer tempInt = new Integer(mFirstTime[1]);
         mTimeString +=  tempInt.toString();
         mTimeString +=  "M";
      }

      if ( mFirstTime[2] != 0 )
      {
         Integer tempInt = new Integer(mFirstTime[2]);
         mTimeString +=  tempInt.toString();
         mTimeString += "D";
      }

      if ( ( mFirstTime[3] != 0 ) || ( mFirstTime[4] != 0 ) 
           || ( mFirstTime[5] != 0 ) || (mFirstTime[6] != 0) )
      {
         mTimeString +=  "T";
      }

      if ( mFirstTime[3] != 0 )
      {
         Integer tempInt = new Integer(mFirstTime[3]);
         mTimeString +=  tempInt.toString();
         mTimeString +=  "H";
      }

      if ( mFirstTime[4] != 0 )
      {
         Integer tempInt = new Integer(mFirstTime[4]);
         mTimeString +=  tempInt.toString();
         mTimeString += "M";
      }

      if ( mFirstTime[5] != 0 )
      {
         Integer tempInt = new Integer(mFirstTime[5]);
         mTimeString +=  tempInt.toString();
      }

      if ( mFirstTime[6] != 0 )
      {
         if ( mFirstTime[5] == 0 )
         {
            mTimeString += "0";
         }
         mTimeString += ".";
         if ( mFirstTime[6] < 10 )
         {
            mTimeString += "0";
         }
         Integer tempInt2 = new Integer(mFirstTime[6]);
         mTimeString +=  tempInt2.toString();
      }
      if ( ( mFirstTime[5] != 0 ) || ( mFirstTime[6] != 0 ) )
      {
         mTimeString += "S";
      }

      return mTimeString;

   }

   /**
    * This method takes the input String parameter which represents
    * a datamodel time interval string and converts it to an array of integers.
    * The array integers represent the years, months, days, hours, minutes, 
    * seconds and decimal portions of seconds of the input time interval 
    * string.  Any on of the time interval sections may be missing
    * 
    * @param iTime The String representation of a datamodel time interval.
    * 
    * @param ioArray An array of integers.
    * 
    */
    private static void timeStringParse(String iTime, int[] ioArray)    
   {
      // P[yY][mM][dD][T[hH][mM][s[.s]S] 
      // P1Y3M2DT3H
      // PT3H5M

      String mInitArray[];
      String mTempArray2[] = { "0", "0", "0" }; 
      String mDate = "0";
      String mTime = "0";

      // make sure the string is not null
      if ( iTime == null )
      {
          return;
      }
         
      // make sure that the string has the right format to split
      if ( ( iTime.length() == 1 ) || ( iTime.indexOf("P") == -1 ) )
      {
          return;
      }

      try
      {
         mInitArray = iTime.split("P");

         // T is present so split into day and time part
         // when "P" is first character in string, rest of string goes in
         // array index 1
         if ( mInitArray[1].indexOf("T") != -1 )
         {
            mTempArray2 = mInitArray[1].split("T");
            mDate =  mTempArray2[0];
            mTime =  mTempArray2[1];
         }
         else
         {
            mDate =  mInitArray[1];
         }

         // Y is present so get year
         if ( mDate.indexOf("Y") != -1 )
         {
            mInitArray = mDate.split("Y");
            Integer tempInt = new Integer(mInitArray[0]);
            ioArray[0] = tempInt.intValue();
         }
         else
         {
            mInitArray[1] = mDate;
         }

         // M is present so get month
         if ( mDate.indexOf("M") != -1 )
         {
            mTempArray2 = mInitArray[1].split("M");
            Integer tempInt = new Integer(mTempArray2[0]);
            ioArray[1] = tempInt.intValue();
         }
         else
         {
            if ( mInitArray.length != 2 )
            {
               mTempArray2[1] = "";
            }
            else
            {               
               mTempArray2[1] = mInitArray[1];
            }
         }

         // D is present so get day
         if ( mDate.indexOf("D") != -1 )
         {
            mInitArray = mTempArray2[1].split("D");
            Integer tempInt = new Integer(mInitArray[0]);
            ioArray[2] = tempInt.intValue();
         }
         else
         {
            mInitArray = new String [2];
         }

         // if string has time portion
         if ( !mTime.equals("0") )
         {
            // H is present so get hour
            if ( mTime.indexOf("H") != -1 )
            {
               mInitArray =  mTime.split("H");
               Integer tempInt = new Integer(mInitArray[0]);
               ioArray[3] = tempInt.intValue();
            }
            else
            {
               mInitArray[1] = mTime;
            }

            // M is present so get minute
            if ( mTime.indexOf("M") != -1 )
            {
               mTempArray2 = mInitArray[1].split("M");
               Integer tempInt = new Integer(mTempArray2[0]);
               ioArray[4] = tempInt.intValue();
            }
            else
            {
               if ( mInitArray.length != 2 )
               {
                  mTempArray2[1] = "";
               }
               else
               {               
                  mTempArray2[1] = mInitArray[1];
               }
            }

            // S is present so get seconds
            if ( mTime.indexOf("S") != -1 )
            {
               mInitArray = mTempArray2[1].split("S");

               if ( mTime.indexOf(".") != -1)
               {
                  // split requires this regular expression for "."
                  mTempArray2 = mInitArray[0].split("[.]");

                  // correct for case such as ".2"
                  if ( mTempArray2[1].length() == 1 )
                  {
                     mTempArray2[1] = mTempArray2[1] + "0";
                  }

                  Integer tempInt2 = new Integer(mTempArray2[1]);
                  ioArray[6] = tempInt2.intValue();
                  Integer tempInt = new Integer(mTempArray2[0]);
                  ioArray[5] = tempInt.intValue();
               }
               else
               {
                  Integer tempInt = new Integer(mInitArray[0]);
                  ioArray[5] = tempInt.intValue();
               }
            }
         }
      }
      catch (NumberFormatException nfe)
      {
         // Do Nothing
      }

      return;
   }

}
