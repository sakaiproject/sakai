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
package org.adl.parsers.dom;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <strong>Filename:</strong> DOMTreeUtility.java<br><br>
 *
 * <strong>Description:</strong>The DOM Tree Utility provides the ability to
 * access subsets of the DOM tree.  This class serves as a utility class for
 * DOM trees manipulation.  <br><br>
 *
 * <strong>Design Issues:</strong> None<br>
 *
 * <strong>Implementation Issues:</strong> None<br><br>
 *
 * <strong>Known Problems:</strong> None<br><br>
 *
 * <strong>Side Effects:</strong> None<br><br>
 *
 * <strong>References:</strong> None<br><br>
 *
 * @author ADL Technical Team
 */
public class DOMTreeUtility {

	/**
	 * A constant string representing the IMS Content Packaging Namespace
	 */
	public static final String IMSCP_NAMESPACE = "http://www.imsglobal.org/xsd/imscp_v1p1";

	/**
	 * A constant string representing the ADL Content Packaging Extension 
	 * Namespace: http://www.adlnet.org/xsd/adlcp_v1p3
	 */
	public static final String ADLCP_NAMESPACE = "http://www.adlnet.org/xsd/adlcp_v1p3";

	/**
	 * A constant string representing the ADL Sequencing Extension Namespace:
	 * http://www.adlnet.org/xsd/adlseq_v1p3
	 */
	public static final String ADLSEQ_NAMESPACE = "http://www.adlnet.org/xsd/adlseq_v1p3";

	/**
	 * A constant string representing the ADL Navigation Extension Namespace:
	 * http://www.adlnet.org/xsd/adlnav_v1p3
	 */
	public static final String ADLNAV_NAMESPACE = "http://www.adlnet.org/xsd/adlnav_v1p3";

	/**
	 * A constant string representing the IMS Simple Sequencing Namespace: 
	 * http://www.imsglobal.org/xsd/imsss
	 */
	public static final String IMSSS_NAMESPACE = "http://www.imsglobal.org/xsd/imsss";

	/**
	 * A constant string representing the IEEE LOM Namespace: 
	 * http://ltsc.ieee.org/xsd/LOM
	 */
	public static final String IEEE_LOM_NAMESPACE = "http://ltsc.ieee.org/xsd/LOM";

	/**
	 * A constant string representing the IMS SSP Namespace.
	 */
	public static final String IMSSSP_NAMESPACE = "http://www.imsglobal.org/xsd/imsssp";

	private static Log log = LogFactory.getLog(DOMTreeUtility.class);

	/**
	 * This method returns the attribute of the given node whose name matches
	 * the named value (iAttributeName) and a particular namespace
	 * (iNamespaceForAttr).
	 *
	 * @param iNode The element containing the attribute
	 * @param iAttributeName The name of the attribute being retrieved
	 *
	 * @return Returns the attribute matching the name and namespace
	 */
	public static Attr getAttribute(Node iNode, String iAttributeName) {
		log.debug("DOMTreeUtility getAttribute()");
		Attr result = null;

		// Determine if the node is null
		if (iNode != null) {
			log.debug("Parent Node: " + iNode.getLocalName());
			log.debug("Node being searched for: " + iAttributeName);
			// If the node is not null, then get the list of attributes from
			// the node
			NamedNodeMap attrList = iNode.getAttributes();

			int numAttr = attrList.getLength();

			Attr currentAttrNode = null;
			String currentNodeName = null;

			// Loop through the attributes and get their values assuming
			// that the multiplicity of each attribute is 1 and only 1.
			for (int k = 0; k < numAttr; k++) {
				// Get the attribute
				currentAttrNode = (Attr) attrList.item(k);

				// Get the local name of the attribute
				currentNodeName = currentAttrNode.getLocalName();

				// First check to see if the current node is the one with the
				// same Local Name as the value we are looking for (iAttributeName)
				if (currentNodeName.equalsIgnoreCase(iAttributeName)) {
					// We have found a node that shares the same name as the
					// node we are looking for (iAttributeName).                       // Matching attribute found
					result = currentAttrNode;
					break;
				}
			} // end for loop
		}

		return result;
	}

