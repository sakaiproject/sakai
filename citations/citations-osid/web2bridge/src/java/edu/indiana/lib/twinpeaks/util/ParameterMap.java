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
package edu.indiana.lib.twinpeaks.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Parameter (name=value pair) manipulation.  In particular, this utility
 * supports multiple parameters with the same name:
 * <code>
 *    xxx=value1&xxx=value2
 * </code>
 */
@Slf4j
public class ParameterMap
{
	static private final int 	PREFIXSIZE 					= 4;
	static private final int 	MAXVALUE   					= (1 << PREFIXSIZE * 4) - 1;

	private HashMap 					_parameterMap;
	private Map.Entry					_entry;
	private int 							_fill;

	/**
	 * Public constructor
	 */
  public ParameterMap() {
  	_parameterMap = new HashMap();
  	_fill 				= 0;
	}

	/**
	 * Empty the parameter map
	 */
	public synchronized void clear() {
	 	_parameterMap.clear();
	}

	/**
	 * Get an Iterator to the parameter map
	 * @return Map iterator
	 */
	public synchronized Iterator getParameterMapIterator() {
		return _parameterMap.entrySet().iterator();
	}

	/**
	 * Save the next Entry Set for this Iterator
	 * @param iterator Parameter map iterator
	 * @return false If no more entries exist
	 */
	public synchronized boolean nextParameterMapEntry(Iterator iterator) {
		if (!iterator.hasNext()) {
			_entry = null;
			return false;
		}

		_entry = (Map.Entry) iterator.next();
		return true;
	}

	/**
	 * Get the parameter name from the current iterator
	 * @return Item name
	 */
	public synchronized String getParameterNameFromIterator() {
		return (String) ((String) _entry.getKey()).substring(PREFIXSIZE);
	}

	/**
	 * Get the parameter value from the current iterator
	 * @return Item value
	 */
	public synchronized String getParameterValueFromIterator() {
		return (String) _entry.getValue();
	}

	/**
	 * Set a name/value pair
	 * @param name Item name
	 * @param value Item value
	 */
	public synchronized void setParameterMapValue(String name, String value) {
			String uniqueName = generateFillText() + name;

			_parameterMap.put(uniqueName, value);
	}

	/**
	 * Get a named value from the parameter map
	 * @param name Item name
	 * @return Item value (a String, null if none)
	 */
	public synchronized String getParameterMapValue(String name) {
		Iterator iterator = getParameterMapIterator();

		while (iterator.hasNext()) {
			Map.Entry entry 	= (Map.Entry) iterator.next();
			String		mapName = (String) ((String) entry.getKey()).substring(PREFIXSIZE);

			if (mapName.equals(name)) {
				return (String) entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Get the parameter name associated with the 1st occurance of this value
	 * @param value Item value
	 * @return Item name (a String, null if none)
	 */
	public synchronized String getParameterMapName(String value) {
		Iterator iterator = getParameterMapIterator();

		while (iterator.hasNext()) {
			Map.Entry entry 		= (Map.Entry) iterator.next();
			String		mapValue	= (String) ((String) entry.getValue());

			if (mapValue.equals(value)) {
				return (String) ((String) entry.getKey()).substring(PREFIXSIZE);
			}
		}
		return null;
	}

	/**
	 * Fetch and increment the "unique fill text" seed
	 * @return Current seed value
	 */
	private synchronized int getFillSeed() {
		return _fill++;
	}

	/**
	 * Build fixed length numeric filler text.
	 * @return Fill text.  This is the HEX representation of the "rightmost"
	 *         <code>PREFIXSIZE</code> digits of the original value.
	 */
	private String generateFillText() {
	  StringBuilder  result;
	  int           value;

		value = getFillSeed();
    if ((value < 0) || (value > MAXVALUE)) {
      throw new UnsupportedOperationException("Value " +  value + " out of range");
    }

	  result  = new StringBuilder();

    for (int i = 0; i < PREFIXSIZE; i++) {
      result.insert(0, Integer.toHexString(value & 0xf));
      value >>= 4;
    }
    return result.toString();
	}

	/**
	 * Populate a parameter map from an URL argument list.  Any of:
	 * <p><code>
	 *    http://x.y.com?xxx=yyy&aaa=bbb
	 * </code>
	 *<br>
	 * <code>
	 *    ?xxx=yyy&aaa=bbb
	 * </code>
	 *<br>
	 * <code>
	 *    xxx=yyy&aaa=bbb
	 * </code><p>
	 * Will populated the map as:
	 * <code>
	 *    xxx=yyy
	 *    aaa=bbb
	 * </code><br>
	 *
	 * @param url Original URL [with optional parameter list] or only
	 *										 the parameter list [with an optional ? prefix]
	 * @param delimiter Argument separator text (regular expression)
	 * @return Number of parameters saved
	 */
  public synchronized int populateMapFromUrlArguments(String url, String delimiter) {
  	String		parameters 	= url;
  	int				count 			= 0;
  	String[]	p;

		/*
		 * Find the argument list
		 */
		if (url.startsWith("?")) {
			parameters = (url.length() == 1) ? "" : url.substring(1);

		} else if (url.indexOf("://") != -1) {
			int index = url.indexOf('?');

			parameters = "";
			if ((index != -1) && (index != url.length())) {
				parameters = url.substring(index + 1);
			}
		}
		/*
		 * Save the name=value pairs
		 */
		p = parameters.split(delimiter);
  	for (int i = 0; i < p.length; i++) {
  		int 		index;

  		if ((index = p[i].indexOf("=")) == -1) {
  			/*
  			 * Omit if no value specified
  			 */
  			continue;
  		}

			setParameterMapValue(p[i].substring(0, index),
													 p[i].substring(index + 1));
			count++;
		}
		return count;
	}
}