package org.sakaiproject.portal.entityprovider;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.RuntimeConstants;

import org.sakaiproject.component.api.ServerConfigurationService;
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
import org.sakaiproject.entitybroker.exception.EntityException;

import org.sakaiproject.profile2.logic.ProfileConnectionsLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfileLinkLogic;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.portal.api.BullhornService;
import org.sakaiproject.portal.beans.BullhornAlert;
import org.sakaiproject.portal.beans.PortalNotifications;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.velocity.util.SLF4JLogChute;

/**
 * An entity provider to serve Portal information
 * 
 */
@Slf4j
public class PortalEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Outputable, ActionsExecutable, Describeable {

	public final static String PREFIX = "portal";
	public final static String TOOL_ID = "sakai.portal";

	@Setter
	private BullhornService bullhornService;

	@Setter
	private SessionManager sessionManager;

	@Setter
	private ProfileConnectionsLogic profileConnectionsLogic;

	@Setter
	private ProfileLogic profileLogic;

	@Setter
	private ProfileLinkLogic profileLinkLogic;

	@Setter
	private ServerConfigurationService serverConfigurationService;

	private Template formattedProfileTemplate = null;

	public void init() {

		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new SLF4JLogChute());

		ve.setProperty("resource.loader", "class");
		ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		// No logging. (If you want to log, you need to set an approrpriate directory in an approrpriate
		// velocity.properties file, or the log will be created in the directory in which tomcat is started, or
		// throw an error if it can't create/write in that directory.)
		ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
		try {
			ve.init();
			formattedProfileTemplate = ve.getTemplate("org/sakaiproject/portal/entityprovider/profile-popup.vm");
		} catch (Exception e) {
			log.error("Failed to load profile-popup.vm", e);
		}
	}

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

    private ActionReturn getBullhornAlerts(List<BullhornAlert> alerts) {

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

	@EntityCustomAction(action = "socialAlerts", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getSocialAlerts(EntityView view) {
		return getBullhornAlerts(bullhornService.getSocialAlerts(getCheckedCurrentUser()));
	}

	@EntityCustomAction(action = "clearBullhornAlert", viewKey = EntityView.VIEW_LIST)
	public boolean clearBullhornAlert(Map<String, Object> params) {

		String currentUserId = getCheckedCurrentUser();

		try {
			long alertId = Long.parseLong((String) params.get("id"));
			return bullhornService.clearBullhornAlert(currentUserId, alertId);
		} catch (Exception e) {
			log.error("Failed to clear social alert", e);
		}

		return false;
	}

	@EntityCustomAction(action = "clearAllSocialAlerts", viewKey = EntityView.VIEW_LIST)
	public boolean clearAllSocialAlerts(Map<String, Object> params) {

		String currentUserId = getCheckedCurrentUser();

		try {
			return bullhornService.clearAllSocialAlerts(currentUserId);
		} catch (Exception e) {
			log.error("Failed to clear all social alerts", e);
		}

		return false;
	}

	@EntityCustomAction(action = "academicAlerts", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getAcademicAlerts(EntityView view) {
		return getBullhornAlerts(bullhornService.getAcademicAlerts(getCheckedCurrentUser()));
	}

	@EntityCustomAction(action = "clearAllAcademicAlerts", viewKey = EntityView.VIEW_LIST)
	public boolean clearAllAcademicAlerts(Map<String, Object> params) {

		String currentUserId = getCheckedCurrentUser();

		try {
			return bullhornService.clearAllAcademicAlerts(currentUserId);
		} catch (Exception e) {
			log.error("Failed to clear all academic alerts", e);
		}

		return false;
	}

	@EntityCustomAction(action = "bullhornCounts", viewKey = EntityView.VIEW_LIST)
	public ActionReturn getBullhornCounts(EntityView view) {

		String currentUserId = getCheckedCurrentUser();

		Map<String, Integer> counts = new HashMap();
		counts.put("academic", bullhornService.getAcademicAlertCount(currentUserId));
		counts.put("social", bullhornService.getSocialAlertCount(currentUserId));

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

	@EntityCustomAction(action="formatted",viewKey=EntityView.VIEW_SHOW)
	public ActionReturn getFormattedProfile(EntityReference ref) {

		String currentUserId = developerHelperService.getCurrentUserId();

		ResourceLoader rl = new ResourceLoader(currentUserId, "profile-popup");

		UserProfile userProfile = (UserProfile) profileLogic.getUserProfile(ref.getId());

		String connectionUserId = userProfile.getUserUuid();

		VelocityContext context = new VelocityContext();
		context.put("displayName", userProfile.getDisplayName());
		context.put("profileUrl", profileLinkLogic.getInternalDirectUrlToUserProfile(connectionUserId));

		String email = userProfile.getEmail();
        if (StringUtils.isEmpty(email)) email = "";
		context.put("email", email);

		context.put("currentUserId", currentUserId);
		context.put("connectionUserId", connectionUserId);
		context.put("requestMadeLabel", rl.getString("connection.requested"));
		context.put("cancelLabel", rl.getString("connection.cancel"));
		context.put("incomingRequestLabel", rl.getString("connection.incoming.request"));
		context.put("removeConnectionLabel", rl.getString("connection.remove"));
		context.put("acceptLabel", rl.getString("connection.accept"));
		context.put("rejectLabel", rl.getString("connection.reject"));
		context.put("addConnectionLabel", rl.getString("connection.add"));

		boolean connectionsEnabled = serverConfigurationService.getBoolean("profile2.connections.enabled",
					ProfileConstants.SAKAI_PROP_PROFILE2_CONNECTIONS_ENABLED);

		if (connectionsEnabled && !currentUserId.equals(connectionUserId)) {

			int connectionStatus = profileConnectionsLogic.getConnectionStatus(currentUserId, connectionUserId);

			if (connectionStatus == ProfileConstants.CONNECTION_CONFIRMED) {
				context.put("connected" , true);
			} else if (connectionStatus == ProfileConstants.CONNECTION_REQUESTED) {
				context.put("requested" , true);
			} else if (connectionStatus == ProfileConstants.CONNECTION_INCOMING) {
				context.put("incoming" , true);
			} else {
				context.put("unconnected" , true);
			}
		}

		StringWriter writer = new StringWriter();

		try {
			formattedProfileTemplate.merge(context, writer);
			return new ActionReturn(Formats.UTF_8, Formats.HTML_MIME_TYPE, writer.toString());
		} catch (IOException ioe) {
			throw new EntityException("Failed to format profile.", ref.getReference());
		}
	}
}
