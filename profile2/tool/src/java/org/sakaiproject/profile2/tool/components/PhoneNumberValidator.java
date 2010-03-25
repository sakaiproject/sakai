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
		regexs.add("\\+?([0-9]+|\\s+)+"); //matches 1,2,3,5
		regexs.add("\\({1}[0-9]+\\){1}([0-9]+|\\s+)+"); //matches 4

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
