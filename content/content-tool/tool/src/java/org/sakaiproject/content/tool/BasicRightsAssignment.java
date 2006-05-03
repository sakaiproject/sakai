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

import java.util.List;
import java.util.Vector;

import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;

/**
 * BasicRightsAssignment can be used to represent a copyright or CreativeCommons designation for a resource.
 * It can be serialized as XML and recreated.  It can be rendered through a Velocity macro or a JSF widget.
 */
public class BasicRightsAssignment
{
	public static final String KEY_AUTHOR_LABEL = "authorLabel";
	
	public static final String KEY_AUTHOR_ME = "author_me";
	
	public static final String KEY_AUTHOR_OTHER = "author_other";

	public static final String KEY_COMMERCIAL_LABEL = "ccCommercialLabel";

	public static final String KEY_COMMERCIAL_NO = "ccCommercial_no";

	public static final String KEY_COMMERCIAL_YES = "ccCommercial_yes";

	public static final String KEY_GRANTED = "CC_granted";

	public static final String KEY_MODIFY_LABEL = "ccModifyLabel";

	public static final String KEY_MODIFY_NO = "ccModify_no";

	public static final String KEY_MODIFY_SHARE = "ccModify_sharealike";

	public static final String KEY_MODIFY_YES = "ccModify_yes";

	public static final String KEY_COPYRIGHT_EXPIRED = "CopyrightExpired";

	public static final String KEY_COPYRIGHT_OWNER_LABEL = "copyrightOwnerLabel";

	public static final String KEY_COPYRIGHT_YEAR_LABEL = "copyrightYearLabel";
	
	public static final String KEY_CREATIVE_COMMONS = "CreativeCommons";
	
	public static final String KEY_FAIR_USE = "FairUse";
	
	public static final String KEY_GOVT_DOC = "GovtDocument";
	
	public static final String KEY_MY_COPYRIGHT = "MyCopyright";
	
	public static final String KEY_MY_TERMS_LABEL = "ccMyTermsLabel";
	
	public static final String KEY_OTHER_TERMS_LABEL = "ccOtherTermsLabel";
	
	public static final String KEY_PD_DEDICATION = "PD_dedication";
	
	public static final String KEY_PREDATES_COPYRIGHT = "PredatesCopyright";
	
	public static final String KEY_PUBLIC_DOMAIN = "PublicDomain";
	
	public static final String KEY_SELECT = "select"; 
	
	public static final String[] KEYLIST_AUTHORSHIP = { KEY_SELECT, KEY_AUTHOR_ME, KEY_AUTHOR_OTHER };
	
	public static final String[] KEYLIST_COMMERCIAL = { KEY_COMMERCIAL_YES, KEY_COMMERCIAL_NO };
	
	public static final String[] KEYLIST_MODIFICATION = { KEY_MODIFY_YES, KEY_MODIFY_SHARE, KEY_MODIFY_NO };
	
	public static final String[] KEYLIST_MYTERMS = { KEY_SELECT, KEY_CREATIVE_COMMONS, KEY_PUBLIC_DOMAIN, KEY_MY_COPYRIGHT };
	
	public static final String[] KEYLIST_OTHERTERMS = { KEY_SELECT, KEY_GRANTED, KEY_PD_DEDICATION, KEY_FAIR_USE, KEY_PREDATES_COPYRIGHT, KEY_COPYRIGHT_EXPIRED, KEY_GOVT_DOC };
	
