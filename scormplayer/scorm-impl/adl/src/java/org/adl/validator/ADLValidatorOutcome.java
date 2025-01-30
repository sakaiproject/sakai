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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.adl.parsers.dom.DOMTreeUtility;
import org.adl.validator.contentpackage.IManifestMap;
import org.adl.validator.contentpackage.ManifestMap;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <strong>Filename: </strong>ADLValidatorOutcome.java<br><br>
 *
 * <strong>Description: </strong> The <code>ADLValidatorOutcome</code> object
 * is returned upon request by the user via the use of the public method
 * available by the ADLSCORMValidator object.  The ADLValidatorOutcome object
 * serves as the central storage of the status of checks performed during the
 * validation activities, including the stored DOM. This object serves as an
 * efficient means for passing the outcome of the validation activites
 * throughout the utilizing system.<br><br>
 *
 * @author ADL Technical Team
 */

public class ADLValidatorOutcome implements IValidatorOutcome {

	/**
	  * 
	  */
	private static final long serialVersionUID = 1L;

	/**
	    * The <code>Document</code> object is an electronic representation of the
	    * XML produced if the parse was successful. A parse for wellformedness
	    * creates a document object while the parse for validation against the
	    * controlling documents creates a document object as well.  This attribute
	    * houses the document object that is created last.  In no document object is
	    * created, the value remains null.
	    */
	private Document mDocument;

	/**
	 * The modified <code>Document</code> object after rollup is performed.
	 * Rollup is performed when an IMS Manifest contains one or more
	 * (sub)manifest elements.  All resources of (sub)manifest elements are
	 * rolled up into the root manifest element.  If a (sub)manifest is
	 * referenced by an item, than the contents of the organization in the
	 * (sub)manifest is rolled up into the item performing the referencing.
	 */
	private Document mRolledUpDocument;

	/**
	 * This attribute describes if the XML instance is found to be wellformed by
	 * the parser.  The value <code>false</code> indicates that the XML 
	 * instance is not wellformed XML, <code>true</code> indicates it is 
	 * wellformed XML.
	 */
	private boolean mIsWellformed;

	/**
	 * This attribute describes if the XML instance is found to be valid against
	 * the controlling documents by the parser.  The value <code>false</code> 
	 * indicates that the XML instance is not valid against the controlling 
	 * documents, <code>true</code> indicates that the XML instance is valid 
	 * against the controlling documents.
	 */
	private boolean mIsValidToSchema;

	/**
	 * This attribute describes if the XML instance is valid to the SCORM
	 * Application Profiles. A <code>true</code> value implies that the 
	 * instance is valid to the rules defined by the Application Profiles, 
	 * <code>false</code> implies otherwise.
	 */
	private boolean mIsValidToApplicationProfile;

	/**
	 * This attribute describes if the XML instance uses extension elements. A
	 * <code>true</code> value implies that extension elements were detected, 
	 * <code>false</code> implies they were not used.
	 */
	private boolean mIsExtensionsUsed;

	/**
	  * This attribute is specific to the content package validator only.  It
	  * describes if the required schemas exist at the root of a content package
	  * test subject necessary for the validation parse.  A <code>true</code>
	  * value implies that the required schemas were detected at the root package,
	  * <code>false</code> implies otherwise.
	  */
	private boolean mDoRequiredCPFilesExist;

	/**
	  * This attribute is specific to the content package validator only.  It
	  * describes if the required IMS Manifest file exists at the root of the
	  * package.  The check is performed before wellformedness due to the order
	  * of events (wellformedness, requiredFilesCheck, schemaValidation, etc.)A
	  * <code>true</code> value implies that the IMS Manifest was detected at 
	  * the root package, <code>false</code> implies otherwise.
	  */
	private boolean mDoesIMSManifestExist;

	/**
	 * This attribute describes if the root element belongs to a namepace that is
	 * not categorized as an extension. For example, if we are dealing with a
	 * content package, this boolean will be set to <code>true</code> if the 
	 * root manifest element belongs to the IMS namespace.  If we are dealing 
	 * with metadata, this boolean will be set to <code>true</code> if we are 
	 * dealing with a root <code>&lt;lom&gt;</code> element that belongs to the 
	 * IEEE LOM namespace.
	 */
	private boolean mIsValidRoot;

