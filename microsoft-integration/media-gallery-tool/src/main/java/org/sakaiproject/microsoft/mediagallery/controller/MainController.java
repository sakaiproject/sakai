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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
	public String showIndex(
			@RequestParam(defaultValue = "") String refreshSection,
			@RequestParam(defaultValue = "name:0") String sortBy,
			@RequestParam(defaultValue = "false") Boolean treeView,
			Model model,
			HttpServletRequest req,
			RedirectAttributes redirectAttributes
		) throws MicrosoftGenericException {
		
		//check if properties are pre-populated from a redirect before to add them to the model
		if(model.getAttribute("refreshSection") == null) {
			model.addAttribute("refreshSection", refreshSection);
		}
		if(model.getAttribute("sortBy") == null) {
			model.addAttribute("sortBy", sortBy);
		}
		if(model.getAttribute("treeView") == null) {
			model.addAttribute("treeView", treeView);
		}
		model.addAttribute("firstTime", mediaGallerySessionBean.isFirstTime());
		
		if(sakaiProxy.isMyWorkspace()) {
			return doIndexMyWorkspace(refreshSection, sortBy, treeView, model, req, redirectAttributes);
		} else {
			return doIndexSite(refreshSection, sortBy, treeView, model, redirectAttributes);
		}
	}
	
	/**
	 * Called the first time by AJAX. Load all items by default into the model
	 * @param model
	 * @return
	 * @throws MicrosoftGenericException
	 */
	@RequestMapping(value = {"/items"}, method = RequestMethod.GET)
	public String loadItems(Model model) throws MicrosoftGenericException {
		
		String defaultRefreshSection = "";
		String defaultSortBy = "name:0";
		boolean defaultTreeView = false;
		
		model.addAttribute("refreshSection", defaultRefreshSection);
		model.addAttribute("sortBy", defaultSortBy);
		model.addAttribute("treeView", defaultTreeView);
		model.addAttribute("firstTime", false);

		if(sakaiProxy.isMyWorkspace()) {
			doIndexMyWorkspace(defaultRefreshSection, defaultSortBy, defaultTreeView, model, null, null);
		} else {
			doIndexSite(defaultRefreshSection, defaultSortBy, defaultTreeView, model, null);
		}
		return INDEX_TEMPLATE + " :: common-body";
	}
	
	//Main screen (Site Tool)
	private String doIndexSite(String refreshSection, String sortBy,  Boolean treeView, Model model, RedirectAttributes redirectAttributes) throws MicrosoftGenericException {
		//first time, loading process could require some time, just show a loading spinner and items will be loaded by AJAX
		if(mediaGallerySessionBean.isFirstTime()) {
			mediaGallerySessionBean.setFirstTime(false);
			return INDEX_TEMPLATE;
		}
		
		//when refreshing, call a redirect to clean all GET parameters from URL. Avoid "hard" refresh if user press F5
		if(StringUtils.isNotBlank(refreshSection)) {
			mediaGallerySessionBean.resetItems(refreshSection);
			microsoftCommonService.resetDriveItemsCache();
			microsoftCommonService.resetGroupDriveItemsCache(refreshSection);
			
			redirectAttributes.addFlashAttribute("refreshSection", refreshSection);
			redirectAttributes.addFlashAttribute("sortBy", sortBy);
			redirectAttributes.addFlashAttribute("treeView", treeView);
			mediaGallerySessionBean.setFirstTime(true);
			
			return REDIRECT_INDEX;
		}
		
		String reset = (String)mediaGallerySessionBean.getReset();
		Map<String, MicrosoftDriveItem> itemsMap = mediaGallerySessionBean.getItemsMap();
		if(itemsMap.isEmpty() || reset != null) {
			String siteId = sakaiProxy.getCurrentSiteId();
			List<SiteSynchronization> ssList = microsoftSynchronizationService.getSiteSynchronizationsBySite(siteId);
			for(SiteSynchronization ss : ssList) {
				if(reset == null || reset.equals(ss.getTeamId())) {
					//check if team exists
					MicrosoftTeam team = microsoftCommonService.getTeam(ss.getTeamId());
					
					if(team != null) {
						mediaGallerySessionBean.addType(team.getId(), team.getName());
						
						List<MicrosoftDriveItem> items = microsoftCommonService.getAllGroupDriveItems(team.getId(), filter);
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
		
		return INDEX_TEMPLATE;
	}
	
	//Main screen (MyWorkspace Tool)
	private String doIndexMyWorkspace(String refreshSection, String sortBy,  Boolean treeView, Model model, HttpServletRequest req, RedirectAttributes redirectAttributes) throws MicrosoftGenericException {
		
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
			
			//first time, loading process could require some time, just show a loading spinner and items will be loaded by AJAX
			if(mediaGallerySessionBean.isFirstTime()) {
				mediaGallerySessionBean.setFirstTime(false);
				return INDEX_WS_TEMPLATE;
			}
			
			//when refreshing, call a redirect to clean all GET parameters from URL. Avoid "hard" refresh if user press F5
			if(StringUtils.isNotBlank(refreshSection)) {
				mediaGallerySessionBean.resetItems(MediaGallerySessionBean.Type.valueOf(refreshSection));
				microsoftCommonService.resetDriveItemsCache();
				microsoftCommonService.resetUserDriveItemsCache(userId);

				redirectAttributes.addFlashAttribute("refreshSection", refreshSection);
				redirectAttributes.addFlashAttribute("sortBy", sortBy);
				redirectAttributes.addFlashAttribute("treeView", treeView);
				mediaGallerySessionBean.setFirstTime(true);
				
				return REDIRECT_INDEX;
			}
			
			MediaGallerySessionBean.Type reset = (MediaGallerySessionBean.Type)mediaGallerySessionBean.getReset();
			Map<String, MicrosoftDriveItem> itemsMap = mediaGallerySessionBean.getItemsMap();
			if(itemsMap.isEmpty() || reset != null) {
				//USER items
				if(reset == null || reset == MediaGallerySessionBean.Type.USER) {
					mediaGallerySessionBean.addType(MediaGallerySessionBean.Type.USER, rb.getString(MediaGallerySessionBean.Type.USER.name()));

					List<MicrosoftDriveItem> userItems = microsoftCommonService.getAllMyDriveItems(userId, filter);
					if(userItems != null && !userItems.isEmpty()) {
						mediaGallerySessionBean.getItemsByType().put(MediaGallerySessionBean.Type.USER, userItems);
						
						//iterate all items and add them to the session maps
						exploreAndDoSomething(userItems, (i) -> mediaGallerySessionBean.addItem(MediaGallerySessionBean.Type.USER, i));
					}
				}
				
				//SHARED items
				if(reset == null || reset == MediaGallerySessionBean.Type.SHARED) {
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
		return INDEX_WS_TEMPLATE;
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
