/**********************************************************************************
*
 * Copyright (c) 2003, 2004, 2007, 2008 The Sakai Foundation
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
package edu.indiana.lib.twinpeaks.search;

import edu.indiana.lib.twinpeaks.util.*;

import java.io.*;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.osid.repository.Asset;
import org.osid.repository.AssetIterator;
import org.osid.repository.Record;
import org.osid.repository.Part;
import org.osid.repository.PartStructure;



/**
 * Encapsulate one matching search item
 */
@Slf4j
public class MatchItem
{
  private String        _database;
	private String				_displayName;
	private String				_description;
	private String				_id;
	private String				_openUrl;
	private List					_partStructureList;

	/**
	 * Constructor
	 */
	public MatchItem()
	{
		super();
		_partStructureList = new ArrayList();
	}

	/**
	 * Fetch the Asset "origin database"
	 * @return The database name (eg EAP)
	 */
	public String getDatabase()
	{
		return _database;
	}

	/**
	 * Set the Asset "origin database"
	 * @param database The database this record was found in
	 */
	public void setDatabase(String database)
	{
		_database = database;
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

  /*
   * Helpers
   */

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