	/**
	 * Logger object used for debug logging.
	 */
	private transient Logger mLogger = Logger.getLogger("org.adl.util.debug.validator");

	/**
	 * Default constructor. Sets the attributes to their initial values.
	 * @param iDoc The Test Subject Document
	 * @param iRequiredManifest Indicates whether or not the manifest file exists
	 *        in the Content Package
	 * @param iWell  Indicates whether or not the Test Subject Manifest is 
	 *               Wellformed
	 * @param iValidToSchema  Indicates whether or not the Test Subject Manifest
	 *                        is valid against the schemas
	 * @param iValidToAppProfile Indicates whether or not the Test Subject 
	 *                           Manifest is valid agains the SCORM 
	 *                           Application Profile
	 * @param iExt Indicates whether or not the Test Subject Manifest contains
	 *             XML extensions
	 * @param iRequiredFiles Indicates whether or not the Test Subject Content
	 *                       Package contains all of the require files
	 * @param iIsValidRoot Indicates whether or not the root element in the Test
	 *                     Subject is from the Namespace that is expected
	 */
	public ADLValidatorOutcome(Document iDoc, boolean iRequiredManifest, boolean iWell, boolean iValidToSchema, boolean iValidToAppProfile, boolean iExt,
	        boolean iRequiredFiles, boolean iIsValidRoot) {
		//mLogger = Logger.getLogger("org.adl.util.debug.validator");

		mLogger.entering("ADLValidatorOutcome", "ADLValidatorOutcome()");

		mDocument = iDoc;
		mDoesIMSManifestExist = iRequiredManifest;
		mRolledUpDocument = null;
		mIsWellformed = iWell;
		mIsValidToSchema = iValidToSchema;
		mIsValidToApplicationProfile = iValidToAppProfile;
		mIsExtensionsUsed = iExt;
		mDoRequiredCPFilesExist = iRequiredFiles;
		mIsValidRoot = iIsValidRoot;

		mLogger.exiting("ADLValidatorOutcome", "ADLValidatorOutcome()");
	}

	/**
	 * This method will apply the value of any xml:base attributes of a root
	 * manifest to any file elements in it's resource elements.
	 *
	 * @param iManifestNode - The root <code>&lt;manfiest$gt;</code> node of a 
	 * manifest.
	 */
	@Override
	public void applyXMLBase(Node iManifestNode) {
		mLogger.entering("ADLValidatorOutcome", "applyXMLBase()");
		String x = "";
		String y = "";
		Node currentNode;
		String currentNodeName = "";
		String currentHrefValue = "";
		Attr currentHrefAttr = null;
		Node currentFileNode;
		String fileNodeName = "";
		String fileHrefValue = "";
		//Get base of manifest node
		x = getXMLBaseValue(iManifestNode);

		//get base of resources node
		Node resourcesNode = DOMTreeUtility.getNode(iManifestNode, "resources");
		String resourcesBase = getXMLBaseValue(resourcesNode);
		if ((!x.isEmpty()) && (!resourcesBase.isEmpty()) && (!x.endsWith("/"))) {
			//x += File.separator;
			x += "/";
		}
		x += resourcesBase;

		NodeList resourceList = resourcesNode.getChildNodes();
		if (resourceList != null) {
			String resourceBase = "";
			for (int i = 0; i < resourceList.getLength(); i++) {
				currentNode = resourceList.item(i);
				currentNodeName = currentNode.getLocalName();

				//Apply to resource level
				if (currentNodeName.equals("resource")) {
					resourceBase = getXMLBaseValue(currentNode);

					if ((!x.isEmpty()) && (!resourceBase.isEmpty()) && (!x.endsWith("/"))) {
						//y = x + File.separator + resourceBase;
						y = x + "/" + resourceBase;
					} else {
						y = x + resourceBase;
					}

					currentHrefAttr = DOMTreeUtility.getAttribute(currentNode, "href");
					if (currentHrefAttr != null) {
						currentHrefValue = currentHrefAttr.getValue();
						if ((!y.isEmpty()) && (!currentHrefValue.isEmpty()) && (!y.endsWith("/"))) {
							currentHrefAttr.setValue(y + "/" + currentHrefValue);
						} else {
							currentHrefAttr.setValue(y + currentHrefValue);
						}
					}

					NodeList fileList = currentNode.getChildNodes();
					if (fileList != null) {
						for (int j = 0; j < fileList.getLength(); j++) {
							currentFileNode = fileList.item(j);
							fileNodeName = currentFileNode.getLocalName();
							if (fileNodeName.equals("file")) {
								Attr fileHrefAttr = DOMTreeUtility.getAttribute(currentFileNode, "href");
								fileHrefValue = fileHrefAttr.getValue();
								if ((!y.isEmpty()) && (!fileHrefValue.isEmpty()) && (!y.endsWith("/"))) {
									fileHrefAttr.setValue(y + "/" + fileHrefValue);
								} else {
									fileHrefAttr.setValue(y + fileHrefValue);
								}
							}
						}
					}
				}
			}
		}
		mLogger.exiting("ADLValidatorOutcome", "applyXMLBase()");
	}

