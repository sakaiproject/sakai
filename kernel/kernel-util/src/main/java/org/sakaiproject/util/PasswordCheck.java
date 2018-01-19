/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang.math.IntRange;

/**
 * <p>
 * Provides a set of methods for checking a password.
 * </p>
 * <p>
 * Based on the library available from http://justwild.us/examples/password/
 * </p>
 * @deprecated unused as of 12 Dec 2011, planned for removal after 2.9
 */
@Slf4j
public class PasswordCheck {
	//return values
	public static final int VERY_STRONG = 5;
	public static final int STRONG = 4;
	public static final int MEDIOCRE = 3;
	public static final int WEAK = 2;
	public static final int VERY_WEAK = 1;
	public static final int NONE = 0;

	/**
	 * Compute the strength of the given password and return it as one of the constant values.
	 * 
	 * @param passwd - The password string to check		
	 * @return strength indication of a password as per constants
	 */
	public static int getPasswordStrength(String passwd)
	{
		int upper = 0, lower = 0, numbers = 0, special = 0, length = 0, strength = 0;
		Pattern p;
		Matcher m;
		
		//null/blank passwords
		if (StringUtils.isBlank(passwd)) {
			log.debug("Password null");
			return NONE;
		}
		
		// LENGTH
		length = passwd.length();
		
		// length 4 or less
		if (length < 5) 
		{
			strength = (strength + 3);
			log.debug("3 points for length (" + length + ")");
		} 
		// length between 5 and 7
		else if (length > 4 && passwd.length() < 8) 
		{
			strength = (strength + 6);
			log.debug("6 points for length (" + length + ")");
		} 
		// length between 8 and 15
		else if (length > 7 && passwd.length() < 16) 
		{
			strength = (strength + 12);
			log.debug("12 points for length (" + length + ")");
		} 
		// length 16 or more
		else if (length > 15) 
		{
			strength = (strength + 18);
			log.debug("18 points for length (" + length + ")");
		}
		
		// LETTERS 
		// lower case
		p = Pattern.compile(".??[a-z]");
		m = p.matcher(passwd);
		while (m.find())
		{
			lower++;
		}
		if (lower > 0)
		{
			strength = (strength + 1);
			log.debug("1 point for a lower case character");
		}
		
		// upper case
		p = Pattern.compile(".??[A-Z]");
		m = p.matcher(passwd);
		while (m.find()) 
		{
			upper++;
		}
		if (upper > 0)
		{
			strength = (strength + 5);
			log.debug("5 points for an upper case character");
		}
		
		// NUMBERS
		// at least one number
		p = Pattern.compile(".??[0-9]");
		m = p.matcher(passwd);
		while (m.find())
		{
			numbers += 1;
		}
		if (numbers > 0)
		{
			strength = (strength + 5);
			log.debug("5 points for a number");
			if (numbers > 1)
			{
				strength = (strength + 2);
				log.debug("2 points for at least two numbers");
				
				if (numbers > 2)
				{
					strength = (strength + 3);
					log.debug("3 points for at least three numbers");
				}
			}
		}
		
		// SPECIAL CHAR
		// at least one special char
		p = Pattern.compile(".??[:,!,@,#,$,%,^,&,*,?,_,~]");
		m = p.matcher(passwd);
		while (m.find())
		{
			special += 1;
		}
		if (special > 0)
		{
			strength = (strength + 5);
			log.debug("5 points for a special character");
			if (special > 1)
			{
				strength += (strength + 5);
				log.debug("5 points for at least two special characters");
			}
		}
		
		// COMBOS
		// both upper and lower case
		if (upper > 0 && lower > 0)
		{
			strength = (strength + 2);
			log.debug("2 combo points for upper and lower letters");
		}
		// both letters and numbers
		if ((upper > 0 || lower > 0) && numbers > 0)
		{
			strength = (strength + 2);
			log.debug("2 combo points for letters and numbers");
		}
		// letters, numbers, and special characters
		if ((upper > 0 || lower > 0) && numbers > 0 && special > 0)
		{
			strength = (strength + 2);
			log.debug("2 combo points for letters, numbers and special chars");
		}
		// upper, lower, numbers, and special characters
		if (upper > 0 && lower > 0 && numbers > 0 && special > 0)
		{
			strength = (strength + 2);
			log.debug("2 combo points for upper and lower case letters, numbers and special chars");
		}
		if (strength < 16)
		{
			log.debug("very weak");
			return VERY_WEAK;
		} 
		else if (strength > 15 && strength < 25)
		{
			log.debug("weak");
			return WEAK;
		}
		else if (strength > 24 && strength < 35)
		{
			log.debug("mediocre");
			return MEDIOCRE;
		}
		else if (strength > 34 && strength < 45)
		{
			log.debug("strong");
			return STRONG;
		} else
		{
			log.debug("very strong");
			return VERY_STRONG;
		}
		
	}
	
