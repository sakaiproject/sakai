/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.adl.validator;

import java.io.Serializable;
import java.util.Vector;

import org.adl.validator.contentpackage.IManifestMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface IValidatorOutcome extends Serializable {

	/**
	 * This method returns the document created during a parse. A parse for
	 * wellformedness creates a document object while the parse for validation
	 * against the controlling documents creates a seperate document object.
	 *
	 * @return Document -  An electronic representation of the XML produced by
	 * the parse.
	 */
	public Document getDocument();

	/**
	 * This method returns the document created during a parse. A parse for
	 * wellformedness creates a document object while the parse for validation
	 * against the controlling documents creates a seperate document object.
	 *
	 * @return Document -  An electronic representation of the XML produced by
	 * the parse.
	 */
	public Document getRolledUpDocument();

	/**
	 * This method returns the root node of the document created during a parse.
	 * A parse for wellformedness creates a document object while the parse for
	 * validation against the controlling documents creates a seperate document
	 * object.
	 *
	 * @return Node - the root node of the DOM stored in memory
	 */
	public Node getRootNode();

	/**
	 * This method returns whether or not the XML instance was found to be
	 * wellformed.  The value <code>false</code> indicates that the XML instance 
	 * is not wellformed XML, <code>true</code> indicates it is wellformed XML.
	 *
	 * @return boolean - describes if the instance was found to be wellformed.
	 */
	public boolean getIsWellformed();

	/**
	 * This method returns whether or not the XML instance was valid to the
	 * schema.  The value <code>false</code> indicates that the XML instance is 
	 * not valid against the controlling documents, <code>true</code> indicates 
	 * that the XML instance is valid against the controlling documents.
	 *
	 * @return boolean - decribes if the XML Instance is valid against the
	 * schema(s).
	 */
	public boolean getIsValidToSchema();

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
	public boolean getIsValidToApplicationProfile();

	/**
	 * This method returns whether or not the XML instance contained extension
	 * elements and/or attributes.  The value <code>false</code> indicates that 
	 * the XML instance does not contain extended elements and/or attributes, 
	 * <code>true</code> indicates that the XML instance did.
	 *
	 * @return boolean - describes if the XML Instance contains extension
	 * elements/attributes.
	 */
	public boolean getIsExtensionsUsed();

	/**
	 * This method is specific to the Content Package Validator only.  This
	 * method returns whether or not the content package test subject
	 * contains the required schemas at the root of the package needed for the
	 * validation parse.
	 *
	 * @return boolean - describes if the required schemas were detected at the
	 * root of the pif, <code>false</code> otherwise.
	 */
	public boolean getDoRequiredCPFilesExist();

	/**
	 * This method is specific to the Content Package Validator only.  This
	 * method returns whether or not the content package test subject
	 * contains the required IMS Manifest at the root of the package.
	 *
	 * @return boolean - describes if the required IMS Manifest was detected at
	 * the root of the pif, <code>false</code> otherwise.
	 */
	public boolean getDoesIMSManifestExist();

	/**
	 * This method returns the boolean that describes if we are dealing with
	 * a valid root manifest (belongs to the IMS namespace) or a valid root
	 * lom element (belongs to the IEEE LOM namespace).
	 * 
	 * @return Returns whether or not the root element is from a Namespace that
	 * was expected.
	 *
	 */
	public boolean getIsValidRoot();

	/**
	 * This method will find the xml:base attribute of the node passed into it
	 * and return it if it has one, if it doesn't, it will return an empty
	 * string.  If the node does have an xml:base attribute, this method will
	 * also set that attribute to an empty string after retrieving it's value.
	 *
	 * @param iNode - the node whose xml:base attribute value is needed.
	 * @return Returns the value of the xml:base attribute of this node.
	 */
	public String getXMLBaseValue(Node iNode);

	/**
	 * This method will apply the value of any xml:base attributes of a root
	 * manifest to any file elements in it's resource elements.
	 *
	 * @param iManifestNode - The root <code>&lt;manfiest$gt;</code> node of a 
	 * manifest.
	 */
	public void applyXMLBase(Node iManifestNode);

	/**
	 * Returns a vector of any sub-items of a given item Node.
	 *
	 * @param iItem - the item Node whose sub-items you wish to obtain.
	 *
	 * @return Returns a Vector of all sub-items of the given item.
	 *
	 */
	public Vector getItems(Node iItem);

	/**
	 * Returns a Vector filled with all of the item Nodes in a manifest node.
	 * This method is scoped only to one level of manifest, as such, it does not
	 * return items in sub-manifests.
	 *
	 * @param iManifest The manifest node you wish to perform this operation on
	 *
	 * @return Returns Vector containing all of the item nodes in the
	 * manifest.
	 */
	public Vector getItemsInManifest(Node iManifest);

	/**
	 * Returns an item Node whose identifier matches the ID passed in.
	 *
	 * @param iItemID The value of the identifier attribute of the item to be
	 *                 found.
	 *
	 * @return Returns the item Node whose identifier matches the ID passed in.
	 */
	public Node getItemWithID(String iItemID);

	/**
	 * Returns a Vector filled with all of the resource Nodes in a DOM
	 *
	 * @param iManifest The manifest node you wish to perform this operation on
	 *
	 * @return Returns a Vector containing all of the resource nodes in the
	 * manifest.
	 */
	public Vector getAllResources(Node iManifest);

	/**
	 * Returns a Vector filled with all of the manifest Nodes in a DOM
	 *
	 * @param iManifest The manifest node you wish to perform this operation on
	 *
	 * @return Returns a Vector containing all of the manifest nodes in the
	 * manifest.
	 */
	public Vector getAllManifests(Node iManifest);

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
	public Node getNodeWithID(Node iManifest, String iID);

	/**
	 * This method loops through the elements in the mItemIdrefs vector of the
	 * ManifestMap object. If the element doesn't match an element in the
	 * mResourceIds vector, it searches the DOM tree of the given manifest Node
	 * for the node with the ID matching the itemIdrefs value.  If the node found
	 * to have the matching ID is a manifest node, the sub-manifest is rolled up,
	 * merging the organization node of the sub-manifest with the item node that
	 * referenced the sub-manifest.  It then recursivly loops through the
	 * mManifestMaps vector, performing these operations on each element.
	 *
	 * @param iManifestMap The ManifestMap that this method is to be performed
	 * on.
	 * @param iManifestNode The root manifest node of the DOM tree.
	 *
	 */
	public void processManifestMap(IManifestMap iManifestMap, Node iManifestNode);

	/**
	 * This method is a control method which deals with rolling up sub-manifests.
	 * It first populates a ManifestMap object and then calls processManifestMap.
	 * It then rolls all resources in any sub-manifest to the root manifest, and
	 * deletes any sub-manifest nodes in the DOM tree.
	 *
	 * @param isResPackage - Whether or not the package is a resource package.
	 */
	public void rollupSubManifests(boolean isResPackage);

}