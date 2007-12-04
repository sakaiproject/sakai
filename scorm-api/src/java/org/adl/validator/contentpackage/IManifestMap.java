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
package org.adl.validator.contentpackage;

import java.io.Serializable;
import java.util.Vector;

import org.w3c.dom.Node;

public interface IManifestMap extends Serializable {

	/**
	 * Gives access to the identifier value of the &lt;manifest&gt; element.
	 *
	 * @return - The identifier value of the &lt;manifest&gt; element.
	 */
	public String getManifestId();

	/**
	 * Gives access to the identifier attributes of all &lt;resource&gt; elements that
	 * belong to the &lt;manifest&gt; element of mManifestId.
	 *
	 * @return - The identifier attributes of all &lt;resource&gt; elements that
	 * belong to the &lt;manifest&gt; element of mManifestId.
	 */
	public Vector getResourceIds();

	/**
	 * Gives access to the identifier attributes of all &lt;item&gt; elements that
	 * belong to the &lt;manifest&gt; element of mManifestId.
	 *
	 * @return - The identifier attributes of all &lt;item&gt; elements that
	 * belong to the &lt;manifest&gt; element of mManifestId.
	 */
	public Vector getItemIds();

	/**
	 * Gives access to the identifier reference values of all &lt;item&gt; elements
	 * that belong to the &lt;manifest&gt; element of mManifestId.
	 *
	 * @return - The identifier reference values of all &lt;item&gt; elements that
	 * belong to the &lt;manifest&gt; element of mManifestId.
	 */
	public Vector getItemIdrefs();

	/**
	 * Gives access to the identifier reference values of all &lt;dependency&gt;
	 * elements that belong to the &lt;manifest&gt; element of mManifestId.
	 *
	 * @return - The identifier reference values of all &lt;dependency&gt; elements
	 * that belong to the &lt;manifest&gt; element of mManifestId.
	 */
	public Vector getDependencyIdrefs();

	/**
	 * Gives access to the ManifestMap objects for all (Sub) manifest elements.
	 *
	 * @return - The ManifestMap objects for all (Sub) manifest elements.
	 */
	public Vector getManifestMaps();

	/**
	 * Gives access to the boolean describing if the manifest utilizes
	 * (Sub) manifest.
	 *
	 * @return - The boolean describing if the manifest utilizes (Sub) manifest.
	 * 
	 */
	public boolean getDoSubmanifestExist();

	/**
	 * Gives access to the String describing which Application Profile the
	 * (Sub) manifest adheres to.
	 *
	 * @return - The boolean describing if the manifest utilizes (Sub) manifest.
	 */
	public String getApplicationProfile();

	/**
	 * Gives access to the String describing which Application Profile the
	 * (Sub) manifest adheres to.
	 *
	 * @param iApplicationProfile The indicator of the Application Profile
	 */
	public void setApplicationProfile(String iApplicationProfile);

	/**
	 * Gives access to the list of IDRefs that reference (Sub) manifest elements.
	 *
	 * @return - The ManifestMap objects for all (Sub) manifest elements.
	 */
	public Vector getSubManifestIDrefs();

	/**
	 * This method populates the ManifestMap object by traversing down
	 * the document node and storing all information necessary for the validation
	 * of (Sub) manifests.  Information stored for each manifest element includes:
	 * manifest identifiers,item identifers, item identifierrefs, and
	 * resource identifiers
	 *
	 * @param iNode the node being checked. All checks will depend on the type of node
	 * being evaluated
	 * 
	 * @return - The boolean describing if the ManifestMap object(s) has been
	 * populated properly.
	 */
	public boolean populateManifestMap(Node iNode);

	/**
	 * This method drives the recursive validation of the referencing of
	 * identifierref values.  It spans the validation of identifierrefs for
	 * each identifierref value.
	 *
	 * @return - The Vector containing the identifierref value(s) that do not
	 * reference valid identifers.
	 *
	 */
	public Vector checkAllIdReferences();

	/**
	 * This method validates that the incoming identifierref value properly
	 * references a valid identifier.  An error is thrown for identifierref
	 * values that perform backwards or sideward referencing, or does not
	 * reference an identifier value at all.
	 *
	 * @param iIdref the identifier reference being checked
	 * 
	 * @param iInSubManifest if true then we treat it differently
	 * 
	 * @return - The Vector containing the identifierref value(s) that do not
	 * reference valid identifers.
	 *
	 */
	public boolean checkIdReference(String iIdref, boolean iInSubManifest);

}