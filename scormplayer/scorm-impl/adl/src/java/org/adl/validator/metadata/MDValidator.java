/******************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) grants you
** ("Licensee") a non-exclusive, royalty free, license to use, modify and
** redistribute this software in source and binary code form, provided that
** i) this copyright notice and license appear on all copies of the software;
** and ii) Licensee does not utilize the software in a manner which is
** disparaging to ADL Co-Lab.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab AND ITS LICENSORS
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO
** EVENT WILL ADL Co-Lab OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE,
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE
** SOFTWARE, EVEN IF ADL Co-Lab HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
** DAMAGES.
**
******************************************************************************/
package org.adl.validator.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.adl.datamodels.datatypes.DateTimeValidator;
import org.adl.datamodels.datatypes.DateTimeValidatorImpl;
import org.adl.datamodels.datatypes.DurationValidator;
import org.adl.datamodels.datatypes.LangStringValidator;
import org.adl.logging.DetailedLogMessageCollection;
import org.adl.parsers.dom.DOMTreeUtility;
import org.adl.util.LogMessage;
import org.adl.util.MessageType;
import org.adl.util.Messages;
import org.adl.validator.ADLSCORMValidator;
import org.adl.validator.RulesValidator;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <strong>Description: </strong>The <code>MDValidator</code> object determines
 * whether the test subject metadata XML instance is conformant with the 
 * ADL Metadata Application Profile as defined in the Content Aggregation 
 * Model of the SCORM. The MDValidator inherits from the ADLSCORMValidator to
 * determine if the metadata test subject is wellformed and valid to the xsd(s).
 * Next, the MDValidator object validates the metadata test subject against 
 * the rules and requirements necessary for meeting the ADL Metadata Application 
 * Profile.
 *
 * @author ADL Technical Team
 */
public class MDValidator extends ADLSCORMValidator {
	/**
	 * Logger object used for debug logging
	 */
	private Logger mLogger;

	/**
	 * The Metadata Rule Validator Object
	 */
	private RulesValidator mMetadataRulesValidator;

	/**
	 * The vocabulary token of the Technical.Requirement.OrComposite.Type element
	 * for enforcement of the type/name best practice vocabulary<br>
	 */
	private String mTypeValue = "";

	/**
	 * The vocabulary token of the Technical.Requirement.OrComposite.Name element
	 * for enforcement of the type/name best practice vocabulary<br>
	 *
	 */
	private String mNameValue = "";

	/**
	 * Boolean describing whether or not the restricted string values of the
	 * <code>&lt;metadataSchema&gt;</code> element have been validated
	 */
	private boolean mMetadataSchemaTracked = false;

	/**
	 * The metadataSchema elements that shall contain restricted string values
	 */
	private List<Node> mMetadataSchemaNodeList;

	/**
	 * The lifeCycle.contribute.role vocabulary values
	 * 
	 */
	private List<String> mLifecycleContributeVocabList;

	/**
	 * Constructor.  Sets the attributes to their initial values.
	 * 
	 * @param iApplicationProfileType The SCORM Metadata application profile type
	 * 
	 */
	public MDValidator(String iApplicationProfileType) {
		super("metadata");
		mLogger = Logger.getLogger("org.adl.util.debug.validator");
		mMetadataRulesValidator = new RulesValidator("metadata");
		mTypeValue = "";
		mNameValue = "";
		mMetadataSchemaNodeList = new ArrayList<>();
		mLifecycleContributeVocabList = new ArrayList<>();
	}

