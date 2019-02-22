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

package org.adl.datamodels;

import java.util.List;

/**
 * Encapsulation of information required for processing a data model request.
 * 
 * <strong>Filename:</strong> DMProcessingInfo.java<br>
 * <br>
 * 
 * <strong>Description:</strong><br>
 * The <code>ADLLaunch</code> encapsulates the information that may be returned
 * (out parameters) during the processing of a data model request.<br>
 * <br>
 * 
 * <strong>Design Issues:</strong><br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * All fields are purposefully public to allow immediate access to known data
 * elements.<br>
 * <br>
 * 
 * <strong>Known Problems:</strong><br>
 * <br>
 * 
 * <strong>Side Effects:</strong><br>
 * <br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 * <li>SCORM 2004
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class DMProcessingInfo {
	private Long id;

	/**
	 * Describes the value being maintained by a data model element.
	 */
	public String mValue = null;

	/**
	 * Describes the data model element that processing should be applied to.
	 */
	public DMElement mElement = null;

	/**
	 * Describes the set this data model element is contained in.
	 */
	public List<DMElement> mRecords = null;

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
		DMProcessingInfo other = (DMProcessingInfo) obj;
		if (id == null) {
			if (other.id != null){
				return false;
			}
		} else if (!id.equals(other.id)){
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

} // end DMProcessingInfo
