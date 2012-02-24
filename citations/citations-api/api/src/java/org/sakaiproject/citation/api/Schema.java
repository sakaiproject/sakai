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

import java.util.List;
import java.util.Set;

public interface Schema 
{
	public static final String LONGTEXT = "longtext";
	public static final String SHORTTEXT = "shorttext";
	public static final String NUMBER = "number";
	public static final String DATE = "date";
	
	public static final int UNLIMITED = Integer.MAX_VALUE;
	
	public static final String ESCAPE_FIELD_NAME = ":&%~!@#$^*()+=\"'`<>,/? ";
	public static final char ESCAPE_CHAR = '_';
	
	// public static final String AUTHOR = "author";
	public static final String TITLE = "title";
	public static final String EDITOR = "editor";
	public static final String CREATOR = "creator";
	public static final String VOLUME = "volume";
	public static final String ISSUE = "issue";
	public static final String PAGES = "pages";
	public static final String PUBLISHER = "publisher";
	public static final String YEAR = "year";
	public static final String ISN = "isnIdentifier";
	public static final String SOURCE_TITLE = "sourceTitle";
	
	public interface Field
	{
		public String getNamespaceAbbreviation();
		public String getIdentifier();
		public String getIdentifier(String format);
		public String[] getIdentifierComplex(String format);		
		public String getDescription();
		public String getValueType();
		public boolean isEditable();
		public boolean isRequired();
		public int getMinCardinality();
		public int getMaxCardinality();
		public Object getDefaultValue();
		public String getLabel();
		public boolean isMultivalued();
		
	}
	
	public String getNamespaceAbbrev();
	
	public String getNamespaceUri(String abbrev);
	
	public String getIdentifier();
	
	public List getNamespaceAbbreviations();
	
	public List getFields();
	
	public List getRequiredFields();
	
	public Field getField(String name);
	
	public Field getField(int index);

}
