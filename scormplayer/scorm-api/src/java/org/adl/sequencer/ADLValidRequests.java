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

import java.io.Serializable;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 * Encapsulation of information required for delivery.<br><br>
 * 
 * <strong>Filename:</strong> ADLValidRequests.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * The <code>ADLUIState</code> encapsulates the information required by the
 * SCORM 2004 3rd Edition Sample RTE delivery system to determine which
 * navigation UI controls should be enabled on for the current launched
 * activity.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE.<br>
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
public class ADLValidRequests implements Serializable, IValidRequests {
	private static final long serialVersionUID = 1L;

	private long id;

	/**
	 * Should a 'Start' button be enabled before the sequencing session begins
	 */
	public boolean mStart = false;

	/**
	 * Should a 'Resume All' button be enabled before the sequencing session begins
	 */
	public boolean mResume = false;

	/**
	 * Should a 'Continue' button be enabled during delivery of the current
	 * activity.
	 */
	public boolean mContinue = false;

	/**
	 * Should a 'Continue' button be enabled during delivery of the current
	 * activity that triggers an Exit navigation request.
	 */
	public boolean mContinueExit = false;

	/**
	 * Should a 'Previous' button be enabled during the delivery of the
	 * current activity.
	 */
	public boolean mPrevious = false;

	/**
	 * Indictates if the sequencing session has begun and a 'SuspendAll'
	 * navigation request is valid.
	 */
	public boolean mSuspend = false;

	/**
	 * Set of valid targets for a choice navigation request
	 */
	public Map<String, ActivityNode> mChoice = null;

	/**
	 * The currently valid table of contents (list of <code>ADLTOC</code>) to be
	 * provided during the current activity.
	 */
	// TODO: Remove this
	//public List mTOC = null;

	public DefaultTreeModel mTreeModel = null;

	/*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
	
	 Public Methods
	
	-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/
	public ADLValidRequests() {
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		ADLValidRequests other = (ADLValidRequests) obj;
		if (id != other.id){
			return false;
		}
		return true;
	}

	@Override
	public Map<String, ActivityNode> getChoice() {
		return mChoice;
	}

	public long getId() {
		return id;
	}

	@Override
	public TreeModel getTreeModel() {
		return mTreeModel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean isContinueEnabled() {
		return mContinue;
	}

	@Override
	public boolean isContinueExitEnabled() {
		return mContinueExit;
	}

	@Override
	public boolean isPreviousEnabled() {
		return mPrevious;
	}

	@Override
	public boolean isResumeEnabled() {
		return mResume;
	}

	@Override
	public boolean isStartEnabled() {
		return mStart;
	}

	@Override
	public boolean isSuspendEnabled() {
		return mSuspend;
	}

	/**
	 * This method provides the state this <code>ADLUIState</code> object for
	 * diagnostic purposes.<br><br>
	 *
	 * NOTE: The table of contents (TOC) is not provided with this method.  For
	 * a dump of the current TOC, call the code>dumpTOC</code> method of on the
	 * <code>ADLSeqUtilities</code> class.
	 * 
	 * @see <code>ADLSeqUtilities</code>
	 */
	/*
	   public void dumpState()
	   {
	      if ( _Debug )
	      {
	         System.out.println("  :: ADLValidRequests   --> BEGIN - dumpState");

	         System.out.println("  ::--> Start         : " + mStart);
	         System.out.println("  ::--> Start         : " + mResume);
	         System.out.println("  ::--> Continue      : " + mContinue);
	         System.out.println("  ::--> Continue Exit : " + mContinueExit);
	         System.out.println("  ::--> Previous      : " + mPrevious);

	         if ( mTOC != null )
	         {
	            System.out.println("  ::--> TOC:           YES");
	            ADLSeqUtilities.dumpTOC(mTOC);
	         }
	         else
	         {
	            System.out.println("  ::--> TOC:           NO");
	         }

	         System.out.println("  :: ADLValidRequests    --> END   - dumpState");
	      }
	   }
	*/

} // end ADLValidRequests
