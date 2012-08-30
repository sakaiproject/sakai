/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.citation.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdUnusedException;


/**
 *
 *
 */
public interface Citation 	// extends Entity
{
  /**
     * When rendering custom (direct) URLs: add or omit the configured URL prefix
     */
    public static final String ADD_PREFIX_TEXT    = "y";
    public static final String OMIT_PREFIX_TEXT   = "n";
    
	/**
	   * Add a new custom (direct) URL
     * @param label Link label
     * @param url The URL to add
     * @return A unique identifier for the URL and its label.
     */
    public String addCustomUrl(String label, String url);

	/**
	   * Add a new custom (direct) URL and determine if the prefix text will
	   * be added to the URL at render-time.
     * @param label Link label
     * @param url The URL to add
     * @param prefixRequest Use Citation.ADD_PREFIX_TEXT to add the prefix,
     *                          Citation.OMIT_PREFIX_TEXT to skip the prefix
     *
     * @return A unique identifier for the URL and its label.
     */
    public String addCustomUrl(String label, String url, String prefixRequest);

    /**
     * @deprecated This should never have been added as other methods are clearer.
     * @see #hasCitationProperty(String)
     * @param name
     * @param value
     */
    public void addPropertyValue(String name, Object value);

    /**
     * Write this citation in RIS format to an output stream
     * @param buffer
     * @throws IOException
     */
    public void exportRis(StringBuilder buffer) throws IOException;

    /**
	 * Access a mapping of name-value pairs for various bibliographic information about the resource.
	 * Ideally, the names will be expressed as URIs for standard tags representing nuggets of bibliographic metadata.
	 * Values will be strings in formats specified by the standards defining the property names.  For example if the
	 * name is a URI referencing a standard format for a "publication_date" and the standard indicates that the value
	 * should be expressed as in xs:date format, the value should be expressed as a string representation of xs:date.
	 * @return The mapping of name-value pairs.  The mapping may be empty, but it should never be null.
	 */
	public Map getCitationProperties();
	
	/**
	 * Access a representation of the value(s) of a named property.  If the property is multivalued,
	 * the object returned will be a (possibly empty) java.util.List.  Otherwise it will be an Object
	 * of a type appropriate for the named property (usually a String or a Date). The value(s)
	 * must be of a type for which toString() is defined and returns a reasonable representation
	 * for use in a textual display of the citation.
	 * @param name The name of the property for which a value is to be returned.
	 * @param needSingleValue If true, forces return of a single string value. Otherwise the 
	 * value returned could be a string or a list, depending on various factors. This allows 
	 * client code to ensure that a single string is returned when necessary. If needSingleValue 
	 * is true and the field actually has multiple values, the first value is returned. 
	 * @return A representation of the value(s) of the named property.  May be an empty String ("")
	 * if the property is not defined.
	 */
	public Object getCitationProperty(String name, boolean needSingleValue);

	/**
	 * Access a representation of the value(s) of a named property. Same as two-parameter getCitationProperty()
	 * method but without the second parameter. This method returns the value as if needSingleValue is false.
	 * @param name The name of the property for which a value is to be returned.  This method is intended 
	 * for use in velocity templates.  In other places, please use the two-parameter method.
	 * @return A representation of the value(s) of the named property.  May be an empty String ("")
	 * if the property is not defined.
	 */
	public Object getCitationProperty(String name);

        /**
         * Check if the citation property is defined.
         * @param name The name of the property to check.
         * @return <code>true</code> if the property is defined and is not null or is not an empty list.
         */
        public boolean hasCitationProperty(String name);
        
	/**
	 * @return
	 */
	public String getCreator();

	/**
	   * Fetch a custom (direct) URL by ID.  The URL prefix text (if any) 
	   * is added.
	   *
     * @param id The internal URL ID
     * @return The URL (with prefix text if applicable)
     */
    public String getCustomUrl(String id) throws IdUnusedException;

		/**
		 * Fetch a custom (direct) URL by ID.  The URL prefix (if applicable)
		 * is not added.
     *
     * @param id The internal URL ID
     * @return The URL
		 */
		public String getUnprefixedCustomUrl(String id) throws IdUnusedException;

		/**
		 * Fetch the URL prefix text
		 *
		 * @return The URL prefix text (null if none) 
		 */
		public String getUrlPrefix();

