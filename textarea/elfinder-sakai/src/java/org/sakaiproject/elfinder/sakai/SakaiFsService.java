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
package org.sakaiproject.elfinder.sakai;

import cn.bluejoe.elfinder.controller.executor.FsItemEx;
import cn.bluejoe.elfinder.service.*;
import org.apache.commons.codec.binary.Base64;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.elfinder.impl.SakaiFsServiceConfig;
import org.sakaiproject.elfinder.sakai.content.ContentFsItem;
import org.sakaiproject.elfinder.sakai.content.ContentSiteVolumeFactory;
import org.sakaiproject.elfinder.sakai.site.SiteFsItem;
import org.sakaiproject.elfinder.sakai.site.SiteFsVolume;
import org.sakaiproject.elfinder.sakai.samigo.SamFsItem;
import org.sakaiproject.elfinder.sakai.samigo.SamSiteVolumeFactory;
import org.sakaiproject.elfinder.sakai.samigo.SamSiteVolumeFactory.SamSiteVolume;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.UserDirectoryService;

import java.io.IOException;
import java.util.*;

import static org.sakaiproject.site.api.SiteService.SelectionType.ACCESS;

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
public class SakaiFsService implements FsService {

	private ContentHostingService contentHostingService;
	private SiteService siteService;
	private UserDirectoryService userDirectoryService;

	private FsSecurityChecker securityChecker;

	private Map<String, SiteVolumeFactory> toolVolume;

	public Map<String, SiteVolumeFactory> getToolVolume() {
		return toolVolume;
	}

	public void setSecurityChecker(FsSecurityChecker securityChecker) {
		this.securityChecker = securityChecker;
	}

	public void setToolVolume(Set<SiteVolumeFactory> toolVolumeSet) {
		toolVolume = new HashMap<>();
		for (SiteVolumeFactory factory : toolVolumeSet) {
			toolVolume.put(factory.getPrefix(), factory);
		}
	}

	String[][] escapes = { { "+", "_P" }, { "-", "_M" }, { "/", "_S" }, { ".", "_D" }, { "=", "_E" } };

	public FsItem fromHash(String hash) throws IOException {
		if (hash == null || hash.isEmpty()) {
			return null;
		}
		for (String[] pair : escapes)
		{
			hash = hash.replace(pair[1], pair[0]);
		}
		String path = new String(Base64.decodeBase64(hash));

		// Work out what we're dealing with.
		String[] parts = path.split("/");
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
					if (path.endsWith("/")) {
						builder.append("/");
					}
					volumePath = builder.toString();
				}
			}
		}

		FsItem fsItem = null;
		if (tool != null) {
			SiteVolumeFactory factory = toolVolume.get(tool);
			if (factory != null) {
				FsVolume volume = factory.getVolume(this, siteId);
				fsItem = volume.fromPath(volumePath);
			}
		} else if (siteId != null) {
			fsItem = getSiteVolume(siteId).getRoot();
		}
		return fsItem;
	}

	public String getHash(FsItem item) throws IOException {
		// Need to get prefix.
		StringBuilder path = new StringBuilder();
		FsVolume volume = item.getVolume();
		if (volume instanceof SiteFsVolume) {
			path.append("/").append(((SiteFsVolume) volume).getSiteId());
		} else if (volume instanceof SiteVolume) {
			path.append("/").append(((SiteVolume)volume).getSiteId());
			// Need prefix but don't want volumes to be able to screw it up.
			path.append("/").append(((SiteVolume)volume).getSiteVolumeFactory().getPrefix());
		} else if (volume instanceof SamSiteVolume) {
			path.append("/").append(((SamSiteVolume)volume).getSiteId());
			// Need prefix but don't want volumes to be able to screw it up.
			path.append("/").append(((SiteVolume)volume).getSiteVolumeFactory().getPrefix());
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

		for (String[] pair : escapes)
		{
			base = base.replace(pair[0], pair[1]);
		}
		return base;
	}

	public FsSecurityChecker getSecurityChecker() {
		return securityChecker;
	}

	public String getVolumeId(FsVolume volume) {
		if (volume instanceof SiteVolume) {
			// This should be the siteID plus /content
			// but I wouldn't expect to ever see this as
			return ((SiteVolume)volume).getSiteId();
		} else if (volume instanceof SiteFsVolume) {
			// Will return the site ID
			return ((SiteFsVolume)volume).getSiteId();
		} else if (volume instanceof SamSiteVolume) {
			// Will return the site ID
			return ((SamSiteVolume)volume).getSiteId();
		} else {
			throw new IllegalArgumentException("Passed argument isn't SakaiFsVolume");
		}
	}

	public FsVolume[] getVolumes() {
		List<Site> sites  = siteService.getSites(ACCESS, null, null, null, null, null);
		// Add the user workspace as volume.
		try {
			String userId = userDirectoryService.getCurrentUser().getId();
			Site myWorkspace = siteService.getSiteVisit(siteService.getUserSiteId(userId));
			sites.add(0,myWorkspace);
		} catch (Exception e) {}
		List<FsVolume> volumes = new ArrayList<>(sites.size());
		for (Site site: sites) {
			String currentSiteId = site.getId();
			//Exclude the admin site as do not contain tools with real content
			if("!admin".equals(currentSiteId) || "~admin".equals(currentSiteId)){
				continue;
			}
			volumes.add(getSiteVolume(currentSiteId));
		}
		return volumes.toArray(new FsVolume[0]);
	}

	/**
	 * This is useful in all the tool implementations when you need the parent item from the parent volume.
	 * @param siteId The site ID.
	 * @return The SiteFsVolume for the site.
	 */
	public FsVolume getSiteVolume(String siteId) {
		return new SiteFsVolume(siteId, this);
	}

	public FsServiceConfig getServiceConfig() {
		return new SakaiFsServiceConfig();
	}

	@Override
	public FsItemEx[] find(FsItemFilter filter) {
		return new FsItemEx[0];
	}

	public ContentHostingService getContent() {
		return contentHostingService;
	}


	public String asId(FsItem fsItem) {
		if (fsItem instanceof ContentFsItem) {
			return ((ContentFsItem) fsItem).getId();
		} else if (fsItem instanceof SiteFsItem) {
			return ((SiteFsItem)fsItem).getId();
		} else if (fsItem instanceof SamFsItem) {
			return ((SamFsItem)fsItem).getId();
		} else {
			throw new IllegalArgumentException("Passed FsItem must be a SakaiFsItem.");
		}
	}

	public SiteService getSiteService() {
		return siteService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

}
