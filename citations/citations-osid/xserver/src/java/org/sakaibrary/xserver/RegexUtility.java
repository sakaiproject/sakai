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

package org.sakaibrary.xserver;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This class is used as a regular expressions utility for XSLT Stylesheets.
 * 
 * @author gbhatnag
 *
 */
public class RegexUtility {

	/**
	 * Tests whether or not the regular expression is contained in the string.
	 * 
	 * @param string string to be tested
	 * @param regex regex to be found
	 * @return true if regex is in string, false otherwise
	 */
    public static boolean test( String string, String regex ) {
    	Pattern pattern = Pattern.compile( regex.trim() );
    	Matcher matcher = pattern.matcher( string.trim() );

    	return matcher.find();
    }
    
    public static boolean startsWith( String string, String regex ) {
    	// get the first character
    	String substr = string.substring( 0, 1 );
    	
    	Pattern pattern = Pattern.compile( regex.trim() );
    	Matcher matcher = pattern.matcher( substr );

    	return matcher.find();
    }
    
    public static int strLength( String string ) {
    	return string.trim().length();
    }
    
    public static boolean equals( String str1, String str2 ) {
    	return str1.trim().equals( str2.trim() );
    }
}

