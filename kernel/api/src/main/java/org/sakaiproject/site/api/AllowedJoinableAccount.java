/**
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.api;

/**
 * Represents an account that is allowed to join a site, as defined by sakai.properties
 * Contains the account type, the label for the user, and the category it belongs to
 * 
 * @author sfoster9, bjones86
 */
public class AllowedJoinableAccount 
{
	/** Sakai account type */
	private String type;
	
	/** Public-facing text to list or describe the account type */
	private String label;
	
	/** Category to organize multiple account types */
	private String category;

	/**
	 *  Default zero-arg constructor
	 */
	public AllowedJoinableAccount()
	{
		type 	 = "";
		label 	 = "";
		category = "";
	}
	
	/**
	 * Full Constructor
	 * 
	 * @param accountType
	 * 				 the account type
	 * @param accountLabel
	 * 				the account type label
	 * @param accountCategory
	 * 				the account type category
	 */
	public AllowedJoinableAccount( String accountType, String accountLabel, String accountCategory )
	{
		type 	 = accountType;
		label 	 = accountLabel;
		category = accountCategory;
	}

	/** Getter for account type */
	public String getType() { return type; }
	
	/** Getter for account label */
	public String getLabel() { return label; }
	
	/** Getter for account category */
	public String getCategory() { return category; }
	
	/** Setter for account type */
	public void setType( String accountType ) { type = accountType; }
	
	/** Setter for account label */
	public void setLabel( String accountLabel ) { label = accountLabel; }
	
	/** Setter for account category */
	public void setCategory( String accountCategory ) { category = accountCategory; }
}