	/**
	 * This method tests for each particular data type and runs the
	 * corresponding tests.
	 *
	 *
	 * @param iCurrentChildName The name of child node
	 * @param iDataType The name of datatype to be tested
	 * @param iCurrentChild The child node to be tested
	 * @param iPath Path of the rule to compare to
	 * @param iMinRule The minimum multipicity 0 implies the element is optional,
	 * 1 implies it is mandatory. 
	 *
	 * @return boolean: result of the parse.  A true value implies that the
	 * well-formedness parse and the scheam validation parse passed, false
	 * implies otherwise.<br>
	 */
	private boolean checkDataTypes(String iCurrentChildName, String iDataType, Node iCurrentChild, String iPath, int iMinRule) {
		String msgText = "";
		boolean result = true;

		if (iDataType.equalsIgnoreCase("parent")) {
			//This is a parent element

			// If we have an <orComposite> element, must perform a special check to
			// enforce that <name> and <type> coexist

			if (iCurrentChildName.equals("orComposite")) {
				msgText = Messages.getString("MDValidator.303");
				mLogger.info("INFO: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

				result = checkThatNameTypeCoexist(iCurrentChild);
			}

			// special best practice check for the lom.lifeCycle.contribute element
			// and lomg.metaMetadata.contribute element
			if (iCurrentChildName.equals("contribute")) {
				checkThatChildrenCoExist(iCurrentChild, "contribute", "role", "entity");
			}

			// special best practice check for the lom.general.identifier element, 
			// lom.metaMetadata element and lom.relation.resource element
			if (iCurrentChildName.equals("identifier")) {
				checkThatChildrenCoExist(iCurrentChild, "identifier", "catalog", "entry");
			}

			// special best practice check for the lom.rights element, 
			if (iCurrentChildName.equals("rights")) {
				checkThatChildrenCoExist(iCurrentChild, "rights", "description");
			}

			// parent element, must recurse to validate its children
			result = compareToRules(iCurrentChild, iPath) && result;
		} else if (iDataType.equalsIgnoreCase("langstring")) {
			// Log: Test the element against the LangString Data Type
			msgText = Messages.getString("MDValidator.43", iCurrentChildName);
			mLogger.info("INFO: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

			// Test that the element meets the requirements of a LangString Data 
			// Type
			result = checkLangString(iCurrentChild, iPath);
		} else if (iDataType.equalsIgnoreCase("datetime") || iDataType.equalsIgnoreCase("duration")) {

			// Log: Test the element against the DateTime or Duration Type
			// This is a datetime or a duration data type element
			msgText = Messages.getString("MDValidator.48", iCurrentChildName, iDataType);
			mLogger.info("INFO: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

			// Test the element against the DateTime or Duration Type
			result = checkDatetimeOrDurationPair(iCurrentChild, iDataType);
		} else if (iDataType.equalsIgnoreCase("nametypepair")) {
			// This is a Technical.OrComposite.Requirement.Name and Type element
			// Step 1:  Log: Testing element for basic vocabulary data type 
			// requirements
			msgText = Messages.getString("MDValidator.53", iCurrentChildName);
			mLogger.info("INFO: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

			result = checkSourceValuePair(iCurrentChild, iCurrentChildName, "bestpractice", iPath);

			// Step 2:  Test to make sure the Name and Type Pair requirements are
			// met.
			checkNameTypePair(iCurrentChildName);

		} else if (iDataType.equalsIgnoreCase("text")) {
			if (iCurrentChildName.equals("language")) {

				//  If the following call returns a null then the language element 
				// is empty
				if (iCurrentChild.getFirstChild() != null) {
					NodeList nodes = iCurrentChild.getChildNodes();
					// The only <language> element that can have a value of "none"
					//  is the language child of general, all others will fail if 
					// they have that value

					// If the node is <language>, the parent is <general> AND the value 
					// is "none"
					if ((iCurrentChild.getParentNode()).getLocalName().equals("general")) {
						if (nodes.item(0).getNodeValue() != null) {
							if (nodes.item(0).getNodeValue().equals("none")) {
								// "none" is a valid value for the general.language element
								msgText = Messages.getString("MDValidator.315", nodes.item(0).getNodeValue());
								mLogger.info("PASSED: " + msgText);
								DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
							}
						}

					} else {
						LangStringValidator langVal = new LangStringValidator();
						int intResult = langVal.validate(nodes.item(0).getNodeValue());
						if (intResult == 0) {
							// no error
							msgText = Messages.getString("MDValidator.315", nodes.item(0).getNodeValue());
							mLogger.info("PASSED: " + msgText);
							DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
						} else {
							// type mismatch error, value is not a valid language code
							msgText = Messages.getString("MDValidator.316", nodes.item(0).getNodeValue());
							mLogger.info("FAILED: " + msgText);
							DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

							result = false;
						}
					}
				} else {
					// the element is empty
					msgText = "The element <language> cannot be empty";
					msgText = Messages.getString("MDValidator.317");
					mLogger.info("FAILED: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
					result = false;
				}

			}
			// This is a text data type
			result = checkText(iCurrentChild, iCurrentChildName, iPath, iMinRule) && result;
		} else if (iDataType.equalsIgnoreCase("bestpracticevocabulary")) {
			// This is a best practice vocabulary
			msgText = Messages.getString("MDValidator.53", iCurrentChildName);
			mLogger.info("INFO: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

			result = checkSourceValuePair(iCurrentChild, iCurrentChildName, "bestpractice", iPath);
		} else if (iDataType.equalsIgnoreCase("restrictedvocabulary")) {
			// This is a restricted vocabulary data type
			msgText = Messages.getString("MDValidator.53", iCurrentChildName);
			mLogger.info("INFO: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));
			result = checkSourceValuePair(iCurrentChild, iCurrentChildName, "restricted", iPath);
		} else {
			// This is an extension element
			// no additional checks needed
		}

		return result;
	}

	/**
	* The method determines if the datetime and description pair of the
	* datetime typed element being tested is valid according to the rules
	* defined in the Application Profile.  The parser verifies only that no
	* more than datetime and description elements exist for an element.
	*
	* @param iElement The Datetime typed or Duration typed element node.
	* @param iTypeOfPair Distinguishes what type is being checked for
	* <ul>
	*    <li><code>DateTime</code></li>
	*    <li><code>Duration</code></li>
	* </ul>
	* 

	*
	* @return boolean: if the DateTime/Duration type check passed of failed.
	* A true value implies that the element passed the DateTime/Duration 
	* checks, false implies otherwise.
	*/
	private boolean checkDatetimeOrDurationPair(Node iElement, String iTypeOfPair) {
		boolean result = true;
		boolean descriptionExists = false;
		int spmRule = -1;
		String msgText = "";
		int minRule = -1;

		if (iTypeOfPair.equals("datetime")) {
			//check to ensure datetime exists 1 and only 1 time
			result = checkForMandatory(iElement, "dateTime") && result;
		} else if (iTypeOfPair.equals("duration")) {
			//check to ensure duration exists 1 and only 1 time
			result = checkForMandatory(iElement, "duration") && result;
		}
		//determine if optional description element exists
		int multiplicityUsed = getMultiplicityUsed(iElement, "description");
		if (multiplicityUsed > 0) {
			descriptionExists = true;
		}

		//retrieve datetime and description elements OR
		// retrive duration and description element
		if (result) {
			NodeList children = iElement.getChildNodes();
			int numChildren = children.getLength();

			for (int i = 0; i < numChildren; i++) {
				Node currentChild = children.item(i);
				String currentChildName = currentChild.getLocalName();

				if ((currentChildName.equals("dateTime")) || (currentChildName.equals("duration"))) {
					// retrieve the value of this element
					String currentChildValue = mMetadataRulesValidator.getTaggedData(currentChild);

					// we must first assign the correct name of the dataType as it
					// exists in the .xml rules files
					String dataTypeElement = "";
					if (currentChildName.equals("dateTime")) {
						dataTypeElement = "DateTime";
					} else if (currentChildName.equals("duration")) {
						dataTypeElement = "Duration";
					}

					//get the spm rule of the DateTime datatype and convert to int
					spmRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue(dataTypeElement, "", "spm"));

					//get the minimum multiplicity rule and convert to an int
					minRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue(dataTypeElement, "", "min"));

					result = checkSPMConformance(currentChildName, currentChildValue, spmRule, minRule) && result;

					// peform valiation to the ISO 8601 format
					if (currentChildName.equals("dateTime")) {
						DateTimeValidator dTimeVal = new DateTimeValidatorImpl(true);

						int intResult = dTimeVal.validate(currentChildValue);
						if (intResult == 0) {
							// no error
							msgText = Messages.getString("MDValidator.319", currentChildValue, "dateTime");
							mLogger.info("PASSED: " + msgText);
							DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
						} else {
							//  error, value is not a valid dateTime value
							msgText = Messages.getString("MDValidator.320", currentChildValue, "dateTime");
							mLogger.info("FAILED: " + msgText);
							DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

							result = false;
						}

					} else {
						DurationValidator durVal = new DurationValidator();
						int intResult = durVal.validate(currentChildValue);
						if (intResult == 0) {
							// no error
							msgText = Messages.getString("MDValidator.321", currentChildValue, "duration");
							mLogger.info("PASSED: " + msgText);
							DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
						} else {
							// type mismatch error, value is not a valid language code
							msgText = Messages.getString("MDValidator.322", currentChildValue, "duration");
							mLogger.info("FAILED: " + msgText);
							DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

							result = false;
						}
					}
				}
			}
		}
		if (descriptionExists) {
			NodeList children = iElement.getChildNodes();
			int numChildren = children.getLength();

			for (int i = 0; i < numChildren; i++) {
				Node currentChild = children.item(i);
				String currentChildName = currentChild.getLocalName();

				if (currentChildName.equals("description")) {
					//This is a Langstring data type element
					msgText = Messages.getString("MDValidator.43", currentChildName);
					mLogger.info("INFO: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

					result = checkLangString(currentChild, "") && result;
				}
			}
		}
		return result;
	}

	/**
	   * This method determines if the given mandatory element is present within
	   * the xml test subject.<br>
	   *
	   * @param iTestSubjectNode
	   *               parent node of the element being searched<br>
	   * @param iNodeName
	   *               element node name being searched for mandatory presence.
	   * <br>
	   *
	   * @return boolean: A true value implies that the mandatory element was
	   * detected in the xml met-data instance, a false implies otherwise.<br>
	   */
	private boolean checkForMandatory(Node iTestSubjectNode, String iNodeName) {

		boolean result = true;
		String msgText = "";
		int multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, iNodeName);

		if (multiplicityUsed < 1) {
			msgText = Messages.getString("MDValidator.198", iNodeName);
			mLogger.info("FAILED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
			result = false;
		}
		return result;
	}

	/**
	* This method verifies that the metadata schema element esists & contains the 
	* values ADLv1.0 and LOMv1.0.
	* 
	* @param iTestSubjectNode is the node for the test subject. 
	* @param iNodeName is the name of the test subject node.
	* @return true if the schemas are present, false otherwise
	*/
	private boolean checkForMandatorySchemas(Node iTestSubjectNode, String iNodeName) {
		boolean result = true;
		int multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, iNodeName);

		if (multiplicityUsed < 2) {
			String msgText = Messages.getString("MDValidator.201", iNodeName);
			mLogger.info("FAILED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
			result = false;
		}
		return result;
	}

	/**
	   * This method validates a langstring type element by performing the
	   * multiplicity and SPM checks on the string element.<br>
	   *
	   * @param iCurrentLangstringElement
	   *               the element node that is of type langstring <br>
	   * @param iPath the unique identifier of the location of the test subject 
	   * node in the DOM tree. 
	   *
	   * @return if the langstring check passed or failed.  A true
	   * value implies that the langstring typed element passed, false implies
	   * otherwise. <br>
	   */
	private boolean checkLangString(Node iCurrentLangstringElement, String iPath) {
		boolean result = true;
		String msgText = "";
		int multiplicityUsed = -1;
		int minRule = -1;
		int maxRule = -1;
		int spmRule = -1;
		String langstringElement = "string";
		String stringValue = "";
		String currentLangstringElementName = iCurrentLangstringElement.getLocalName();

		if (currentLangstringElementName != null) {
			//ensure that the mandatory string exists
			boolean stringElementExists = checkForMandatory(iCurrentLangstringElement, "string");

			if (stringElementExists) {
				//only check multiplicity if string element exists
				multiplicityUsed = getMultiplicityUsed(iCurrentLangstringElement, "string");

				//get the min rule for string and convert to an int
				minRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue("string", "", "min"));
				//get the max rule for string and convert to an int
				maxRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue("string", "", "max"));
				//check multiplicity of string
				result = checkMultiplicity(multiplicityUsed, minRule, maxRule, "string") && result;

				List<Node> childNodes = DOMTreeUtility.getNodes(iCurrentLangstringElement, "string");

				//test multiplicity of language attribute
				for (int i = 0; i < childNodes.size(); i++) {
					if (childNodes.get(i).getLocalName().equals("string")) {
						Node stringNode = childNodes.get(i);
						//look for the attribute of this element
						NamedNodeMap attrList = childNodes.get(i).getAttributes();

						for (int j = 0; j < attrList.getLength(); j++) {
							if (((Attr) attrList.item(j)).getName().equals("language")) {
								//check multiplicity of language attribute

								msgText = Messages.getString("MDValidator.232");
								DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));
								msgText = Messages.getString("MDValidator.234");
							}
						}
						//check to see if smp is defined for element.
						//SPECIAL CASE:  this should only occur for description of
						//               datetime/duration type
						//get the spm rule and convert to an int

						spmRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue(currentLangstringElementName, iPath, "spm"));
						int parentMinRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue(currentLangstringElementName, iPath, "min"));

						//check to see if smp is defined for element
						// retrieve the value of this child element
						stringValue = mMetadataRulesValidator.getTaggedData(stringNode);

						// check spm conformance by passing in the minRule of the Parent
						result = checkSPMConformance(langstringElement, stringValue, spmRule, parentMinRule) && result;
					}
				}
			} else {
				result = stringElementExists && result;
			}
		}
		return result;
	}