	/**
	 * Returns a List filled with all of the manifest Nodes in a DOM
	 *
	 * @param iManifest The manifest node you wish to perform this operation on
	 *
	 * @return Returns a List containing all of the manifest nodes in the
	 * manifest.
	 */
	@Override
	public List<Node> getAllManifests(Node iManifest) {
		mLogger.entering("ADLValidatorOutcome", "getAllManifests()");
		List<Node> resultList = new ArrayList<>();
		List<Node> manifestList = new ArrayList<>();
		Node currentManifest = null;
		if (iManifest != null) {
			manifestList = DOMTreeUtility.getNodes(iManifest, "manifest");
			resultList = new ArrayList<>(manifestList);
		}

		for (int manifestCount = 0; manifestCount < manifestList.size(); manifestCount++) {
			currentManifest = manifestList.get(manifestCount);
			resultList.addAll(getAllManifests(currentManifest));
		}
		mLogger.exiting("ADLValidatorOutcome", "getAllManifests()");
		return resultList;
	}

	/**
	 * Returns a List filled with all of the resource Nodes in a DOM
	 *
	 * @param iManifest The manifest node you wish to perform this operation on
	 *
	 * @return Returns a List containing all of the resource nodes in the
	 * manifest.
	 */
	@Override
	public List<Node> getAllResources(Node iManifest) {
		mLogger.entering("ADLValidatorOutcome", "getAllResources()");
		List<Node> resourceList = new ArrayList<>();
		List<Node> manifestList = new ArrayList<>();
		Node resourcesNode = DOMTreeUtility.getNode(iManifest, "resources");
		resourceList = DOMTreeUtility.getNodes(resourcesNode, "resource");
		manifestList = DOMTreeUtility.getNodes(iManifest, "manifest");
		Node currentManifest = null;

		for (int i = 0; i < manifestList.size(); i++) {
			currentManifest = manifestList.get(i);
			resourceList.addAll(getAllResources(currentManifest));
		}
		mLogger.exiting("ADLValidatorOutcome", "getAllResources()");
		return resourceList;
	}

	/**
	 * This method returns the document created during a parse. A parse for
	 * wellformedness creates a document object while the parse for validation
	 * against the controlling documents creates a seperate document object.
	 *
	 * @return Document -  An electronic representation of the XML produced by
	 * the parse.
	 */
	@Override
	public Document getDocument() {
		return mDocument;
	}

	/**
	 * This method is specific to the Content Package Validator only.  This
	 * method returns whether or not the content package test subject
	 * contains the required IMS Manifest at the root of the package.
	 *
	 * @return boolean - describes if the required IMS Manifest was detected at
	 * the root of the pif, <code>false</code> otherwise.
	 */
	@Override
	public boolean getDoesIMSManifestExist() {
		return mDoesIMSManifestExist;
	}

	/**
	 * This method is specific to the Content Package Validator only.  This
	 * method returns whether or not the content package test subject
	 * contains the required schemas at the root of the package needed for the
	 * validation parse.
	 *
	 * @return boolean - describes if the required schemas were detected at the
	 * root of the pif, <code>false</code> otherwise.
	 */
	@Override
	public boolean getDoRequiredCPFilesExist() {
		return mDoRequiredCPFilesExist;
	}

