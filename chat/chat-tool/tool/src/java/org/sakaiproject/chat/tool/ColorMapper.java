/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.chat.tool;

import java.lang.reflect.Array;
import java.util.Hashtable;
import java.util.Map;

/**
* <p>ColorMapper is a wrapper for a Hashtable that maps user names (or any set of Strings) to colors.</p>
* <p>The colors are standard names for HTML colors.</p>
*/
public class ColorMapper
{
	// The index of the next color in the COLORS array that will be assigned to a name
	protected int m_next = 0;
	
	// A mapping of names to colors
	protected Map m_map;

	// An array of Strings representing standard HTML colors.
	protected static final String[] COLORS = 
			{ "red", "blue", "green", "orange", "firebrick", "teal", "goldenrod", 
			  "darkgreen", "darkviolet", "lightslategray", "peru", "deeppink", "dodgerblue", 
			  "limegreen", "rosybrown", "cornflowerblue", "crimson", "turquoise", "darkorange", 
			  "blueviolet", "royalblue", "brown", "magenta", "olive", "saddlebrown", "purple", 
			  "coral", "mediumslateblue", "sienna", "mediumturquoise", "hotpink", "lawngreen", 
			  "mediumvioletred", "slateblue", "indianred", "slategray", "indigo", "darkcyan",
			  "springgreen", "darkgoldenrod", "steelblue", "darkgray", "orchid", "darksalmon", 
			  "lime", "gold", "darkturquoise", "navy", "orangered",  "darkkhaki", "darkmagenta", 
			  "darkolivegreen", "tomato", "aqua", "darkred", "olivedrab" 
			};

	// the size of the COLORS array
	protected static final int NumColors = Array.getLength(COLORS);
	
	/**
	* Construct the ColorMapper.
	*/
	public ColorMapper()
	{
		m_map = new Hashtable();
		
	}	// ColorMapper
	
	/**
	* get the color associated with a name.  if name not already associated with a color,
	* make a new association and determine the next color that will be used.
	*/
	public String getColor(String name)
	{
		String color;
		if(m_map.containsKey(name))
		{
			color = (String) m_map.get(name);
		}
		else
		{
			color = COLORS[m_next++];
			m_map.put(name, color);
			if(m_next >= NumColors)
			{
				m_next = 0;
			}
		}
		
		return color;
		
	}	// getColor
	
	/**
	* Returns the mapping of names to colors.
	*/
	public Map getMapping()
	{
		return m_map;
		
	}	// getColors
		
	/**
	* Returns the index of the next color in the COLORS array that will be assigned to a name.
	*/
	public int getNext()
	{
		return m_next;
		
	}	// getNext
	
	/**
	* Returns the entire array of color names.
	*/
	public String[] getColors()
	{
		return COLORS;
		
	}	// getColors
	
	/**
	* Returns the size of the array of color names.
	*/
	public int getNum_colors()
	{
		return NumColors;
		
	}	// getNum_colors
	
}	// ColorMapper



