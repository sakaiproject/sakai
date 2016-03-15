/**
 * Copyright 2011-2013 The Australian National University
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package au.edu.anu.portal.portlets.rss.validator;

import java.util.Arrays;
import java.util.Collections;

import javax.portlet.PortletPreferences;
import javax.portlet.PreferencesValidator;
import javax.portlet.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.UrlValidator;
import org.apache.commons.validator.routines.IntegerValidator;

import au.edu.anu.portal.portlets.rss.utils.Constants;

/**
 * A validator for the preferences of the Simple RSS Portlet
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 * @throws ValidatorException if there are any errors
 */
public class SimpleRSSPreferencesValidator implements PreferencesValidator  {
	private final String PREF_FEED_URL = "feed_url";

	@Override
	public void validate(PortletPreferences prefs) throws ValidatorException {
		
		//get prefs as strings
		String max_items = prefs.getValue("max_items", Integer.toString(Constants.MAX_ITEMS));
		String feed_url = prefs.getValue(PREF_FEED_URL, null);
		
		//check readonly
		boolean feedUrlIsLocked = prefs.isReadOnly(PREF_FEED_URL);
		
		/**
		 * max_items
		 */
		IntegerValidator integerValidator = IntegerValidator.getInstance();
		Integer maxItems = integerValidator.validate(max_items);
		
		
		//check it's a number
		if(maxItems == null) {
			throw new ValidatorException("Invalid value, must be a number", Collections.singleton("max_items"));
		}
		
		//check greater than or equal to a minimum of 1
		if(!integerValidator.minValue(maxItems, Constants.MIN_ITEMS)) {
			throw new ValidatorException("Invalid number, must be greater than 0", Collections.singleton("max_items"));
		}
		
		/**
		 * feed_url
		 */
		//only validate if it's not readonly
		if(!feedUrlIsLocked) {
			String[] schemes = {"http","https"};
			DetailedUrlValidator urlValidator = new DetailedUrlValidator(schemes);
			
			//check not null
			if(StringUtils.isBlank(feed_url)){
				throw new ValidatorException("You must specify a URL for the RSS feed", Collections.singleton(PREF_FEED_URL));
			}
			
		    //check valid scheme
		    if(!urlValidator.isValidScheme(feed_url)){
				throw new ValidatorException("Invalid feed scheme. Must be one of: " + Arrays.toString(schemes), Collections.singleton(PREF_FEED_URL));
		    }
		    
		    //check valid URL
		    if(!urlValidator.isValid(feed_url)){
				throw new ValidatorException("Invalid feed URL", Collections.singleton(PREF_FEED_URL));
	
		    }
		}
		
		/**
		 * portlet_title not validated here as it is reasonable to allow blank entry. We deal with this later
		 */
		
	}
	

}

/**
 * This is a replacement for commons-validator UrlValidator. 
 * 
 * The original has all protected methods so we can't get individual components tested.
 * This simply overrides them and allows us to do so.
 * Note that the impl of isValidScheme is custom.
 * 
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
class DetailedUrlValidator extends UrlValidator {
	
	private static final long serialVersionUID = 1L;
	
	private String[] schemes;
	
	public DetailedUrlValidator(String[] schemes) {
		super(schemes);
		this.schemes = schemes;
	}
	
	@Override
	protected boolean isValidScheme(String url) {
		
		//check if url starts with one of the schemes
		if(StringUtils.startsWithAny(url, schemes)) {
			return true;
		} 
		return false;
		
	}
	
	
	
}