	/**
	 * This method returns whether or not the XML instance contained extension
	 * elements and/or attributes.  The value <code>false</code> indicates that 
	 * the XML instance does not contain extended elements and/or attributes, 
	 * <code>true</code> indicates that the XML instance did.
	 *
	 * @return boolean - describes if the XML Instance contains extension
	 * elements/attributes.
	 */
	@Override
	public boolean getIsExtensionsUsed() {
		return mIsExtensionsUsed;
	}

	/**
	 * This method returns the boolean that describes if we are dealing with
	 * a valid root manifest (belongs to the IMS namespace) or a valid root
	 * lom element (belongs to the IEEE LOM namespace).
	 * 
	 * @return Returns whether or not the root element is from a Namespace that
	 * was expected.
	 *
	 */
	@Override
	public boolean getIsValidRoot() {
		return mIsValidRoot;
	}

	/**
	 * This method returns whether or not the XML instance was valid to the
	 * application profile checks.  The value <code>false</code> indicates that 
	 * the XML instance is not valid to the application profiles, 
	 * <code>true</code> indicates that the XML instance is valid to the 
	 * application profiles.
	 *
	 * @return boolean - decribes if the XML Instance is valid according to the
	 * SCORM Profiles.
	 */
	@Override
	public boolean getIsValidToApplicationProfile() {
		return mIsValidToApplicationProfile;
	}

	/**
	 * This method returns whether or not the XML instance was valid to the
	 * schema.  The value <code>false</code> indicates that the XML instance is 
	 * not valid against the controlling documents, <code>true</code> indicates 
	 * that the XML instance is valid against the controlling documents.
	 *
	 * @return boolean - decribes if the XML Instance is valid against the
	 * schema(s).
	 */
	@Override
	public boolean getIsValidToSchema() {
		return mIsValidToSchema;
	}

	/**
	 * This method returns whether or not the XML instance was found to be
	 * wellformed.  The value <code>false</code> indicates that the XML instance 
	 * is not wellformed XML, <code>true</code> indicates it is wellformed XML.
	 *
	 * @return boolean - describes if the instance was found to be wellformed.
	 */
	@Override
	public boolean getIsWellformed() {
		return mIsWellformed;
	}

	/**
	 * Returns a List of any sub-items of a given item Node.
	 *
	 * @param iItem - the item Node whose sub-items you wish to obtain.
	 *
	 * @return Returns a List of all sub-items of the given item.
	 *
	 */
	@Override
	public List<Node> getItems(Node iItem) {
		mLogger.entering("ADLValidatorOutcome", "getItems()");
		List<Node> result = new ArrayList<>();
		List<Node> itemList = new ArrayList<>();
		Node currentItem = null;
		if (iItem != null) {
			itemList = DOMTreeUtility.getNodes(iItem, "item");
		}
		result.addAll(itemList);
		for (int itemCount = 0; itemCount < itemList.size(); itemCount++) {
			currentItem = itemList.get(itemCount);
			result.addAll(getItems(currentItem));
		}
		mLogger.exiting("ADLValidatorOutcome", "getItems()");
		return result;
	}

	/**
	 * Returns a List filled with all of the item Nodes in a manifest node.
	 * This method is scoped only to one level of manifest, as such, it does not
	 * return items in sub-manifests.
	 *
	 * @param iManifest The manifest node you wish to perform this operation on
	 *
	 * @return Returns List containing all of the item nodes in the
	 * manifest.
	 */
	@Override
	public List<Node> getItemsInManifest(Node iManifest) {
		mLogger.entering("ADLValidatorOutcome", "getItemsInManifest()");
		Node organizationsNode = null;
		List<Node> organizationList = new ArrayList<>();
		List<Node> itemList = new ArrayList<>();
		List<Node> resultList = new ArrayList<>();
		Node currentOrg = null;
		Node currentItem = null;
		organizationsNode = DOMTreeUtility.getNode(iManifest, "organizations");
		if (organizationsNode != null) {
			organizationList = DOMTreeUtility.getNodes(organizationsNode, "organization");
		}

		for (int orgCount = 0; orgCount < organizationList.size(); orgCount++) {
			currentOrg = organizationList.get(orgCount);
			itemList = DOMTreeUtility.getNodes(currentOrg, "item");
			for (int itemCount = 0; itemCount < itemList.size(); itemCount++) {
				currentItem = itemList.get(itemCount);
				resultList.add(currentItem);
				resultList.addAll(getItems(currentItem));
			}
		}
		mLogger.exiting("ADLValidatorOutcome", "getItemsInManifest()");
		return resultList;
	}

