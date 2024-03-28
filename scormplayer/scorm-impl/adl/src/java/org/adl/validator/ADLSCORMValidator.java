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
package org.adl.validator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.adl.logging.DetailedLogMessageCollection;
import org.adl.parsers.dom.ADLDOMParser;
import org.adl.parsers.dom.DOMTreeUtility;
import org.adl.util.LogMessage;
import org.adl.util.MessageCollection;
import org.adl.util.MessageType;
import org.adl.util.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * <strong>Filename: </strong>ADLSCORMValidator.java<br><br>
 *
 * <strong>Description: </strong>The <code>ADLSCORMValidator</code> class
 *               serves as the main interface for obtaining a Metadata or
 *               Content Package Validator.  This object houses the common
 *               functionality of both types of Validators (Metadata/CP) -
 *               serving as the parent for inheritance.<br><br>
 *
 * <strong>Design Issues: </strong>none<br><br>
 *
 * <strong>Implementation Issues: </strong>none<br><br>
 *
 * <strong>Known Problems: </strong>none<br><br>
 *
 * <strong>Side Effects: </strong>Populates the MessageCollection<br><br>
 *
 * <strong>References: </strong>SCORM<br><br>
 *
 * @author ADL Technical Team
 */
public class ADLSCORMValidator {
	/**
	 * The <code>Document</code> object is an electronic representation of the
	 * XML produced if the parse was successful. A parse for wellformedness
	 * creates a document object while the parse for validation against the
	 * controlling documents creates a document object as well.  This attribute
	 * houses the document object that is created last.  In no document object is
	 * created, the value remains null. This value is determined by the
	 * ADLDOMParser class.
	 */
	protected Document mDocument;

	/**
	 * This attribute describes if the XML instance is found to be wellformed by
	 * the parser.  The value <code>false</code> indicates that the XML instance 
	 * is not wellformed XML, <code>true</code> indicates it is wellformed XML.
	 * This value is determined by the ADLDOMParser class.
	 */
	protected boolean mIsWellformed;

	/**
	 * This attribute describes if the XML instance is found to be valid against
	 * the controlling documents by the parser.  The value <code>false</code> 
	 * indicates that the XML instance is not valid against the controlling 
	 * documents, <code>true</code> indicates that the XML instance is valid 
	 * against the controlling documents. This value is determined by the 
	 * <code>ADLDOMParser</code> class.
	 */
	protected boolean mIsValidToSchema;

	/**
	 * This attribute describes if the XML instance is valid to the SCORM
	 * Application Profiles. A <code>true</code> value implies that the 
	 * instance is valid to the rules defined by the Application Profiles, 
	 * <code>false</code> implies otherwise.
	 */
	protected boolean mIsValidToApplicationProfile;

	/**
	  * This attribute is specific to the content package validator only.  It
	  * describes if the required schemas exist at the root of a content package 
	  * test subject that are necessary for the validation parse.  A 
	  * <code>true</code> value implies that the required schemas were detected 
	  * at the root package, <code>false</code> implies otherwise.
	  */
	protected boolean mIsRequiredFiles;

	/**
	  * This attribute is specific to the content package validator only.  It
	  * describes if the required IMS Manifest exists at the root of a content
	  * package test subject. A <code>true</code> value implies that the 
	  * required IMS Manifest was detected at the root package, 
	  * <code>false</code> implies otherwise.
	  */
	protected boolean mIsIMSManifestPresent;

	/**
	 * This attribute describes if the XML instance uses extension elements. A
	 * <code>true</code> value implies that extension elements were detected, 
	 * <code>false</code> implies they were not used.
	 */
	protected boolean mIsExtensionsUsed;

	/**
	 * Describes what SCORM Validator is in use.  Valid values include:
	 * <ul>
	 *    <li><code>metadata</code></li>
	 *    <li><code>contentpackage</code></li>
	 * </ul>
	 */
	protected String mValidatorType;

	/**
	 * This attribute contains the string describing the location of the
	 * controlling documents that the parser shall parse against.  The format of
	 * this string value shall be identical to the representation of the
	 * schemaLocation attribute in the XML declation
	 * (ie, [namespace of schema] [location of schema] ).
	 */
	private String mSchemaLocation;

