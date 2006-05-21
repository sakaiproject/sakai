/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.tool;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;

/**
 * BasicRightsAssignment can be used to represent a copyright or CreativeCommons designation for a resource.
 * It can be serialized as XML and recreated.  It can be rendered through a Velocity macro or a JSF widget.
 * 
 * TAG
 * KEY
 * KEYLIST
 * LABEL
 */
public class BasicRightsAssignment
{
	protected static final String DELIM = "_";

	protected static final String FIELD_COMMERCIAL_USE = "myCommercialUse";
	
	protected static final String FIELD_INFO = "jargon";

	protected static final String FIELD_MODIFICATIONS = "myModifications";
	
	protected static final String FIELD_MORE_INFO = "moreinfo";
	
	protected static final String FIELD_MY_CR_OWNER = "myCopyrightOwner";
	
	protected static final String FIELD_MY_CR_YEAR = "myCopyrightYear";
	
	protected static final String FIELD_OFFER = "offer";
	
	protected static final String FIELD_OTHER_CR_OWNER = "otherCopyrightOwner";
	
	protected static final String FIELD_OTHER_CR_YEAR = "otherCopyrightYear";
	
	protected static final String FIELD_TERMS = "terms";
	
	protected static final String FIELD_TITLE = "title";
	
	protected static final String KEY_CREATE_CC = "new_creative_commons";
	
	protected static final String KEY_CREATE_PD = "new_public_domain";
	
	protected static final String KEY_CREATIVE_COMMONS = "creative_commons";
	
	protected static final String KEY_FAIR_USE = "fair_use";

	protected static final String KEY_LABEL = "label";

	protected static final String KEY_MY_COPYRIGHT = "my_copyright";

	protected static final String KEY_NO = "no";
	
	protected static final String KEY_OTHER_COPYRIGHT = "other_copyright";
	
	protected static final String KEY_PUBLIC_DOMAIN = "public_domain";
	
	protected static final String KEY_SHARE_ALIKE = "share_alike";
	
	protected static final String KEY_YES = "yes";
	
	protected static final String[] KEYLIST_TERMS = { KEY_MY_COPYRIGHT, KEY_OTHER_COPYRIGHT, KEY_PUBLIC_DOMAIN, KEY_CREATIVE_COMMONS };

	protected static final String FIELD_DONE = "done";
	
	/** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("right");
    
    protected String ccCommercialUse;
    
    protected String ccModifications;
	
    /** Member variable representing the name of the copyright holder (copyright holder is this user) */
	protected String myCopyrightOwner;
	
    /** Member variable representing the year of the copyright (copyright holder is this user) */
	protected String myCopyrightYear;
	
	/** A unique identifier that can be used in form fields to distinguish this rights obj from others in the same form */
	protected String name;
	
	/** Member variable representing user's secondary choice of how their work is offered to users (full copyright, new CC license, new PD dedication) */
	protected String offer;
	
	/** Member variable representing the name of the copyright holder (copyright holder is NOT this user) */
	protected String otherCopyrightOwner;
	
    /** Member variable representing the year of the copyright (copyright holder is NOT this user) */
	protected String otherCopyrightYear;
	
	/** Member variable representing the status of IP rights for this work (user's copyright, someone else's copyright, existing CC license or existing PD status) */
	protected String terms;
	
	/** Member variable representing whether we using the new Creative Commons dialog or the old Sakai Copyright dialog */
	protected boolean usingCreativeCommons;
	
	/**
	 * Construct
	 * @param name A name for this instance that can be used as a unique identifier in rendering a set of form fields 
	 * 	to input values for this object. Should not contain characters that are not valid in id's and names of html
	 * 	form-input elements.
	 * @param usingCreativeCommons true if the Creative Commons License should be shown as an option.
	 */
	public BasicRightsAssignment(String name, boolean usingCreativeCommons)
	{
		this.name = name;
		this.usingCreativeCommons = usingCreativeCommons;
		
		terms = KEY_MY_COPYRIGHT;
		//terms = KEY_OTHER_COPYRIGHT;
		offer = KEY_CREATE_CC;
	}
	
