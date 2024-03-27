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

package org.adl.datamodels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.adl.datamodels.ieee.IValidatorFactory;

/**
 * <strong>Filename:</strong> DMElement.java<br>
 * <br>
 * 
 * <strong>Description:</strong>
 * 
 * @author ADL Technical Team
 */
public abstract class DMElement implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -2651530842578225219L;

	/**
	 * Primary key
	 */
	protected Long id;

	/**
	 * Describes the qualities of this data model element.
	 */
	protected DMElementDescriptor mDescription;

	/**
	 * Describes the parent of this data model element.
	 */
	protected DMElement mParent = null;

	/**
	 * Describes the binding strings for all of this element's children.
	 */
	protected List<String> mChildrenBindings = null;

	/**
	 * Describes this element's children.
	 */
	protected Map<String, DMElement> mChildren = null;

	/**
	 * Describes the data model records managed by this data model element.
	 */
	protected List<DMElement> mRecords = null;

	/**
	 * Describes this data model element's value.
	 */
	protected String mValue = null;

	/**
	 * Describes the set of delimiters associated with this element
	 */
	protected List<DMDelimiter> mDelimiters = null;

	/**
	 * Describes if the data model element's value has been initialized
	 */
	protected boolean mInitialized = false;

	/**
	 * Describes if values should be truncated at SPM
	 */
	protected boolean mTruncSPM = false;

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
		DMElement other = (DMElement) obj;
		if (id == null){
			return false; // Always return false is id == null
		} else if (!id.equals(other.id)){
			return false;
		}
		return true;
	}

	/**
	 * Compares the provided value to the value stored in this data model
	 * element.
	 * 
	 * @param iValue
	 *            A token (<code>RequestToken</code>) object that provides the
	 *            value to be compared against the exiting value; this request
	 *            may include a set of delimiters.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public int equals(RequestToken iValue) {
		return equals(iValue, true);
	}

	/**
	 * Compares the provided value to the value stored in this data model
	 * element.
	 * 
	 * @param iValue
	 *            A token (<code>RequestToken</code>) object that provides the
	 *            value to be compared against the exiting value; this request
	 *            may include a set of delimiters.
	 * 
	 * @param iValidate
	 *            Describes if the value being compared should be validated
	 *            first.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public abstract int equals(RequestToken iValue, boolean iValidate);

	public List<DMElement> getAllChilden() {
		Map<String, DMElement> children = getChildren();
		List<DMElement> all = new ArrayList<>(children.size());
		Set<Entry<String, DMElement>> entrySet = children.entrySet();
		for (Entry<String, DMElement> entry : entrySet) {
			all.add(entry.getValue());
			all.addAll(entry.getValue().getAllChilden());
		}
		return all;
	}

	public Map<String, DMElement> getChildren() {
		return (mChildren != null ? mChildren : (mChildren = new HashMap<>()));
	}

	public List<DMDelimiter> getDelimiters() {
		return mDelimiters;
	}

	/**
	 * Describes the characteristics of this data model element
	 * 
	 * @return The description <code>DMElementDescriptor</code> of this data
	 *         model element.
	 */
	public DMElementDescriptor getDescription() {
		return mDescription;
	}

	/**
	 * Describes this data model element's binding string.
	 * 
	 * @return This data model element's binding string. Note: The <code>
	 *         String</code> returned only applies in the context of its
	 *         containing data model or parent data model element.
	 */
	public String getDMElementBindingString() {
		return mDescription.mBinding;
	}

	/**
	 * Retrieve the dot-notation bound string of the data model element.
	 * 
	 * @param iDM
	 *            The data model this element belongs to.
	 * 
	 * @return The dot-notation bound string for the data model element.<br>
	 * <br>
	 * 
	 *         Note: The string returned does not account for specific array
	 *         indices, instead it simply returns the placeholder 'n'.
	 */
	protected String getDotNotation(DataModel iDM) {
		String name = "";

		// Recursively call this method to get all of the required 'pieces' of
		// the dot-notation string;
		if (mParent != null) {
			name += mParent.getDotNotation(iDM);

			if (mDescription != null) {
				// Get the next part of the dot-notation string
				if (mDescription.mBinding != null) {
					if (mParent.mRecords != null && mParent.mRecords.contains(this)) {
						name = name + "." + mParent.mRecords.indexOf(this);
					} else {
						name = name + "." + mDescription.mBinding;
					}

					// Check to see if this part is an element or an index
					if (mDescription.mOldSPM > 0) {
						name = name + ".n";
					}
				} else {
					name = "ERROR";
				}
			} else {
				name = "ERROR";
			}
		} else {
			// Provide the dot-notation binding of the data model name
			name = iDM.getDMBindingString() + "." + mDescription.mBinding;
		}

		return name;
	}

	public Long getId() {
		return id;
	}

	public DMElement getParent() {
		return mParent;
	}

	/**
	 * Attempt to get the value of this data model element, which may include
	 * default delimiters.
	 * 
	 * @param iArguments
	 *            Describes the arguments for this getValue() call.
	 * 
	 * @param iAdmin
	 *            Describes if this request is an administrative action.
	 * 
	 * @param iDelimiters
	 *            Indicates if the data model element's default delimiters
	 *            should be included in the return string.
	 * 
	 * @param oInfo
	 *            Provides the value of this data model element. <b>Note: The
	 *            caller of this function must provide an initialized (new)
	 *            <code>DMProcessingInfo</code> to hold the return value.</b>
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public abstract int getValue(RequestToken iArguments, boolean iAdmin, boolean iDelimiters, DMProcessingInfo oInfo);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Describes if the value for this data model element has been initialized.
	 * 
	 * @return <code>true</code> if the value of this data model elemnt has been
	 *         set by the SCO, otherwise <code>false</code>.
	 */
	public boolean isInitialized() {
		return mInitialized;
	}

	/**
	 * Processes a data model request on this data model element. This method
	 * will enforce data model element depedencies and keyword application.
	 * 
	 * @param ioRequest
	 *            Provides the dot-notation request being applied to this data
	 *            model element. The <code>DMRequest</code> will be updated to
	 *            account for processing against this data model element.
	 * 
	 * @param oInfo
	 *            Provides the value of this data model element. <b>Note: The
	 *            caller of this function must provide an initialized (new)
	 *            <code>DMProcessingInfo</code> to hold the return value.</b>
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public abstract int processRequest(DMRequest ioRequest, DMProcessingInfo oInfo);

	/**
	 * Adds the provided data model element to this data model element's set of
	 * children. <br>
	 * <br>
	 * Note: If the provided data model element is already a child of this data
	 * model element, it is replaced.
	 * 
	 * @param iName
	 *            The dot-notation binding name of the data model element to be
	 *            added.
	 * @param iElement
	 *            The <code>DMElement</code> object representing the named data
	 *            model element.
	 */
	public void putChild(String iName, DMElement iElement) {
		if (mChildren == null) {
			mChildren = new Hashtable<>();

		}

		mChildren.put(iName, iElement);
	}

	/**
	 * Overwrites the description of this data model element with the provided
	 * data model element description.
	 * 
	 * @param iDescription
	 *            The new description of this data model element.
	 */
	public void setDescription(DMElementDescriptor iDescription) {
		mDescription = iDescription;
	}

	/**
	 * Attempt to set the value of this data model element to the value
	 * indicated by the dot-notation token.
	 * 
	 * @param iValue
	 *            A token (<code>RequestToken</code>) object that provides the
	 *            value to be set and may include a set of delimiters.
	 * @param iAdmin
	 *            Indicates if this operation is administrative or not. If The
	 *            operation is administrative, read/write and data type
	 *            characteristics of the data model element should be ignored.
	 * @param validatorFactory TODO
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public abstract int setValue(RequestToken iValue, boolean iAdmin, IValidatorFactory validatorFactory);

	@Override
	public String toString() {
		return "DMElement [mDescription=" + mDescription + ", mValue=" + mValue + "]";
	}

	/**
	 * Validates a dot-notation token against this data model's defined data
	 * type.
	 * 
	 * @param iValue
	 *            A token (<code>RequestToken</code>) object that provides the
	 *            value to be checked, possibily including a set of delimiters.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public abstract int validate(RequestToken iValue);

}