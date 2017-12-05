/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool.jsf;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIData;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.tool.util.Utilities;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * <p>
 * This is a class for JSF Sign-up tool UIBean. It provides permission editing
 * functionality for Sign-up tool.
 * 
 * @author Peter Liu
 * 
 * </P>
 */
@Slf4j
public class SignupPermissionsUpdateBean {

	private SakaiFacade sakaiFacade;

	private List<RealmItem> realmItems;

	private UIData permissionTable;

	private Boolean showPermissionLink = null;

	/* in Sakai.properties file  then sakaiConfig file*/
	private static final boolean ENABLE_PERMISSION_FEATURE_FLAG = "true".equalsIgnoreCase(Utilities.getSignupConfigParamVal("signup.permission.update.enabled", "true")) ? true : false;

	/**
	 * Default Constructor
	 * 
	 */
	public SignupPermissionsUpdateBean() {
	}

	/**
	 * It will initialize the a list of RealmItem objects, which contain all the
	 * realm information
	 */
	public void init() {
		this.realmItems = getCurrentSiteRealmItems();
	}

	/**
	 * This is a JSF action method which is used the JSF page
	 * 
	 * @return a page url string
	 */
	public String processPermission() {
		init();
		return "doPermission";
	}

	/**
	 * This is a JSF button action method, which is used by JSF page
	 * 
	 * @return a string
	 */
	public String updatePermission() {
		RealmItem realmItem = (RealmItem) permissionTable.getRowData();

		String desc = "";
		Object[] params = null;
		if (realmItem.isSiteLevel()) {
			params = new Object[] { realmItem.getSiteTitle(), realmItem.getRefId() };
			desc = MessageFormat.format(Utilities.rb.getString("permission.info.for.site.scope"), params);
		} else {
			params = new Object[] { realmItem.getSiteTitle(), realmItem.getGroupTitle(), realmItem.getRefId() };
			desc = MessageFormat.format(Utilities.rb.getString("permission.info.for.group.scope"), params);
		}

		return doPermissions(realmItem.getRefId(), desc);
	}

	/**
	 * Delegate the permission update job to sakai-permissions-helper tool
	 */
	private String doPermissions(String siteRef, String description) {
		try {
			ToolSession currentToolSession = SessionManager.getCurrentToolSession();
			currentToolSession.setAttribute(PermissionsHelper.PREFIX, "signup.");
			currentToolSession.setAttribute(PermissionsHelper.TARGET_REF, siteRef);
			currentToolSession.setAttribute(PermissionsHelper.DESCRIPTION, description);
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			context.redirect("sakai.permissions.helper.helper/tool");
		} catch (Exception e) {
			log.error(this + ".processPermissionUpdateRedirect - " + e);
		}

		return null;
	}

	private List<RealmItem> getCurrentSiteRealmItems() {
		List<RealmItem> realmItemList = new ArrayList<RealmItem>();
		String cur_siteId = getSakaiFacade().getCurrentLocationId();

		try {
			Site site = SiteService.getSite(cur_siteId);
			RealmItem item = new RealmItem(site.getTitle(), "", SiteService.siteReference(cur_siteId), true);
			/* set permission flag: 'realm.upd' for site realm level */
			item.setAllowedUpd(getSakaiFacade().isAllowedSite(getSakaiFacade().getCurrentUserId(),
					AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP, site.getId()));
			realmItemList.add(item);

			/* group levels */
			Collection groups = site.getGroups();
			if (groups != null) {
				for (Iterator iterator = groups.iterator(); iterator.hasNext();) {
					Group grp = (Group) iterator.next();
					item = new RealmItem(site.getTitle(), grp.getTitle(), SiteService.siteGroupReference(cur_siteId,
							grp.getId()), false);

					/* set permission flag: 'realm.upd' for group realm level */
					item.setAllowedUpd(getSakaiFacade().isAllowedGroup(getSakaiFacade().getCurrentUserId(),
							AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP, site.getId(), grp.getId()));
					realmItemList.add(item);
				}

			}
		} catch (IdUnusedException e) {
			log.error("IdUnusedException: siteId is not found" + e);
			Utilities.addErrorMessage("No such site Id is found!");
		}

		return realmItemList;
	}

	public SakaiFacade getSakaiFacade() {
		return sakaiFacade;
	}

	public void setSakaiFacade(SakaiFacade sakaiFacade) {
		this.sakaiFacade = sakaiFacade;
	}

	private static Boolean allowedToUpdate = null;

	/**
	 * This is a getter method for UI
	 * 
	 * @return true if user has the permission to do it
	 */
	public boolean isShowPermissionLink() {
		/* initialize only once on an user's session for efficiency */
		if (this.showPermissionLink == null) {
			boolean show = false;
			if (allowedToUpdate == null) {
				allowedToUpdate = new Boolean(ENABLE_PERMISSION_FEATURE_FLAG);
			}

			if (sakaiFacade.isUserAdmin(sakaiFacade.getCurrentUserId())
					|| (hasSiteUpdatePermission() && (allowedToUpdate.booleanValue() || isUserHasAllRealmUpdPermissionsForGroupLevels()))) {
				show = true;
			}

			showPermissionLink = new Boolean(show);
		}

		return showPermissionLink.booleanValue();
	}

	private boolean hasSiteUpdatePermission() {
		boolean allowed = false;

		/* Check user's permissions for 'site.upd' */
		if (getSakaiFacade().isAllowedSite(getSakaiFacade().getCurrentUserId(), SiteService.SECURE_UPDATE_SITE,
				getSakaiFacade().getCurrentLocationId())) {
			allowed = true;
		}

		return allowed;
	}

	/*
	 * Since the tool permissions are complex and only powerUser may work on
	 * this. This method gives Administrator a chance to set all realm.upd to
	 * all groups levels and the Permissions link on the sign-up tool menu bar
	 * will appears dynamically, without introducing
	 * signup.permission.update.enabled=true in sakai.properties file for all
	 * the sites.
	 */
	private boolean isUserHasAllRealmUpdPermissionsForGroupLevels() {
		boolean hasPermission = true;
		this.realmItems = getCurrentSiteRealmItems();

		/* One group in the site is the minimum requirement */
		if (realmItems == null || realmItems.size() < 2)
			return false;

		/* Check if the realm.upd exist in all levels */
		for (Iterator iterator = realmItems.iterator(); iterator.hasNext();) {
			RealmItem item = (RealmItem) iterator.next();
			if (!item.isAllowedUpd()) {
				hasPermission = false;
				break;
			}
		}

		return hasPermission;
	}

	public List<RealmItem> getRealmItems() {
		return realmItems;
	}

	/**
	 * This is a JSF getter method for UI
	 * 
	 * @return a UIData object
	 */
	public UIData getPermissionTable() {
		return permissionTable;
	}

	/**
	 * This is a JSF setter method for UI
	 * 
	 * @param permissionTable
	 *            a UIData object
	 */
	public void setPermissionTable(UIData permissionTable) {
		this.permissionTable = permissionTable;
	}

	/**
	 * This is a getter method for UI
	 * 
	 * @return true if the user is a administrator
	 */
	public boolean isAdmin() {
		return getSakaiFacade().isUserAdmin(getSakaiFacade().getCurrentUserId());
	}

}
