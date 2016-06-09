package org.sakaiproject.portal.entityprovider;

import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

import org.sakaiproject.portal.api.PortalService;
import org.sakaiproject.portal.beans.BullhornAlert;
import org.sakaiproject.portal.beans.PortalNotifications;

import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileLinkLogic;
import org.sakaiproject.profile2.model.Person;

import org.sakaiproject.util.ResourceLoader;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An entity provider to serve Portal information
 * 
 */
@Setter @Slf4j
public class PortalEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Outputable, ActionsExecutable, Describeable {

	public final static String PREFIX = "portal";
	public final static String TOOL_ID = "sakai.portal";

	private PortalService portalService;
	private ProfileConnectionsLogic profileConnectionsLogic;
	private ProfileLinkLogic profileLinkLogic;
	private SessionManager sessionManager;

	public String getEntityPrefix() {
		return PREFIX;
	}

	public String getAssociatedToolId() {
		return TOOL_ID;
	}

        public String[] getHandledOutputFormats() {
            return new String[] { Formats.TXT ,Formats.JSON, Formats.HTML};
        }

	// So far all we do is errors to solve SAK-29531
	// But this could be extended...
	@EntityCustomAction(action = "notify", viewKey = "")
	public PortalNotifications handleNotify(EntityView view) {
		Session s = sessionManager.getCurrentSession();
		if ( s == null ) {
			throw new IllegalArgumentException("Session not found");
		}
		List<String> retval = new ArrayList<String> ();
                String userWarning = (String) s.getAttribute("userWarning");
                if (StringUtils.isNotEmpty(userWarning)) {
			retval.add(userWarning);
		}
		PortalNotifications noti = new PortalNotifications ();
		noti.setError(retval);
		s.removeAttribute("userWarning");
		return noti;
	}

	@EntityCustomAction(action = "socialAlerts", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getSocialAlerts(EntityView view) {

		String currentUserId = getCheckedCurrentUser();

		List<BullhornAlert> alerts = portalService.getSocialAlerts(currentUserId);

		ResourceLoader rl = new ResourceLoader("bullhorns");

		if (alerts.size() > 0) {
			Map<String, Object> data = new HashMap();
			data.put("alerts", alerts);
			data.put("i18n", rl);

			return new ActionReturn(data);
		} else {
			Map<String, String> i18n = new HashMap();
			i18n.put("noAlerts", rl.getString("noAlerts"));

			Map<String, Object> data = new HashMap();
			data.put("message", "NO_ALERTS");
			data.put("i18n", i18n);

			return new ActionReturn(data);
		}
	}

	@EntityCustomAction(action = "clearSocialAlert", viewKey = EntityView.VIEW_LIST)
	public boolean clearSocialAlert(Map<String, Object> params) {

		String currentUserId = getCheckedCurrentUser();

		try {
			long alertId = Long.parseLong((String) params.get("id"));
			return portalService.clearSocialAlert(currentUserId, alertId);
		} catch (Exception e) {
			log.error("Failed to clear social alert", e);
		}

		return false;
	}

	@EntityCustomAction(action = "clearAllSocialAlerts", viewKey = EntityView.VIEW_LIST)
	public boolean clearAllSocialAlerts(Map<String, Object> params) {

		String currentUserId = getCheckedCurrentUser();

		try {
			return portalService.clearAllSocialAlerts(currentUserId);
		} catch (Exception e) {
			log.error("Failed to clear all social alerts", e);
		}

		return false;
	}

	@EntityCustomAction(action = "academicAlerts", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getAcademicAlerts(EntityView view) {

		String currentUserId = getCheckedCurrentUser();

		List<BullhornAlert> alerts = portalService.getAcademicAlerts(currentUserId);

		ResourceLoader rl = new ResourceLoader("bullhorns");

		if (alerts.size() > 0) {
			Map<String, Object> data = new HashMap();
			data.put("alerts", alerts);
			data.put("i18n", rl);

			return new ActionReturn(data);
		} else {
			Map<String, String> i18n = new HashMap();
			i18n.put("noAlerts", rl.getString("noAlerts"));

			Map<String, Object> data = new HashMap();
			data.put("message", "NO_ALERTS");
			data.put("i18n", i18n);

			return new ActionReturn(data);
		}
	}

	@EntityCustomAction(action = "clearAcademicAlert", viewKey = EntityView.VIEW_LIST)
	public boolean clearAcademicAlert(Map<String, Object> params) {

		String currentUserId = getCheckedCurrentUser();

		try {
			long alertId = Long.parseLong((String) params.get("id"));
			return portalService.clearAcademicAlert(currentUserId, alertId);
		} catch (Exception e) {
			log.error("Failed to clear academic alert", e);
		}

		return false;
	}

	@EntityCustomAction(action = "clearAllAcademicAlerts", viewKey = EntityView.VIEW_LIST)
	public boolean clearAllAcademicAlerts(Map<String, Object> params) {

		String currentUserId = getCheckedCurrentUser();

		try {
			return portalService.clearAllAcademicAlerts(currentUserId);
		} catch (Exception e) {
			log.error("Failed to clear all academic alerts", e);
		}

		return false;
	}

	@EntityCustomAction(action = "bullhornCounts", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getBullhornCounts(EntityView view) {

		String currentUserId = getCheckedCurrentUser();

		Map<String, Integer> counts = new HashMap();
		counts.put("academic", portalService.getAcademicAlertCount(currentUserId));
		counts.put("social", portalService.getSocialAlertCount(currentUserId));

		return new ActionReturn(counts);
	}

	private String getCheckedCurrentUser() throws SecurityException {

		String currentUserId = developerHelperService.getCurrentUserId();

		if (StringUtils.isBlank(currentUserId)) {
			throw new SecurityException("You must be logged in to use this service");
		} else {
            return currentUserId;
        }
	}
}