	/** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("right");
	
	public static final String TAG_AUTHOR = "ccAuthorship";
	
	public static final String TAG_COMMERCIAL = "ccCommercial";
	
	public static final String TAG_MODIFY = "ccModification";
	
	public static final String TAG_MY_TERMS = "ccTerms";
	
	public static final String TAG_OTHER_TERMS = "ccOtherTerms";
	
	public static final String TAG_RIGHTS_OWNER = "ccRightsOwner";
	
	public static final String TAG_RIGHTS_YEAR = "ccRightsYear";
	
	public static final String TAG_COPYRIGHT_ALERT = "copyrightAlert";
	
	public static final String TAG_COPYRIGHT_INFO = "copyrightInfo";

	public static final String TAG_COPYRIGHT_STATUS = "copyrightStatus";

	public static final String TAG_COPYRIGHT_TERMS = "copyrightTerms";
	
	protected String ccCommercial;
	
	protected String ccModification;
	
	protected String ccOwnership;
	
	protected String ccRightsOwner;
	
	protected String ccRightsTerms;
	
	protected String ccRightsYear;
	
	protected String ccTerms;
	
	protected boolean copyrightAlert;
	
	protected String copyrightInfo;
	
	protected String copyrightStatus;
	
	protected String name;

	protected boolean usingCreativeCommons = true;

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
	}
	
	public void captureValues(ParameterParser params)
	{
		if(usingCreativeCommons)
		{
			String author = params.getString(getFieldNameAuthorship());
			if(author != null)
			{
				this.setCcOwnership(author);
			}
			String ccTerms = params.getString(getFieldNameMyTerms());
			if(ccTerms != null)
			{
				this.setCcTerms(ccTerms);
			}
			String ccRightsTerms = params.getString(getFieldNameOtherTerms());
			if(ccRightsTerms != null)
			{
				this.setCcRightsTerms(ccRightsTerms);
			}
			String ccCommercial = params.getString(getFieldNameCcCommercial());
			if(ccCommercial != null)
			{
				this.setAllowCommercial(ccCommercial);
			}
			String ccModification = params.getString(getFieldNameCcModify());
			if(ccCommercial != null)
			{
				this.setAllowModifications(ccModification);
			}
			String ccRightsYear = params.getString(getFieldNameCcRightsYear());
			if(ccRightsYear != null)
			{
				this.setRightstyear(ccRightsYear);
			}
			String ccRightsOwner = params.getString(getFieldNameCcRightsOwner());
			if(ccRightsOwner != null)
			{
				this.setRightsowner(ccRightsOwner);
			}
		}
		else
		{
			// check for copyright status
			// check for copyright info
			// check for copyright alert

			String copyrightStatus = StringUtil.trimToNull(params.getString (getFieldNameCopyrightStatus()));
			if(copyrightStatus != null)
			{
				this.copyrightStatus = copyrightStatus;
			}
			String copyrightInfo = StringUtil.trimToNull(params.getCleanString (getFieldNameCopyrightInfo()));
			if(copyrightInfo != null)
			{
				this.copyrightInfo = copyrightInfo;
			}
			String copyrightAlert = StringUtil.trimToNull(params.getString(getFieldNameCopyrightAlert()));
			this.copyrightAlert = copyrightAlert != null && copyrightAlert.equals(Boolean.TRUE.toString());
		}
	}
	
	/**
	 * @return
	 */
	public List getCcAuthorshipKeys()
	{
		return getKeys(KEYLIST_AUTHORSHIP);
	}
	
	/**
	 * @return
	 */
	public List getCcAuthorshipOptions()
	{
		return getLabels(KEYLIST_AUTHORSHIP);
	}
	/**
	 * @return Returns the ccCommercial.
	 */
	public String getCcCommercial()
	{
		return ccCommercial;
	}
	/**
	 * @return Returns the ccModification.
	 */
	public String getCcModification()
	{
		return ccModification;
	}
	/**
	 * @return Returns the ccOwnership.
	 */
	public String getCcOwnership()
	{
		return ccOwnership;
	}
	/**
	 * @return Returns the ccRightsOwner.
	 */
	public String getCcRightsOwner()
	{
		return ccRightsOwner;
	}
	public String getCcRightsTerms()
	{
		return this.ccRightsTerms;
	}
	/**
	 * @return Returns the ccRightsYear.
	 */
	public String getCcRightsYear()
	{
		return ccRightsYear;
	}
	/**
	 * @return Returns the ccTerms.
	 */
	public String getCcTerms()
	{
		return ccTerms;
	}

	public List getCommercialKeys()
	{
		return getKeys(KEYLIST_COMMERCIAL);
	}

	public List getCommercialOptions()
	{
		return getLabels(KEYLIST_COMMERCIAL);
	} 
	
