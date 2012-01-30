package org.adl.validator;

import java.io.Serializable;
import java.util.List;

import org.adl.validator.contentpackage.IMetadataData;
import org.adl.validator.contentpackage.LaunchData;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public interface IValidator extends Serializable {

	public IValidatorOutcome getADLValidatorOutcome();

	/**
	 * This method retrives the launch data from the manifestHandler object. The
	 * launch data returned contains information for the launching of SCOs found
	 * within the content package test subject. This method is only called if the
	 * user chooses to validate SCOs.
	 * 
	 * @param iDefaultOrganizationOnly A boolean representing whether or not
	 *           launch data should be collected from the default organization
	 *           only.
	 * @param iRemoveAssets A boolean representing whether or not assets should
	 *           be removed in the LaunchData list.
	 * @return Vector: Containing the launch information
	 */
	public List<LaunchData> getLaunchData(boolean iDefaultOrganizationOnly, boolean iRemoveAssets);

	/**
	 * This method accesses the manifest handler to extract all SCO launch
	 * information
	 * 
	 * @param iRolledUpDocument - Dom with manifest rollup
	 * @param iDefaultOrganizationOnly - boolean describing which organization to
	 *           read from
	 * @param iRemoveAssets - boolean describing if to include assets in launch
	 *           locatation
	 * @return Returns a vector of launch data
	 */
	public List<LaunchData> getLaunchData(Document iRolledUpDocument, boolean iDefaultOrganizationOnly, boolean iRemoveAssets);

	/**
	 * This method retrives the metadata information from the manifestHandler
	 * object. The metadata information returned contains information for the
	 * validation of the metadata found within the content package test subject.
	 * This method is only called if the user chooses to validate metadata.
	 * 
	 * @return Vector: Containing the metadata launch information
	 */
	public List<? extends IMetadataData> getMetadataDataList();

	/**
	 * This method assists with the application profile check for the
	 * multiplicity of attributes. This method returns the number of times the
	 * attribue was detected based on the given attribute name and the given
	 * parent node of that element name.
	 * 
	 * @param iAttributeMap A list of attributes
	 * @param iNodeName The name of the element being searched for
	 * @return - int: number of instances of a given attribute
	 */
	public int getMultiplicityUsed(NamedNodeMap iAttributeMap, String iNodeName);

	/**
	 * This method assists with the application profile check for the
	 * multiplicity of elements. This method returns the number of times the
	 * element was detected based on the given element name and the given parent
	 * node of that element name.
	 * 
	 * @param iParentNode The parent node of the element being searched
	 * @param iNodeName The name of the element being searched for
	 * @return - int: number of instances of a given element
	 */
	public int getMultiplicityUsed(Node iParentNode, String iNodeName);

	/**
	 * This method is used to turn full content package validation off -
	 * including required files check, validation to the schema, and application
	 * profile checks. Turning full validation off allows only a parse for
	 * well-formedness to be preformed.
	 * 
	 * @param iValue True implies to parse for well-formedness and validation to
	 *           the schema, false implies to parse for well-formedness only.
	 */
	public void setPerformValidationToSchema(boolean iValue);

	/**
	 * This method initiates the validation process. The checks called from this
	 * method include the required files check, manifest checks (wellformedness
	 * and schema validation), and application profile checks. If the test
	 * subject is in the form of a zip, then the extract is performed prior to
	 * the calls mentioned above.
	 * 
	 * @param iFileName The name of the SCORM Content Package test subject
	 * @param iTestType The type of test subject ( pif or media )
	 * @param iApplicationProfileType The Application Profile type of the test
	 *           subject (content aggregation or resource )
	 * @param iManifestOnly The boolean describing whether or not the IMS
	 *           Manifest is to be the only subject validated. True implies that
	 *           validation occurs only on the IMS Manifest (checks include
	 *           wellformedness, schema validation, and application profile
	 *           checks). False implies that the entire Content Package be
	 *           validated (IMS Manifest checks with the inclusion of the
	 *           required files checks, metadata, and sco testing).
	 * @param encoding, the encoding of the zip file
	 * @return - Boolean value indicating the outcome of the validation checks
	 *         True implies that validation was error free, false implies
	 *         otherwise.
	 */
	public boolean validate(String iFileName, String iTestType, String iApplicationProfileType, boolean iManifestOnly, String encoding);

}