	/**
	 * Returns an item Node whose identifier matches the ID passed in.
	 *
	 * @param iItemID The value of the identifier attribute of the item to be
	 *                 found.
	 *
	 * @return Returns the item Node whose identifier matches the ID passed in.
	 */
	@Override
	public Node getItemWithID(String iItemID) {
		mLogger.entering("ADLValidatorOutcome", "getItemsWithID()");
		Node manifestNode = mDocument.getDocumentElement();

		List<Node> manifestList = getAllManifests(manifestNode);

		Node currentItem = null;
		Node currentManifest = null;
		Node theNode = null;
		String currentItemID = "";
		List<Node> itemList = new ArrayList<>();
		boolean isFound = false;

		itemList = getItemsInManifest(manifestNode);
		for (int itemCount = 0; itemCount < itemList.size(); itemCount++) {
			currentItem = itemList.get(itemCount);
			if (currentItem != null) {
				currentItemID = DOMTreeUtility.getAttributeValue(currentItem, "identifier");
				if (currentItemID.equalsIgnoreCase(iItemID)) {
					theNode = currentItem;
					isFound = true;
				}
			}
		}
		if (!isFound) {
			for (int manCount = 0; manCount < manifestList.size(); manCount++) {
				currentManifest = manifestList.get(manCount);
				if (currentManifest != null) {
					itemList = getItemsInManifest(currentManifest);
					for (int count = 0; count < itemList.size(); count++) {
						currentItem = itemList.get(count);
						if (currentItem != null) {
							currentItemID = DOMTreeUtility.getAttributeValue(currentItem, "identifier");
							if (currentItemID.equalsIgnoreCase(iItemID)) {
								theNode = currentItem;
								isFound = true;
								break;
							}
						}
					}
				}
			}
		}
		mLogger.exiting("ADLValidatorOutcome", "getItemWithID()");
		return theNode;
	}

	/**
	 * Returns the resource or manifest node in a manifest whose identifier
	 * attribute matches the ID passed in.
	 *
	 * @param iManifest The manifest node you wish to perform this operation on
	 *
	 * @param iID The value of the identifier of the node you are looking
	 * for.
	 *
	 * @return Returns the Node that has the identifier matching the ID passed 
	 * in.
	 */
	@Override
	public Node getNodeWithID(Node iManifest, String iID) {
		mLogger.entering("ADLValidatorOutcome", "getNodeWithID()");
		boolean isFound = false;
		Node theNode = null;
		Node currentManifest = null;
		Node currentResource = null;
		List<Node> allManifests = getAllManifests(iManifest);
		int i = 0;
		int j = 0;
		String manifestID = "";
		String resourceID = "";

		while ((i < allManifests.size()) && (!isFound)) {
			currentManifest = allManifests.get(i);
			manifestID = DOMTreeUtility.getAttributeValue(currentManifest, "identifier");
			if (manifestID.equalsIgnoreCase(iID)) {
				isFound = true;
				theNode = allManifests.get(i);
				break;
			}

			// Manifest not found increment counter and check the next 
			// manifest identifier
			i++;
		}

		if (!isFound) {
			mLogger.info("NOT FOUND" + iID);
			List<Node> allResources = getAllResources(iManifest);
			while ((j < allResources.size()) && (!isFound)) {
				currentResource = allResources.get(j);
				resourceID = DOMTreeUtility.getAttributeValue(currentResource, "identifier");
				if (resourceID.equalsIgnoreCase(iID)) {
					isFound = true;
					theNode = allResources.get(j);
					break;
				}

				// Manifest not found increment counter and check the next 
				// manifest identifier
				j++;
			}
		}
		mLogger.exiting("ADLValidatorOutcome", "getNodeWithID()");
		return theNode;
	}

	/**
	 * This method returns the document created during a parse. A parse for
	 * wellformedness creates a document object while the parse for validation
	 * against the controlling documents creates a seperate document object.
	 *
	 * @return Document -  An electronic representation of the XML produced by
	 * the parse.
	 */
	@Override
	public Document getRolledUpDocument() {
		return mRolledUpDocument;
	}