	/**
	 * @return Returns the copyrightStatus.
	 */
	public String getCopyrightStatus()
	{
		return copyrightStatus;
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

	/**
	 * @return Returns the field name for authorship.
	 */
	public String getFieldNameAuthorship()
	{
		return name + TAG_AUTHOR;
	}

	/**
	 * @return Returns the field name for ccCommercial.
	 */
	public String getFieldNameCcCommercial()
	{
		return name + TAG_COMMERCIAL;
	}

	/**
	 * @return Returns the field name for ccModification.
	 */
	public String getFieldNameCcModify()
	{
		return name + TAG_MODIFY;
	}

	/**
	 * @return Returns the field name for ccRightsOwner.
	 */
	public String getFieldNameCcRightsOwner()
	{
		return name + TAG_RIGHTS_OWNER;
	}

	/**
	 * @return Returns the field name for ccRightsYear.
	 */
	public String getFieldNameCcRightsYear()
	{
		return name + TAG_RIGHTS_YEAR;
	}

	/**
	 * @return Returns the field name for copyrightAlert.
	 */
	public String getFieldNameCopyrightAlert()
	{
		return name + TAG_COPYRIGHT_ALERT;
	}

	/**
	 * @return Returns the field name for copyrightInfo.
	 */
	public String getFieldNameCopyrightInfo()
	{
		return name + TAG_COPYRIGHT_INFO;
	}

	/**
	 * @return Returns the field name for copyrightStatus.
	 */
	public String getFieldNameCopyrightStatus()
	{
		return name + TAG_COPYRIGHT_STATUS;
	}

	/**
	 * @return Returns the field name for ccTerms.
	 */
	public String getFieldNameMyTerms()
	{
		return name + TAG_MY_TERMS;
	}

	private String getFieldNameOtherTerms()
	{
		return name + TAG_OTHER_TERMS;
	}

	/**
	 * @return
	 */
	public String getKeyAuthorMe()
	{
		return KEY_AUTHOR_ME;
	}

	/**
	 * @return
	 */
	public String getKeyAuthorOther()
	{
		return KEY_AUTHOR_OTHER;
	}

	/**
	 * @return Returns the key for ccOwnership.
	 */
	public String getKeyCcAuthorship()
	{
		return TAG_AUTHOR;
	}

	/**
	 * @return Returns the key for ccOwnership.
	 */
	public String getKeyCcAuthorshipLabel()
	{
		return KEY_AUTHOR_LABEL;
	}

	/**
	 * @return Returns the key for ccCommercial.
	 */
	public String getKeyCcCommercialLabel()
	{
		return KEY_COMMERCIAL_LABEL;
	}

	/**
	 * @return Returns the key for ccCommercial.
	 */
	public String getKeyCcCommercialNo()
	{
		return KEY_COMMERCIAL_NO;
	}

	/**
	 * @return Returns the key for ccCommercial.
	 */
	public String getKeyCcCommercialYes()
	{
		return KEY_COMMERCIAL_YES;
	}
	
	/**
	 * @return Returns the key for ccModification.
	 */
	public String getKeyCcModify()
	{
		return TAG_MODIFY;
	}

	/**
	 * @return Returns the key for ccModification.
	 */
	public String getKeyCcModifyYes()
	{
		return KEY_MODIFY_YES;
	}

	/**
	 * @return Returns the key for ccModification.
	 */
	public String getKeyCcModifyNo()
	{
		return KEY_MODIFY_NO;
	}

	/**
	 * @return Returns the key for ccModification.
	 */
	public String getKeyCcModifyShare()
	{
		return KEY_MODIFY_SHARE;
	}

	/**
	 * @return Returns the key for ccModification.
	 */
	public String getKeyCcModifyLabel()
	{
		return KEY_MODIFY_LABEL;
	}

	/**
	 * @return Returns the key for ccTerms.
	 */
	public String getKeyCcMyTerms()
	{
		return TAG_MY_TERMS;
	}
	
	/**
	 * @return Returns the key for ccRightsTerms.
	 */
	public String getKeyCcOtherTerms()
	{
		return TAG_OTHER_TERMS;
	}

	/**
	 * @return Returns the key for ccRightsOwner.
	 */
	public String getKeyCcRightsOwner()
	{
		return TAG_RIGHTS_OWNER;
	}

	/**
	 * @return Returns the key for ccRightsYear.
	 */
	public String getKeyCcRightsYear()
	{
		return TAG_RIGHTS_YEAR;
	}

	/**
	 * @return Returns the key for copyrightAlert.
	 */
	public String getKeyCopyrightAlert()
	{
		return TAG_COPYRIGHT_ALERT;
	}

	/**
	 * @return Returns the key for name.
	 */
	public String getKeyCopyrightInfo()
	{
		return TAG_COPYRIGHT_INFO;
	}

	/**
	 * @return Returns the key for Copyright Owner label.
	 */
	public String getKeyCopyrightOwnerLabel()
	{
		return KEY_COPYRIGHT_OWNER_LABEL;
	}

	/**
	 * @return Returns the key for copyrightStatus.
	 */
	public String getKeyCopyrightStatus()
	{
		return TAG_COPYRIGHT_STATUS;
	}

	/**
	 * @return Returns the key for Copyright Year label.
	 */
	public String getKeyCopyrightYearLabel()
	{
		return KEY_COPYRIGHT_YEAR_LABEL;
	}
	
	/**
	 * @return
	 */
	public String getKeyCreativeCommons()
	{
		return KEY_CREATIVE_COMMONS;
	}

	/**
	 * @return Returns the key for MyCopyright.
	 */
	public String getKeyMyCopyright()
	{
		return KEY_MY_COPYRIGHT;
	}

	public String getKeyMyTermsLabel()
	{
		return KEY_MY_TERMS_LABEL;
	}

	public String getKeyOtherTermsLabel()
	{
		return KEY_OTHER_TERMS_LABEL;
	}

	protected List getKeys(String[] array)
	{
		List list = new Vector();
		for(int i = 0; i < array.length; i++ )
		{
			list.add(array[i]);
		}
		return list;
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

	public List getModifyKeys()
	{
		return getKeys(KEYLIST_MODIFICATION);
	}
	
	public List getModifyOptions()
	{
		return getLabels(KEYLIST_MODIFICATION);
	}

	public List getMyTermsKeys()
	{
		return getKeys(KEYLIST_MYTERMS);
	}

	public List getMyTermsOptions()
	{
		return getLabels(KEYLIST_MYTERMS);
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return name;
	}
	
	public List getOtherTermsKeys()
	{
		return getKeys(KEYLIST_OTHERTERMS);
	}

	public List getOtherTermsOptions()
	{
		return getLabels(KEYLIST_OTHERTERMS);
	}

	/**
	 * @param key
	 * @return
	 */
	public String getString(String key)
	{
		return rb.getString(key);
	}
	
	/**
	 * @param key
	 * @return
	 */
	public String[] getStrings(String key)
	{
		return rb.getStrings(key);
	}
	
	

	/**
	 * @return Returns the copyrightAlert.
	 */
	public boolean isCopyrightAlert()
	{
		return copyrightAlert;
	}

	/**
	 * @return Returns the usingCreativeCommons.
	 */
	public boolean isUsingCreativeCommons()
	{
		return usingCreativeCommons;
	}

	/**
	 * @param ccCommercial
	 */
	public void setAllowCommercial(String ccCommercial)
	{
		this.ccCommercial = ccCommercial;
		
	}

	/**
	 * @param ccModification
	 */
	public void setAllowModifications(String ccModification)
	{
		this.ccModification = ccModification;
		
	}

	/**
	 * @param ccCommercial The ccCommercial to set.
	 */
	public void setCcCommercial(String ccCommercial)
	{
		this.ccCommercial = ccCommercial;
	}

	/**
	 * @param ccModification The ccModification to set.
	 */
	public void setCcModification(String ccModification)
	{
		this.ccModification = ccModification;
	}

	/**
	 * @param ccOwnership The ccOwnership to set.
	 */
	public void setCcOwnership(String ccOwnership)
	{
		this.ccOwnership = ccOwnership;
	}

	/**
	 * @param ccRightsOwner The ccRightsOwner to set.
	 */
	public void setCcRightsOwner(String ccRightsOwner)
	{
		this.ccRightsOwner = ccRightsOwner;
	}

	/**
	 * @param ccRightsTerms
	 */
	public void setCcRightsTerms(String ccRightsTerms)
	{
		this.ccRightsTerms = ccRightsTerms;
	}

	/**
	 * @param ccRightsYear The ccRightsYear to set.
	 */
	public void setCcRightsYear(String ccRightsYear)
	{
		this.ccRightsYear = ccRightsYear;
	}

	/**
	 * @param ccTerms
	 */
	public void setCcTerms(String ccTerms)
	{
		this.ccTerms = ccTerms;
		
	}

	/**
	 * @param b
	 */
	public void setCopyrightAlert(boolean b)
	{
		this.copyrightAlert = b;
		
	}

	/**
	 * @param copyrightStatus
	 */
	public void setCopyrightStatus(String copyrightStatus)
	{
		this.copyrightStatus = copyrightStatus;
		
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	public void setOwnership(String ccOwnership)
	{
		this.ccOwnership = ccOwnership;
		
	}

	public void setRightsowner(String ccRightsOwner)
	{
		this.ccRightsOwner = ccRightsOwner;
		
	}
	
	public void setRightstyear(String ccRightsYear)
	{
		this.ccRightsYear = ccRightsYear;
		
	}
	
	/**
	 * @param usingCreativeCommons The usingCreativeCommons to set.
	 */
	public void setUsingCreativeCommons(boolean usingCreativeCommons)
	{
		this.usingCreativeCommons = usingCreativeCommons;
	}
	
}