	/**
	 * Retrieve values for the rights assignment from a Velocity context.
	 * @param params 
	 */
	public void captureValues(ParameterParser params)
	{
		if(usingCreativeCommons)
		{
			String terms = params.getString(getFieldNameTerms());
			if(terms != null)
			{
				this.setTerms(terms);
			}
			String myCopyrightYear = params.getString(getFieldNameMyCopyrightYear());
			if(myCopyrightYear != null)
			{
				this.setMyCopyrightYear(myCopyrightYear);
			}
			String myCopyrightOwner = params.getString(getFieldNameMyCopyrightOwner());
			if(myCopyrightOwner != null)
			{
				this.setMyCopyrightOwner(myCopyrightOwner);
			}
			String otherCopyrightYear = params.getString(getFieldNameOtherCopyrightYear());
			if(otherCopyrightYear != null)
			{
				this.setOtherCopyrightYear(otherCopyrightYear);
			}
			String otherCopyrightOwner = params.getString(getFieldNameOtherCopyrightOwner());
			if(otherCopyrightOwner != null)
			{
				this.setOtherCopyrightOwner(otherCopyrightOwner);
			}
			String offer = params.getString(getFieldNameOffer());
			if(offer != null)
			{
				this.setOffer(offer);
			}
		}
		else
		{
		}
	}
	
	/**
	 * @return Returns the ccCommercialUse.
	 */
	public String getCcCommercialUse()
	{
		return ccCommercialUse;
	}
	
	/**
	 * @return Returns the ccModifications.
	 */
	public String getCcModifications()
	{
		return ccModifications;
	}
	
	/** 
	 * Access the current user's display name.
	 * @return
	 */
	public String getDefaultCopyrightOwner()
	{
		String username = UserDirectoryService.getCurrentUser().getDisplayName(); 
		return username;
	}
	
	/**
	 * Returns the current year.
	 * @return
	 */
	public String getDefaultCopyrightYear()
	{
		int year = TimeService.newTime().breakdownLocal().getYear();
		return Integer.toString(year);

	}
	
	public String getFieldNameCommercialUse()
	{
		return name + DELIM + FIELD_COMMERCIAL_USE;
	}
	
	public String getFieldNameModifications()
	{
		return name + DELIM + FIELD_MODIFICATIONS;
	}
	
	
	/**
	 * Returns the field name for the copyright "owner" element.
	 */
	public String getFieldNameMyCopyrightOwner()
	{
		return name + DELIM + FIELD_MY_CR_OWNER;
	}
	
	/**
	 * Returns the field name for the copyright "year" element.
	 */
	public String getFieldNameMyCopyrightYear()
	{
		return name + DELIM + FIELD_MY_CR_YEAR;
	}

	/**
	 * Returns the field name for the copyright "year" element.
	 */
	public String getFieldNameOffer()
	{
		return name + DELIM + FIELD_OFFER;
	}

	/**
	 * Returns the field name for the copyright "owner" element.
	 */
	public String getFieldNameOtherCopyrightOwner()
	{
		return name + DELIM + FIELD_OTHER_CR_OWNER;
	}

	/**
	 * Returns the field name for the copyright "year" element.
	 */
	public String getFieldNameOtherCopyrightYear()
	{
		return name + DELIM + FIELD_OTHER_CR_YEAR;
	}

	/**
	 * Returns the field name for the "terms" element.
	 */
	public String getFieldNameTerms()
	{
		return name + DELIM + FIELD_TERMS;
	}

	/**
	 * 
	 * @return
	 */
	public String getInfoCreativeCommons()
	{
		return getString(FIELD_INFO, KEY_CREATIVE_COMMONS);
	}