	/**
	  * This method assists with the application profile check for valid
	  * restricted string values.  The restricted string value is compared to
	  * those defined by the application profile rules.
	  *
	  * @return     true if the value is a valid string token, false otherwise<br>
	  */
	private boolean checkMetadataSchema() {
		mLogger.entering("MDValidator", "checkMetadataSchema()");

		boolean result = false;
		String msgText;
		String currentElementName = "metadataSchema";
		boolean foundLOMSchema = false;
		boolean foundADLSchema = false;

		// The <metadataSchema> element shall contain restricted string values

		msgText = Messages.getString("MDValidator.243", currentElementName);

		mLogger.info("INFO: " + msgText);
		DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

		// retrieve the restricted string values for the <metadataSchema> element
		List<String> vocabValues = mMetadataRulesValidator.getVocabRuleValues(currentElementName, "lom.metaMetadata");
		int mMetadataSchemaNodeListSize = mMetadataSchemaNodeList.size();

		for (int i = 0; i < mMetadataSchemaNodeListSize; i++) {
			Node currentMetadataSchemaNode = mMetadataSchemaNodeList.get(i);
			// retrieve the value of this element
			String currentMetadataSchemaValue = mMetadataRulesValidator.getTaggedData(currentMetadataSchemaNode);

			// Now loop through the valid vocabulary List to see if the
			// value matches a valid token

			int vocabValuesSize = vocabValues.size();
			for (int j = 0; j < vocabValuesSize; j++) {
				String currentVocabToken = vocabValues.get(j);

				if (currentMetadataSchemaValue.equals(currentVocabToken)) {
					if (currentVocabToken.equals("LOMv1.0")) {
						foundLOMSchema = true;
						mLogger.finer("Found LOMv1.0");
						break;
					} else if (currentVocabToken.equals("ADLv1.0")) {
						foundADLSchema = true;
						mLogger.finer("Found ADLv1.0");
						break;
					}
				}
			}
		}

		if (foundLOMSchema) {
			msgText = Messages.getString("MDValidator.251", "LOMv1.0", currentElementName);
			mLogger.info("PASSED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
		} else {
			msgText = Messages.getString("MDValidator.253", "LOMv1.0", currentElementName);
			mLogger.info("FAILED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
		}
		if (foundADLSchema) {
			msgText = Messages.getString("MDValidator.251", "ADLv1.0", currentElementName);
			mLogger.info("PASSED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
		} else {
			msgText = Messages.getString("MDValidator.253", "ADLv1.0", currentElementName);
			mLogger.info("FAILED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
		}

		mMetadataSchemaTracked = true;
		mLogger.exiting("MDValidator", "checkMetadataSchema");

		result = foundLOMSchema && foundADLSchema;
		return result;
	}

	/**
	  * This method performs the check of the multiplicy used against the
	  * supplied min and max rule values.<br>
	  *
	  * @param iMultiplicityUsed the multiplicity value determined for the 
	  *        element<br>
	  * @param iMinRule the minimum value allowed for the element<br>
	  * @param iMaxRule the maximum value allowed for the element<br>
	  * @param iElementName the name of the element being evaluated
	  *
	  * @return boolean: if the multiplicty checks passed or failed.  A true
	  * value implies that the element was found to be within the multiplicity
	  * bounds, a false implies otherwise.<br>
	  */
	private boolean checkMultiplicity(int iMultiplicityUsed, int iMinRule, int iMaxRule, String iElementName) {
		boolean result = true;
		String msgText = "";

		if ((iMinRule != -1) && (iMaxRule != -1)) {
			if (iMultiplicityUsed >= iMinRule && iMultiplicityUsed <= iMaxRule) {
				msgText = Messages.getString("MDValidator.73", iElementName);
				mLogger.info("PASSED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
			} else {
				if ((iMaxRule > 1) || (iMaxRule > 2))
				// we are handing spm multiplicity
				{
					msgText = Messages.getString("MDValidator.208", iElementName, iMaxRule);
					mLogger.info("WARNING: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));
				}
				// we are dealing with no spm multiplicity but rather a 0 or 1 max
				else {
					msgText = Messages.getString("MDValidator.211", iElementName);
					mLogger.info("FAILED: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
					result = false;
				}

			}
		} else if ((iMinRule != -1) && (iMaxRule == -1)) {
			if (iMultiplicityUsed >= iMinRule) {
				msgText = Messages.getString("MDValidator.73", iElementName);
				mLogger.info("PASSED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
			} else {
				msgText = Messages.getString("MDValidator.211", iElementName);
				mLogger.info("FAILED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
				result = false;
			}
		}
		return result;
	}

	/**
	 * The method determines if the type and name elements abide to the best
	 * practice vocabulary rules as defined by LOM.<br>
	 *
	 * @param iCurrentChildName
	 *           The element name in question<br>
	 */
	private void checkNameTypePair(String iCurrentChildName) {
		String msgText = "";

		if (iCurrentChildName.equalsIgnoreCase("name")) {
			if (!mTypeValue.isEmpty()) {
				if (mTypeValue.equals("operating system")
				        && (!mNameValue.equals("pc-dos") && !mNameValue.equals("ms-windows") && !mNameValue.equals("macos") && !mNameValue.equals("unix")
				                && !mNameValue.equals("multi-os") && !mNameValue.equals("none"))) {
					msgText = Messages.getString("MDValidator.302", iCurrentChildName);
					mLogger.info("WARNING: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));
				} else if (mTypeValue.equals("browser")
				        && (!mNameValue.equals("any") && !mNameValue.equals("netscape communicator") && !mNameValue.equals("ms-internet explorer")
				                && !mNameValue.equals("opera") && !mNameValue.equals("amaya"))) {
					msgText = Messages.getString("MDValidator.302", iCurrentChildName);
					mLogger.info("WARNING: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));
				}
			} else {
				msgText = Messages.getString("MDValidator.314");
				mLogger.info("WARNING: " + msgText);
			}
		}
	}// end of checkNameTypePair()

	/**
	* This method activates the parse for validation against the schema(s).
	*
	* @param iXMLFileName - name of the metadata XML test subject<br>
	*
	* @return boolean - result of the parse for validation to the schema. False
	* implies that errors occured during validation to the schema parse,
	* true implies the document is valid to the schema(s).<br>
	*
	*/
	private boolean checkSchema(String iXMLFileName) {
		boolean schemaResult = true;

		super.setPerformFullValidation(true);
		super.performValidatorParse(iXMLFileName);

		mLogger.info("************************************");
		mLogger.info(" VALIDSCHEMA Result is " + super.getIsValidToSchema());
		mLogger.info(" mIsExtensionsUsed is " + super.getIsExtensionsUsed());
		mLogger.info("************************************");

		if (!super.getIsValidToSchema()) {
			schemaResult = false;
		}

		return schemaResult;
	}

	/**
	* The method validates the vocabulary typed elements by determining if the
	* source and value pair of the element being tested is valid according to
	* the rules defined in the Application Profile.  The parser validates only
	* that 1 source and value elements exist for an element.
	*
	* @param iElementName The name of the element being checked for valid  
	*                     vocabulary
	* @param iVocabularyElement The vocabulary type element node.
	* @param iVocabularyType The vocabulary type being checked for
	* <ul>
	*    <li><code>bestpractice</code></li>
	*    <li><code>restricted</code></li>
	* </ul>
	* 
	* @param iPath The path identifier of the element in question
	*
	* @return boolean: if the vocabulary type check passed of failed. A true
	* value implies that the vocabulary typed element passed the checks, false
	* implies otherwise.<br>
	*/
	private boolean checkSourceValuePair(Node iVocabularyElement, String iElementName, String iVocabularyType, String iPath) {
		boolean result = true;
		int spmRule = -1;
		String msgText = "";
		String vocabularyElementName = iVocabularyElement.getLocalName();
		boolean checkBestPracticeVocabulary = true;
		int minRule = -1;

		//check to ensure source exists 1 and only 1 time
		result = checkForMandatory(iVocabularyElement, "source") && result;

		//check to ensure value exists 1 and only 1 time
		result = checkForMandatory(iVocabularyElement, "value") && result;

		//only continue if these elements exist
		if (result) {

			//retrieve source and value elements
			NodeList vocabularyChildren = iVocabularyElement.getChildNodes();
			int numVocabChildren = vocabularyChildren.getLength();

			for (int i = 0; i < numVocabChildren; i++) {
				Node currentVocabChild = vocabularyChildren.item(i);
				String currentVocabChildName = currentVocabChild.getLocalName();

				if (currentVocabChildName.equals("source")) {
					// retrieve the value of this element
					String currentVocabChildValue = mMetadataRulesValidator.getTaggedData(currentVocabChild);

					//get the spm rule and convert to an int
					spmRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue(currentVocabChildName, "", "spm"));

					//get the minimum multiplicity rule and convert to an int
					minRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue(currentVocabChildName, "", "min"));

					result = checkSPMConformance(currentVocabChildName, currentVocabChildValue, spmRule, minRule) && result;

					if (!(currentVocabChildValue.equalsIgnoreCase("LOMv1.0")) && iVocabularyType.equals("bestpractice")) {
						msgText = Messages.getString("MDValidator.284", iElementName);
						mLogger.info("WARNING: " + msgText);
						DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));
						checkBestPracticeVocabulary = false;
					}
				} else if (currentVocabChildName.equals("value")) {
					// retrieve the value of this element
					String currentVocabChildValue = mMetadataRulesValidator.getTaggedData(currentVocabChild);

					//SPECIAL CASE for the name/type pair
					if (vocabularyElementName.equalsIgnoreCase("type")) {
						mTypeValue = currentVocabChildValue;
					} else if (vocabularyElementName.equalsIgnoreCase("name")) {
						mNameValue = currentVocabChildValue;

					}

					//SPECIAL CASE for lom.lifeCycle.contribute
					// store contribute values for best practice to contain at least 1 "author"
					if (vocabularyElementName.equalsIgnoreCase("role") && iPath.equals("lom.lifeCycle.contribute")) {
						mLifecycleContributeVocabList.add(currentVocabChildValue);
					}

					//get the spm rule and convert to an int
					spmRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue(currentVocabChildName, "", "spm"));

					//get the minimum multiplicity rule and convert to an int
					minRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue(currentVocabChildName, "", "min"));

					result = checkSPMConformance(currentVocabChildName, currentVocabChildValue, spmRule, minRule) && result;
					// check vocab token
					List<String> vocabValues = mMetadataRulesValidator.getVocabRuleValues(iElementName, iPath);

					if (checkBestPracticeVocabulary) {
						result = checkVocabulary(iElementName, currentVocabChildValue, vocabValues) && result;
					}
				}

			}
		}
		return result;
	}

	/**
	* This method performs the smallest permitted maximum check on an elements
	* value.<br>
	*
	* @param iElementName Name of the element being checked for SPM
	* @param iElementValue Element value being checked for SPM
	* @param iSPMRule Allowable SPM value
	* @param iMinRule This value describes if we are dealing with an optional
	*                 or mandatory element.  If the element is option, the 
	*                 value will be 0 and if mandatory, than 1.
	*
	* @return boolean: result of spm check.  A true value implies that the
	* smallest permitted maximum checks passed, false implies otherwise.
	*/
	private boolean checkSPMConformance(String iElementName, String iElementValue, int iSPMRule, int iMinRule) {
		boolean result = true;
		String msgText = "";

		int elementValueLength = iElementValue.length();

		if (iSPMRule != -1) {
			if (elementValueLength > iSPMRule) {
				msgText = Messages.getString("MDValidator.154", iElementName, Integer.toString(iSPMRule));
				mLogger.info("WARNING: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));
			} else if (elementValueLength < 1) {
				// only need to fail mandatory elements if they contain no data
				if (iMinRule == 1) {
					msgText = Messages.getString("MDValidator.158", iElementName);
					mLogger.info("FAILED: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

					result = false;
				}
			} else {
				msgText = Messages.getString("MDValidator.161", iElementName);
				mLogger.info("PASSED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
			}
		} else if (elementValueLength < 1) {
			// spm is -1.  This occurs for dateTime and Duration elements
			// only need to fail mandatory elements if they contain no data
			if (iMinRule == 1) {
				msgText = Messages.getString("MDValidator.158", iElementName);
				mLogger.info("FAILED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

				result = false;

			}
		} else {
			msgText = Messages.getString("MDValidator.161", iElementName);
			mLogger.info("PASSED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
		}

		return result;
	}

	/**
	     * This method validates the text typed elements by performing the
	     * smallest permitted maximum. 
	     *
	     * @param iTextElement The element node that is of type text
	     * @param iTextElementName The element name that is of type text
	     * @param iPath The path identifier of the element in question
	     * @param iMinRule The minimum multipicity 0 implies the element is 
	     * optional, 1 implies it is mandatory. 
	     *
	     * @return boolean: if the text check passed or failed.  A true value
	     * implies that the text typed element passed the check, false implies
	     * otherwise.
	     */
	private boolean checkText(Node iTextElement, String iTextElementName, String iPath, int iMinRule) {

		int spmRule = -1;
		boolean result = true;

		// first must retrieve the value of this child element
		String textElementValue = mMetadataRulesValidator.getTaggedData(iTextElement);

		//get the spm rule and convert to an int
		spmRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue(iTextElementName, iPath, "spm"));

		result = checkSPMConformance(iTextElementName, textElementValue, spmRule, iMinRule) && result;
		return result;
	}

	/**
	* This method enforces the Best Practice SCORM recommendation that if an 
	* optional parent elements exists, than its child should exist also.  If 
	* this method detects the parent element without its child element, than a
	* best practice warning will be thrown.  
	* 
	* @param iParentNode The parent node element
	* @param iParentName The name of the parent node element
	* @param iChild1 The name of the expected child
	*/
	private void checkThatChildrenCoExist(Node iParentNode, String iParentName, String iChild1) {
		boolean doesChild1Exist = false;
		String msgText = "";

		NodeList children = iParentNode.getChildNodes();

		if (children != null) {
			int childrenLength = children.getLength();

			// loop through the children of iParentNode to determine if both
			// iChild1 and iChild2 elements exist.

			for (int i = 0; i < childrenLength; i++) {
				Node currentChild = children.item(i);

				if (currentChild.getLocalName().equals(iChild1)) {
					doesChild1Exist = true;
				}
			}

			if (!doesChild1Exist) {
				msgText = Messages.getString("MDValidator.326", iParentName, iChild1);
				mLogger.info("WARNING: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));
			}
		}
	}

	/**
	* This method enforces the Best Practice SCORM recommendation that if an 
	* optional parent elements exists, than the children should exist also.  If 
	* this method detects the parent element without its children, than a
	* best practice warning is thrown.  
	* 
	* @param iParentNode The parent node element
	* @param iParentName The name of the parent node element
	* @param iChild1 The name of an expected child
	* @param iChild2 The name of an expected child
	*/
	private void checkThatChildrenCoExist(Node iParentNode, String iParentName, String iChild1, String iChild2) {
		boolean doesChild1Exist = false;
		boolean doesChild2Exist = false;
		String msgText = "";

		NodeList children = iParentNode.getChildNodes();

		if (children != null) {
			int childrenLength = children.getLength();

			// loop through the children of iParentNode to determine if both
			// iChild1 and iChild2 elements exist.

			for (int i = 0; i < childrenLength; i++) {
				Node currentChild = children.item(i);

				if (currentChild.getLocalName().equals(iChild1)) {
					doesChild1Exist = true;
				}
				if (currentChild.getLocalName().equals(iChild2)) {
					doesChild2Exist = true;
				}
			}

			if (!doesChild1Exist) {
				msgText = Messages.getString("MDValidator.326", iParentName, iChild1);
				mLogger.info("WARNING: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));

			}
			if (!doesChild2Exist) {
				msgText = Messages.getString("MDValidator.326", iParentName, iChild2);
				mLogger.info("WARNING: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));

			}
		}
	}

	/**
	* This method performs the check that the &lt;orComposite&gt; element contains
	* both a &lt;type&gt; and a &lt;name&gt; element as children.  A failure shall occur
	* if &lt;type&gt; and &lt;name&gt; do not coexist. <br>
	*
	* @param iOrCompositeNode  the parent node that contains the &lt;type&gt; and
	*                          &lt;name&gt; elements.<br>
	*
	* @return boolean  returns false if &lt;type&gt; and &lt;name&gt; do not coexist, true
	*                  otherwise.<br>
	*
	*/

	private boolean checkThatNameTypeCoexist(Node iOrCompositeNode) {
		String msgText = "";
		boolean doesTypeExist = false;
		boolean doesNameExist = false;
		boolean result = true;

		// retrieve children of <orComposite>

		NodeList children = iOrCompositeNode.getChildNodes();

		if (children != null) {
			int childrenLength = children.getLength();

			// loop through the children of orComposite to determine if both
			// <type> and <name> elements exist.

			for (int i = 0; i < childrenLength; i++) {
				Node currentChild = children.item(i);

				if (currentChild.getLocalName().equals("type")) {
					doesTypeExist = true;
				}
				if (currentChild.getLocalName().equals("name")) {
					doesNameExist = true;
				}
			}

			if (doesTypeExist && doesNameExist) {
				msgText = Messages.getString("MDValidator.305");
				mLogger.info("PASSED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));

			} else {
				// must error, they do not coexist.
				msgText = Messages.getString("MDValidator.314");
				mLogger.info("WARNING: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));
				result = true;
			}
		}

		return result;
	}

	/**
	 * This method assists with the application profile check for valid
	 * vocabularies.  The vocabulary value is compared to those defined by the
	 * application profile rules. It is assumed that only 1 vocabulary token may
	 * exist for an element/attribute.<br>
	 *
	 * @param iName Name of the element/attribute being checked
	 *              for valid vocabulary.<br>
	 * @param iValue Vocabulary string value that exists for the
	 *               element/attribute in the test subject<br>
	 * @param iVocabValues List containing a list of the valid vocabulary 
	 *                values for the element/attribute.<br>
	 *
	 * @return     true if the value is a valid vocab token, false otherwise<br>
	 */
	private boolean checkVocabulary(String iName, String iValue, List<String> iVocabValues) {
		mLogger.entering("MDValidator", "checkVocabulary()");

		boolean result = false;
		String msgText;

		// loop through the valid vocabulary List to see if the
		// attribute value matches a valid token

		int iVocabValuesSize = iVocabValues.size();
		for (int i = 0; i < iVocabValuesSize; i++) {
			if (iValue.equals(iVocabValues.get(i))) {
				result = true;
			}
		}

		if (result) {
			msgText = Messages.getString("MDValidator.172", iValue, iName);
			mLogger.info("PASSED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
		} else {
			msgText = Messages.getString("MDValidator.176", iValue, iName);
			mLogger.info("FAILED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
		}
		mLogger.exiting("MDValidator", "checkVocabulary()");

		return result;
	}

	/**
	   * This method activates the parse for wellformedness.
	   *
	   * @param iXMLFileName - name of the metadata XML test subject<br>
	   *
	   * @return boolean - result of the parse for wellformedness. False implies
	   * that errors occured during wellformedness parse, true implies the document
	   * is wellformed
	   *
	   */
	private boolean checkWellformedness(String iXMLFileName) {
		boolean wellnessResult = true;

		super.setPerformFullValidation(false);
		super.performValidatorParse(iXMLFileName);

		mLogger.info("************************************");
		mLogger.info(" WELLFORMED Result is " + super.getIsWellformed());
		mLogger.info("************************************");

		if (!super.getIsWellformed()) {
			wellnessResult = false;
		}

		return wellnessResult;
	}

	/**
	    * A recursive method that is driven by the test subject dom.
	    * This method performs the application profiles rules checks.<br>
	    *
	    * @param iTestSubjectNode
	    *               Test Subject DOM<br>
	    * @param iPath
	    *               Path of the rule to compare to<br>
	    *
	    * @return boolean: result of the application profile checks performed.  A
	    * true value implies that the test subject passes the application profile
	    * rules, a false implies otherwise.<br>
	    */
	private boolean compareToRules(Node iTestSubjectNode, String iPath) {
		// looks exactly like prunetree as we walk down the tree
		mLogger.entering("MDValidator", "compareToRules");

		boolean result = true;
		String msgText = "";

		// is there anything to do?
		if (iTestSubjectNode == null) {
			result = false;
			return result;
		}

		int type = iTestSubjectNode.getNodeType();

		switch (type) {
		case Node.PROCESSING_INSTRUCTION_NODE: {
			break;
		}

		// document
		case Node.DOCUMENT_NODE: {
			// Found the root element for the metadata instance (<lom>)
			Node rootNode = ((Document) iTestSubjectNode).getDocumentElement();
			String rootNodeName = rootNode.getLocalName();

			msgText = Messages.getString("MDValidator.70", rootNodeName);
			mLogger.info("INFO: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

			// XML Parser would have caught if the <lom> element was found more
			// than once.  Since we are here, the <lom> element only exists once
			msgText = Messages.getString("MDValidator.73", rootNodeName);
			mLogger.info("PASSED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));

			result = compareToRules(rootNode, "") && result;
			break;
		}

		// element node
		case Node.ELEMENT_NODE: {
			String parentNodeName = iTestSubjectNode.getLocalName();
			String dataType = null;
			int multiplicityUsed = -1;
			int minRule = -1;
			int maxRule = -1;

			// Test the child Nodes
			NodeList children = iTestSubjectNode.getChildNodes();

			if (children != null) {
				int numChildren = children.getLength();

				// update the path for this child element
				String path;

				if (iPath.isEmpty() || parentNodeName.equalsIgnoreCase("lom")) {
					// the Node is a DOCUMENT OR
					// the Node is a <lom>

					path = parentNodeName;
				} else {
					path = iPath + "." + parentNodeName;
				}

				// SPECIAL CASE: check for mandatory elements
				if (parentNodeName.equalsIgnoreCase("lom")) {
					result = checkForMandatory(iTestSubjectNode, "general") && result;
					result = checkForMandatory(iTestSubjectNode, "technical") && result;
					result = checkForMandatory(iTestSubjectNode, "rights") && result;

					result = checkForMandatory(iTestSubjectNode, "lifeCycle") && result;

					result = checkForMandatory(iTestSubjectNode, "metaMetadata") && result;

				} else if (parentNodeName.equalsIgnoreCase("general")) {
					result = checkForMandatory(iTestSubjectNode, "title") && result;

					result = checkForMandatory(iTestSubjectNode, "description") && result;

					result = checkForMandatory(iTestSubjectNode, "keyword") && result;

				} else if (parentNodeName.equalsIgnoreCase("lifeCycle")) {
					result = checkForMandatory(iTestSubjectNode, "version") && result;

				} else if (parentNodeName.equalsIgnoreCase("metaMetadata")) {

					// special case, multiplicity is 2 or more
					result = checkForMandatorySchemas(iTestSubjectNode, "metadataSchema") && result;

					// need to add all metadataschema values to a List for a
					// later check for restricted strings.
					NodeList metaMetadataChildren = iTestSubjectNode.getChildNodes();

					if (metaMetadataChildren != null) {
						int metaMetadataChildrenLength = metaMetadataChildren.getLength();
						for (int i = 0; i < metaMetadataChildrenLength; i++) {
							Node currentChild = metaMetadataChildren.item(i);
							if (currentChild.getLocalName().equals("metadataSchema")) {
								mMetadataSchemaNodeList.add(currentChild);
							}
						}
					}
				} else if (parentNodeName.equalsIgnoreCase("technical")) {
					result = checkForMandatory(iTestSubjectNode, "format") && result;
				} else if (parentNodeName.equalsIgnoreCase("rights")) {
					result = checkForMandatory(iTestSubjectNode, "copyrightAndOtherRestrictions") && result;

				}

				String lastChildChecked = "";
				for (int z = 0; z < numChildren; z++) {
					Node currentChild = children.item(z);
					String currentChildName = currentChild.getLocalName();

					if (currentChildName != null) {

						dataType = mMetadataRulesValidator.getRuleValue(currentChildName, path, "datatype");

						// we do not want to test for extensions here
						if (dataType.equalsIgnoreCase("parent") || dataType.equalsIgnoreCase("langstring") || dataType.equalsIgnoreCase("text")
						        || dataType.equalsIgnoreCase("restrictedvocabulary") || dataType.equalsIgnoreCase("bestpracticevocabulary")
						        || dataType.equalsIgnoreCase("nametypepair") || dataType.equalsIgnoreCase("datetime")
						        || dataType.equalsIgnoreCase("metadataschema") || dataType.equalsIgnoreCase("duration")) {

							if (!lastChildChecked.equals(currentChildName) || lastChildChecked.isEmpty()) {
								lastChildChecked = currentChildName;
								multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, currentChildName);
								//get the min rule and convert to an int
								minRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue(currentChildName, path, "min"));
								//get the max rule and convert to an int
								maxRule = Integer.parseInt(mMetadataRulesValidator.getRuleValue(currentChildName, path, "max"));

								msgText = Messages.getString("MDValidator.70", currentChildName);
								mLogger.info("INFO: " + msgText);
								DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

								result = checkMultiplicity(multiplicityUsed, minRule, maxRule, currentChildName) && result;
							}
							// special handling of validation for <metadataSchema>
							if (dataType.equals("metadataschema") && !mMetadataSchemaTracked) {

								//  We need to make sure we check both metadataSchema elements. The first is
								//    tested here, the second will be tested in the checkDataTypes() because
								//    mMetadataSchemaTracked will be 'true' and fall to the else

								result = checkMetadataSchema() && result;
							} else {
								// test for each particular data type
								result = checkDataTypes(currentChildName, dataType, currentChild, path, minRule) && result;
							}
						}
					}
				}
			}

			break;
		}
		// handle entity reference nodes
		case Node.ENTITY_REFERENCE_NODE: {
			break;
		}
		// text
		case Node.COMMENT_NODE: {
			break;
		}
		case Node.CDATA_SECTION_NODE: {
			break;
		}
		case Node.TEXT_NODE: {
			break;
		}
		default: {
			break;
		}
		}
		mLogger.exiting("MDValidator", "compareToRules()");
		return result;
	}

	/**
	    * This method returns the number of elements with the given name based on
	    * the given parent node of the dom tree.<br>
	    *
	    * @param iParentNode
	    *                  parent node of the element being searched<br>
	    * @param iNodeName
	    *                  name of the element being searched for<br>
	    *
	    * @return int: number of instances of a given element with a given path.<br>
	    */
	private int getMultiplicityUsed(Node iParentNode, String iNodeName) {
		//need a list to find how many kids to cycle through
		NodeList kids = iParentNode.getChildNodes();
		int count = 0;

		int kidsLength = kids.getLength();
		for (int i = 0; i < kidsLength; i++) {
			if (kids.item(i).getNodeType() == Node.ELEMENT_NODE) {
				String currentNodeName = kids.item(i).getLocalName();
				if (currentNodeName.equalsIgnoreCase(iNodeName)) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	   * This overloaded method is called during content package integration to
	   * initiate the validation process. This method will trigger the parsing
	   * activity done by the ADLSCORMValidator. Next, the dom will be used to
	   * validate the remaining checks required for full SCORM Validation.<br>
	   *
	   * @param iRootLOMNode The root LOM element to be validated for SCORM 
	   * conformance. 
	   * 
	   * @param iDidValidationToSchemaPass Boolean describing if the metadata xml 
	   * instance was valid to the controlling docs.  
	   * 
	   * @return boolean: describes if the checks passed or failed. A true value
	   * implies that the checks passed, false implies otherwise.<br>
	   */
	public boolean validate(Node iRootLOMNode, boolean iDidValidationToSchemaPass) {
		boolean validateResult = true;
		String msgText;

		mLogger.entering("MDValidator", "validate(iRootLOMNode)");

		// DEBUG LOG: Testing the Metadata XML Instance for wellformedness
		mLogger.info("INFO: Testing the Metadata XML Instance for Well-" + "formedness");

		msgText = Messages.getString("MDValidator.14");
		mLogger.info(msgText);
		DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

		msgText = Messages.getString("MDValidator.22");
		mLogger.info(msgText);
		DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));

		super.setIsWellformed(true);

		// determine if we are dealing with a root lom element that belongs to the
		// IEEE LOM Namespace.

		// All extensions have been stripped at this point so we are sure we 
		// are dealing with a LOM namespace, let the parent know this fact.
		super.setIsRootElement(true);

		msgText = Messages.getString("MDValidator.15");
		mLogger.info(msgText);
		DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

		if (iDidValidationToSchemaPass) {
			msgText = Messages.getString("MDValidator.23");

			mLogger.info(msgText);

			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));

			super.setIsValidToSchema(true);
		} else {
			msgText = Messages.getString("MDValidator.24");

			mLogger.info(msgText);

			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
		}

		mLogger.exiting("MDValidator", "validate()");
		return validateResult;
	}

	/**
	   * This method is called by the user to initiate the validation process.
	   * This method will trigger the parsing activity done by the
	   * ADLSCORMValidator. Next, the dom will be used to validate the remaining
	   * checks required for full SCORM Validation.<br>
	   *
	   * @param iXMLFileName Name of the metadata XML test subject<br>
	   *
	   * @return Returns a boolean describing if the checks passed or failed. 
	   * A <code>true</code> value implies that the checks passed, 
	   * <code>false</code> otherwise.
	   */
	public boolean validate(String iXMLFileName) {
		boolean validateResult = true;

		mLogger.entering("MDValidator", "validate(iXMLFileName)");
		mLogger.finer("iXMLFileName coming in is " + iXMLFileName);

		// Perform Wellformedness Parse
		mLogger.info("INFO: Testing the Metadata XML Instance for " + "Well-formedness");

		// Well-formedness and Validity to Schema Check
		validateResult = checkWellformedness(iXMLFileName) && validateResult;

		// temp change inflicted from removal of MD app profile
		super.setIsRootElement(true);

		// Perform Validation to the Schema Parse
		mLogger.info("INFO: Testing the Metadata XML Instance for Validity" + " to the Controlling Documents");

		// Well-formedness and Validity to Schema Check
		validateResult = checkSchema(iXMLFileName) && validateResult;

		mLogger.exiting("MDValidator", "validate()");
		return validateResult;
	}

}