	/**
	 * This attribute contains describes if we are dealing with a root element
	 * that belongs to a valid namespace (IMS, IEEE).  A <code>true</code> 
	 * implies that the root element belongs to a valid namespace, 
	 * <code>false</code> implies that it belongs to an extended namespace.
	 */
	private boolean mIsRootElement;

	/**
	 * Logger object used for debug logging.
	 */
	private Logger mLogger;

	/**
	 * Describes whether or not full validation occurs.  A <code>true</code> 
	 * implies that full validation is performed, <code>false</code> implies 
	 * wellformedness check only.
	 */
	private boolean mPerformFullValidation;

	/**
	 * Indicates if the xsi:schemaLocation was declared in the XML instance. The
	 * xsi:schemalocation attribute provides a hint to the XML Parser as to where
	 * the controlling documents are located.  The xsi:schemalocation attribute
	 * is an optional attribute. 
	 */
	private boolean mSchemaLocExists;

	/**
	 * Contains the list of namespaces that have been declared.  This list is
	 * used to track the required files necessary when setting the schema
	 * location to the default values.  
	 */
	private List<String> mDeclaredNamespaces;

	/**
	 * Constructor.  Sets the attributes to their initial values.
	 *
	 * @param iValidator The type of validator in use.  Valid values include:
	 * <ul>
	 *    <li><code>metadata</code></li>
	 *    <li><code>contentpackage</code></li>
	 * </ul>
	 */
	public ADLSCORMValidator(String iValidator) {
		mLogger = Logger.getLogger("org.adl.util.debug.validator");

		mLogger.entering("ADLSCORMValidator", "ADLSCORMValidator()");
		mLogger.finest("      iValidator coming in is " + iValidator);

		mDocument = null;
		mIsIMSManifestPresent = true;
		mIsWellformed = false;
		mIsValidToSchema = false;
		mSchemaLocation = null;
		mIsValidToApplicationProfile = false;
		mIsExtensionsUsed = false;
		mValidatorType = iValidator;
		mIsRootElement = false;
		mSchemaLocExists = false;
		mDeclaredNamespaces = new ArrayList<>();

		mLogger.exiting("ADLSCORMValidator", "ADLSCORMValidator()");
	}

	/**
	* This method cleans up the temporary folder used by the CPValidator
	* for extraction of the test subject package.  This method loops through
	* the temporary PackageImport folder to remove all files that have been
	* extracted during the content package extract.
	*
	* @param iPath Temporary directory location where package was
	* extracted and in need of cleanup.
	* 
	*/
	public void cleanImportDirectory(String iPath) {
		try {
			File theFile = new File(iPath);
			File allFiles[] = theFile.listFiles();

			for (File allFile : allFiles) {
				if (allFile.isDirectory()) {
					cleanImportDirectory(allFile.toString());
					allFile.delete();
				} else {
					allFile.delete();
				}
			}
		} catch (NullPointerException npe) {
			mLogger.severe(iPath + " did not exist and was not cleaned!!");
		}
	}

