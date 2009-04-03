/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/chat/trunk/chat-tool/tool/src/java/org/sakaiproject/chat/tool/ColorMapper.java $
 * $Id: ColorMapper.java 8206 2006-04-24 19:40:15Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.chat2.tool;


import java.lang.reflect.Array;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
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
	* get the color associated with a string.  if name not already associated with a color,
	* make a new association and determine the next color that will be used.  the same string will
   * always have the same color
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
      
   }  // getMapping

   public class KeyValue {
      String k,v;
      public KeyValue(String k, String v){this.k = k; this.v = v;}
      public String getKey() {return k;}
      public String getValue() {return v;}
   }
   
   /**
   * Returns the mapping of names to colors.
   */
   public List getMappingList()
   {
      List mapList = new ArrayList();
      
      for(Iterator i = m_map.keySet().iterator(); i.hasNext(); ) {
         String key = (String)i.next();
         String value = (String)m_map.get(key);
         
         mapList.add(new KeyValue(key,value));
      }
      
      return mapList;
      
   }  // getMapping
		
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



