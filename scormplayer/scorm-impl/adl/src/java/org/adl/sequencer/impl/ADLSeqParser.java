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

package org.adl.sequencer.impl;


import org.sakaiproject.util.Xml;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * <strong>Filename:</strong> ADLSeqParser.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * Provides the elements from an imsmanefest.xml file required to build an
 * activity tree for the course.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the 
 * SCORM 2004 3rd Edition Sample RTE.<br>
 * <br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS Specification</li>
 *     <li>SCORM 2004 3rd Edition</li>
 * </ul>
 * 
 * @author ADL Technical Team
 */
@Slf4j
public class ADLSeqParser {

    /**
	 * This controls display of log messages to the java console
	 */
	private String mFileToParse = null;

	/**
	 * Identifier of the course being parsed.  
	 */
	private String mCourseID = null;

	/**
	 * Identifier of the default organization.
	 * This is the content aggregation to be sequenced.
	 */
	private String mOrganizationID = null;

	/**
	 * Declares if the global shared objectives used by the content structure
	 * are global to the system.
	 */
	private boolean mGlobalToSystem = true;

	/**
	 * Contains the <code>&lt;sequencingCollection&gt;</code> XML fragment.
	 */
	private Node mSequencingCollection = null;

	/**
	 * The parsed XML document
	 */
	private Document mDocument = null;

	/**
	 * Initializes the current test case.
	 * 
	 * @param iFileName Name of the XML file containing the intended test script.
	 */
	public ADLSeqParser(String iFileName) {

        log.debug("  :: ADLSeqParser   --> BEGIN - constructor\n  ::--> {}", iFileName);
		mFileToParse = iFileName;
	}

	/**
	 * Attempts to parse the associated imsmanifest.xml file and extract the
	 * default <code>&lt;organization&gt;</code> element.  This element will be 
	 * used to construct an activity tree for this course.
	 * 
	 * @return The default <code>&lt;organization&gt;</code> element of the CP, 
	 * or <code>null</code> if the parse fails.
	 */
	public Node findDefaultOrganization() {

        log.debug("  :: ADLSeqParser   --> BEGIN - findDefaultOrganization");

		Node organization = null;

		// Parse the target XML document
		boolean result = parseFile();

		if (result) {

			// Get the root node of the imsmanifest.xml file <manifest>
			Node root = mDocument.getDocumentElement();

			// Get and set the identifier of the course
			// This is a required attribute, so we don't have to test for null.
			mCourseID = getAttribute(root, "identifier");

			// Get the children of the root node
			NodeList children = root.getChildNodes();

			boolean done = false;
			boolean foundFirstOrg = false;

			// Find the <organizations> node
			for (int i = 0; i < children.getLength(); i++) {
				Node curNode = children.item(i);

				// Make sure this is an "element node"
				if (curNode.getNodeType() == Node.ELEMENT_NODE) {
					if (curNode.getLocalName().equals("organizations")) {
                        log.debug("  ::--> Found the <organizations> element");

						// Get and set the identifier for the default organization
						mOrganizationID = getAttribute(curNode, "default");

						// Get the children of the <organizations> node
						NodeList orgs = curNode.getChildNodes();

						// Find the <oranization> nodes -- and match the default 
						for (int j = 0; j < orgs.getLength() && !done; j++) {
							Node curOrg = orgs.item(j);

							// Make sure this is an "element node"
							if (curOrg.getNodeType() == Node.ELEMENT_NODE) {
								if (curOrg.getLocalName().equals("organization")) {
                                    log.debug("  ::--> Found an <organization> element");

									// Compare this organization's ID to the default
									String id = getAttribute(curOrg, "identifier");

									if (mOrganizationID != null) {
										if (id.equals(mOrganizationID)) {

											// Check the scope of the objectives   
											// for this organization 

											String temp = getAttribute(curOrg, "objectivesGlobalToSystem");

											if (temp != null) {
												mGlobalToSystem = (Boolean.valueOf(temp));
											}

											// We found the default organization
											organization = curOrg;
											done = true;
											continue;
										}
									} else {
										if (!foundFirstOrg) {
											mOrganizationID = id;
											organization = curOrg;
											done = true;
											continue;
										}
									}

									if (!foundFirstOrg) {
										foundFirstOrg = true;
									}

								}
							}
						}

						// We are done looking at the <organizations> element
						// Make sure we have found the default
						if (organization == null) {
                            log.debug("  ::--> Default <organization> not found, using the first one.");

							// Use the first <organization> by default
							organization = orgs.item(0);
						}
					} else if (curNode.getLocalName().equals("resources")) {
                        log.debug("  ::--> Found the <resources> element");
					} else if (curNode.getLocalName().equals("sequencingCollection")) {
                        log.debug("  ::--> Found the <sequencingCollection> element");
						mSequencingCollection = curNode;
					}
				}
			}
		} else {
            log.debug("  ::-->  ERROR: Parse failed");
		}

        log.debug("  :: ADLSeqParser   --> END   - " + "findDefaultOrganization");
		return organization;
	}