	/**
	 * This method provides the outcome of the validate at the time of call.  The
	 * returned object serves as the storage for the checks performed during
	 * validation and their outcomes. This object serves as an efficent means for
	 * passing the outcome of the validation activites throughout the
	 * utilizing system.
	 *
	 * @return ADLValidator Object containing the outcome of validation. <br>
	 */
	public IValidatorOutcome getADLValidatorOutcome() {
		mLogger.entering("ADLSCORMValidator", "getADLValidatorOutcome()");

		// create an instance of the ADLValidator object with the current state of
		// of the ADLSCORMValidator attributes values

		ADLValidatorOutcome outcome = new ADLValidatorOutcome(getDocument(), getIsIMSManifestPresent(), getIsWellformed(), getIsValidToSchema(),
		        getIsValidToApplicationProfile(), getIsExtensionsUsed(), getIsRequiredFiles(), getIsRootElement());

		mLogger.exiting("ADLSCORMValidator", "getADLValidatorOutcome()");
		return outcome;
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getDeclaredNamespaces() {
		return mDeclaredNamespaces;
	}

	/**
	 * This method returns the document created during a parse. A parse for
	 * wellformedness creates a document object while the parse for validation
	 * against the controlling documents creates a seperate document object.
	 *
	 * @return Document -  An electronic representation of the XML produced by
	 * the parse.
	 */
	public Document getDocument() {
		return mDocument;
	}

	/**
	 * This method returns whether or not the XML instance contained extension
	 * elements and/or attributes.  The value <code>false</code> indicates that 
	 * the XML instance does not contain extended elements and/or attributes, 
	 * <code>true</code> indicates that the XML instance did.
	 *
	 * @return boolean Describes if extension elements were found.
	 */
	public boolean getIsExtensionsUsed() {
		return mIsExtensionsUsed;
	}

	/**
	 * This method returns a boolean describing if the required IMS manifest
	 * is at the root of the package.
	 *
	 * @return boolean Describes if the required imsmanifest.xml was found at
	 * the root of the package, <code>false</code> implies otherwise.
	 */
	public boolean getIsIMSManifestPresent() {
		return mIsIMSManifestPresent;
	}

	/**
	 * This method is specific to the Content Package Validator only.  This
	 * method returns whether or not the content package test subject
	 * contains the required schemas at the root of the package
	 * needed for the validation parse.
	 *
	 * @return boolean Describes if the required files were found at the root
	 * of the package, <code>false</code> implies otherwise.
	 */
	public boolean getIsRequiredFiles() {
		return mIsRequiredFiles;
	}

	/**
	 * This method is used by both the MD and CP Validators.  It is used
	 * to describe if the root element of the test subject belongs to a valid
	 * namespace (IMS or IEEE LOM).
	 *
	 * @return boolean Describes if the root element belongs to a valid
	 * namespace
	 */
	public boolean getIsRootElement() {
		return mIsRootElement;
	}

	/**
	 * This method returns whether or not the XML instance was valid to the
	 * application profile checks.  The value <code>false</code> indicates that 
	 * the XML instance is not valid to the application profiles, 
	 * <code>true</code> indicates that the XML instance is valid to the 
	 * application profiles.
	 *
	 * @return boolean Describes if the instance was found to be valid
	 * against the SCORM Application Profiles.
	 */
	public boolean getIsValidToApplicationProfile() {
		return mIsValidToApplicationProfile;
	}

	/**
	 * This method returns whether or not the XML instance was valid to the
	 * schema.  The value <code>false</code> indicates that the XML instance is 
	 * not valid against the controlling documents, <code>true</code> indicates 
	 * that the XML instance is valid against the controlling documents.
	 *
	 * @return boolean - describes if the instance was found to be valid
	 *                   against the schema(s).
	 */
	public boolean getIsValidToSchema() {
		return mIsValidToSchema;
	}

	/**
	 * This method returns whether or not the XML instance was found to be
	 * wellformed.  The value <code>false</code> indicates that the XML 
	 * instance is not wellformed XML, <code>true</code> indicates it is 
	 * wellformed XML.
	 *
	 * @return boolean - describes if the instance was found to be wellformed.
	 */
	public boolean getIsWellformed() {
		return mIsWellformed;
	}

	/**
	 * This method returns the schemaLocation string that contains the schema
	 * locations values retrieved after walking the wellformeness DOM, or, if no
	 * schemalocation values were found in the dom, then this value contains the
	 * default schema location values.
	 *
	 * @return mSchemaLocation -- the schemaLocation string.
	 */
	public String getSchemaLocation() {
		return mSchemaLocation;
	}

	/**
	 * This method returns whether or not the XML instance contains the optional
	 * xsi:schemalocation attribute declaration.  The xsi:schemalocation attribute
	 * provides a hint to the XML Parser as to where the controlling documents
	 * can be found.  
	 * 
	 * @return boolean describing if the xsi:schemalocation attribute was
	 * declared in the XML instance. 
	 */
	public boolean getSchemaLocExists() {
		return mSchemaLocExists;
	}

	/**
	* This method determines if the IMS Manifest being tested is truely an
	* IMS Manifest.  It does this by comparing the root elements local name
	* and namespace with the defined IMS root node name and namespace.  If
	* the root node is not what is expected, then the method logs an error
	* and returns false.
	*
	* @return Returns a boolean value that indicates whether or not the
	* validator is processing an IMS manifest.
	*/
	public boolean isRootElementValid() {
		boolean result = false;

		Node rootNode = mDocument.getDocumentElement();
		String rootNodeName = rootNode.getLocalName();
		String rootNodeNamespace = rootNode.getNamespaceURI();

		if (rootNodeName.equals("manifest")) {
			if (rootNodeNamespace.equals(DOMTreeUtility.IMSCP_NAMESPACE)) {
				result = true;
			} else {
				String msgText = Messages.getString("ADLSCORMValidator.24", rootNodeName, rootNodeNamespace, DOMTreeUtility.IMSCP_NAMESPACE);

				mLogger.info("FAILED: " + msgText);

				MessageCollection.getInstance().add(new LogMessage(MessageType.FAILED, msgText));
				// This is the updated logging method invocation        
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.TERMINATE, msgText));
			}
		} else if (rootNodeName.equals("lom")) {
			if (rootNodeNamespace.equals(DOMTreeUtility.IEEE_LOM_NAMESPACE)) {
				result = true;
			} else {
				String msgText = Messages.getString("ADLSCORMValidator.30", rootNodeName, rootNodeNamespace, DOMTreeUtility.IEEE_LOM_NAMESPACE);

				mLogger.info("FAILED: " + msgText);
				MessageCollection.getInstance().add(new LogMessage(MessageType.FAILED, msgText));
				// This is the updated logging method invocation        
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.TERMINATE, msgText));
			}
		}
		return result;
	}

	/**
	 * This method parses the provided XML file for wellformedness
	 * and validation against the controlling documents through interaction
	 * with the DOMParser.  The usage of extended elements and/or attributes is
	 * also determined here.
	 *
	 * @param iXMLFileName The xml file test subject
	 * 
	 *
	 */
	protected void performValidatorParse(String iXMLFileName) {

		mLogger.entering("ADLSCORMValidator", "performValidatorParse()");
		mLogger.finest("   iXMLFileName coming in is " + iXMLFileName);

		// create an adldomparser object
		ADLDOMParser adldomparser = new ADLDOMParser();

		if (mSchemaLocation != null) {
			//set schemaLocation property and perform parsing on the test subject
			adldomparser.setSchemaLocation(getSchemaLocation());

			// call the appropriate parse method based on what type of parse is
			// indicated by the mPerformaFullValidation parameter

			if (!mPerformFullValidation) {
				adldomparser.parseForWellformedness(iXMLFileName, true, false);
				setSchemaLocation(adldomparser.getSchemaLocation());
				mDocument = adldomparser.getDocument();
				// flag if the xsi:schemalocation attribute was declared in the XML
				mSchemaLocExists = adldomparser.getSchemaLocExists();

				// extensions are detected and the flag is set during prunetree
				// of wellformedness parse only
				mIsExtensionsUsed = adldomparser.isExtensionsFound();

			} else {
				adldomparser.setSchemaLocation(mSchemaLocation);
				adldomparser.parseForValidation(iXMLFileName);
			}
		} else {
			String msgText = Messages.getString("ADLSCORMValidator.18");
			mLogger.severe("TERMINATE: " + msgText);
			MessageCollection.getInstance().add(new LogMessage(MessageType.TERMINATE, msgText));
			// This is the updated logging method invocation        
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.TERMINATE, msgText));
		}

		// retrieve adldomparser attribute values and assign to the SCORMValidator
		//mDocument = adldomparser.getDocument();
		mIsWellformed = adldomparser.getIsWellformed();
		mIsValidToSchema = adldomparser.getIsValidToSchema();
		mDeclaredNamespaces = adldomparser.getDeclaredNamespaces();

		// perform garabage cleanup
		(Runtime.getRuntime()).gc();

		mLogger.exiting("ADLSCORMValidator", "performValidatorParse()");
	}

	/**
	 * This method is specific to the Content Package Validator only.  This
	 * method sets whether or not the content package test subject
	 * contains the required files at the root of the package.  The required
	 * files include the imsmanifest.xml file as well as the controlling
	 * documents needed for the validation parse.
	 *
	 * @param imsManifestResult Boolean indicating the result of the
	 * required files check.
	 * 
	 */
	protected void setIsIMSManifestPresent(boolean imsManifestResult) {
		mIsIMSManifestPresent = imsManifestResult;
	}

	/**
	 * This method is specific to the Content Package Validator only.  This
	 * method sets whether or not the content package test subject
	 * contains the required files at the root of the package.  The required
	 * files include the imsmanifest.xml file as well as the controlling
	 * documents needed for the validation parse.
	 *
	 * @param iRequiredFilesResult Boolean indicating the result of the
	 * required files check
	 * 
	 */
	protected void setIsRequiredFiles(boolean iRequiredFilesResult) {
		mIsRequiredFiles = iRequiredFilesResult;
	}

	/**
	 * This method is used by both the MD and CP Validators.  It is used
	 * to set the value that describes if the root element of the test subject
	 * belongs to a valid namespace (IMS or IEEE LOM)
	 *
	 * @param iIsRootElement Boolean value indicating whether or not (true/false)
	 * the root element of the XML Instance is what was expected.
	 * 
	 */
	public void setIsRootElement(boolean iIsRootElement) {
		mIsRootElement = iIsRootElement;
	}

	/**
	 * This method sets whether or not the XML instance was valid to the
	 * application profile checks.  The value <code>false</code> indicates that 
	 * the XML instance is not valid to the application profiles, 
	 * <code>true</code> indicates that the XML instance is valid to the 
	 * application profiles.
	 *
	 * @param isValidToAppProf Boolean indicating the application profile
	 * check result.
	 * 
	 */
	protected void setIsValidToApplicationProfile(boolean isValidToAppProf) {
		mIsValidToApplicationProfile = isValidToAppProf;
	}

	/**
	 * This method sets whether or not the XML instance was valid to the
	 * schema.  The value <code>false</code> indicates that the XML instance is 
	 * not valid against the controlling documents, <code>true</code> indicates 
	 * that the XML instance is valid against the controlling documents.
	 *
	 * @param iIsValidToSchema Describes if the instance was found to be valid
	 * against the schema(s)
	 * 
	 */
	public void setIsValidToSchema(boolean iIsValidToSchema) {
		mIsValidToSchema = iIsValidToSchema;
	}

	/**
	 * This method sets whether or not the XML instance was found to be
	 * wellformed.  The value "false" indicates that the XML instance is not
	 * wellformed XML, <code>true</code> indicates it is wellformed XML.
	 *
	 * @param iIsWellformed Describes if the instance was found to be wellformed.
	 * 
	 */
	protected void setIsWellformed(boolean iIsWellformed) {
		mIsWellformed = iIsWellformed;
	}

	/**
	  * This method sets whether or not full validation is to be performed
	  * by the Validator.
	  *
	  * @param iPerformFullValidation Describes if full validation occurs.
	  * 
	  */
	public void setPerformFullValidation(boolean iPerformFullValidation) {
		mPerformFullValidation = iPerformFullValidation;
	}

	/**
	  * The parser allows a using system to hardcode the location of the
	  * controlling documents that are to be used during the parse for validation.
	  * This method permits the setting of these controlling document locations.
	  * 
	  * @param iSchemaLocation - The schemaLocation string in the exact format as
	  * it would appear in the xsi:schemaLocation attribute of an XML instance.
	  * 
	  */
	public void setSchemaLocation(String iSchemaLocation) {
		mLogger.entering("ADLSCORMValidator", "setSchemaLocation()");

		mSchemaLocation = iSchemaLocation;

		mLogger.finest("mSchemaLocation set to " + mSchemaLocation);
		mLogger.exiting("ADLSCORMValidator", "setSchemaLocation()");
	}
}