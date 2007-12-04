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

package org.adl.sequencer;

import org.adl.util.debug.DebugIndicator;
import java.io.Serializable;

/**
 * <strong>Filename:</strong> SeqObjectiveMap.java<br><br>
 *
 * <strong>Description:</strong><br>
 * The <code>SeqObjectiveMap</code> encapsulates the information describing how
 * an activity's local objectives are mapped to the global objective space
 * for the current content aggregation.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE. <br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * All fields are purposefully public to allow immediate access to known data
 * elements.<br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS 1.0</li>
 *     <li>SCORM 2004 3rd Edition</li>
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class SeqObjectiveMap implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long id;
	
   /**
    * This controls display of log messages to the java console
    */
   private static boolean _Debug = DebugIndicator.ON;

   /**
    * The target global objective.
    */
   public String mGlobalObjID = null;

   /**
    * Indicates if satisfied status should be read.
    */
   public boolean mReadStatus = true;

   /**
    * Indicates if measure should be read.
    */
   public boolean mReadMeasure = true;

   /**
    * Indicates if satisfied status should be written.
    */
   public boolean mWriteStatus = false;

   /**
    * Indicates if measure should be written.
    */
   public boolean mWriteMeasure = false;

   /**
    * This method provides the state this <code>SeqObjectiveMap</code> object 
    * for diagnostic purposes.
    */
   public void dumpState()
   {
      if ( _Debug )
      {
         System.out.println("  :: SeqObjectiveMap   --> BEGIN - dumpState");

         System.out.println("  ::--> Global ID:       " + mGlobalObjID);
         System.out.println("  ::--> Read Status?:    " + mReadStatus);
         System.out.println("  ::--> Read Measure?:   " + mReadMeasure);
         System.out.println("  ::--> Write Status?:   " + mWriteStatus);
         System.out.println("  ::--> Write Measure?:  " + mWriteMeasure);

         System.out.println("  :: SeqObjectiveMap   --> END   - dumpState");
      }
   }
}  // end SeqObjectiveMap
