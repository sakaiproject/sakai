package org.sakaiproject.portal.charon.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.portal.api.Portal;
import org.sakaiproject.portal.api.PortalHandlerException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.cover.PreferencesService;
import org.sakaiproject.user.api.Preferences;
import java.util.Collections;
import org.sakaiproject.tool.cover.SessionManager;
import java.util.List;
import org.sakaiproject.entity.api.ResourceProperties;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.IdUnusedException;

/**
 * Handler for manipulating the list of favorite sites for the current user.
 * Handles AJAX requests from the "More Sites" drawer.
 *
 */
public class FavoritesHandler extends BasePortalHandler
{
	private static final String URL_FRAGMENT = "favorites";
	private static final String SEPARATOR = ";";
	private static final String PREFS_PROPERTY = "sakai:portal:sitenav";
	private static final String FAVORITES_PROPERTY = "order";

	public FavoritesHandler()
	{
		setUrlFragment(URL_FRAGMENT);
	}


	@Override
	public int doGet(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session)
			throws PortalHandlerException
	{
		if ((parts.length == 3) && (parts[1].equals(URL_FRAGMENT)))
		{
			try {
				List<String> favorites = favoriteSiteIds(session.getUserId());
				res.getWriter().write(StringUtils.join(favorites, SEPARATOR));
				return END;
			} catch (Exception e) {
				throw new PortalHandlerException(e);
			}
		}
		return NEXT;
	}

	@Override
	public int doPost(String[] parts, HttpServletRequest req,
			HttpServletResponse res, Session session)
			throws PortalHandlerException {

		if ((parts.length == 3) && (parts[1].equals(URL_FRAGMENT)))
		{
			try {
				String favorites = req.getParameter("favorites");

				if (favorites != null) {
					setFavoriteSiteIds(session.getUserId(), favorites.split(SEPARATOR));
				}
				return END;
			} catch (Exception e) {
				throw new PortalHandlerException(e);
			}
		}
		return NEXT;
	}

	private List<String> favoriteSiteIds(String userId) {
		if (userId == null) {
			// User isn't logged in
			return Collections.<String>emptyList();
		}

		Preferences prefs = PreferencesService.getPreferences(userId);
		ResourceProperties props = prefs.getProperties(PREFS_PROPERTY);

		List<String> result = (List<String>)props.getPropertyList(FAVORITES_PROPERTY);

		if (result == null) {
			return Collections.<String>emptyList();
		} else {
			return result;
		}
	}

	private void setFavoriteSiteIds(String userId, String[] siteIds) throws PermissionException, InUseException, IdUnusedException {
		if (userId == null) {
			return;
		}

		PreferencesEdit edit = PreferencesService.edit(userId);
		ResourcePropertiesEdit props = edit.getPropertiesEdit(PREFS_PROPERTY);

		// Replace all existing values
		props.removeProperty(FAVORITES_PROPERTY);

		for (String siteId : siteIds) {
			props.addPropertyToList(FAVORITES_PROPERTY, siteId);
		}

		PreferencesService.commit(edit);
	}
}
