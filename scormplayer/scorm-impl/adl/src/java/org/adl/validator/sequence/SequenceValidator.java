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
package org.adl.validator.sequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.adl.logging.DetailedLogMessageCollection;
import org.adl.parsers.dom.DOMTreeUtility;
import org.adl.util.LogMessage;
import org.adl.util.MessageType;
import org.adl.util.Messages;
import org.adl.validator.RulesValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <strong>Filename: </strong>SequenceValidator.java<br><br>
 *
 * <strong>Description: </strong>The <code>SequenceValidator</code> object
 * determines whether the content package test subject is conformant with the
 * Sequence Application Profile. The SequenceValidator is spawned from
 * the CPValidator to validate the content package test subject
 * against the rules and requirements necessary for meeting each
 * Sequence Application Profile.
 *
 * @author ADL Technical Team
 */
public class SequenceValidator {

	/**
	 * Logger object used for debug logging.
	 */
	private static Log log = LogFactory.getLog(SequenceValidator.class);

	/**
	 * RulesValidator object
	 */
	private RulesValidator mRulesValidator;

	/**
	 * This attribute contains the populated ObjectiveMap object, containing all
	 * the information needed to validate global objectives.
	 */
	private ObjectiveMap mObjectiveInfo;

	/**
	 * This attribute contains a list of the referencedObjective attributes of a
	 * ruleCondition.
	 */
	private List<String> mReferencedObjectiveList;

	/**
	 * This attribute contains a list of all of the objective identifier values.
	 * The objective identifier values include those of the primaryObjective
	 * and objective elements.
	 */
	private List<String> mObjectivesIDList;

	/**
	 * Default constructor method which instantiates a new sequence rules 
	 * validator and an ObjectiveMap class object
	 * 
	 */
	public SequenceValidator() {
		mRulesValidator = new RulesValidator("sequence");
		mObjectiveInfo = new ObjectiveMap();
		mReferencedObjectiveList = new ArrayList<>();
		mObjectivesIDList = new ArrayList<>();

	}