	/**
	 * This method returns the root node of the document created during a parse.
	 * A parse for wellformedness creates a document object while the parse for
	 * validation against the controlling documents creates a seperate document
	 * object.
	 *
	 * @return Node - the root node of the DOM stored in memory
	 */
	@Override
	public Node getRootNode() {
		return mDocument.getDocumentElement();
	}

	/**
	 * This method will find the xml:base attribute of the node passed into it
	 * and return it if it has one, if it doesn't, it will return an empty
	 * string.  If the node does have an xml:base attribute, this method will
	 * also set that attribute to an empty string after retrieving it's value.
	 *
	 * @param iNode - the node whose xml:base attribute value is needed.
	 * @return Returns the value of the xml:base attribute of this node.
	 */
	@Override
	public String getXMLBaseValue(Node iNode) {
		mLogger.entering("ADLValidatorOutcome", "getXMLBaseValue()");
		String result = "";

		if (iNode != null) {
			Attr baseAttr = null;
			baseAttr = DOMTreeUtility.getAttribute(iNode, "base");
			if (baseAttr != null) {
				result = baseAttr.getValue();
				DOMTreeUtility.removeAttribute(iNode, "xml:base");
			}
		}
		mLogger.exiting("ADLValidatorOutcome", "getXMLBaseValue()");
		return result;
	}

	/**
	 * This method loops through the elements in the mItemIdrefs List of the
	 * ManifestMap object. If the element doesn't match an element in the
	 * mResourceIds List, it searches the DOM tree of the given manifest Node
	 * for the node with the ID matching the itemIdrefs value.  If the node found
	 * to have the matching ID is a manifest node, the sub-manifest is rolled up,
	 * merging the organization node of the sub-manifest with the item node that
	 * referenced the sub-manifest.  It then recursivly loops through the
	 * mManifestMaps List, performing these operations on each element.
	 *
	 * @param iManifestMap The ManifestMap that this method is to be performed
	 * on.
	 * @param iManifestNode The root manifest node of the DOM tree.
	 *
	 */
	@Override
	public void processManifestMap(IManifestMap iManifestMap, Node iManifestNode) {
		mLogger.entering("ADLValidatorOutcome", "processManifestMap()");
		boolean isInResources = false;
		Node theNode = null;
		String theNodeName = "";

		List<String> resourceIDs = iManifestMap.getResourceIds();
		List<String> itemIDs = iManifestMap.getItemIds();
		List<String> itemIdrefs = iManifestMap.getItemIdrefs();
		List<? extends IManifestMap> manifestMaps = iManifestMap.getManifestMaps();

		String itemIdref = "";
		String resourceID = "";
		Node organizationsNode = null;
		List<Node> organizationNodes = new ArrayList<>();
		int organizationIndex = 0;
		String defaultOrgID = "";
		String tempOrgID = "";
		Node tempOrgNode = null;
		Node organizationNode = null;
		String organizationID = "";
		NodeList orgChildren = null;
		String identifierToReplace = "";
		Node oldItem = null;
		NodeList oldItemChildren;
		Node currentOldChild = null;
		Node currentChild = null;
		Attr identifierAttr = null;
		ManifestMap currentManifestMap;

		//Loop through the itemIdref List
		for (int idRefCount = 0; idRefCount < itemIdrefs.size(); idRefCount++) {
			itemIdref = itemIdrefs.get(idRefCount);
			if (!itemIdref.isEmpty()) {
				theNode = null;
				isInResources = false;
				for (int resourceIDCount = 0; resourceIDCount < resourceIDs.size(); resourceIDCount++) {
					resourceID = resourceIDs.get(resourceIDCount);
					if (itemIdref.equals(resourceID)) {
						isInResources = true;
						break;
					}
				}

				//If the itemIdref is not in the resourceIDs List
				if (!isInResources) {
					//Get the node whose identifier is in this position of the
					//itemIdref List.
					theNode = getNodeWithID(iManifestNode, itemIdref);

					if (theNode != null) {
						theNodeName = theNode.getLocalName();

						//If theNode is a manifest node
						if (theNodeName.equals("manifest")) {
							organizationsNode = null;

							organizationsNode = DOMTreeUtility.getNode(theNode, "organizations");
							organizationNodes = DOMTreeUtility.getNodes(organizationsNode, "organization");

							organizationIndex = 0;

							//Find the organization node referanced by the "default"
							//attribute of the organizations node.  If no match is
							//found, use the first organization.
							defaultOrgID = DOMTreeUtility.getAttributeValue(organizationsNode, "default");
							for (int orgCount = 0; orgCount < organizationNodes.size(); orgCount++) {
								tempOrgNode = organizationNodes.get(orgCount);

								tempOrgID = DOMTreeUtility.getAttributeValue(tempOrgNode, "identifier");
								if (tempOrgID.equals(defaultOrgID)) {
									organizationIndex = orgCount;
									break;
								}
							}

							organizationNode = organizationNodes.get(organizationIndex);

							organizationID = DOMTreeUtility.getAttributeValue(organizationNode, "identifier");

							orgChildren = organizationNode.getChildNodes();

							identifierToReplace = itemIDs.get(idRefCount);
							oldItem = getItemWithID(identifierToReplace);

							oldItemChildren = oldItem.getChildNodes();

							for (int oldChildCount = 0; oldChildCount < oldItemChildren.getLength(); oldChildCount++) {
								currentOldChild = oldItemChildren.item(oldChildCount);
								oldItem.removeChild(currentOldChild);
							}

							// Loop through all of the children of the sub-manifest
							// organization and append them to the item (oldItem) that
							// referenced the sub-manifest.
							for (int childCount = 0; childCount < orgChildren.getLength();) {
								currentChild = orgChildren.item(childCount);
								try {
									oldItem.appendChild(currentChild);
								} catch (DOMException domExcep) {
									domExcep.printStackTrace();
								}
							}

							oldItemChildren = oldItem.getChildNodes();

							identifierAttr = DOMTreeUtility.getAttribute(oldItem, "identifier");

							identifierAttr.setValue(organizationID);

							DOMTreeUtility.removeAttribute(oldItem, "identifierref");
						}
					}
				}
			}
		}
		for (int mmCount = 0; mmCount < manifestMaps.size(); mmCount++) {
			currentManifestMap = (ManifestMap) manifestMaps.get(mmCount);
			processManifestMap(currentManifestMap, iManifestNode);
		}
		mLogger.exiting("ADLValidatorOutcome", "processManifestMap()");
	}

