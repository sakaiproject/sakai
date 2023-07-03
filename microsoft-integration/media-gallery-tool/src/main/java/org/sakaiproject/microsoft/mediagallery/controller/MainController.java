/**
* Copyright (c) 2023 Apereo Foundation
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

package org.sakaiproject.microsoft.mediagallery.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.microsoft.api.MicrosoftAuthorizationService;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.MicrosoftDriveItem;
import org.sakaiproject.microsoft.api.data.MicrosoftDriveItemFilter;
import org.sakaiproject.microsoft.api.data.MicrosoftRedirectURL;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftInvalidTokenException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftNoCredentialsException;
import org.sakaiproject.microsoft.api.model.MicrosoftAccessToken;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.microsoft.mediagallery.auxiliar.MediaGallerySessionBean;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
public class MainController {
	
	public static final String SORT_BY_NAME = "name";
	public static final String SORT_BY_DATE = "date";
	public static final String SORT_ASCENDING = ":0";
	public static final String SORT_DESCENDING = ":1";
	
	private static ResourceLoader rb = new ResourceLoader("Messages");

	@Autowired
	private MicrosoftSynchronizationService microsoftSynchronizationService;
	
	@Autowired
	private MicrosoftCommonService microsoftCommonService;
	
	@Autowired
	private MicrosoftAuthorizationService microsoftAuthorizationService;
	
	@Autowired
	private SakaiProxy sakaiProxy;
	
	@Autowired
	private MediaGallerySessionBean mediaGallerySessionBean;
	
	private static final String INDEX_TEMPLATE = "index";
	private static final String INDEX_WS_TEMPLATE = "index_ws";
	private static final String BODY_TEMPLATE = "body";
	private static final String INFO_TEMPLATE = "info :: info-body";
	private static final String ERROR_TEMPLATE = "error";
	private static final String REDIRECT_INDEX = "redirect:/index";
	private static final String REDIRECT_ERROR = "redirect:/error";
	
	private MicrosoftDriveItemFilter filter = MicrosoftDriveItemFilter.builder()
			.contentType(MicrosoftDriveItemFilter.VIDEO_CONTENT_TYPE)
			.contentType(MicrosoftDriveItemFilter.AUDIO_CONTENT_TYPE)
			.build();
	
	@ModelAttribute("locale")
	public Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {
        Locale loc = sakaiProxy.getLocaleForCurrentUser();
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        localeResolver.setLocale(request, response, loc);
        return loc;
    }
	
	@ExceptionHandler(MicrosoftGenericException.class)
	public String handleCredentialsError(HttpServletRequest req, Exception ex, RedirectAttributes redirectAttributes) {
		//store i18n exception message and redirect
		redirectAttributes.addFlashAttribute("exception_error", rb.getString(ex.getMessage()));

		return REDIRECT_ERROR;
	}
	
	@RequestMapping(value = {"/error"}, method = RequestMethod.GET)
	public String showError() {
		return ERROR_TEMPLATE;
	}

	@RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
	public String showIndex(Model model, HttpServletRequest req) throws MicrosoftGenericException {
		
		if(sakaiProxy.isMyWorkspace()) {
			String userId = sakaiProxy.getCurrentUserId();
			
			try {
				//check delegated client -> will throw an exception if access token is invalid
				microsoftAuthorizationService.checkDelegatedClient(userId);
			}catch(MicrosoftInvalidTokenException e) {
				//our configured access token is not valid. Try to get a new one through the automatic authorization process 
				//(automatic: no confirmation screen will be shown)
				return sendMicrosoftAuthorizationRedirect(true, req);
			}catch(MicrosoftNoCredentialsException e) {
				//this means there is no access token configured. Do nothing, continue and show the "configure" button.
			}catch(MicrosoftCredentialsException e) {
				//unexpected error. Remove current access token and let the process starts from the beginning
				microsoftAuthorizationService.revokeAccessToken(userId);
			}
			
			MicrosoftAccessToken mcAccessToken = microsoftAuthorizationService.getAccessToken(userId);
			model.addAttribute("delegatedClientConfigured", (mcAccessToken != null));
			if(mcAccessToken != null) {
				model.addAttribute("mcUserAccount", mcAccessToken.getMicrosoftUserId());
			}
			
			return INDEX_WS_TEMPLATE;
		} else {
			return INDEX_TEMPLATE;
		}
	}
	
	/**
	 * Called by AJAX. Load all items into the model. Returns FRAGMENT
	 * @param model
	 * @return FRAGMENT
	 * @throws MicrosoftGenericException
	 */
	@RequestMapping(value = {"/items"}, method = RequestMethod.GET)
	public String loadItems(
			@RequestParam(defaultValue = "") String refreshSection,
			@RequestParam(defaultValue = "name:0") String sortBy,
			@RequestParam(defaultValue = "false") Boolean treeView,
			Model model
	) throws MicrosoftGenericException {
		
		model.addAttribute("refreshSection", refreshSection);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("treeView", treeView);

		if(sakaiProxy.isMyWorkspace()) {
			doIndexMyWorkspace(refreshSection, sortBy, treeView, model);
		} else {
			doIndexSite(refreshSection, sortBy, treeView, model);
		}
		return BODY_TEMPLATE;
	}
	
	//Main screen (Site Tool)
	private void doIndexSite(String refreshSection, String sortBy,  Boolean treeView, Model model) throws MicrosoftGenericException {
		if(StringUtils.isNotBlank(refreshSection)) {
			mediaGallerySessionBean.resetItems(refreshSection);
			microsoftCommonService.resetDriveItemsCache();
			microsoftCommonService.resetGroupDriveItemsCache(refreshSection);
		}
		
		Map<String, MicrosoftDriveItem> itemsMap = mediaGallerySessionBean.getItemsMap();
		if(itemsMap.isEmpty() || StringUtils.isNotBlank(refreshSection)) {
			String siteId = sakaiProxy.getCurrentSiteId();
			Site site = sakaiProxy.getSite(siteId);
			String userId = sakaiProxy.getCurrentUserId();
			
			List<SiteSynchronization> ssList = microsoftSynchronizationService.getSiteSynchronizationsBySite(siteId);
			for(SiteSynchronization ss : ssList) {
				if(StringUtils.isBlank(refreshSection) || refreshSection.equals(ss.getTeamId())) {
					//check if team exists
					MicrosoftTeam team = microsoftCommonService.getTeam(ss.getTeamId());
					
					if(team != null) {
						mediaGallerySessionBean.addType(team.getId(), team.getName());
						
						//teacher -> get all elements (including all private channels)
						List<String> channelIds = null;
						//student -> filter elements based on group
						if(!sakaiProxy.canUpdateSite(site.getReference(), userId)) {
							channelIds = new ArrayList<>();
							
							if(site.hasGroups() && ss.getGroupSynchronizationsList() != null && !ss.getGroupSynchronizationsList().isEmpty()) {
								//get all groups user pertains to
								Set<String> pertainsTo = site.getGroupsWithMember(userId).stream().map(g -> g.getId()).collect(Collectors.toSet());
								//get Microsoft channels related to these groups
								channelIds = ss.getGroupSynchronizationsList().stream()
									.filter(gs -> pertainsTo.contains(gs.getGroupId()))
									.map(gs -> gs.getChannelId())
									.collect(Collectors.toList());
							}
						}
						
						List<MicrosoftDriveItem> items = microsoftCommonService.getAllGroupDriveItems(team.getId(), channelIds, filter);
						if(items != null && !items.isEmpty()) {
							mediaGallerySessionBean.getItemsByType().put(team.getId(), items);
							
							//iterate all items and add them to the session maps
							exploreAndDoSomething(items, (i) -> mediaGallerySessionBean.addItem(team.getId(), i));
						}
					
					}
				}
			}
		}
		
		//sort items by name or date
		Comparator<MicrosoftDriveItem> comparator = new ItemsComparator(sortBy);
		for(Object teamKey : mediaGallerySessionBean.getTypesMap().keySet()) {
			exploreAndSort(mediaGallerySessionBean.getItemsByType().get(teamKey), comparator);
			sort(mediaGallerySessionBean.getAllItemsByType().get(teamKey), comparator);
		}
		
		model.addAttribute("typesMap", mediaGallerySessionBean.getTypesMap());
		model.addAttribute("typesKeys", mediaGallerySessionBean.getSortedTypeKeys());
		model.addAttribute("allItemsByType", mediaGallerySessionBean.getAllItemsByType());
		model.addAttribute("itemsByType", mediaGallerySessionBean.getItemsByType());
	}
	
	//Main screen (MyWorkspace Tool)
	private void doIndexMyWorkspace(String refreshSection, String sortBy,  Boolean treeView, Model model) throws MicrosoftGenericException {
		
		String userId = sakaiProxy.getCurrentUserId();
		
		if(StringUtils.isNotBlank(refreshSection)) {
			mediaGallerySessionBean.resetItems(MediaGallerySessionBean.Type.valueOf(refreshSection));
			microsoftCommonService.resetDriveItemsCache();
			microsoftCommonService.resetUserDriveItemsCache(userId);
		}
		
		Map<String, MicrosoftDriveItem> itemsMap = mediaGallerySessionBean.getItemsMap();
		if(itemsMap.isEmpty() || StringUtils.isNotBlank(refreshSection)) {
			//USER items
			if(StringUtils.isBlank(refreshSection) || MediaGallerySessionBean.Type.USER.name().equals(refreshSection)) {
				mediaGallerySessionBean.addType(MediaGallerySessionBean.Type.USER, rb.getString(MediaGallerySessionBean.Type.USER.name()));

				List<MicrosoftDriveItem> userItems = microsoftCommonService.getAllMyDriveItems(userId, filter);
				if(userItems != null && !userItems.isEmpty()) {
					mediaGallerySessionBean.getItemsByType().put(MediaGallerySessionBean.Type.USER, userItems);
					
					//iterate all items and add them to the session maps
					exploreAndDoSomething(userItems, (i) -> mediaGallerySessionBean.addItem(MediaGallerySessionBean.Type.USER, i));
				}
			}
			
			//SHARED items
			if(StringUtils.isBlank(refreshSection) || MediaGallerySessionBean.Type.SHARED.name().equals(refreshSection)) {
				mediaGallerySessionBean.addType(MediaGallerySessionBean.Type.SHARED, rb.getString(MediaGallerySessionBean.Type.SHARED.name()));

				List<MicrosoftDriveItem> sharedItems = microsoftCommonService.getAllMySharedDriveItems(userId, filter);
				if(sharedItems != null && !sharedItems.isEmpty()) {
					mediaGallerySessionBean.getItemsByType().put(MediaGallerySessionBean.Type.SHARED, sharedItems);
					
					//iterate all items and add them to the session maps
					exploreAndDoSomething(sharedItems, (i) -> mediaGallerySessionBean.addItem(MediaGallerySessionBean.Type.SHARED, i));
				}
			}
		}
		
		//sort items by name or date
		Comparator<MicrosoftDriveItem> comparator = new ItemsComparator(sortBy);
		for(Object typeKey : mediaGallerySessionBean.getTypesMap().keySet()) {
			exploreAndSort(mediaGallerySessionBean.getItemsByType().get(typeKey), comparator);
			sort(mediaGallerySessionBean.getAllItemsByType().get(typeKey), comparator);
		}
					
		model.addAttribute("typesMap", mediaGallerySessionBean.getTypesMap());
		model.addAttribute("typesKeys", mediaGallerySessionBean.getSortedTypeKeys());
		model.addAttribute("allItemsByType", mediaGallerySessionBean.getAllItemsByType());
		model.addAttribute("itemsByType", mediaGallerySessionBean.getItemsByType());
	}
	
	@GetMapping(value = {"/thumbnail/{itemId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getThumbnail(@PathVariable String itemId, @RequestParam(defaultValue = "0") Integer maxWidth, @RequestParam(defaultValue = "0") Integer maxHeight) throws MicrosoftGenericException {
		
		String ret = "";
		MicrosoftDriveItem item = mediaGallerySessionBean.getItem(itemId);
		if(item != null && item.getMimeType().toLowerCase().contains(MicrosoftDriveItemFilter.VIDEO_CONTENT_TYPE)) {
			ret = (item.getThumbnail() != null) ? item.getThumbnail() : microsoftCommonService.getThumbnail(item, maxWidth, maxHeight);
			if(ret == null) {
				ret = "";
				item.setThumbnail(ret);
			}
		}
		
		return ret;
	}
	
	@GetMapping(value = {"/link/{itemId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String getLink(@PathVariable String itemId) throws MicrosoftGenericException {
		
		String ret = "";
		MicrosoftDriveItem item = mediaGallerySessionBean.getItem(itemId);
		if(item != null) {
			if(sakaiProxy.isMyWorkspace()) {
				ret = item.getUrl();
			} else {
				List<String> teamIds = mediaGallerySessionBean.getTypesMap().keySet().stream().map(o -> (String)o).collect(Collectors.toList());
				ret = (item.getLinkURL() != null) ? 
						item.getLinkURL() : 
						microsoftCommonService.createLinkForTeams(item, teamIds, MicrosoftCommonService.PermissionRoles.READ);
				
				//in case link creation fails, return generic URL
				if(StringUtils.isBlank(ret)) {
					item.setLinkURL(item.getUrl());
					ret = item.getUrl();
				}
			}
		}
		
		return ret;
	}
	
	@GetMapping(value = {"/info/{itemId}"})
	public String getInfo(@PathVariable String itemId, Model model) throws MicrosoftGenericException {
		MicrosoftDriveItem item = mediaGallerySessionBean.getItem(itemId);
		if(item != null) {
			model.addAttribute("item", item);
		}
		
		return INFO_TEMPLATE;
	}
	
	@GetMapping(value = {"/configure"})
	public String doConfigure(HttpServletRequest req) throws MicrosoftGenericException {
		return sendMicrosoftAuthorizationRedirect(false, req);
	}
	
	@GetMapping(value = {"/revoke"})
	public String doRevoke(Model model) throws MicrosoftGenericException {
		microsoftAuthorizationService.revokeAccessToken(sakaiProxy.getCurrentUserId());
		return REDIRECT_INDEX;
	}
	
	private void exploreAndDoSomething(List<MicrosoftDriveItem> itemsList, Consumer<MicrosoftDriveItem> method) {
		for(MicrosoftDriveItem item : itemsList) {
			if(item.isFolder()) {
				if(item.hasChildren()) {
					exploreAndDoSomething(item.getChildren(), method);
				}
			} else {
				//do something
				method.accept(item);
			}
		}
	}
	
	private void exploreAndSort(List<MicrosoftDriveItem> itemsList, Comparator<MicrosoftDriveItem> comparator) {
		if(itemsList != null) {
			for(MicrosoftDriveItem item : itemsList) {
				if(item.isFolder() && item.hasChildren()) {
					exploreAndSort(item.getChildren(), comparator);
				}
			}
			sort(itemsList, comparator);
		}
	}
	
	private void sort(List<MicrosoftDriveItem> itemsList, Comparator<MicrosoftDriveItem> comparator) {
		if(itemsList != null) {
			if(comparator != null) {
				Collections.sort(itemsList, comparator);
			} else {
				Collections.sort(itemsList);
			}
		}
	}
	
	private String sendMicrosoftAuthorizationRedirect(boolean autoReturn, HttpServletRequest req) throws MicrosoftCredentialsException {
		MicrosoftRedirectURL authURL = microsoftAuthorizationService.getAuthenticationUrl();
		//store in cache where to redirect back after authorization code is received
		//also store "state" (from authURL) sent to Microsoft, so we can check it when the request returns
		MicrosoftRedirectURL afterTokenURL = authURL.toBuilder().URL(req.getContextPath()).auto(autoReturn).build();
		sakaiProxy.getCurrentSession().setAttribute(MicrosoftAuthorizationService.MICROSOFT_SESSION_REDIRECT, afterTokenURL);

		return "redirect:"+authURL.getURL();
	}
	
	@AllArgsConstructor
	private class ItemsComparator implements Comparator<MicrosoftDriveItem> {
		private String sortBy = "";
		
		@Override
		public int compare(MicrosoftDriveItem a, MicrosoftDriveItem b) {
			if(a.isFolder() == b.isFolder()) {
				if(sortBy.startsWith(SORT_BY_NAME)) {
					if(sortBy.endsWith(SORT_DESCENDING)){
						return b.getName().compareToIgnoreCase(a.getName());
					} else {
						return a.getName().compareToIgnoreCase(b.getName());
					}
				}
				if(sortBy.startsWith(SORT_BY_DATE)) {
					if(sortBy.endsWith(SORT_DESCENDING)){
						return a.getModifiedAt().compareTo(b.getModifiedAt());
					} else {
						return b.getModifiedAt().compareTo(a.getModifiedAt());
					}
				}
			}
			if(a.isFolder()) {
				return -1;
			}
			return 1;
		}
		
	}
}
