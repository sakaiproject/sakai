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