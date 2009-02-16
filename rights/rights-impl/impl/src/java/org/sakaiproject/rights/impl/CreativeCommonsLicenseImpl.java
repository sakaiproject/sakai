/**
 * 
 */
package org.sakaiproject.rights.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.sakaiproject.rights.api.CreativeCommonsLicense;
import org.sakaiproject.rights.api.CreativeCommonsLicenseManager;
import org.sakaiproject.rights.util.RightsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CreativeCommonsLicenseImpl implements CreativeCommonsLicense
{
	protected static String baseURL = "http://creativecommons.org/licenses/";
	protected String uri;
	protected String source;
	protected String replacedBy;
	
	protected String identifier;
	protected String version;
	protected String jurisdiction;
	
	protected String creator;
	protected String legalcode;
	
	protected Set<String> permissions = new TreeSet<String>();
	protected Set<String> prohibitions = new TreeSet<String>();
	protected Set<String> requirements = new TreeSet<String>();

	protected Map<String, String> descriptions = new HashMap<String, String>();
	protected Map<String, String> titles = new HashMap<String, String>();


	public void addDescriptions(Map<String, String> descriptions) 
	{
		this.descriptions.putAll(descriptions);
	}

	public void addPermission(String permission) 
	{
		this.permissions.add(permission);
	}

	public void addPermissions(Set<String> permissions) 
	{
		this.permissions.addAll(permissions);
	}

	public void addProhibition(String prohibition) 
	{
		this.prohibitions.add(prohibition);
	}

	public void addProhibitions(Set<String> prohibitions) 
	{
		this.prohibitions.addAll(prohibitions);
	}

	public void addRequirement(String requirement) 
	{
		this.requirements.add(requirement);
	}

	public void addRequirements(Set<String> requirements) 
	{
		this.requirements.addAll(requirements);
	}

	public void addTitles(Map<String, String> titles) 
	{
		this.titles.putAll(titles);
	}

	public String getCreator() 
	{
		return this.creator;
	}

	public String getDescription() 
	{
		return null;
	}

	public Map<String, String> getDescriptions() 
	{
		return new HashMap<String, String>(this.descriptions);
	}

	public String getIdentifier() 
	{
		return this.identifier;
	}

	public String getJurisdiction() 
	{
		return this.jurisdiction;
	}

	public String getLegalcode() 
	{
		return this.legalcode;
	}

	public Collection<String> getPermissions() 
	{
		return new TreeSet<String>(permissions);
	}

	public Collection<String> getProhibitions() 
	{
		return new TreeSet<String>(prohibitions);
	}

	public String getReplacedBy() 
	{
		return this.replacedBy;
	}

	public Collection<String> getRequirements() 
	{
		return new TreeSet<String>(requirements);
	}

	public String getSource() 
	{
		return this.source;
	}

	public String getTitle() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getTitles() 
	{
		return new HashMap<String, String>(this.titles);
	}

	public String getUri() 
	{
		return this.uri;
	}

	public String getVersion() 
	{
		return this.version;
	}

	public boolean hasPermissions() 
	{
		return permissions != null && ! permissions.isEmpty();
	}

	public boolean hasProhibitions() 
	{
		return prohibitions != null && ! prohibitions.isEmpty();
	}

	public boolean hasRequirements() 
	{
		return requirements != null && ! requirements.isEmpty();
	}

	public boolean isReplaced() 
	{
		return this.replacedBy != null && ! this.replacedBy.equals("");
	}

	public void setCreator(String creator) 
	{
		this.creator = creator;
	}

	public void setJurisdiction(String jurisdiction) 
	{
		this.jurisdiction = jurisdiction;
	}

	public void setLegalcode(String legalcode) 
	{
		this.legalcode = legalcode;
	}

	public void setReplacedBy(String replacedBy) 
	{
		this.replacedBy = replacedBy;
	}

	public void setSource(String source) 
	{
		this.source = source;
	}

	public void setUri(String uri) 
	{
		this.uri = uri;
	}

	public void setVersion(String version) 
	{
		this.version = version;
	}

	public String toJSON() 
	{
		JSONObject json = new JSONObject();
		
		json.element(CreativeCommonsLicenseManager.RDF_ABOUT, this.uri);
		
		if(this.creator != null && ! this.creator.trim().equals(""))
		{
			json.element(CreativeCommonsLicenseManager.DC_CREATOR, this.creator);
		}
		if(this.jurisdiction != null && ! this.jurisdiction.trim().equals(""))
		{
			json.element(CreativeCommonsLicenseManager.CC_JURISDICTION, this.jurisdiction);
		}
		if(this.legalcode != null && ! this.legalcode.trim().equals(""))
		{
			json.element(CreativeCommonsLicenseManager.CC_LEGALCODE, this.legalcode);
		}
		if(this.replacedBy != null && ! this.replacedBy.trim().equals(""))
		{
			json.element(CreativeCommonsLicenseManager.DCQ_IS_REPLACED_BY, this.replacedBy);
		}
		if(this.source != null && ! this.source.trim().equals(""))
		{
			json.element(CreativeCommonsLicenseManager.DC_SOURCE, this.source);
		}
		if(this.version != null && ! this.version.trim().equals(""))
		{
			json.element(CreativeCommonsLicenseManager.DCQ_HAS_VERSION, this.version);
		}
		if(this.permissions != null && ! this.permissions.isEmpty())
		{
			json.element(CreativeCommonsLicenseManager.CC_PERMITS, this.permissions);
		}
		if(this.prohibitions != null && ! this.prohibitions.isEmpty())
		{
			json.element(CreativeCommonsLicenseManager.CC_PROHIBITS, this.prohibitions);
		}
		if(this.requirements != null && ! this.requirements.isEmpty())
		{
			json.element(CreativeCommonsLicenseManager.CC_REQUIRES, this.requirements);
		}
		if(this.titles != null && ! this.titles.isEmpty())
		{
			json.element(CreativeCommonsLicenseManager.DC_TITLE, this.titles);
		}
		if(this.descriptions != null && ! this.descriptions.isEmpty())
		{
			json.element(CreativeCommonsLicenseManager.DC_DESCRIPTION, this.descriptions);
		}
		
		return json.toString();
	}
	
	public void fromJSON(String jsonStr)
	{
		JSONObject json = JSONObject.fromObject(jsonStr);
		try
		{
			if(json.containsKey(CreativeCommonsLicenseManager.RDF_ABOUT))
			{
				this.uri = json.getString(CreativeCommonsLicenseManager.RDF_ABOUT);
				
				if(json.containsKey(CreativeCommonsLicenseManager.DC_CREATOR))
				{
					this.creator = json.getString(CreativeCommonsLicenseManager.DC_CREATOR);
				}
				
				if(json.containsKey(CreativeCommonsLicenseManager.CC_JURISDICTION))
				{
					this.jurisdiction = json.getString(CreativeCommonsLicenseManager.CC_JURISDICTION);
				}

				if(json.containsKey(CreativeCommonsLicenseManager.CC_LEGALCODE))
				{
					this.legalcode = json.getString(CreativeCommonsLicenseManager.CC_LEGALCODE);
				}

				if(json.containsKey(CreativeCommonsLicenseManager.DCQ_IS_REPLACED_BY))
				{
					this.replacedBy = json.getString(CreativeCommonsLicenseManager.DCQ_IS_REPLACED_BY);
				}

				if(json.containsKey(CreativeCommonsLicenseManager.DC_SOURCE))
				{
					this.source = json.getString(CreativeCommonsLicenseManager.DC_SOURCE);
				}

				if(json.containsKey(CreativeCommonsLicenseManager.DCQ_HAS_VERSION))
				{
					this.version = json.getString(CreativeCommonsLicenseManager.DCQ_HAS_VERSION);
				}

				if(json.containsKey(CreativeCommonsLicenseManager.CC_PERMITS))
				{
					JSONArray jsonlist = json.getJSONArray(CreativeCommonsLicenseManager.CC_PERMITS);
					for(int i = 0; i < jsonlist.size(); i++)
					{
						try
						{
							this.permissions.add(jsonlist.getString(i));
						}
						catch(JSONException e)
						{
							logger.debug("Problem getting permission at index " + i + " from JSON array: \n" + jsonlist.toString() + "\nuri " + this.uri);
						}
					}
				}
				
				if(json.containsKey(CreativeCommonsLicenseManager.CC_PROHIBITS))
				{
					JSONArray jsonlist = json.getJSONArray(CreativeCommonsLicenseManager.CC_PROHIBITS);
					for(int i = 0; i < jsonlist.size(); i++)
					{
						try
						{
							this.prohibitions.add(jsonlist.getString(i));
						}
						catch(JSONException e)
						{
							logger.debug("Problem getting prohibition at index " + i + " from JSON array: \n" + jsonlist.toString() + "\nuri " + this.uri);
						}
					}
				}
				
				if(json.containsKey(CreativeCommonsLicenseManager.CC_REQUIRES))
				{
					JSONArray jsonlist = json.getJSONArray(CreativeCommonsLicenseManager.CC_REQUIRES);
					for(int i = 0; i < jsonlist.size(); i++)
					{
						try
						{
							this.requirements.add(jsonlist.getString(i));
						}
						catch(JSONException e)
						{
							logger.debug("Problem getting requirement at index " + i + " from JSON array: \n" + jsonlist.toString() + "\nuri " + this.uri);
						}
					}
				}
				
				if(json.containsKey(CreativeCommonsLicenseManager.DC_TITLE))
				{
					JSONObject jsonobj = json.getJSONObject(CreativeCommonsLicenseManager.DC_TITLE);
					
					for(String key : jsonobj.keySet())
					{
						try
						{
							this.titles.put(key, jsonobj.getString(key));
						}
						catch(JSONException e)
						{
							logger.debug("Problem getting title for key " + key + " for uri " + this.uri + " JSON for titles:\n" + jsonobj.toString());
						}
					}
				}
				
				if(json.containsKey(CreativeCommonsLicenseManager.DC_DESCRIPTION))
				{
					JSONObject jsonobj = json.getJSONObject(CreativeCommonsLicenseManager.DC_DESCRIPTION);
					
					for(String key : jsonobj.keySet())
					{
						try
						{
							this.descriptions.put(key, jsonobj.getString(key));
						}
						catch(JSONException e)
						{
							logger.debug("Problem getting description for key " + key + " for uri " + this.uri + " JSON for titles:\n" + jsonobj.toString());
						}
					}
				}
			}
			else
			{
				logger.warn("No uri in json string:\n" + jsonStr);
			}
			
		}
		catch(JSONException e)
		{
			logger.warn("Error processing license from json:\n" + jsonStr, e);
		}
	}

}