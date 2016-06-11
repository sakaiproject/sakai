package org.sakaiproject.portal.entityprovider;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

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
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.portal.beans.PortalNotifications;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * An entity provider to serve Portal information
 * 
 */
@Slf4j
public class PortalEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Outputable, ActionsExecutable, Describeable {

	public final static String PREFIX = "portal";
	public final static String TOOL_ID = "sakai.portal";

	@Setter
	private ProfileConnectionsLogic profileConnectionsLogic;

	@Setter
	private ProfileLogic profileLogic;

	@Setter
	private ServerConfigurationService serverConfigurationService;

	@Setter
	private SessionManager sessionManager;

	private Template formattedProfileTemplate = null;

	public void init() {

		VelocityEngine ve = new VelocityEngine();
		ve.setProperty("resource.loader", "class");
		ve.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		try {
			ve.init();
			formattedProfileTemplate = ve.getTemplate("org/sakaiproject/portal/entityprovider/profile-popup.vm");
		} catch (Exception e) {
			log.error("Failed to load formatted_profile.vm", e);
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

	@EntityCustomAction(action="formatted",viewKey=EntityView.VIEW_SHOW)
	public ActionReturn getFormattedProfile(EntityReference ref) {

		String currentUserId = developerHelperService.getCurrentUserId();

		ResourceLoader rl = new ResourceLoader(currentUserId, "profile-popup");

		UserProfile userProfile = (UserProfile) profileLogic.getUserProfile(ref.getId());

		String connectionUserId = userProfile.getUserUuid();

		VelocityContext context = new VelocityContext();
		context.put("displayName", userProfile.getDisplayName());
		context.put("email", userProfile.getEmail());
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