	/**
	 * Method for checking the length of a password is within the bounds
	 * 
	 * @param passwd - string to check length of
	 * @param min	- minimum length
	 * @param max	- maximum length (must be >= min)
	 * @return
	 */
	public static boolean isAcceptableLength(String passwd, int min, int max) {
		
		//null
		if (StringUtils.isBlank(passwd))
		{
			return false;
		}
		
		//check bounds
		if(min > max){
			log.error("Invalid bounds supplied, min (" + min + ") is greater than max (" + max + ")");
		}
		
		// LENGTH
		int length = passwd.length();
		
		//check range
		IntRange range = new IntRange(min, max);
		
		if(range.containsInteger(length))
		{
			log.debug("Range ok");
			return true;
		}
		log.debug("Range bad; min=" + min + ", max=" + max + ", length=" + length);
		return false;
	}

	/**
	 * Generate a 9 digit password. Chars are randomly picked from lower-case/upper-case alpha lists, a numerical list, and a symbol list.
	 * 
	 * NOTE: the following characters/numbers are not included in the algorithm likely due to the fact that they can be difficult to distinguish visually:
	 * capitals D, O, numbers 1, 0, and lower case o, and l.
	 * 
	 * @return generated password
	 */
	public static String generatePassword()
	{
		return generatePassword(9);
	}
	
	/**
	 * Generate a variable length password. Chars are randomly picked from lower-case/upper-case alpha lists, a numerical list, and a symbol list.
	 * 
	 * NOTE: the following characters/numbers are not included in the algorithm likely due to the fact that they can be difficult to distinguish visually:
	 * capitals D, O, numbers 1, 0, and lower case o, and l.
	 * 
	 * @param passwordLength the length of password desired
	 * @return generated password
	 */
	public static String generatePassword(int passwordLength)
	{
		char[] LOWER_ALPHA_ARRAY =
		{
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm', 'n', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
		};
		char[] UPPER_ALPHA_ARRAY =
		{
			'A', 'B', 'C', 'E', 'F',
			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
			'W', 'X', 'Y', 'Z'
		};
		char[] NUMBER_ARRAY =
		{
			'2', '3', '4', '5', '6', '7', '8', '9'
		};
		// set random password
		StringBuilder rndbld = new StringBuilder(passwordLength);
		for (int i = 0; i < passwordLength; ++i){
				int chooseArray = (int) (4 * Math.random());
			switch (chooseArray) {
				case 0:
					rndbld.append(LOWER_ALPHA_ARRAY[(int) ( LOWER_ALPHA_ARRAY.length * Math.random())]);
					break;
				case 1:
					rndbld.append(UPPER_ALPHA_ARRAY[(int) ( UPPER_ALPHA_ARRAY.length * Math.random())]);
					break;
				case 2:
					rndbld.append(NUMBER_ARRAY[(int) ( NUMBER_ARRAY.length * Math.random())]);
					break;
				default:
					break;
			}
		}
		return rndbld.toString();
	}
}