		/**
		 * Add prefix text to this URL at render-time?
     * @param id The internal URL ID
     * @return true If the prefix should be added to the URL
		 */
		public boolean addPrefixToUrl(String id) throws IdUnusedException;

	/**
     * @return
     */
    public List getCustomUrlIds();

	/**
     * @param id
     * @return
	 * @throws IdUnusedException
     */
    public String getCustomUrlLabel(String id) throws IdUnusedException;

    public String getYear();

	/**
	 * Access the brief "title" of the resource, which can be used to display the item in a list of items.
	 * @return The display name.
	 */
	public String getDisplayName();

	/**
     * @return
     */
    public String getFirstAuthor();

	/**
     * @return
     */
    public String getId();

	/**
	 * @return
	 */
	public String getOpenurl();

	/**
	 * @return
	 */
	public String getOpenurlParameters();

	/**
	 * Access the id for the custom-url (if any) that should be used in place of the open-url.
	 * @return The id of the preferred-url, or null if no preferred-url is defined, in which case, the open-url should be used.
	 */
	public String getPreferredUrlId();

  /**
   * Get the primary URL for this resource: if a "preferred" URL was provided,
   * use it as the title link.  Otherwise, use an OpenURL.
   */
  public String getPrimaryUrl();

	/**
	 * Access the schema for the Citation
	 * @return th
	 */
	public Schema getSchema();

    /**
	 * @return
	 */
	public String getSource();
	
    /**
     * This only makes sense, and will only be set, in the context of a collection.
	 */
	public int getPosition();

	/**
     * @return
     */
    public boolean hasCustomUrls();

    /**
     * Does this citation have a custom-url that should be displayed instead of the open-url?
     */
    public boolean hasPreferredUrl();

	/**
         * @deprecated This doesn't fit with the naming pattern.
         * @see #hasCitationProperty(String)
	 * @return
	 */
	public boolean hasPropertyValue(String fieldId);

	/**
     * Read this citation from an input stream in RIS format
     * @param istream
	 * @throws IOException
     */
    public void importFromRis(InputStream ris) throws IOException;

    /**
     * Read in a citation from a List of RIS lines.
     * @param risImportList A list with each entry being a non blank String of an RIS file (e.g. "TY - BOOK").
     * @return True if the import was successful. False otherwise.
     */
	public boolean importFromRisList(List risImportList);

	/**
	 * @return
	 */
	public boolean isAdded();

    /**
     * @param fieldId
     * @return
     */
    public boolean isMultivalued(String fieldId);

	/**
	 * Access a list of names of citation properties defined for this resource.
	 * @return The list of property names.  The list may be empty, but it should never be null.
	 */
	public List listCitationProperties();

    /**
     * @param added
     */
    public void setAdded(boolean added);

    /**
	 * @param name
	 * @param value
	 */
	public void setCitationProperty(String name, Object value);

    /**
	 * @param name
	 */
	public void setDisplayName(String name);
	
    /**
     * This only makes sense, and will only be set, in the context of a collection.
     * 
     * @param position
     */
    public void setPosition(int position);

    /**
     * @param schema
     */
    public void setSchema(Schema schema);

	/**
	 * Replaces the current value(s) of a citation property.  If the field identified by the parameter "name"
	 * is multivalued, the values in the list parameter "values" replace the current values.  If the field is
	 * single valued, the first value in the list "values" replaces the current value. In either case, if the
	 * values parameter is null or empty, all current values are removed and no new values are added.
	 *
	 * @param name
	 * @param values
	 */
	public void updateCitationProperty(String name, List values);

	/**
     * @param urlid Unique ID for this URL
     * @param label Link label
     * @param url The URL
     * @param prefixRequest Use Citation.ADD_PREFIX_TEXT to add the URL prefix,
     *                          Citation.OMIT_PREFIX_TEXT to skip the prefix
     */
    public void updateCustomUrl(String urlid, String label, String url, String prefixRequest);

    /**
     * Designate that a previously defined custom-url should be used in place of the open-url as the primary
     * link in displaying the citation.
     * @param urlid The id of the custom-url to use as the preferred-url, or null to remove any previous
     * designations and restore the open-url as the primary url for the citation.
     */
    public void setPreferredUrl(String urlid);

} // interface Citation

