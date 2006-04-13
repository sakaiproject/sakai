/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;

import uk.ac.cam.caret.sakai.rwiki.tool.util.WikiPageAction;

/**
 * @author andrew
 */
// FIXME: Tool
public class AuthZGroupBean extends ViewBean
{

	private AuthzGroup currentRealm = null;

	private String realmId = null;

	private boolean siteUpdateAllowed;

	public AuthZGroupBean(String pageName, String localSpace)
	{
		super(pageName, localSpace);
	}

	public AuthZGroupBean()
	{
	}

	public List getRoles()
	{
		if (currentRealm == null)
		{
			return new ArrayList();
		}
		else
		{
			Set roleset = currentRealm.getRoles();

			if (roleset == null)
			{
				return new ArrayList();
			}

			Role[] roles = (Role[]) roleset.toArray(new Role[roleset.size()]);

			Arrays.sort(roles);
			ArrayList roleBeans = new ArrayList(roles.length);
			for (int i = 0; i < roles.length; i++)
			{
				roleBeans.add(new RoleBean(roles[i]));
			}

			return roleBeans;
		}

	}

	public boolean isActiveAuthZGroup()
	{
		return (currentRealm != null);
	}

	public String getEditRealmUrl()
	{
		return this.getPageUrl(getPageName(), WikiPageAction.EDIT_REALM_ACTION
				.getName());
	}

	public String getEditRealmManyUrl()
	{
		return this.getPageUrl(getPageName(),
				WikiPageAction.EDIT_REALM_MANY_ACTION.getName());
	}

	public String getPreferencesUrl()
	{
		return this.getPageUrl(getPageName(), WikiPageAction.PREFERENCES_ACTION
				.getName());
	}

	public AuthzGroup getRealmEdit()
	{
		return currentRealm;
	}

	public String getRealmId()
	{
		return realmId;
	}

	public String getRealmReference()
	{
		return currentRealm.getReference();
	}

	public void setCurrentRealm(AuthzGroup currentRealm)
	{
		this.currentRealm = currentRealm;
		this.setLocalSpace(currentRealm.getId());

	}

	public void setRealmId(String realmId)
	{
		this.realmId = realmId;
	}

	public boolean isSiteUpdateAllowed()
	{
		return siteUpdateAllowed;
	}

	public void setSiteUpdateAllowed(boolean siteUpdateAllowed)
	{
		this.siteUpdateAllowed = siteUpdateAllowed;
	}

	public String getEscapedId()
	{
		return realmId.replaceAll("_", "__");
	}
}