	/**
	 * Attempts to find the indicated attribute of the target element.
	 * 
	 * @param iNode      The DOM node of the target element.
	 * 
	 * @param iAttribute The requested attribute.
	 * 
	 * @return The value of the requested attribute on the target element, or
	 *         <code>null</code> if the attribute does not exist.
	 */
	private String getAttribute(Node iNode, String iAttribute) {

        log.debug("  :: ADLSeqParser   --> BEGIN - getAttribute\n  ::-->  {}", iAttribute);
        String value = null;

		// Extract the node's attribute list and check if the requested
		// attribute is contained in it.
		NamedNodeMap attrs = iNode.getAttributes();

		if (attrs != null) {

			Attr currentAttrNode;
			String currentNodeName;

			// loop through the attributes and get their values assuming
			// that the multiplicity of each attribute is 1 and only 1.
			for (int k = 0; k < attrs.getLength(); k++) {
				currentAttrNode = (Attr) attrs.item(k);
				currentNodeName = currentAttrNode.getLocalName();

				// store the value of the attribute
				if (currentNodeName.equalsIgnoreCase(iAttribute)) {
					value = currentAttrNode.getNodeValue();
				}
			}

			if (value == null) {
                log.debug("  ::-->  The attribute \"{}\" does not exist.", iAttribute);
			}
		} else {
            log.debug("  ::-->  This node has no attributes.");
		}
        log.debug("  ::-->  {}\n  :: ADLSeqParser   --> END - getAttribute", value);
		return value;
	}

	/**
	 * Retrieves the identifier for this course.
	 * 
	 * @return The identifier (<code>String</code>) for this course, or
	 *         <code>null</code> if the document has not been initialized.
	 */
	public String getCourseID() {

        log.debug("""
                  :: ADLSeqParser   --> BEGIN - getCourseID
                  ::-->  {}
                  :: ADLSeqParser   --> END   - getCourseID
                """, mCourseID);
        return mCourseID;
    }

	/**
	 * Retrieves the identifier for the default organization.
	 * 
	 * @return The identifier (<code>String</code>) for the default organization,
	 *         or <code>null</code> if the document has not been initialized.
	 */
	public String getOrganizationID() {

        log.debug("""
                  :: ADLSeqParser   --> BEGIN - getOrganizationID
                  ::-->  {}
                  :: ADLSeqParser   --> END   - getOrganizationID
                """, mOrganizationID);
        return mOrganizationID;
	}

	/**
	 * Returns the scope ID for the objectives associated with this activity
	 * tree.
	 * 
	 * @return The ID associated with the scope of this activity tree's
	 *         objectives, or <code>null</code> if the objectives are global to
	 *         the system.
	 */
	public String getScopeID() {

        log.debug("""
                    :: ADLSeqParser   --> BEGIN - getScopeID
                    ::-->  {}
                  """, mGlobalToSystem);

        String scopeID = null;

		if (!mGlobalToSystem) {
			scopeID = mCourseID + "__" + mOrganizationID;
		}

        log.debug("""
                    ::-->  {};
                    :: ADLSeqParser   --> END   - getScopeID
                  """, scopeID);
		return scopeID;
	}

	/**
	 * Retrieves the <code>&lt;sequencingCollection&gt;</code> node, if one 
	 * exists.
	 * 
	 * @return The XML fragment (<code>Node</code>) for the collection of
	 *         reusable sequencing rules <code>null</code> if one does not exist.
	 */
	public Node getSequencingCollection() {

        log.debug("""
                    :: ADLSeqParser   --> BEGIN - getSequencingCollection
                    ::-->  {}
                    :: ADLSeqParser   --> END   - getSequencingCollection
                  """, mSequencingCollection);
		return mSequencingCollection;
	}

	/**
	 * Parses the imsmanifest XML file associated with this course. 
	 * 
	 * @return <code>true</code> if the XML file was successfully parsed,
	 *         otherwise <code>false</code>.
	 */
	private boolean parseFile() {
		mDocument = mFileToParse == null ? null : Xml.readDocument(mFileToParse);
		return mDocument != null && mDocument.hasChildNodes();
	}
}
