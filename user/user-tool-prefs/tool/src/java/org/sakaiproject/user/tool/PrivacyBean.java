package org.sakaiproject.user.tool;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;


public class PrivacyBean {
	
	private final String VISIBLE = "privacy_visible";
	private final String HIDDEN = "privacy_hidden";
	private final String HIDE = "privacy_check_hide";
	private final String SHOW = "privacy_check_show";
	private final String SET_SHOW_ALL_STRING = "privacy_show_all_set";
	private final String SET_HIDE_ALL_STRING = "privacy_hide_all_set";
	private final String SHOW_ME="Show Me";
	
	private String checkboxText;
	private boolean changeStatus;
	private String selectedSite;
	private boolean siteSelected = false;
	private String curSite;
	private boolean allChanged = false;
	private String changeAllMsg;

	private SelectItem[] sites;

	/** * Resource bundle messages */
	ResourceLoader msgs = new ResourceLoader("user-tool-prefs");

	/** Inject PrivacyManager */
	private PrivacyManager privacyManager;

	private Log LOG = LogFactory.getLog(PrivacyBean.class);

	/**
	 * Returns 'visible' or 'hidden' based on status within site
	 * 
	 * @return
	 * 		Either 'visible' or 'hidden' depending on user's privacy status
	 */
	public String getCurrentStatus() {
		if (!isMyWorkspace()) {
			curSite = getSiteId();
		}

		if (privacyManager.isViewable(curSite, getUserId())) {
			return getMessageBundleString(VISIBLE);
		} 
		else {
			return getMessageBundleString(HIDDEN);
		}
	}

	/**
	 * Return TRUE if privacy set to visible, FALSE if set to hidden
	 * @return
	 */
	public boolean isShow() {
		if (isMyWorkspace()) {
			return privacyManager.isViewable(curSite, getUserId());
		} 
		else {
			return privacyManager.isViewable(getSiteId(), getUserId());
		}
	}

	/**
	 * Retrieve the site id
	 */
	public String getSiteId() {
		return ToolManager.getCurrentPlacement().getContext();
	}

	/**
	 * Retrieve the current user id
	 */
	public String getUserId() {
		return SessionManager.getCurrentSessionUserId();
	}

	/**
	 * Injects the PrivacyManager
	 * 
	 * @param privacyManager
	 */
	public void setPrivacyManager(PrivacyManager privacyManager) {
		this.privacyManager = privacyManager;
	}

	/**
	 * Returns TRUE if on MyWorkspace, FALSE if on a specific site
	 * 
	 * @return
	 */
	public boolean isMyWorkspace() {

		// get Site id
		String siteId = getSiteId();

		if (SiteService.getUserSiteId("admin").equals(siteId))
			return false;

		final boolean where = SiteService.isUserSite(siteId);

		LOG.debug("Result of determinig if My Workspace: " + where);

		return where;
	}

	/**
	 * 
	 * @return
	 */
	public String getCheckboxText() {
		if (!isMyWorkspace()) {
			curSite = getSiteId();
		}

		if (privacyManager.isViewable(curSite, getUserId())) {
			checkboxText = getMessageBundleString(HIDE);
		}
		else {
			checkboxText = getMessageBundleString(SHOW);
		}

		return checkboxText;
	}

	/**
	 * 
	 * @param checkboxText
	 */
	public void setCheckboxText(String checkboxText) {
		this.checkboxText = checkboxText;
	}

	/**
	 * Sets the privacy status for the user
	 *
	 * @return
	 * 		String for navigation
	 */
	public String processUpdate() {
		// if user checked the checkbox
		if (changeStatus) {
			processChoice(isMyWorkspace() ? curSite : getSiteId(), 
							new Boolean(SHOW_ME.equals(checkboxText)));

			// Reset the checkbox to not checked
			changeStatus = false;
		}

		return "main";
	}

	/**
	 * 
	 * @param siteId
	 */
	private void processChoice(String siteId, Boolean status) {
		privacyManager.setViewableState(siteId, getUserId(), status,
				privacyManager.USER_RECORD_TYPE);

	}

	/**
	 * 
	 * @return
	 */
	public String processShowAll() {
		List mySites = getSiteList();

		Iterator siteIter = mySites.iterator();

		while (siteIter.hasNext()) {
			Site curSite = (Site) siteIter.next();

			processChoice(curSite.getId(), Boolean.TRUE);
		}

		allChanged = true;
		changeAllMsg = getMessageBundleString(SET_SHOW_ALL_STRING);

		// Below so UI shows no site selected
		siteSelected = false;
		selectedSite = "";
		
		return "main";
	}

	/**
	 * 
	 * @return
	 */
	public String processHideAll() {
		List mySites = getSiteList();

		Iterator siteIter = mySites.iterator();

		while (siteIter.hasNext()) {
			Site curSite = (Site) siteIter.next();

			processChoice(curSite.getId(), Boolean.FALSE);
		}

		allChanged = true;
		changeAllMsg = getMessageBundleString(SET_HIDE_ALL_STRING);

		// Below so UI shows no site selected
		siteSelected = false;
		selectedSite = "";

		return "main";
	}

	/**
	 * Pulls the message from the message bundle using the name passed in
	 * 
	 * @param key
	 *            The name in the MessageBundle for the message wanted
	 * 
	 * @return String The string that is the value of the message
	 */
	private String getMessageBundleString(String key) {
		return msgs.getString(key);

	}

	/**
	 * 
	 * @return
	 */
	public boolean isChangeStatus() {
		return changeStatus;
	}

	/**
	 * 
	 * @param changeStatus
	 */
	public void setChangeStatus(boolean changeStatus) {
		this.changeStatus = changeStatus;
	}

	/**
	 * 
	 * @return
	 */
	public String getSelectedSite() {
		return selectedSite;
	}

	/**
	 * 
	 * @param selectedSite
	 */
	public void setSelectedSite(String selectedSite) {
		this.selectedSite = selectedSite;
	}

	/**
	 * Returns a list of sites user has access to (is a member of) 
	 *  
	 * @return
	 * 		List of sites user has access to (is a member of)
	 */
	private List getSiteList() {
		return SiteService.getSites(
					org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
					null, null, null,
					org.sakaiproject.site.api.SiteService.SortType.ID_ASC, null);

	}

	/**
	 * Returns an array of SelectItem for MyWorkspace page dropdown list
	 * @return
	 */
	public SelectItem[] getSites() {
		final List mySites = getSiteList();

		sites = new SelectItem[mySites.toArray().length + 1];

		final Iterator siteIter = mySites.iterator();
		int sitesIndex = 1;

		sites[0] = new SelectItem("", "");

		while (siteIter.hasNext()) {
			final Site site = (Site) siteIter.next();
			sites[sitesIndex++] = new SelectItem(site.getId(), site.getTitle());
		}

		return sites;
	}

	/**
	 * 
	 * @param e
	 */
	public void processSiteSelected(ValueChangeEvent e) {
		allChanged = false;
		
		if ("".equals((String) e.getNewValue())) {
			siteSelected = false;
		} else {
			curSite = (String) e.getNewValue();
			siteSelected = true;
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSiteSelected() {
		return (isMyWorkspace()) ? siteSelected : false;
	}

	/**
	 * 
	 * @param siteSelected
	 */
	public void setSiteSelected(boolean siteSelected) {
		this.siteSelected = siteSelected;
	}

	/**
	 * 
	 * @return
	 */
	public boolean getAllChanged() {
		return allChanged;
	}

	/**
	 * 
	 * @return
	 */
	public String getChangeAllMsg() {
		return changeAllMsg;
	}
}
