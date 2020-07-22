/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.elfinder.impl;

import static org.sakaiproject.site.api.SiteService.SelectionType.ACCESS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.elfinder.SakaiFsItem;
import org.sakaiproject.elfinder.SakaiFsItemFilter;
import org.sakaiproject.elfinder.SakaiFsService;
import org.sakaiproject.elfinder.SakaiFsVolume;
import org.sakaiproject.elfinder.ToolFsVolume;
import org.sakaiproject.elfinder.ToolFsVolumeFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.impl.SiteFsVolume;
import org.sakaiproject.user.api.UserDirectoryService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * Volume layout:
 * /site1
 * /site1/content
 * /site1/forums
 * /site2
 * /site2/content
 * /site2/forums
 *
 * Then within each volume there will be files. The one that needs to be done really carefully is
 */
@Slf4j
public class SakaiFsServiceImpl implements SakaiFsService {

	private static final String[][] ESCAPE_PAIRS = { { "+", "_P" }, { "-", "_M" }, { "/", "_S" }, { ".", "_D" }, { "=", "_E" } };

	@Getter @Setter private ContentHostingService contentHostingService;
	@Setter private SiteService siteService;
	@Setter private UserDirectoryService userDirectoryService;

	@Getter private Map<String, ToolFsVolumeFactory> toolVolumes = new HashMap<>();

	public void registerToolVolume(ToolFsVolumeFactory volumeFactory) {
		if (volumeFactory != null) {
			if (toolVolumes.containsKey(volumeFactory.getPrefix())) {
				log.warn("Tool Volume already registered: {}, skipping", volumeFactory.getPrefix());
			} else {
				toolVolumes.put(volumeFactory.getPrefix(), volumeFactory);
				log.debug("Tool Volume {} registered", volumeFactory.getPrefix());
			}
		} else {
			log.warn("Can't register a null Tool Volume");
		}
	}

	public SakaiFsItem fromHash(final String hash) throws IOException {
		if (StringUtils.isBlank(hash)) return null;

		String path = StringUtils.replaceEach(
				hash,
				Stream.of(ESCAPE_PAIRS).map(i -> i[1]).toArray(String[]::new),
				Stream.of(ESCAPE_PAIRS).map(i -> i[0]).toArray(String[]::new));

		String encodedPath = new String(Base64.decodeBase64(path));

		// Work out what we're dealing with.
		String[] parts = encodedPath.split("/");
		String siteId = null;
		String tool = null;
		String volumePath = null;
		if (parts.length > 1) {
			siteId = parts[1];
			if (parts.length > 2) {
				tool = parts[2];
				if (parts.length > 3) {
					StringBuilder builder = new StringBuilder();
					for (int i = 3; i < parts.length; i++) {
						builder.append("/");
						builder.append(parts[i]);
					}
					// This gets lost in the split
					if (encodedPath.endsWith("/")) {
						builder.append("/");
					}
					volumePath = builder.toString();
				}
			}
		}

		SakaiFsItem fsItem = null;
		if (tool != null) {
			ToolFsVolumeFactory factory = toolVolumes.get(tool);
			if (factory != null) {
				SakaiFsVolume volume = factory.getVolume(siteId);
				fsItem = volume.fromPath(volumePath);
			}
		} else if (siteId != null) {
			fsItem = getSiteVolume(siteId).getRoot();
		}
		return fsItem;
	}

	public String getHash(SakaiFsItem item) throws IOException {
		// Need to get prefix.
		StringBuilder path = new StringBuilder();
		SakaiFsVolume volume = item.getVolume();
		// TODO ern
		if (volume instanceof SiteFsVolume) {
			path.append("/").append(((SiteFsVolume) volume).getSiteId());
		} else if (volume instanceof ToolFsVolume) {
		 	path.append("/").append(((ToolFsVolume) volume).getSiteId());
			// Need prefix but don't want volumes to be able to screw it up.
			path.append("/").append(((ToolFsVolume) volume).getToolVolumeFactory().getPrefix());
		// } else if (volume instanceof SamSiteVolume) {
		// 	path.append("/").append(((SamSiteVolume)volume).getSiteId());
		// 	// Need prefix but don't want volumes to be able to screw it up.
		// 	path.append("/").append(((SiteVolume)volume).getSiteVolumeFactory().getPrefix());
		} else {
			throw new IllegalArgumentException("Expected different type of FsItem: "+ volume.getClass());
		}
		String volumePath = volume.getPath(item);
		// We have to have a separator but don't want multiple ones.
		if (volumePath != null ) {
			if (!volumePath.startsWith("/")) {
				path.append("/");
			}
			path.append(volumePath);
		}

		String base = new String(Base64.encodeBase64(path.toString().getBytes()));

		for (String[] pair : ESCAPE_PAIRS)
		{
			base = base.replace(pair[0], pair[1]);
		}
		return base;
	}

	public String getVolumeId(SakaiFsVolume volume) {
		// TODO ern
		if (volume instanceof ToolFsVolume) {
			// This should be the siteID plus /content
			// but I wouldn't expect to ever see this as
			return ((ToolFsVolume) volume).getSiteId();
		} else if (volume instanceof SiteFsVolume) {
			// Will return the site ID
			return ((SiteFsVolume) volume).getSiteId();
		// } else if (volume instanceof SamSiteVolume) {
		// 	// Will return the site ID
		// 	return ((SamSiteVolume)volume).getSiteId();
		} else {
			throw new IllegalArgumentException("Passed argument isn't SakaiFsVolume");
		}
	}

	public SakaiFsVolume[] getVolumes() {
		List<Site> sites  = siteService.getSites(ACCESS, null, null, null, null, null);
		// Add the user workspace as volume.
		try {
			String userId = userDirectoryService.getCurrentUser().getId();
			Site myWorkspace = siteService.getSiteVisit(siteService.getUserSiteId(userId));
			sites.add(0,myWorkspace);
		} catch (Exception e) {}
		List<SakaiFsVolume> volumes = new ArrayList<>(sites.size());
		for (Site site: sites) {
			String currentSiteId = site.getId();
			//Exclude the admin site as do not contain tools with real content
			if("!admin".equals(currentSiteId) || "~admin".equals(currentSiteId)){
				continue;
			}
			try
			{
				volumes.add(getSiteVolume(siteService.getSiteVisit(currentSiteId).getId()));
			} catch (IdUnusedException e) {
				log.warn("Unexpected IdUnusedException: {}", e.getMessage());
			} catch (PermissionException e) {
				log.warn("Unexpected PermissionException: {}", e.getMessage());
			}
		}
		return volumes.toArray(new SakaiFsVolume[0]);
	}

	/**
	 * This is useful in all the tool implementations when you need the parent item from the parent volume.
	 * @param siteId The site ID.
	 * @return The SiteFsVolume for the site.
	 */
	public SakaiFsVolume getSiteVolume(String siteId) {
		return new SiteFsVolume(siteId, this, siteService);
	}

	@Override
	public SakaiFsItem[] find(SakaiFsItemFilter filter) {
		return new SakaiFsItem[0];
	}
}
