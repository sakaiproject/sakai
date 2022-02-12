package org.sakaiproject.content.tool.inputpreserver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.tool.CopyrightDelegate;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;

/**
 * Preserves input for the DropboxMultipleFoldersUpload view
 */
@Slf4j
public class DropboxMultipleFoldersUploadInputPreserver extends UserInputPreserver
{
	private static final String PARAM_DISPLAY_NAME ="MultipleFolderDisplayName";
	private static final String CONTEXT_DISPLAY_NAME = "displayName";

	private static final String PARAM_USERS_DROPBOX_SELECTION = "usersDropbox-selection";
	private static final String CONTEXT_USERS_DROPBOX_SELECTION = "selectedDropboxes";

	private static final String PARAM_FILEITEM = "MultipleFolderContent";
	private static final String CONTEXT_FILEITEM = "alreadyUploadedFile";

	private static final String PARAM_COPYRIGHT = "copyright";
	private static final String CONTEXT_COPYRIGHT = "copyright";

	private static final String PARAM_NOTIFY_DROPBOX = "notify_dropbox";
	private static final String CONTEXT_NOTIFY_DROPBOX = "notify_dropbox";

	public static final String DROPBOX_SELECTION_TYPE_USER = "user";
	public static final String DROPBOX_SELECTION_TYPE_GROUP = "group";

	private static DropboxMultipleFoldersUploadInputPreserver instance = null;

	public static final ResourceLoader trb = new ResourceLoader("types");

	private static final UserDirectoryService userDirectoryService = ComponentManager.get(UserDirectoryService.class);
	private static final SiteService siteService = ComponentManager.get(SiteService.class);
	private static final ToolManager toolManager = ComponentManager.get(ToolManager.class);

	final static Map<String, String> paramToContextKeys = new HashMap<>();
	static
	{
		paramToContextKeys.put(PARAM_DISPLAY_NAME, CONTEXT_DISPLAY_NAME);
		paramToContextKeys.put(PARAM_USERS_DROPBOX_SELECTION, CONTEXT_USERS_DROPBOX_SELECTION);
		paramToContextKeys.put(PARAM_FILEITEM, CONTEXT_FILEITEM);
		paramToContextKeys.put(PARAM_COPYRIGHT, CONTEXT_COPYRIGHT);
		paramToContextKeys.put(PARAM_NOTIFY_DROPBOX, CONTEXT_NOTIFY_DROPBOX);
	}

	private DropboxMultipleFoldersUploadInputPreserver()
	{ }

	/**
	 * Gets the singleton instance
	 */
	public static DropboxMultipleFoldersUploadInputPreserver get()
	{
		instance = instance == null ? new DropboxMultipleFoldersUploadInputPreserver() : instance;
		return instance;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNamespace()
	{
		return "dropbox_multiple_folders_upload_input_preserver";
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getParamToContextKeys()
	{
		return paramToContextKeys;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object parseParameter(ParameterParser params, String key, SessionState state)
	{
		switch(key)
		{
			case PARAM_DISPLAY_NAME:
				return params.getString(key);
			case PARAM_USERS_DROPBOX_SELECTION:
				return parseDropboxSelectionsFromParams(params);
			case PARAM_FILEITEM:
				return parseFileItem(params, state);
			case PARAM_COPYRIGHT:
				CopyrightDelegate cd = new CopyrightDelegate();
				cd.captureCopyright(params);
				return cd;
			case PARAM_NOTIFY_DROPBOX:
				return params.getBoolean(key);
		}

		throw new UnsupportedOperationException("Unexpected key: " + key);
	}

	/**
	 * Parses the dropbox selections, a list of uuids representing a mix of users and groups, then converts them into lists of Strings appropriate for the 'Available users' widget
	 */
	public List<List<String>> parseDropboxSelectionsFromParams(ParameterParser params)
	{
		String[] selectedDropboxes = params.getStrings(PARAM_USERS_DROPBOX_SELECTION);
		if (selectedDropboxes == null || selectedDropboxes.length < 1)
		{
			return null;
		}

		// Extract Users and Groups
		List<User> selectedUsers = new ArrayList<>(selectedDropboxes.length);
		List<Group> selectedGroups = new ArrayList<>(selectedDropboxes.length);

		String siteId = toolManager.getCurrentPlacement().getContext();
		Site site = null;
		for (String selectedDropbox : selectedDropboxes)
		{
			// Check if it's a user, if not, check if it's a group in the site
			try
			{
				selectedUsers.add(userDirectoryService.getUser(selectedDropbox));
			}
			catch (UserNotDefinedException ex)
			{
				try
				{
					if (site == null)
					{
						site = siteService.getSite(siteId);
					}
					selectedGroups.add(site.getGroup(selectedDropbox));
				}
				catch (IdUnusedException e)
				{
					log.warn("Selected dropbox is neither a user nor a group in the site: " + selectedDropbox);
				}
			}
		}

		return prepareGroupsAndUsersForContext(selectedGroups, selectedUsers);
	}

	/**
	 * Converts the specified lists of groups and users into lists of Strings as expected in the context by the 'Available users' widget
	 */
	public List<List<String>> prepareGroupsAndUsersForContext(Collection<Group> groups, Collection<User> users)
	{
		List<List<String>> selectionsForContext = new ArrayList<>(groups.size() + users.size());
		groups.stream().forEach(group ->
		{
			List<String> row = new ArrayList<>(3);
			String title = new StringBuilder(trb.getString("multiple.file.upload.group"))
				.append(" ").append(group.getTitle()).toString();
			row.add(group.getId());
			row.add(title);
			row.add(DROPBOX_SELECTION_TYPE_GROUP);
			selectionsForContext.add(row);
		});
		users.stream().forEach(user ->
		{
			List<String> row = new ArrayList<>(3);
			StringBuilder displayName = new StringBuilder();
			String lastName = user.getLastName();
			String firstName = user.getFirstName();
			if (!StringUtils.isBlank(lastName))
			{
				if (!StringUtils.isBlank(firstName))
				{
					displayName.append(lastName).append(", ").append(firstName);
				}
				else
				{
					displayName.append(lastName);
				}
			}
			else
			{
				String uDisplayName = user.getDisplayName();
				if (!StringUtils.isBlank(uDisplayName))
				{
					displayName.append(uDisplayName);
				}
				else
				{
					displayName.append(user.getEid());
				}
			}
			row.add(user.getId());
			row.add(displayName.toString());
			row.add(DROPBOX_SELECTION_TYPE_USER);

			selectionsForContext.add(row);
		});
		return selectionsForContext;
	}

	/**
	 * Retrieves the last uploaded file:
	 * Tries params first to get a file uploaded with the form submission.
	 * If no file is present, it tries to grab one from the preserved input in the state.
	 * Returns null if neither is present.
	 */
	public FileItem parseFileItem(ParameterParser params, SessionState state)
	{
		FileItem fileItem = params.getFileItem(PARAM_FILEITEM);
		if (fileItem == null || StringUtils.isBlank(fileItem.getFileName()))
		{
			// Can't repopulate filepickers, so we must keep the file previously saved in the state
			fileItem = (FileItem)state.getAttribute(getNamespacedKey(PARAM_FILEITEM));
		}
		return fileItem;
	}
}
