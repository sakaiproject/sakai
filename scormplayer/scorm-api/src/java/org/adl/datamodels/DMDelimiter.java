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

import java.io.Serializable;

/**
 * 
 * <strong>Filename:</strong> DMDelimiter.java<br><br>
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
public class DMDelimiter implements Serializable
{
	private long id;
	

   /**
    * Describes the properties of this delimiter.
    */
   public DMDelimiterDescriptor mDescription = null;


   /**
    * Describes the value of this delimiter.
    */
   public String mValue = null;


   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Public Methods
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   /**
    * Default constructor required for serialization support.
    */
   public DMDelimiter() 
   {
      // Empty constructor - no defined functionallity   
   }


   /**
    * Creates a <code>DMDelimiter</code> object that exhibits the qualities
    * described in its <code>DMDelimiterDescriptor</code>.
    * 
    * @param iDescription Describes this <code>DMDelimiter</code>
    */
   public DMDelimiter(DMDelimiterDescriptor iDescription)
   {
      mDescription = iDescription;
   }

   public DMDelimiterDescriptor getDescription() {
	   return mDescription;
   }
   

   /**
    * Provides the dot-notation binding for this delimiter.
    *
    * @param iDelimiters Indicates if the data model element's default
    *                    delimiters should be included in the return string.    
    *
    * @return The dot-notation <code>String</code> corresponding to this
    *         delimiter.
    */
   public String getDotNotation(boolean iDelimiters)
   {
      String dot = "";

      if ( mValue != null )
      {
         dot = "{" + mDescription.mName + "=" + mValue + "}";
      }
      else
      {
         if ( iDelimiters )
         {
            dot = "{" + mDescription.mName + "=" + mDescription.mDefault + "}";
         }
      }

      return dot;
   }

} // end DMDelimiter
