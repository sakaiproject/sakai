/*
 * #%L
 * SCORM API
 * %%
 * Copyright (C) 2007 - 2016 Sakai Project
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.adl.validator.contentpackage;

import org.w3c.dom.Node;

public interface IMetadataData {

	/**
	 * This method returns the application profile type of the metadata instance.
	 * Valid values include:
	 * <ul>
	 *    <li><code>adlreg</code></li>
	
	 * </ul>
	 * 
	 * @return the Application Profile Type
	 */
	public abstract String getApplicationProfileType();

	/**
	 * This method returns the identifier attribute which stores the identifier
	 * value of the major elements (item, orgs, etc/) that house the metadata
	 * instance.
	 *
	 * @return String The identifier value of the parent of the metadata.
	 */
	public abstract String getIdentifier();

	/**
	 * This method returns the uri location value of the external metadata
	 * instance. If the metadata instance is in the form of inline metadata,
	 * then the value returned will be "inline".
	 *
	 * @return String location value of the metadata test subject.
	 */
	public abstract String getLocation();

	/**
	 * This method retruns the root node of the inline metadata if it exists
	 * in the form of extensions to the imsmanifest file.
	 *
	 * @return Node root lom node of the inline metadata.
	 */
	public abstract Node getRootLOMNode();

	/**
	 * This method returns a boolean value based on the form of metadata.  If the
	 * metadata is in the form of inline metadata, than the boolean value
	 * <code>true</code> is returned.  If the metadata is in the form of 
	 * external standalone metadata, than the boolean value of 
	 * <code>false</code> is returned.
	 *
	 * @return boolean 
	 * <ul>
	 *    <li><code>true</code>:  if the metadata is inline</li>
	 *    <li><code>false</code>: otherwises</li>
	 * </ul>
	 */
	public abstract boolean isInlineMetadata();

}