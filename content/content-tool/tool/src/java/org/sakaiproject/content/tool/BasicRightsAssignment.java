/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.content.tool;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
	/** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("right");
    
	/** kernel api **/
	private static UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);
	private static TimeService timeService = ComponentManager.get(TimeService.class);
	
	public class RightsChoice
	{
		protected String id;
		protected String renderAs;
		protected String label;
		protected List  options;
		protected Map optionMapping;
		protected String moreInfoUrl;
		protected String moreInfoTitle;
		protected String moreInfoText;
		
		public RightsChoice(String id, String label, String renderAs, List options, Map optionsMapping)
		{
			this.id = id;
			this.label = label;
			this.renderAs = renderAs;
			this.options = new Vector(options);
			this.optionMapping = new Hashtable(optionsMapping);
		}
		
		public String getId() 
		{
			return id;
		}
		public void setId(String id) 
		{
			this.id = id;
		}
		public String getLabel()
		{
			return label;
		}
		public void setLabel(String label) 
		{
			this.label = label;
		}
		public String getMoreInfoTitle() 
		{
			return moreInfoTitle;
		}

		public void setMoreInfoTitle(String moreInfoLabel) 
		{
			this.moreInfoTitle = moreInfoLabel;
		}

		public String getMoreInfoText() 
		{
			return moreInfoText;
		}

		public void setMoreInfoText(String moreInfoText) 
		{
			this.moreInfoText = moreInfoText;
		}

		public String getMoreInfoUrl() 
		{
			return moreInfoUrl;
		}

		public void setMoreInfoUrl(String moreInfoUrl) 
		{
			this.moreInfoUrl = moreInfoUrl;
		}

		public Map getOptionMapping() 
		{
			return optionMapping;
		}
		public void setOptionMapping(Map optionMapping) 
		{
			this.optionMapping = optionMapping;
		}
		public List getOptions() 
		{
			return options;
		}
		public void setOptions(List options) 
		{
			this.options = options;
		}
		public String getRenderAs() 
		{
			return renderAs;
		}
		public void setRenderAs(String renderAs) 
		{
			this.renderAs = renderAs;
		}
	}
	
	public static class RightsOption
	{
		protected String id;
		protected String label;
		protected String moreInfoUrl;
		protected String moreInfoTitle;
		protected String moreInfoText;

		public RightsOption(String id, String label)
		{
			this.id = id;
			this.label = label;
		}
		public String getId() 
		{
			return id;
		}
		public void setId(String id) 
		{
			this.id = id;
		}
		public String getLabel()
		{
			return label;
		}
		public void setLabel(String label) 
		{
			this.label = label;
		}
		
		public String getMoreInfoTitle() 
		{
			return moreInfoTitle;
		}

		public void setMoreInfoTitle(String moreInfoLabel) 
		{
			this.moreInfoTitle = moreInfoLabel;
		}

		public String getMoreInfoText() 
		{
			return moreInfoText;
		}

		public void setMoreInfoText(String moreInfoText) 
		{
			this.moreInfoText = moreInfoText;
		}

		public String getMoreInfoUrl() 
		{
			return moreInfoUrl;
		}

		public void setMoreInfoUrl(String moreInfoUrl) 
		{
			this.moreInfoUrl = moreInfoUrl;
		}

	}
	
	protected static final String RENDER_AS_TEXT_INPUT = "text";
	protected static final String RENDER_AS_RADIO_BUTTONS = "radio";
	protected static final String RENDER_AS_TEXTAREA = "textarea";
	protected static final String RENDER_AS_SELECT = "select";
	protected static final String RENDER_AS_DATE_INPUT = "date";
	protected static final String RENDER_AS_CHECKBOX = "checkbox";
	protected static final String RENDER_AS_CHECKBOXES = "checkboxes";
	
	protected String[] user = { userDirectoryService.getCurrentUser().getDisplayName() };
	
	/*
	status.label						= Who owns the rights to this material?
	status.unknown					= Copyright status is not yet determined.
	status.other						= Someone else holds the copyright on this material.
	status.mine						= I hold the copyright on this material.
	status.public_domain				= This material is in the public domain.


	 */
	
	protected static final String USING_CREATIVE_COMMONS = "usingCreativeCommons";
	
	protected static final String PD_URL = "http://creativecommons.org/license/publicdomain";
	
	protected static final String DELIM = "_";

	protected static final String FIELD_COMMERCIAL_USE = "myCommercialUse";
	
	protected static final String FIELD_DONE = "done";
	
	protected static final String FIELD_INFO = "jargon";

	protected static final String FIELD_MODIFICATIONS = "myModifications";
	
	protected static final String FIELD_MORE_INFO = "moreinfo";
	
	protected static final String FIELD_MY_CR_OWNER = "myCopyrightOwner";
	
	protected static final String FIELD_MY_CR_YEAR = "myCopyrightYear";
	
	protected static final String FIELD_OFFER = "offer";
	
	protected static final String FIELD_OTHER_OFFER = "otherOffer";
	
	protected static final String FIELD_OTHER_COMMERCIAL_USE = "otherCommercialUse";
	
	protected static final String FIELD_OTHER_CR_OWNER = "otherCopyrightOwner";
	
	protected static final String FIELD_OTHER_CR_YEAR = "otherCopyrightYear";
	
	protected static final String FIELD_OTHER_MODIFICATIONS = "otherModifications";
	
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

	protected static final String[] KEYLIST_TERMS = { KEY_MY_COPYRIGHT, KEY_OTHER_COPYRIGHT, KEY_PUBLIC_DOMAIN };
	
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
	
	protected String otherCommercialUse;
	
	/** Member variable representing the name of the copyright holder (copyright holder is NOT this user) */
	protected String otherCopyrightOwner;
	
	/** Member variable representing the year of the copyright (copyright holder is NOT this user) */
	protected String otherCopyrightYear;
	
    protected String otherModifications;
	
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
		
		//terms = KEY_MY_COPYRIGHT;
		//terms = KEY_OTHER_COPYRIGHT;
		//offer = KEY_CREATE_CC;
	}
	
	/**
	 * Construct from info in XML in a DOM element.
	 * 
	 * @param el
	 *        The XML DOM element.
	 */
	public BasicRightsAssignment(Element el)
	{
		String usingCC = el.getAttribute(USING_CREATIVE_COMMONS);
		this.usingCreativeCommons = Boolean.TRUE.toString().equals(usingCC);
		this.terms = el.getAttribute(FIELD_TERMS);
		if(this.terms == null)
		{
		
		}
		else if(KEY_MY_COPYRIGHT.equals(terms))
		{
			this.myCopyrightYear = el.getAttribute(FIELD_MY_CR_YEAR);
			this.myCopyrightOwner = el.getAttribute(FIELD_MY_CR_OWNER);
			this.offer = el.getAttribute(FIELD_OFFER);
			if(KEY_FAIR_USE.equals(offer))
			{
				
			}
			else if(KEY_CREATE_CC.equals(offer))
			{
				ccModifications = el.getAttribute(FIELD_MODIFICATIONS);
				ccCommercialUse = el.getAttribute(FIELD_COMMERCIAL_USE);
			}
			else if(KEY_CREATE_PD.equals(offer))
			{
				
			}
		}
		else if(KEY_OTHER_COPYRIGHT.equals(terms))
		{
			otherCopyrightYear = el.getAttribute(FIELD_OTHER_CR_YEAR);
			otherCopyrightOwner = el.getAttribute(FIELD_OTHER_CR_OWNER);
		}
		else if(KEY_CREATIVE_COMMONS.equals(terms))
		{
			otherModifications = el.getAttribute(FIELD_OTHER_MODIFICATIONS);
			otherCommercialUse = el.getAttribute(FIELD_OTHER_COMMERCIAL_USE);
		}
		
	} // BasicRightsAssignment

	/**
	 * Construct from info in ResourceProperties.
	 * 
	 * @param properties
	 *        
	 */
	public BasicRightsAssignment(String name, ResourceProperties properties)
	{
		this.name = name;
		try 
		{
			this.usingCreativeCommons = properties.getBooleanProperty(USING_CREATIVE_COMMONS);
		} 
		catch (Exception e) 
		{
			this.usingCreativeCommons = false;
		}

		this.terms = properties.getProperty(FIELD_TERMS);
		if(this.terms == null)
		{
			this.terms = KEY_OTHER_COPYRIGHT;
		}
		else if(KEY_MY_COPYRIGHT.equals(terms))
		{
			this.myCopyrightYear = properties.getProperty(FIELD_MY_CR_YEAR);
			this.myCopyrightOwner = properties.getProperty(FIELD_MY_CR_OWNER);
			this.offer = properties.getProperty(FIELD_OFFER);
			if(KEY_FAIR_USE.equals(offer))
			{
				
			}
			else if(KEY_CREATE_CC.equals(offer))
			{
				ccModifications = properties.getProperty(FIELD_MODIFICATIONS);
				ccCommercialUse = properties.getProperty(FIELD_COMMERCIAL_USE);
			}
			else if(KEY_CREATE_PD.equals(offer))
			{
				
			}
		}
		else if(KEY_OTHER_COPYRIGHT.equals(terms))
		{
			otherCopyrightYear = properties.getProperty(FIELD_OTHER_CR_YEAR);
			otherCopyrightOwner = properties.getProperty(FIELD_OTHER_CR_OWNER);
		}
		else if(KEY_CREATIVE_COMMONS.equals(terms))
		{
			otherModifications = properties.getProperty(FIELD_OTHER_MODIFICATIONS);
			otherCommercialUse = properties.getProperty(FIELD_OTHER_COMMERCIAL_USE);
		}
		
	} // BasicRightsAssignment

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
			String ccModifications = params.getString(getFieldNameModifications());
			if(ccModifications != null)
			{
				this.setCcModifications(ccModifications);
			}
			String otherModifications = params.getString(getFieldNameOtherModifications());
			if(otherModifications != null)
			{
				this.setOtherModifications(otherModifications);
			}
			String ccCommercialUse = params.getString(getFieldNameCommercialUse());
			if(ccCommercialUse != null)
			{
				this.setCcCommercialUse(ccCommercialUse);
			}
			String otherCommercialUse = params.getString(getFieldNameOtherCommercialUse());
			if(otherCommercialUse != null)
			{
				this.setOtherCommercialUse(otherCommercialUse);
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
		String username = userDirectoryService.getCurrentUser().getDisplayName(); 
		return username;
	}
	
	/**
	 * Returns the current year.
	 * @return
	 */
	public String getDefaultCopyrightYear()
	{
		int year = timeService.newTime().breakdownLocal().getYear();
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
	
	public String getFieldNameOtherCommercialUse()
	{
		return name + DELIM + FIELD_OTHER_COMMERCIAL_USE;
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

	public String getFieldNameOtherModifications()
	{
		return name + DELIM + FIELD_OTHER_MODIFICATIONS;
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
	public String getKeyNewPublicDomain()
	{
		return KEY_CREATE_PD;
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
	public String getKeyYes()
	{
		return KEY_YES;
	}
	
	public String getLabelCommercialUse()
	{
		return getString(FIELD_COMMERCIAL_USE, KEY_LABEL);
	}
	
	public String getLabelCommercialUseNo()
	{
		return getString(FIELD_COMMERCIAL_USE, KEY_NO);
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
	
	public String getLabelOtherCommercialUse()
	{
		return getString(FIELD_OTHER_COMMERCIAL_USE, KEY_LABEL);
	}
	
	public String getLabelOtherCommercialUseNo()
	{
		return getString(FIELD_OTHER_COMMERCIAL_USE, KEY_NO);
	}
	
	public String getLabelOtherCommercialUseYes()
	{
		return getString(FIELD_OTHER_COMMERCIAL_USE, KEY_YES);
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
	
	public String getLabelOtherModifications()
	{
		return getString(FIELD_OTHER_MODIFICATIONS, KEY_LABEL);
	}
	
	public String getLabelOtherModificationsNo()
	{
		return getString(FIELD_OTHER_MODIFICATIONS, KEY_NO);
	}
	
	public String getLabelOtherModificationsShareAlike()
	{
		return getString(FIELD_OTHER_MODIFICATIONS, KEY_SHARE_ALIKE);
	}
	
	public String getLabelOtherModificationsYes()
	{
		return getString(FIELD_OTHER_MODIFICATIONS, KEY_YES);
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
	 * @return Returns the ccCommercialUse.
	 */
	public String getOtherCommercialUse()
	{
		return otherCommercialUse;
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
	 * @return Returns the ccModifications.
	 */
	public String getOtherModifications()
	{
		return otherModifications;
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
	 * @param ccCommercialUse The ccCommercialUse to set.
	 */
	public void setOtherCommercialUse(String otherCommercialUse)
	{
		this.otherCommercialUse = otherCommercialUse;
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
	 * @param otherModifications The otherModifications to set.
	 */
	public void setOtherModifications(String otherModifications)
	{
		this.otherModifications = otherModifications;
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

	/**
	 * Serialize the rights-assignment into XML, adding an element to the doc under the top of the stack element.
	 * 
	 * @param doc
	 *        The DOM doc to contain the XML (or null for a string return).
	 * @param stack
	 *        The DOM elements, the top of which is the containing element of the new "rightsAssignment" element.
	 * @return The newly added element.
	 */
	public Element toXml(Document doc, Stack stack)
	{
		Element rightsAssignment = doc.createElement("rightsAssignment");

		if (stack.isEmpty())
		{
			doc.appendChild(rightsAssignment);
		}
		else
		{
			((Element) stack.peek()).appendChild(rightsAssignment);
		}

		stack.push(rightsAssignment);

		if(usingCreativeCommons)
		{
			rightsAssignment.setAttribute(USING_CREATIVE_COMMONS, "true");
		}
		else
		{
			rightsAssignment.setAttribute(USING_CREATIVE_COMMONS, "false");
		}
		
		rightsAssignment.setAttribute(FIELD_TERMS, terms);
		if(KEY_MY_COPYRIGHT.equals(terms))
		{
			rightsAssignment.setAttribute(FIELD_MY_CR_YEAR, myCopyrightYear);
			rightsAssignment.setAttribute(FIELD_MY_CR_OWNER, myCopyrightOwner);
			rightsAssignment.setAttribute(FIELD_OFFER, offer);
			if(KEY_FAIR_USE.equals(offer))
			{
				
			}
			else if(KEY_CREATE_CC.equals(offer))
			{
				rightsAssignment.setAttribute(FIELD_MODIFICATIONS, ccModifications);
				rightsAssignment.setAttribute(FIELD_COMMERCIAL_USE, ccCommercialUse);
			}
			else if(KEY_CREATE_PD.equals(offer))
			{
				
			}
		}
		else if(KEY_OTHER_COPYRIGHT.equals(terms))
		{
			rightsAssignment.setAttribute(FIELD_OTHER_CR_YEAR, otherCopyrightYear);
			rightsAssignment.setAttribute(FIELD_OTHER_CR_OWNER, otherCopyrightOwner);
		}
		else if(KEY_CREATIVE_COMMONS.equals(terms))
		{
			rightsAssignment.setAttribute(FIELD_OTHER_MODIFICATIONS, otherModifications);
			rightsAssignment.setAttribute(FIELD_OTHER_COMMERCIAL_USE, otherCommercialUse);
		}

		stack.pop();


		return rightsAssignment;

	} // toXml

	/**
	 * .
	 * 
	 * @param properties
	 *
	 */
	public void addResourceProperties(ResourcePropertiesEdit properties)
	{
		properties.removeProperty(FIELD_TERMS);
		properties.removeProperty(FIELD_MY_CR_YEAR);
		properties.removeProperty(FIELD_MY_CR_OWNER);
		properties.removeProperty(FIELD_OFFER);
		properties.removeProperty(FIELD_MODIFICATIONS);
		properties.removeProperty(FIELD_COMMERCIAL_USE);
		properties.removeProperty(FIELD_OTHER_CR_YEAR);
		properties.removeProperty(FIELD_OTHER_CR_OWNER);
		properties.removeProperty(FIELD_OTHER_MODIFICATIONS);
		properties.removeProperty(FIELD_OTHER_COMMERCIAL_USE);
		properties.addProperty(FIELD_TERMS, terms);
		properties.addProperty(USING_CREATIVE_COMMONS, Boolean.toString(usingCreativeCommons));
		if(KEY_MY_COPYRIGHT.equals(terms))
		{
			properties.addProperty(FIELD_MY_CR_YEAR, myCopyrightYear);
			properties.addProperty(FIELD_MY_CR_OWNER, myCopyrightOwner);
			properties.addProperty(FIELD_OFFER, offer);
			if(KEY_FAIR_USE.equals(offer))
			{
				
			}
			else if(KEY_CREATE_CC.equals(offer))
			{
				properties.addProperty(FIELD_MODIFICATIONS, ccModifications);
				properties.addProperty(FIELD_COMMERCIAL_USE, ccCommercialUse);
			}
			else if(KEY_CREATE_PD.equals(offer))
			{
				
			}
		}
		else if(KEY_OTHER_COPYRIGHT.equals(terms))
		{
			properties.addProperty(FIELD_OTHER_CR_YEAR, otherCopyrightYear);
			properties.addProperty(FIELD_OTHER_CR_OWNER, otherCopyrightOwner);
		}
		else if(KEY_CREATIVE_COMMONS.equals(terms))
		{
			properties.addProperty(FIELD_OTHER_MODIFICATIONS, otherModifications);
			properties.addProperty(FIELD_OTHER_COMMERCIAL_USE, otherCommercialUse);
		}

	} // addResourceProperties


	
}