	/**
	 * This method is a control method which deals with rolling up sub-manifests.
	 * It first populates a ManifestMap object and then calls processManifestMap.
	 * It then rolls all resources in any sub-manifest to the root manifest, and
	 * deletes any sub-manifest nodes in the DOM tree.
	 *
	 * @param isResPackage - Whether or not the package is a resource package.
	 */
	@Override
	public void rollupSubManifests(boolean isResPackage) {
		mLogger.entering("ADLValidatorOutcome", "rollupSubManifests()");
		Node manifest = mDocument.getDocumentElement();
		ManifestMap manifestMap = new ManifestMap();
		List<Node> manifestList = new ArrayList<>();
		List<Node> resourceList = new ArrayList<>();
		Node rootResources = DOMTreeUtility.getNode(manifest, "resources");
		Node currentManifest = null;
		Node currentResource = null;

		manifestMap.populateManifestMap(manifest);
		applyXMLBase(manifest);

		// Are there any sub-manifests?
		if (manifestMap.getDoSubmanifestExist()) {
			if (!isResPackage) {
				processManifestMap(manifestMap, manifest);
			}

			manifestList = DOMTreeUtility.getNodes(manifest, "manifest");
			for (int i = 0; i < manifestList.size(); i++) {
				currentManifest = manifestList.get(i);
				resourceList.addAll(getAllResources(currentManifest));
			}

			//rollup all resources to the root manifest
			for (int j = 0; j < resourceList.size(); j++) {
				currentResource = resourceList.get(j);
				rootResources.appendChild(currentResource);
			}

			if (!isResPackage) {
				//delete all sub-manifests
				for (int k = 0; k < manifestList.size(); k++) {
					currentManifest = manifestList.get(k);
					if (currentManifest != null) {
						manifest.removeChild(currentManifest);
					}
				}
			}
		}
		mLogger.exiting("ADLValidatorOutcome", "rollupSubManifests()");
	}
}