	/**
	  * This method validates the attribute values based on the rules defined
	  * in the SCORM Application Profile rules.
	  *
	  * @param iNode element parent node of the attribute being validated
	  * @param iNodeName Parent element node name
	  * @param iPath path of the rule to compare to
	  *
	  * @return True if the value is a valid vocab token,
	  * false otherwise.
	  *
	  */
	public boolean checkAttributes(Node iNode, String iNodeName, String iPath) {
		String dataType = null;
		boolean result = true;
		String msgText = "";
		int multiplicityUsed = -1;

		NamedNodeMap attrList = iNode.getAttributes();
		int numAttr = attrList.getLength();
		log.debug("There are " + numAttr + " attributes of " + iNodeName + " to test");

		// SPECIAL CASE: check for mandatory/shall not exist attributes on
		// sequencingCollection child elements

		if (iNodeName.equalsIgnoreCase("sequencing") && iPath.equals("sequencingCollection")) {

			multiplicityUsed = getMultiplicityUsed(attrList, "ID");

			msgText = Messages.getString("SequenceValidator.145");
			log.debug("INFO: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

			if (multiplicityUsed < 1) {

				msgText = Messages.getString("SequenceValidator.147");
				log.debug("FAILED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

				result = false;

			} else {
				msgText = Messages.getString("SequenceValidator.149");

				log.debug("PASSED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
				result = true && result;
			}

			// IDRef is not allowed to be present
			multiplicityUsed = getMultiplicityUsed(attrList, "IDRef");
			if (multiplicityUsed >= 1) {
				msgText = Messages.getString("SequenceValidator.152");
				log.debug("FAILED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
				result = false;
			}

		}

		Attr currentAttrNode;
		String currentNodeName;
		String attributeValue = null;
		int minRule = -1;
		int maxRule = -1;
		int spmRule = -1;

		// test the attributes
		for (int j = 0; j < numAttr; j++) {
			currentAttrNode = (Attr) attrList.item(j);
			currentNodeName = currentAttrNode.getLocalName();

			dataType = mRulesValidator.getRuleValue(iNodeName, iPath, "datatype", currentNodeName);

			// make sure that this is a SCORM recognized attribute
			if (!dataType.equalsIgnoreCase("-1")) {
				msgText = Messages.getString("SequenceValidator.156", currentNodeName);
				log.debug("INFO: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

				// check for multiplicity if the attribute is not deprecated
				if (!dataType.equalsIgnoreCase("deprecated")) {
					multiplicityUsed = getMultiplicityUsed(attrList, currentNodeName);

					// We will assume that no attribute can exist more than
					// once (ever).  According to W3C.  Therefore, min and max
					// rules must exist.

					//get the min rule and convert to an int
					minRule = Integer.parseInt(mRulesValidator.getRuleValue(iNodeName, iPath, "min", currentNodeName));
					//get the max rule and convert to an int
					maxRule = Integer.parseInt(mRulesValidator.getRuleValue(iNodeName, iPath, "max", currentNodeName));

					if ((minRule != -1) || (maxRule != -1)) {
						if (multiplicityUsed >= minRule && multiplicityUsed <= maxRule) {
							msgText = Messages.getString("SequenceValidator.162", currentNodeName);
							log.debug("PASSED: " + msgText);
							DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
						} else {
							msgText = Messages.getString("SequenceValidator.165", currentNodeName);
							log.debug("FAILED: " + msgText);
							DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

							result = false;
						}
					}

					//get the spm rule and convert to an int
					spmRule = Integer.parseInt(mRulesValidator.getRuleValue(iNodeName, iPath, "spm", currentNodeName));

					attributeValue = currentAttrNode.getValue();
				}

				// check the contents of the attribute
				if (dataType.equalsIgnoreCase("idref")) {
					// This is a IDREF data type
				} else if (dataType.equalsIgnoreCase("id")) {
					// This is a ID data type
				} else if (dataType.equalsIgnoreCase("vocabulary")) {
					// This is a VOCAB data type
					// retrieve the vocab rule values and check against the
					// vocab values that exist within the test subject

					msgText = Messages.getString("SequenceValidator.172", currentNodeName);
					log.debug("INFO: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

					List<String> vocabAttribValues = mRulesValidator.getAttribVocabRuleValues(iNodeName, iPath, currentNodeName);

					// we are assuming that only 1 vocabulary value may
					// exist for an attribute
					result = checkVocabulary(currentNodeName, attributeValue, vocabAttribValues, true) && result;
				} else if (dataType.equalsIgnoreCase("deprecated")) {
					// This is a deprecated attribute
					msgText = Messages.getString("SequenceValidator.176", currentNodeName);
					log.debug("FAILED: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
					result = false;
				} else if (dataType.equalsIgnoreCase("text")) {
					//This is a TEXT data type
					// check the attributes for smallest permitted maximum
					// (spm) conformance.
					result = checkSPMConformance(currentNodeName, attributeValue, spmRule, true) && result;
					// we have to store the referencedObjective attribute to
					// validate that it references a primary or objective identifier
					if (currentNodeName.equals("referencedObjective")) {
						mReferencedObjectiveList.add(attributeValue);
					}
				} else if (dataType.equalsIgnoreCase("boolean")) {
					//This is a BOOLEAN data type
				} else if (dataType.equalsIgnoreCase("decimal")) {
					//This is a DECIMAL data type
				} else if (dataType.equalsIgnoreCase("integer")) {
					//This is a INTEGER data type
				} else if (dataType.equalsIgnoreCase("duration") || dataType.equalsIgnoreCase("dateTime")) {
					msgText = Messages.getString("SequenceValidator.185", currentNodeName);
					log.debug("INFO: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));
					// We can assume that the schema validation has validated
					// the format.
					msgText = Messages.getString("SequenceValidator.188", currentNodeName);
					log.debug("PASSED: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));

					if (currentNodeName.equals("attemptExperiencedDurationLimit") || currentNodeName.equals("activityAbsoluteDurationLimit")
					        || currentNodeName.equals("activityExperiencedDurationLimit") || currentNodeName.equals("beginTimeLimit")
					        || currentNodeName.equals("endTimeLimit")) {

						// this attribute is out of scope of SCORM
						msgText = Messages.getString("SequenceValidator.196", currentNodeName);
						log.warn("WARNING: " + msgText);
						DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));

					}
				} else {
					// it is an extension element
				}
			}
		}
		return result;
	}

	/**
	 * This method validates that all objectiveIDs on a primary objective and
	 * an objective for a given activity are unique.
	 *
	 * @param iObjectivesNode The objectives node that is the parent to the 
	 * <code>&lt;primaryObject&gt;</code> and <code>&lt;objective&gt;</code>
	 * elements.
	 *
	 * @return <code>true</code> if the objectiveIDs for a given 
	 * activity are unique, <code>false</code> otherwise.
	 *
	 */
	private boolean checkObjectiveIDsForUniqueness(Node iObjectivesNode) {
		boolean result = true;
		String msgText = "";

		// retrive ObjectiveNodes
		List<Node> objectivesList = DOMTreeUtility.getNodes(iObjectivesNode, "objective");

		// retrieve <primaryObjective> node
		Node primObjNode = DOMTreeUtility.getNode(iObjectivesNode, "primaryObjective");
		// add primaryOjective node to obectivesList being both have objectiveIDs
		// that must be unique
		if (primObjNode != null) {
			objectivesList.add(primObjNode);
		}

		// Loop through the objective and primaryObjective elements to retrieve
		// the objectiveID attribute values

		int objNodesSize = objectivesList.size();

		Set<String> objectiveIDList = new HashSet<>();

		for (int i = 0; i < objNodesSize; i++) {
			Node currentChild = objectivesList.get(i);
			String objectiveID = DOMTreeUtility.getAttributeValue(currentChild, "objectiveID");

			// need to populate the objectives ID values for validation that the 
			// referencedObjection attribute
			mObjectivesIDList.add(objectiveID);

			// this call will return a false if the id already exists in the list
			result = objectiveIDList.add(objectiveID);

			if (!result) {
				msgText = Messages.getString("SequenceValidator.84", objectiveID);

				log.debug("FAILED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
			}
		}

		return result;
	}

	/**
	 * This method determines that if the referencedObjective attribute of a 
	 * ruleCondition is used, then the value represents an identifier of an 
	 * objective (primaryObjective and objective) found within the IMS Manifest.
	 * 
	 * @return Boolean true implies that the referencedObjective attribute references
	 * a valid objectives identifier, false implies otherwise.
	 */
	private boolean checkReferencedObjectives() {
		// flag that rolls up the overall check result
		boolean result = true;
		String referencedObjectiveValue = "";
		String msgText = "";

		int numReferencedObjectives = mReferencedObjectiveList.size();

		if (numReferencedObjectives > 0) {
			// if we have objectives identifiers to compare to, than loop through the
			// lists for a match
			if (!mObjectivesIDList.isEmpty()) {
				for (int i = 0; i < numReferencedObjectives; i++) {
					referencedObjectiveValue = (mReferencedObjectiveList.get(i));
					// flag that is used to signal error messages to log
					boolean foundMatch = true;

					// tests to see if the referencedObjective value exists in the 
					// list that contains all the primaryObjective and objective elements
					// identifier values
					if (!mObjectivesIDList.contains(referencedObjectiveValue)) {
						// referencedObjective has NOT been found, error
						foundMatch = false;
						result = false;
					}
					if (!foundMatch) {
						msgText = Messages.getString("SequenceValidator.197", referencedObjectiveValue);
						log.debug("FAILED: " + msgText);
						DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
					}
				}
			} else {
				// error, we have referenceObjectives but no 
				// objective (primary/objective) identifier values
				result = false;
				msgText = Messages.getString("SequenceValidator.198");
				log.debug("FAILED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
			}
		}

		return result;
	}

	/**
	 * This method validates the sequencingcollection, if it exists, against the
	 * rules defined in the Sequence Application Profile.
	 *
	 * @param iSequencingcollectionNode  The sequencingCollection node
	 *
	 * @return Boolean describing if the sequencingCollection element is
	 * value or not.
	 */
	private boolean checkSequencingcollection(Node iSequencingcollectionNode) {
		boolean result = true;
		boolean foundSequencing = false;
		String msgText = "";

		// loop through children and call compare
		NodeList kids = iSequencingcollectionNode.getChildNodes();
		Node currentNode = null;

		//cycle through all children of node to find the <sequencing> nodes
		if (kids != null) {
			int n = kids.getLength();

			for (int i = 0; i < n - 1; i++) {
				currentNode = kids.item(i);

				// find the <sequencing> nodes
				if (currentNode.getLocalName().equals("sequencing")) {
					foundSequencing = true;
					// validate to the application profiles
					result = compareToRules(currentNode, "sequencingCollection") && result;
				}
			}
		}
		if (!foundSequencing) {
			//report an error for not having mandatory sequencing children
			result = false;
			msgText = Messages.getString("SequenceValidator.15");
			log.debug("FAILED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
		}
		return result;
	}

	/**
	  * This method performs the smallest permitted maximum check.
	  *
	  * @param iElementName Name of the element being checked for SPM.
	  * @param iElementValue Value being checked for SPM.
	  * @param iSPMRule Value allowed for spm ( value retrieved from rules ).
	  * @param iAmAnAttribute flags determines if its an attribute (true), or an
	  * element that is being validated for valid vocabulary tokens.
	  *
	  * @return - Boolean result of spm check.  A true value implies that the
	  * smallest permitted checks passed, false implies otherwise.
	  *
	  */
	private boolean checkSPMConformance(String iElementName, String iElementValue, int iSPMRule, boolean iAmAnAttribute) {
		boolean result = true;
		String msgText = "";

		int elementValueLength = iElementValue.length();

		if (iSPMRule != -1) {
			if (elementValueLength > iSPMRule) {
				if (iAmAnAttribute) {
					msgText = Messages.getString("SequenceValidator.86", iElementName, iSPMRule);
				} else {
					msgText = Messages.getString("SequenceValidator.90", iElementName, iSPMRule);
				}
				log.debug("WARNING: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));
			} else if (elementValueLength < 1) {
				if (iAmAnAttribute) {
					msgText = Messages.getString("SequenceValidator.94", iElementName);
				} else {
					msgText = Messages.getString("SequenceValidator.96", iElementName);

				}
				log.debug("FAILED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

				result = false;
			} else {
				if (iAmAnAttribute) {
					msgText = Messages.getString("SequenceValidator.99", iElementName);
				} else {
					msgText = Messages.getString("SequenceValidator.101", iElementName);
				}
				log.debug("PASSED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
			}
		} else if (elementValueLength < 1) {
			if (iAmAnAttribute) {
				msgText = Messages.getString("SequenceValidator.94", iElementName);
			} else {
				msgText = Messages.getString("SequenceValidator.106", iElementName);
			}
			log.debug("FAILED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

			result = false;
		} else {
			if (iAmAnAttribute) {
				msgText = Messages.getString("SequenceValidator.99", iElementName);
			} else {
				msgText = Messages.getString("SequenceValidator.101", iElementName);
			}
			log.debug("PASSED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
		}

		return result;
	}

	/**
	 * Determines if the vocabulary value is a valid vocabulary token based on
	 * the rules defined in the Application Profile. It is assumed that only
	 * 1 vocabulary token may exist for an element/attribute.
	 *
	 * @param iName Name of the element/attribute being checked for valid
	 * vocabulary.
	 *  
	 * @param iValue Vocabulary string value that exists for the
	 * element/attribute in the test subject.
	 * 
	 * @param iVocabValues List containing a list of the valid vocabulary 
	 * values for the element/attribute.
	 * 
	 * @param iAmAnAttribute flags determines if its an attribute (true), or an
	 * element that is being validated for valid vocabulary tokens.
	 *
	 * @return Returns <code>true</code> if the value is a valid vocab token,
	 * <code>false</code> otherwise.<br>
	 *
	 */
	private boolean checkVocabulary(String iName, String iValue, List<String> iVocabValues, boolean iAmAnAttribute) {
		log.debug("checkVocabulary()");

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

			if (iAmAnAttribute) {

				msgText = Messages.getString("SequenceValidator.116", iValue, iName);
			} else {

				msgText = Messages.getString("SequenceValidator.119", iValue, iName);

			}
			log.debug("PASSED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
		} else {

			if (iAmAnAttribute) {

				msgText = Messages.getString("SequenceValidator.123", iValue, iName);

			} else {

				msgText = Messages.getString("SequenceValidator.126", iValue, iName);

			}
			log.debug("FAILED: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
		}

		if ((iName.equals("condition")) && ((iValue.equals("timeLimitExceeded")) || (iValue.equals("outsideAvailableTimeRange")))) {
			// this vocabulary token is out of scope of SCORM
			msgText = Messages.getString("SequenceValidator.133", iValue, iName);

			log.debug("WARNING: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));

		}
		log.debug("checkVocabulary()");

		return result;
	}

	/**
	 * A recursive method that is driven by the test subject dom.
	 * This method performs the application profiles rules checks.
	 *
	 * @param iTestSubjectNode Test Subject DOM
	 * @param iPath Path of the rule to compare to
	 *
	 * @return - Boolean result of the checks performed
	 *
	 */
	private boolean compareToRules(Node iTestSubjectNode, String iPath) {
		// looks exactly like prunetree as we walk down the tree
		log.debug("compareToRules");

		boolean result = true;
		String msgText = "";

		// is there anything to do?
		if (iTestSubjectNode == null) {
			result = false;
			return result;
		}

		int type = iTestSubjectNode.getNodeType();

		switch (type) {
		// element with attributes
		case Node.ELEMENT_NODE: {
			String parentNodeName = iTestSubjectNode.getLocalName();

			result = checkAttributes(iTestSubjectNode, parentNodeName, iPath) && result;

			String dataType = null;
			int multiplicityUsed = -1;
			int minRule = -1;
			int maxRule = -1;
			int spmRule = -1;

			// Test the child Nodes
			NodeList children = iTestSubjectNode.getChildNodes();

			if (children != null) {
				int numChildren = children.getLength();

				// update the path for this child element
				String path;

				if (iPath.isEmpty() || parentNodeName.equalsIgnoreCase("sequencing")) {
					// the Node is a DOCUMENT OR
					// the Node is a <manifest>

					path = parentNodeName;
				} else {
					path = iPath + "." + parentNodeName;
				}

				// SPECIAL CASE: check for mandatory elements
				// there are currently no mandatory elements

				for (int z = 0; z < numChildren; z++) {
					Node currentChild = children.item(z);
					String currentChildName = currentChild.getLocalName();

					// must enforce that the adlseq namespaced elements exist
					// as a child of sequencing only.
					if (((currentChildName.equals("constrainedChoiceConsiderations")) || (currentChildName.equals("rollupConsiderations")))
					        && (!parentNodeName.equals("sequencing"))) {

						result = false;

						msgText = Messages.getString("SequenceValidator.8", currentChildName, "sequencing");

						log.debug("FAILED: " + msgText);
						DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
					}

					dataType = mRulesValidator.getRuleValue(currentChildName, path, "datatype");

					// make sure that this is a SCORM recognized attribute
					if (!dataType.equalsIgnoreCase("-1")) {
						msgText = Messages.getString("SequenceValidator.30", currentChildName);
						log.debug("INFO: " + msgText);
						DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

						// check for multiplicity if the element is not deprecated
						if (!dataType.equalsIgnoreCase("deprecated")) {
							multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, currentChildName);

							// get the min rule and convert to an int
							minRule = Integer.parseInt(mRulesValidator.getRuleValue(currentChildName, path, "min"));
							//get the max rule and convert to an int
							maxRule = Integer.parseInt(mRulesValidator.getRuleValue(currentChildName, path, "max"));

							if ((minRule != -1) && (maxRule != -1)) {
								if (multiplicityUsed >= minRule && multiplicityUsed <= maxRule) {
									msgText = Messages.getString("SequenceValidator.36", currentChildName);
									log.debug("PASSED: " + msgText);
									DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
								} else {
									msgText = Messages.getString("SequenceValidator.39", currentChildName);
									log.debug("FAILED: " + msgText);
									DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

									result = false;
								}
							} else if ((minRule != -1) && (maxRule == -1)) {
								if (multiplicityUsed >= minRule) {
									msgText = Messages.getString("SequenceValidator.36", currentChildName);
									log.debug("PASSED: " + msgText);
									DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
								} else {
									msgText = Messages.getString("SequenceValidator.39", currentChildName);
									log.debug("FAILED: " + msgText);
									DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
									result = false;
								}
							}
						}

						// check the contents of the attribute
						if (dataType.equalsIgnoreCase("parent")) {
							//This is a parent element

							// special validation for sequencingCollection
							if (currentChildName.equals("sequencingCollection") && iPath.isEmpty()) {

								result = checkSequencingcollection(currentChild) && result;
							}

							if (currentChildName.equals("objectives")) {
								// we must enforce that objectiveIDs must be unique
								// for a given activity only.
								result = checkObjectiveIDsForUniqueness(currentChild) && result;
							}

							// objectiveID is mandatory for primaryObjective 
							// only if it contains a mapInfo as a child
							if (currentChildName.equals("primaryObjective")) {
								// Test the child Nodes
								NodeList objChildren = currentChild.getChildNodes();

								boolean isObjectiveIDMandatory = false;
								for (int i = 0; i < objChildren.getLength(); i++) {
									Node objChild = objChildren.item(i);
									String objChildName = objChild.getLocalName();

									if (objChildName.equals("mapInfo")) {
										isObjectiveIDMandatory = true;
									}
								}

								if (isObjectiveIDMandatory) {
									// ObjectiveID attribute is mandatory if mapInfo
									// child elements are present.
									Attr currAttribute = DOMTreeUtility.getAttribute(currentChild, "objectiveID");

									if (currAttribute == null) {
										result = result && false;
										msgText = Messages.getString("SequenceValidator.55");

										log.debug("FAILED: " + msgText);
										DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

									}
								}

							}

							// special validation for global objectives
							if (currentChildName.equals("objectives") && path.equals("sequencing")) {
								msgText = Messages.getString("SequenceValidator.59");
								log.debug("INFO: " + msgText);
								DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

								mObjectiveInfo.populateObjectiveMap(currentChild);

								result = mObjectiveInfo.validateObjectiveMaps(mObjectiveInfo) && result;
							}

							if (currentChildName.equals("auxiliaryResources") && path.equals("sequencing")) {
								// this element is out of scope of SCORM
								msgText = Messages.getString("SequenceValidator.63", currentChildName);
								log.debug("WARNING: " + msgText);
								DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));
							}

							result = compareToRules(currentChild, path) && result;

						} else if (dataType.equalsIgnoreCase("deprecated")) {
							// This is a deprecated element
							msgText = Messages.getString("SequenceValidator.67", currentChildName);
							log.debug("FAILED: " + msgText);
							DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
							result = false;
						} else if (dataType.equalsIgnoreCase("text")) {
							// This is a text data type
							// check spm

							// first must retrieve the value of this child element
							String currentChildValue = mRulesValidator.getTaggedData(currentChild);

							//get the spm rule and convert to an int
							spmRule = Integer.parseInt(mRulesValidator.getRuleValue(currentChildName, path, "spm"));

							result = checkSPMConformance(currentChildName, currentChildValue, spmRule, false) && result;
						} else if (dataType.equalsIgnoreCase("vocabulary")) {
							// This is a vocabulary data type
							// more than one vocabulary token may exist

							msgText = Messages.getString("SequenceValidator.73", currentChildName);
							log.debug("INFO: " + msgText);
							DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

							// retrieve the value of this element
							String currentChildValue = mRulesValidator.getTaggedData(currentChild);

							List<String> vocabValues = mRulesValidator.getVocabRuleValues(currentChildName, path);

							result = checkVocabulary(currentChildName, currentChildValue, vocabValues, false) && result;
						}
						if (dataType.equalsIgnoreCase("decimal")) {
							//This is a decimal element
						} else if (dataType.equalsIgnoreCase("leaf")) {
							// This is a leaf data type, must check attributes
							result = checkAttributes(currentChild, currentChildName, path) && result;
						} else {
							// This is an extension element
							// no additional checks needed
						}
					}
				}
			}
			break;
		}

		default: {
			break;
		}
		}

		log.debug("compareToRules()");

		return result;
	}

	/**
	 * Returns the number of attributes with the given name based on the
	 * attributelist passed in.
	 *
	 * @param iAttributeMap List of attributes
	 * @param iNodeName Name of the element being searched for.
	 *
	 * @return \Number of instances of a given attribute.
	 *
	 */
	public int getMultiplicityUsed(NamedNodeMap iAttributeMap, String iNodeName) {
		int result = 0;
		int length = iAttributeMap.getLength();
		String currentName;

		for (int i = 0; i < length; i++) {
			currentName = ((Attr) iAttributeMap.item(i)).getLocalName();

			if (currentName.equals(iNodeName)) {
				result++;
			}
		}

		return result;
	}

	/**
	  * Returns the number of elements with the given
	  * name based on the given parent node of the dom tree.
	  *
	  * @param iParentNode Parent node of the element being searched.
	  * @param iNodeName Name of the element being searched for.
	  *
	  * @return - int: number of instances of a given element
	  */
	public int getMultiplicityUsed(Node iParentNode, String iNodeName) {
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
	* This method is called to initiate the validation process.
	* This method will trigger the parsing activity done by the 
	* <code>ADLSCORMValidator</code>. Next, the DOM will be used to validate 
	* the remaining checks required for full SCORM Validation.
	*
	* @param iRootNode Root sequence element.
	*
	* @return - Boolean value indicating the outcome of the validation checks.
	*/
	public boolean validate(Node iRootNode) {
		boolean validateResult = true;
		String msgText;
		String nodeName = iRootNode.getLocalName();

		log.debug("validate()");

		log.debug("      iRootNodeName coming in is " + nodeName);

		mRulesValidator.readInRules("sequence");

		// check the parent and make sure it is in the right place
		String parentNodeName = iRootNode.getParentNode().getLocalName();

		if (nodeName.equals("sequencingCollection")) {
			if (!parentNodeName.equals("manifest")) {
				msgText = Messages.getString("SequenceValidator.8", "sequencingCollection", "manifest");
				log.debug("FAILED: " + msgText);
				DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
				validateResult = false;
			}

		}
		// check the <sequencing> element and its children
		validateResult = compareToRules(iRootNode, "") && validateResult;
		// continuation of the application profile check, must validate that the 
		// referencedObjective attribute references an objective 
		//attribute (primary / objective)
		validateResult = checkReferencedObjectives() && validateResult;

		log.debug("validate()");

		return validateResult;
	}
}