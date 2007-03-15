/**********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package edu.indiana.lib.twinpeaks.search;

import edu.indiana.lib.twinpeaks.util.*;

import java.io.*;
import java.util.*;

import org.osid.repository.Asset;
import org.osid.repository.AssetIterator;
import org.osid.repository.Record;
import org.osid.repository.Part;
import org.osid.repository.PartStructure;



/**
 * Encapsulate one matching search item
 */
public class MatchItem
{

private static org.apache.commons.logging.Log	_log = LogUtils.getLog(MatchItem.class);

	private String				_displayName;
	private String				_description;
	private String				_id;
	private String				_openUrl;
	private List					_partStructureList;

	private String				_previewUrl;
	private String				_previewImage;
	private String				_previewText;
	private String				_persistentUrl;
	private String				_persistentParameters;
	private String				_persistentParametersForEncoding;
	private String				_persistentText;

	/**
	 * Constructor
	 */
	public MatchItem() {
		super();

		_partStructureList = new ArrayList();
	}

	/**
	 * Fetch the Asset "display name"
	 * @return Display name text
	 */
	public String getDisplayName()
	{
		return _displayName;
	}

	/**
	 * Set the Asset "display name"
	 * @param displayName Display name text
	 */
	public void setDisplayName(String displayName)
	{
		_displayName = displayName;
	}

	/**
	 * Fetch additional descriptive text
	 * @return Additional text
	 */
	public String getDescription()
	{
		return _description;
	}

	/**
	 * Set description text
	 * @param text Description text
	 */
	public void setDescription(String text)
	{
		_description = normalizeDescription(text);
	}

	/**
	 * Fetch the Asset ID
	 * @return ID text
	 */
	public String getId()
	{
		return _id;
	}

	/**
	 * Set the ID
	 * @param id ID text
	 */
	public void setId(String id)
	{
		_id = normalizeDescription(id);
	}

 	/**
 	 * Add an additional PartStructure too the list
 	 * @param partStructureId The PartStructure Id to add
 	 * @param value PartStructure value
 	 */
	public void addPartStructure(org.osid.shared.Id partStructureId, Serializable value)
	{
 		_partStructureList.add(new PartPair(partStructureId, value));
	}

 	/**
 	 * Get an interator for the PartStructureId list (PartPair objects)
 	 * @return PartStructure list Iterator
 	 */
	public Iterator partPairIterator()
	{
 		return _partStructureList.iterator();
	}

	/**
	 * Fetch the OpenURL generated at search time
	 * @return The generated OpenURL
	 */
	public String getOpenUrl() {
		return _openUrl;
	}

	/**
	 * Save the OpenUrl discovered at search time
	 * @param url URL string
	 */
	public void setOpenUrl(String url) {
		_openUrl = url;
	}







	/**
	 * Fetch the URL of the "preview" view of the matching item
	 * @return A fully qualified URL value
	 */
	public String getPreviewUrl() {
		return _previewUrl;
	}

	/**
	 * Set the "preview" URL
	 * @param url Preview URL string
	 */
	public void setPreviewUrl(String url) {
		_previewUrl = url;
	}

	/**
	 * Fetch the image associated with the "preview" URL
	 * @return Image source specification
	 */
	public String getPreviewImage(){
		return _previewImage;
	}

	/**
	 * Set "preview" URL image
	 * @param source Image source specification
	 */
	public void setPreviewImage(String source) {
		_previewImage = source;
	}

	/**
	 * Fetch the text associated with the "preview" URL
	 * @return Text
	 */
	public String getPreviewText(){
		return _previewText;
	}

	/**
	 * Set "preview" URL text
	 * @param text Preview text
	 */
	public void setPreviewText(String text) {
		_previewText = text;
	}

	/**
	 * Fetch the URL for the persistent reference
	 * @return A fully qualified URL value (minus any arguments)
	 */
	public String getPersistentUrl() {
		return _persistentUrl;
	}

	/**
	 * Set the persistent URL
	 * @param url Persistent URL
	 */
	public void setPersistentUrl(String url) {
		_persistentUrl = url;
	}

	/**
	 * Set the persistent URL and simple parameters
	 * @param persistentHref Persistent URL
	 */
	public void setPersistentUrlAndParameters(String persistentHref) {
		String url = persistentHref;

		if (!StringUtils.isNull(url)) {
			int index;

			if ((index = url.indexOf('?')) != -1) {
				if (++index < url.length()) {
					_persistentParameters = url.substring(index);
				}
				url = url.substring(0, index);
			}
		}
		_persistentUrl = url;
	}

	/**
	 * Fetch simple parameters for the persistent reference
	 * @return Parameters (<code>?name1=value1&name2=value2</code>)
	 */
	public String getPersistentUrlParameters() {
		return (_persistentParameters == null) ? "" : _persistentParameters;
	}

	/**
	 * Set persistent URL parameter values
	 * @param parameters Parameter text (name=value pairs)
	 */
	public void setPersistentUrlParameters(String parameters) {
		_persistentParameters = parameters;
	}

	/**
	 * Fetch any persistent reference parameters that <i>must be</i> URL encoded.
	 *<p>
	 * Note: When used with IE, the HTML editor parses encoded text, replacing
	 * entity sequences and the like with "real" characters - in rare cases, this
	 * will cause the resultant URL to fail.  As a work around, the query handler
	 * can specifiy parameters that need to be wrapped in a JavaScript escape()
	 * at URL "click time".
	 *
	 * @return Parameters (<code>?name3=value3&name4=value4</code>)
	 */
	public String getPersistentUrlParametersForEncoding() {
		return _persistentParametersForEncoding;
	}

	/**
	 * Set any persistent URL parameter values that require JavaScript encoding
	 * @param parameters Parameter text (name=value pairs)
	 */
	public void setPersistentUrlParametersForEncoding(String parameters) {
		_persistentParametersForEncoding = parameters;
	}

	/**
	 * Fetch the link text associated with the persistent reference
	 * @return Anchor text
	 */
	public String getPersistentText() {
		return _persistentText;
	}

	/**
	 * Set persistent URL text
	 * @param text Anchor text
	 */
	public void setPersistentText(String text) {
		_persistentText = normalizePersistentText(text);
	}

	/*
	 * Helpers
	 */

	/**
	 * Normalize persistent text:
	 * <ul>
	 * <li> Remove trailing dots (.)
	 * </ul>
	 * @param text Persistent text
	 * @return Normalized text
	 */
	private String normalizePersistentText(String text) {
		return StringUtils.trimEnd(text, '.');
	}

	/**
	 * Normalize description text:
	 * <ul>
	 * <li> Remove leading dots (.)
	 * </ul>
	 * @param text Description text
	 * @return Normalized description
	 */
	private String normalizeDescription(String text) {
		String result = StringUtils.trimFront(text, '.');

		result = StringUtils.trimFront(result, ',');
		result = StringUtils.trimFront(result, ';');

		return result;
	}

	/**
	 * Container class for Part ID and Value
	 */
	public static class PartPair
	{
		private org.osid.shared.Id 		__id;
		private Serializable					__value;

		/**
		 * Simple constructor (unused)
		 */
		private PartPair() { }

		/**
		 * Constructor (save ID and Value)
		 */
		private PartPair(org.osid.shared.Id id, Serializable value)
		{
			__id		= id;
			__value	= value;
		}

		/**
		 * Return ID
		 */
		public org.osid.shared.Id getId()
		{
			return __id;
		}

		/**
		 * Return Value
		 */
		public Serializable getValue()
		{
			return __value;
		}
	}
}