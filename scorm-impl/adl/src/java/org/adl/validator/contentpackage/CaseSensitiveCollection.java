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
package org.adl.validator.contentpackage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
/**
 * this class extends ArrayList and contains all of the hrefs in the manifest.
 * This collection of hrefs is used to determine if the same file is referenced
 * with a difference in case ("aground.jpg" and "Agroung.jpg") as this will
 * raise a warning in the logs.
 * @author 
 *
 */
public class CaseSensitiveCollection extends ArrayList implements Serializable
{
   /**
    * iterates through the collection and checks to see if the String objects
    * stored are equal except for case.  Calls the String.equalsIgnoreCase()
    * method to check the str argument passed in against the elements in the
    * collection
    * @param str String object to check against the collection
    * @return true if this is in the collection using .equalsIgnoreCase(),
    * false if it isnt in there at all
    */
   public boolean containsIgnoreCase(String str)
   {
      Iterator iter = this.iterator();

      // iterate through the collection and check the String objects
      while(iter.hasNext())
      {
         if(((String)iter.next()).equalsIgnoreCase(str))
            return true;
      }
      return false;
   }

}