	/**
	 * This method returns the value of the attribute that matches the
	 * attribute name (iAttributeName) and namepace (iNamespaceForAttr) in
	 * the node.  This is to cover cases where elements have multiple
	 * attributes that have the same local name but come from different
	 * namespaces.
	 *
	 * @param iNode The element containing the attribute
	 * @param iAttributeName The name of the attribute being retrieved
	 * 
	 * @return Returns the value the attribute<br>
	 */
	public static String getAttributeValue(Node iNode, String iAttributeName) {
		log.debug("DOMTreeUtility getAttributeValue()");
		log.debug("Parent Node: " + iNode.getLocalName());
		log.debug("Node being searched for: " + iAttributeName);
		String result = "";
		// Get the attribute from the node matching the attribute name
		// and namespace
		Attr theAttribute = getAttribute(iNode, iAttributeName);

		// Make sure the attribute was present for the element
		if (theAttribute != null) {
			// If present, retrieve the value of the attribute
			result = theAttribute.getValue();
		}
		// return the value
		return result;
	}

	/**
	 * This method returns the desired node which is determined by the
	 * provided node name and namespace.
	 *
	 * @param iNode The provided node structure to be traversed.
	 * @param iNodeName The name of the node being searched for.
	 *
	 * @return Returns the desired node.
	 */
	public static Node getNode(Node iNode, String iNodeName) {
		log.debug("DOMTreeUtility getNode()");
		Node result = null;

		if (iNode != null) {
			log.debug("Parent Node: " + iNode.getLocalName());
			log.debug("Node being searched for: " + iNodeName);
			
			// Get the children of the current node
			NodeList children = iNode.getChildNodes();

			// If there are children, loop through the children nodes looking
			// for the appropriate node
			if (children != null) {
				for (int i = 0; i < children.getLength(); i++) {
					// Get the child node
					Node currentChild = children.item(i);

					// Get the current child node's local name
					String currentChildName = currentChild.getLocalName();
					log.debug("Child #" + i + ": " + currentChildName);

					if (currentChildName != null) {
						// Determine if the current child node is the one that
						// is being looked for
						if (currentChildName.equalsIgnoreCase(iNodeName)) {
							result = currentChild;
							break;
						}
					}
				} // end looping of children
			}
		}

		// return the resulting vector of nodes
		return result;
	}

