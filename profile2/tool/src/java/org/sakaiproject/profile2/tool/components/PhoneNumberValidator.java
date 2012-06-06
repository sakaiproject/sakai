/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.components;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
/**
 * Validator for checking a phone number. There are many different types so the check is pretty loose.
 * 
 * <p>For instance, the following are valid Australian formats for the same number: <br />
 *	
 *  1. +61 2 1234 5678 (abbreviated international number)<br />
 *	2. 0011 61 2 1234 5678 (full international number) <br />
 *	3. 02 1234 5678 (expanded area code) <br />
 *	4. (02) 1234 5678 (expanded area code) <br />
 *	5. 1234 5678 (if calling in 02 areacode) <br /><br />
 *  
 *  plus all of the above without spaces.</p>
 *  
 *  <p>Also added the following style numbers:<br />
 *  6. 111-222-3333 <br />
 *  7. 111.222.3333 <br />
 *  8. (111) 222-3333-444 <br />
 *  9. (111) 222.3333.444 <br /><br />
 *  
 *  plus combinations of . and - in the same numbers above</p>
 *  
 *  <p>Now for numbers with extensions, add (\\w+)?+ 
 *  10. All of the above with any letter/number combo afterwards but it must start with a letter and not contain spaces and only occur once or not at all
 *   eg (123-456-789 x123, 123 456 789 ext5)
 *  
 *  </p>
 *  
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class PhoneNumberValidator extends AbstractValidator {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see AbstractValidator#onValidate(IValidatable)
	 */
	protected void onValidate(IValidatable validatable)
	{
		//setup list
		List<String> regexs = new ArrayList<String>();
		regexs.add("\\+?([0-9]+|\\s+)+(\\w+)?+"); //matches 1,2,3,5 with 10
		regexs.add("\\({1}[0-9]+\\){1}([0-9]+|\\s+)+(\\w+)?+"); //matches 4 with 10
		regexs.add("([0-9]+(\\-|\\.)?)+(\\w+)?+"); //matches 6, 7 with 10
		regexs.add("\\({1}[0-9]+\\){1}([0-9]+|\\s+|\\-?|\\.?)+(\\w+)?+"); //matches 8,9 with 10
		
		//check each, if none, error
		for(String r: regexs) {
			Pattern p = Pattern.compile(r);
			if (p.matcher((String)validatable.getValue()).matches()) {
				return;
			}
		}
		
		//if we haven't matched, error.
		error(validatable);
	}
	
}