	/**
	 * 
	 * @return
	 */
	public String getInfoMyCopyright()
	{
		return getString(FIELD_INFO, KEY_MY_COPYRIGHT);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getInfoOtherCopyright()
	{
		return getString(FIELD_INFO, KEY_OTHER_COPYRIGHT);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getInfoPublicDomain()
	{
		return getString(FIELD_INFO, KEY_PUBLIC_DOMAIN);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getKeyCreativeCommons()
	{
		return KEY_CREATIVE_COMMONS;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getKeyFairUse()
	{
		return KEY_FAIR_USE;
	}
	
	public List getKeylistTerms()
	{
		return getKeys(KEYLIST_TERMS);
	}

	/**
	 * 
	 * @return
	 */
	public String getKeyMyCopyright()
	{
		return KEY_MY_COPYRIGHT;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getKeyNewCreativeCommons()
	{
		return KEY_CREATE_CC;
	}

	/**
	 * 
	 * @return
	 */
	public String getKeyNo()
	{
		return KEY_NO;
	}

	/**
	 * 
	 * @return
	 */
	public String getKeyYes()
	{
		return KEY_YES;
	}

	/**
	 * 
	 * @return
	 */
	public String getKeyShareAlike()
	{
		return KEY_SHARE_ALIKE;
	}

	/**
	 * 
	 * @return
	 */
	public String getKeyNewPublicDomain()
	{
		return KEY_CREATE_PD;
	}

	/**
	 * 
	 * @return
	 */
	public String getKeyOtherCopyright()
	{
		return KEY_OTHER_COPYRIGHT;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getKeyPublicDomain()
	{
		return KEY_PUBLIC_DOMAIN;
	}
	
	/**
	 * Return a list of keys.
	 * @param array An array of strings containing the keys.
	 */
	protected List getKeys(String[] array)
	{
		return Arrays.asList(array);
	}
	
	public String getLabelCommercialUseNo()
	{
		return getString(FIELD_COMMERCIAL_USE, KEY_NO);
	}
	
	public String getLabelCommercialUse()
	{
		return getString(FIELD_COMMERCIAL_USE, KEY_LABEL);
	}
	
	public String getLabelCommercialUseYes()
	{
		return getString(FIELD_COMMERCIAL_USE, KEY_YES);
	}

	public String getLabelDone()
	{
		return getString(FIELD_DONE, KEY_LABEL);
	}

	/**
	 * 
	 * @return
	 */
	public String getLabelFairUse()
	{
		return getString(FIELD_OFFER, KEY_FAIR_USE);
	}
	
	public String getLabelModifications()
	{
		return getString(FIELD_MODIFICATIONS, KEY_LABEL);
	}
	
	public String getLabelModificationsNo()
	{
		return getString(FIELD_MODIFICATIONS, KEY_NO);
	}
	
	public String getLabelModificationsShareAlike()
	{
		return getString(FIELD_MODIFICATIONS, KEY_SHARE_ALIKE);
	}
	
	public String getLabelModificationsYes()
	{
		return getString(FIELD_MODIFICATIONS, KEY_YES);
	}
	
	public String getLabelMoreInfo()
	{
		return getString(FIELD_MORE_INFO, KEY_LABEL);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLabelMyCopyrightOwner()
	{
		return getString(FIELD_MY_CR_OWNER, KEY_LABEL);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLabelMyCopyrightYear()
	{
		return getString(FIELD_MY_CR_YEAR, KEY_LABEL);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLabelNewCreativeCommons()
	{
		return getString(FIELD_OFFER, KEY_CREATE_CC);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLabelNewPublicDomain()
	{
		return getString(FIELD_OFFER, KEY_CREATE_PD);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLabelOtherCopyrightOwner()
	{
		return getString(FIELD_OTHER_CR_OWNER, KEY_LABEL);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLabelOtherCopyrightYear()
	{
		return getString(FIELD_OTHER_CR_YEAR, KEY_LABEL);
	}
	
	protected List getLabels(String[] array)
	{
		List list = new Vector();
		for(int i = 0; i < array.length; i++ )
		{
			String label = rb.getString(array[i]);
			list.add(label);
		}
		return list;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLabelTerms()
	{
		return getString(FIELD_TERMS, KEY_LABEL);
	}
	
	/**
	 * @return Returns the copyrightOwner.
	 */
	public String getMyCopyrightOwner()
	{
		return myCopyrightOwner;
	}
		
	/**
	 * @return Returns the copyrightYear.
	 */
	public String getMyCopyrightYear()
	{
		return myCopyrightYear;
	}
		
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}
		
	/**
	 * @return Returns the offer.
	 */
	public String getOffer()
	{
		return offer;
	}
	
	/**
	 * @return Returns the otherCopyrightOwner.
	 */
	public String getOtherCopyrightOwner()
	{
		return otherCopyrightOwner;
	}
		
	/**
	 * @return Returns the otherCopyrightYear.
	 */
	public String getOtherCopyrightYear()
	{
		return otherCopyrightYear;
	}
		
	/**
	 * Access a string from the resource bundle identified by a key. 
	 * @param key
	 * @return
	 */
	public String getString(String key)
	{
		return rb.getString(key);
	}
		
	/**
	 * Access a string from the resource bundle identified by a key. 
	 * @param key
	 * @return
	 */
	public String getString(String category, String item)
	{
		return getString(category + "." + item);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public String[] getStrings(String key)
	{
		return rb.getStrings(key);
	}

	/**
	 * @return Returns the terms.
	 */
	public String getTerms()
	{
		return terms;
	}

	/**
	 * 
	 * @return
	 */
	public String getTitleCreativeCommons()
	{
		return getString(FIELD_TITLE, KEY_CREATIVE_COMMONS);
	}

	/**
	 * 
	 * @return
	 */
	public String getTitleMyCopyright()
	{
		return getString(FIELD_TITLE, KEY_MY_COPYRIGHT);
	}

	/**
	 * 
	 * @return
	 */
	public String getTitleOtherCopyright()
	{
		return getString(FIELD_TITLE, KEY_OTHER_COPYRIGHT);
	}

	/**
	 * 
	 * @return
	 */
	public String getTitlePublicDomain()
	{
		return getString(FIELD_TITLE, KEY_PUBLIC_DOMAIN);
	}

	/**
	 * @return Returns the usingCreativeCommons.
	 */
	public boolean getUsingCreativeCommons()
	{
		return usingCreativeCommons;
	}

	/**
	 * @param ccCommercialUse The ccCommercialUse to set.
	 */
	public void setCcCommercialUse(String ccCommercialUse)
	{
		this.ccCommercialUse = ccCommercialUse;
	}

	/**
	 * @param ccModifications The ccModifications to set.
	 */
	public void setCcModifications(String ccModifications)
	{
		this.ccModifications = ccModifications;
	}

	/**
	 * @param copyrightOwner The copyrightOwner to set.
	 */
	public void setMyCopyrightOwner(String copyrightOwner)
	{
		this.myCopyrightOwner = copyrightOwner;
	}

	/**
	 * @param copyrightYear The copyrightYear to set.
	 */
	public void setMyCopyrightYear(String copyrightYear)
	{
		this.myCopyrightYear = copyrightYear;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @param offer The offer to set.
	 */
	public void setOffer(String offer)
	{
		this.offer = offer;
	}

	/**
	 * @param otherCopyrightOwner The otherCopyrightOwner to set.
	 */
	public void setOtherCopyrightOwner(String otherCopyrightOwner)
	{
		this.otherCopyrightOwner = otherCopyrightOwner;
	}

	/**
	 * @param otherCopyrightYear The otherCopyrightYear to set.
	 */
	public void setOtherCopyrightYear(String otherCopyrightYear)
	{
		this.otherCopyrightYear = otherCopyrightYear;
	}

	/**
	 * @param terms The terms to set.
	 */
	public void setTerms(String terms)
	{
		this.terms = terms;
	}

	/**
	 * @param usingCreativeCommons The usingCreativeCommons to set.
	 */
	public void setUsingCreativeCommons(boolean usingCreativeCommons)
	{
		this.usingCreativeCommons = usingCreativeCommons;
	}

	

	
}