	/**
	 * This method returns an ordered list containing the desired nodes which is
	 * determined by the provided node name and namespace.
	 *
	 * @param iNode The provided node structure to be traversed.
	 * @param iNodeName The name of the node being searched for.
	 *
	 * @return Returns an ordered list containing the desired nodes.
	 */
	public static List<Node> getNodes(Node iNode, String iNodeName) {
		log.debug("DOMTreeUtility getNodes()");
		// Create a vector to hold the results of the method
		List<Node> result = new ArrayList<>();

		// Check to see if the input node is null
		if (iNode != null) {
			log.debug("Parent Node: " + iNode.getLocalName());
			log.debug("Node being searched for: " + iNodeName);
			// Get the set of child nodes of the input node
			NodeList children = iNode.getChildNodes();

			// If there are children nodes loop through them looking for
			// the node matching the name and namespace
			if (children != null) {
				int numChildren = children.getLength();

				// Loop over the children searching for the desired node
				for (int i = 0; i < numChildren; i++) {
					// get the current child in the list
					Node currentChild = children.item(i);

					// Get the local name of the child node
					String currentChildName = currentChild.getLocalName();

					if (currentChildName != null) {
						// Determine if the current child node is the one that
						// is being looked for
						if (currentChildName.equalsIgnoreCase(iNodeName)) {
							result.add(currentChild);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * This method gets the text node of an input node.
	 *
	 * @param iNode The node that contains the desired text node
	 *
	 * @return Returns the desired value of the node.
	 */
	public static String getNodeValue(Node iNode) {
		// Create a string to hold the results.
		String value = "";

		// Check to make sure node is not null
		if (iNode != null) {
			// Get a list of the children of the input node
			NodeList children = iNode.getChildNodes();

			// Cycle through all children of node to get the text
			if (children != null) {
				int numChildren = children.getLength();
				for (int i = 0; i < numChildren; i++) {
					// make sure we have a text element
					if ((children.item(i).getNodeType() == Node.TEXT_NODE) || (children.item(i).getNodeType() == Node.CDATA_SECTION_NODE)) {
						value = value + children.item(i).getNodeValue().trim();
					}
				} // end looping over the children nodes
			}
		}

		// Return the value of the node.
		return value;
	}

	/**
	 * This method determins if a node in the DOM Tree <code>(iNode)</code> is
	 * the node we are looking for.  This is done by comparing the node's
	 * local name and namespace with a given node name <code>(iNodeName)</code>
	 * and namespace <code>(iNamespace)</code>.
	 *
	 * @param iNode The Node we are trying to determine if it is the correct
	 *              node
	 * @param iNodeName The name of the node we are looking for.
	 * @param iNamespace The namespace of the node we are looking for.
	 *
	 * @return A boolean value indicating whether or not this is the
	 *         correct node we are looking for
	 */
	public static boolean isAppropriateElement(Node iNode, String iNodeName, String iNamespace) {
		log.debug("DOMTreeUtility isAppropriateElement()");
		log.debug("Input Parent Node: " + iNode.getLocalName());
		log.debug("Input Node being searched for: " + iNodeName);
		log.debug("Input Namespace of node being searched for: " + iNamespace);

		boolean result = false;

		if (iNode.getNodeType() == Node.ATTRIBUTE_NODE) {
			if (iNode.getNamespaceURI() == null) {
				// Attribute has been passed in and its namepsace is null, get the
				// attributes parent's namespace
				String parentsNamespace = ((Attr) iNode).getOwnerElement().getNamespaceURI();
				if ((iNode.getLocalName().equals(iNodeName)) && (parentsNamespace.equals(iNamespace))) {
					result = true;
				}
			} else {
				if ((iNode.getLocalName().equals(iNodeName)) && (iNode.getNamespaceURI().equals(iNamespace))) {
					result = true;
				}
			}
		} else if ((iNode.getLocalName().equals(iNodeName)) && (iNode.getNamespaceURI().equals(iNamespace))) {
			result = true;
		}

		return result;
	}

	/**
	 * This method determines whether or not the current node is a SCORM
	 * Application Profile node.  The parent node is needed for cases where the
	 * current node is an attribute node
	 * 
	 * @param iCurrentNode The current node being processed
	 * @param iParentNode The parent of the current node being processed
	 * @return Returns whether or not (true/false) the current node is a SCORM
	 * application profile node
	 */
	public static boolean isSCORMAppProfileNode(Node iCurrentNode, Node iParentNode) {
		log.debug("DOMTreeUtility isSCORMAppProfileNode");
		log.debug("Input Current Node: " + iCurrentNode.getLocalName());
		log.debug("Input Parent Node: " + iParentNode.getLocalName());

		boolean result = false;

		// If the current node is from one of the known SCORM testable
		// namespaces then return true
		String namespace = iCurrentNode.getNamespaceURI();

		if (namespace == null) {
			String parentsNamespace = StringUtils.trimToEmpty(iParentNode.getNamespaceURI());

			// Check the parent nodes namespace
			if ((parentsNamespace.equals(ADLCP_NAMESPACE)) 
					|| (parentsNamespace.equals(IMSCP_NAMESPACE)) 
					|| (parentsNamespace.equals(ADLNAV_NAMESPACE))
			        || (parentsNamespace.equals(IEEE_LOM_NAMESPACE)) 
			        || (parentsNamespace.equals(ADLSEQ_NAMESPACE))
			        || (parentsNamespace.equals("http://www.w3.org/XML/1998/namespace"))
			        || (parentsNamespace.equals("http://www.w3.org/2001/XMLSchema-instance")) 
			        || (parentsNamespace.equals("http://www.w3.org/2000/xmlns/"))
			        || (parentsNamespace.equals(IMSSSP_NAMESPACE)) 
			        || (parentsNamespace.equals(IMSSS_NAMESPACE))) {
				result = true;
			}
		} else if ((namespace.equals(ADLCP_NAMESPACE)) 
				|| (namespace.equals(IMSCP_NAMESPACE)) 
				|| (namespace.equals(IEEE_LOM_NAMESPACE))
		        || (namespace.equals(ADLNAV_NAMESPACE)) 
		        || (namespace.equals(ADLSEQ_NAMESPACE)) 
		        || (namespace.equals("http://www.w3.org/XML/1998/namespace"))
		        || (namespace.equals("http://www.w3.org/2001/XMLSchema-instance")) 
		        || (namespace.equals("http://www.w3.org/2000/xmlns/"))
		        || (namespace.equals(IMSSSP_NAMESPACE)) 
		        || (namespace.equals(IMSSS_NAMESPACE))) {
			result = true;
		}

		return result;
	}

	/**
	 * This method removes the specified attribute from the specified node
	 *
	 * @param iNode The node whose attribute is to be removed
	 * @param iAttributeName The name of the attribute to be removed
	 */
	public static void removeAttribute(Node iNode, String iAttributeName) {
		NamedNodeMap attrList = iNode.getAttributes();
		attrList.removeNamedItem(iAttributeName);
	}
}
