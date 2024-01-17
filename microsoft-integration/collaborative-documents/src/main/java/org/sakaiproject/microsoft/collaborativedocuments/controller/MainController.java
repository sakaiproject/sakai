/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.collaborativedocuments.controller;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;

import org.sakaiproject.microsoft.api.MicrosoftAuthorizationService;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.MicrosoftDriveItem;
import org.sakaiproject.microsoft.api.data.MicrosoftDriveItemFilter;
import org.sakaiproject.microsoft.api.data.MicrosoftRedirectURL;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.data.MicrosoftTeamWrapper;
import org.sakaiproject.microsoft.api.exceptions.AjaxException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftGenericException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftInvalidTokenException;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftNoCredentialsException;
import org.sakaiproject.microsoft.api.model.MicrosoftAccessToken;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.microsoft.collaborativedocuments.auxiliar.CollaborativeDocumentsSessionBean;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.ResourceLoader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
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
	public static final String SORT_BY_USER= "user";
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
	private MicrosoftConfigurationService microsoftConfigurationService;
	
	@Autowired
	private SakaiProxy sakaiProxy;
	
	@Autowired
	private CollaborativeDocumentsSessionBean collaborativeDocumentsSessionBean;
	
	private static final String INDEX_TEMPLATE = "index";
	private static final String BODY_TEMPLATE = "body";
	private static final String ITEM_TEMPLATE = "fragments/driveItems :: printObject";
	private static final String PERMISSIONS_TEMPLATE = "permissions";
	private static final String ERROR_TEMPLATE = "error :: error";
	private static final String REDIRECT_INDEX = "redirect:/index";
	private static final String REDIRECT_ERROR = "redirect:/error";
	
	private static final MicrosoftDriveItemFilter filter = MicrosoftDriveItemFilter.builder()
			.contentType(MicrosoftDriveItemFilter.MICROSOFT_DOCUMENT_TYPE)
			.build();
	
	private static final Detector DETECTOR = new DefaultDetector(MimeTypes.getDefaultMimeTypes());
	
	@ModelAttribute("locale")
	public Locale localeResolver(HttpServletRequest request, HttpServletResponse response) {
		Locale loc = sakaiProxy.getLocaleForCurrentUser();
		LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
		localeResolver.setLocale(request, response, loc);
		return loc;
	}
	
	@ModelAttribute("allowPermissions")
	public boolean allowPermissions() {
		return sakaiProxy.canUpdateSite(sakaiProxy.getSite(sakaiProxy.getCurrentSiteId()).getReference(), sakaiProxy.getCurrentUserId());
	}
	
	@ModelAttribute("allowCreateFiles")
	public boolean allowCreateFiles() {
		return sakaiProxy.isAdmin() || checkAccessToken() && sakaiProxy.checkPermissions(sakaiProxy.getCurrentUserId(), MicrosoftCommonService.PERM_CREATE_FILES, sakaiProxy.getSite(sakaiProxy.getCurrentSiteId()).getReference());
	}
	
	@ModelAttribute("allowCreateFolders")
	public boolean allowCreateFolders() {
		return sakaiProxy.isAdmin() || checkAccessToken() && sakaiProxy.checkPermissions(sakaiProxy.getCurrentUserId(), MicrosoftCommonService.PERM_CREATE_FOLDERS, sakaiProxy.getSite(sakaiProxy.getCurrentSiteId()).getReference());
	}
	
	@ModelAttribute("allowDeleteFiles")
	public boolean allowDeleteFiles() {
		return sakaiProxy.checkPermissions(sakaiProxy.getCurrentUserId(), MicrosoftCommonService.PERM_DELETE_FILES, sakaiProxy.getSite(sakaiProxy.getCurrentSiteId()).getReference());
	}
	
	@ModelAttribute("allowDeleteFolders")
	public boolean allowDeleteFolders() {
		return sakaiProxy.checkPermissions(sakaiProxy.getCurrentUserId(), MicrosoftCommonService.PERM_DELETE_FOLDERS, sakaiProxy.getSite(sakaiProxy.getCurrentSiteId()).getReference());
	}
	
	@ModelAttribute("allowUploadFiles")
	public boolean allowUploadFiles() {
		return sakaiProxy.isAdmin() || checkAccessToken() && sakaiProxy.checkPermissions(sakaiProxy.getCurrentUserId(), MicrosoftCommonService.PERM_UPLOAD_FILES, sakaiProxy.getSite(sakaiProxy.getCurrentSiteId()).getReference());
	}
	
	@ModelAttribute("maxUploadSize")
	//MB
	public long maxUploadSize() {
		return microsoftConfigurationService.getMaxUploadSize();
	}
	
	private boolean checkAccessToken() {
		String userId = sakaiProxy.getCurrentUserId();
		MicrosoftAccessToken mcAccessToken = microsoftAuthorizationService.getAccessToken(userId);
		
		return (mcAccessToken != null);
	}
	
	@ResponseStatus(value=HttpStatus.CONFLICT)
	@ExceptionHandler(AjaxException.class)
	@ResponseBody
	public Map<String, String> handleAjaxError(HttpServletRequest req, Exception ex) {
		log.debug("AJAX exception: {}", ex.getMessage());
		Map<String, String> ret = new HashMap<>();
		ret.put("body", rb.containsKey(ex.getMessage()) ? rb.getString(ex.getMessage()) : ex.getMessage());
		return ret;
	}
	
	@ExceptionHandler(MicrosoftGenericException.class)
	public String handleCredentialsError(HttpServletRequest req, Exception ex, RedirectAttributes redirectAttributes) {
		//store i18n exception message and redirect
		redirectAttributes.addFlashAttribute("exception_error", rb.getString(ex.getMessage()));

		return REDIRECT_ERROR;
	}
	
	@GetMapping(value = {"/error"})
	public String showError() {
		return ERROR_TEMPLATE;
	}

	@GetMapping(value = {"/", "/index"})
	public String showIndex(Model model, HttpServletRequest req) throws MicrosoftGenericException {
		model.addAttribute("sortBy", StringUtils.isNotBlank(collaborativeDocumentsSessionBean.getLastSortBy()) ? collaborativeDocumentsSessionBean.getLastSortBy() :  SORT_BY_NAME + SORT_ASCENDING);
		
		String userId = sakaiProxy.getCurrentUserId();
		String currentSiteRef = sakaiProxy.getSite(sakaiProxy.getCurrentSiteId()).getReference();

		//show login button if user can create/upload items and is not admin
		if(!sakaiProxy.isAdmin() && (sakaiProxy.checkPermissions(userId, MicrosoftCommonService.PERM_CREATE_FILES, currentSiteRef) ||
									sakaiProxy.checkPermissions(userId, MicrosoftCommonService.PERM_CREATE_FOLDERS, currentSiteRef) ||
									sakaiProxy.checkPermissions(userId, MicrosoftCommonService.PERM_UPLOAD_FILES, currentSiteRef))) 
		{
			model.addAttribute("showLogin", true);
			
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
		}
		
		return INDEX_TEMPLATE;
	}
	
	@GetMapping(value = {"/permissions"})
	public String showPermissions(Model model) throws MicrosoftGenericException {
		
		return PERMISSIONS_TEMPLATE;
	}
	
	/**
	 * Called by AJAX - returns FRAGMENT
	 * @param itemId
	 * @param model
	 * @return FRAGMENT
	 * @throws MicrosoftGenericException
	 */
	@GetMapping(value = {"/items"})
	public String loadItems(
			@RequestParam(required = false) String teamId,
			@RequestParam(required = false) String itemId,
			@RequestParam(defaultValue = "name:0") String sortBy,
			Model model
	) throws MicrosoftGenericException {
		
		model.addAttribute("sortBy", sortBy);
		collaborativeDocumentsSessionBean.setLastSortBy(sortBy);
		
		//first load
		if(teamId == null) {
			List<String> sortedTeamKeys = null;
			if(collaborativeDocumentsSessionBean.getItemsByTeam().isEmpty()) {
				//get teams linked with this site
				List<SiteSynchronization> ssList = microsoftSynchronizationService.getSiteSynchronizationsBySite(sakaiProxy.getCurrentSiteId());
				for(SiteSynchronization ss : ssList) {
					//check if team exists
					MicrosoftTeam team = microsoftCommonService.getTeam(ss.getTeamId());
					
					if(team != null) {
						collaborativeDocumentsSessionBean.getItemsByTeam().put(ss.getTeamId(), MicrosoftTeamWrapper.builder(team).build());
					}
				}
				//first Team will be expanded by default, so we need to load it
				sortedTeamKeys = collaborativeDocumentsSessionBean.getSortedTeamKeys();
				Optional<String> firstKey = sortedTeamKeys.stream().findFirst();
				if(firstKey.isPresent()) {
					MicrosoftTeamWrapper teamWrapper = collaborativeDocumentsSessionBean.getItemsByTeam().get(firstKey.get());
					loadTeam(teamWrapper, sortBy, model);
				}
			} else {
				sortedTeamKeys = collaborativeDocumentsSessionBean.getSortedTeamKeys();
			}
			
			model.addAttribute("teamKeys", sortedTeamKeys);
			model.addAttribute("itemsByTeam", collaborativeDocumentsSessionBean.getItemsByTeam());
			model.addAttribute("currentItem", collaborativeDocumentsSessionBean.getCurrentItem());
			model.addAttribute("currentTeam", collaborativeDocumentsSessionBean.getCurrentTeam());
			return BODY_TEMPLATE;
		} else {
			MicrosoftTeamWrapper teamWrapper = collaborativeDocumentsSessionBean.getItemsByTeam().get(teamId);
			
			//Team expanded/collapsed
			if(itemId == null) {
				if(teamWrapper != null) {
					loadTeam(teamWrapper, sortBy, model);

					return ITEM_TEMPLATE;
				}
			} else {
				//item expanded/collapsed
				MicrosoftDriveItem item = collaborativeDocumentsSessionBean.getItemsMap().get(itemId);

				if(item != null && item.isFolder() && item.hasChildren()) {
					List<MicrosoftDriveItem> children = item.getChildren();
					if(children == null) {						
						children = microsoftCommonService.getDriveItemsByItemId(item.getDriveId(), itemId, null)
								.stream()
								.filter(i -> filter.matches(i))
								.collect(Collectors.toList());

						//attach children to parent
						item.setChildren(children);

						//add children to map, so we can find them
						children.stream().forEach(i -> collaborativeDocumentsSessionBean.getItemsMap().put(i.getId(), i));
					}
					Comparator<MicrosoftDriveItem> comparator = new ItemsComparator(sortBy);
					Collections.sort(children, comparator);
				}
				collaborativeDocumentsSessionBean.setCurrentItem(item);
				collaborativeDocumentsSessionBean.setCurrentTeam(teamWrapper.getTeam());
				model.addAttribute("item", item);
				model.addAttribute("currentTeam", teamWrapper.getTeam());
				return ITEM_TEMPLATE;
			}
		}

		model.addAttribute("exception_error", rb.getString("error.items"));
		return ERROR_TEMPLATE;
	}
	
	@GetMapping(value = {"/refresh/{teamId}"})
	public String refreshTeam(
			@PathVariable String teamId,
			@RequestParam(defaultValue = "name:0") String sortBy,
			Model model
	) throws MicrosoftGenericException {
		
		MicrosoftTeamWrapper teamWrapper = collaborativeDocumentsSessionBean.getItemsByTeam().get(teamId);
		if(teamWrapper != null) {
			teamWrapper.clearItems();
			microsoftCommonService.resetGroupDriveItemsCache(teamId);
			microsoftCommonService.resetDriveItemsCache();
		}
		
		return loadItems(teamId, null, sortBy, model);
	}
	
	/**
	 * Called by AJAX - returns FRAGMENT
	 * @param name
	 * @param type
	 * @param teamId
	 * @param itemId
	 * @param model
	 * @return FRAGMENT
	 * @throws MicrosoftGenericException, AjaxException
	 */
	@PostMapping(value = {"/addItem"}, consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public String addItem(
			@RequestParam String name,
			@RequestParam MicrosoftDriveItem.TYPE type,
			@RequestParam(required = false) String teamId,
			@RequestParam(required = false) String itemId,
			Model model
	) throws MicrosoftGenericException {
		if((type == MicrosoftDriveItem.TYPE.FOLDER && !allowCreateFolders()) || (type != MicrosoftDriveItem.TYPE.FOLDER && !allowCreateFiles())) {
			throw new AjaxException("error.adding_item");
		}
		
		String userId = (!sakaiProxy.isAdmin()) ? sakaiProxy.getCurrentUserId() : null;
		
		MicrosoftDriveItem newItem = null;
		//added to root Team
		if(itemId == null) {
			MicrosoftTeamWrapper teamWrapper = collaborativeDocumentsSessionBean.getItemsByTeam().get(teamId);
			MicrosoftDriveItem item = microsoftCommonService.getDriveItemFromTeam(teamId);
			if(item != null && teamWrapper != null) {
				newItem = microsoftCommonService.createDriveItem(item, type, name, userId);
				teamWrapper.addItem(newItem);
				microsoftCommonService.resetGroupDriveItemsCache(teamId);
			}
		} else { //added to folder
			MicrosoftDriveItem item = collaborativeDocumentsSessionBean.getItemsMap().get(itemId);
			if(item != null) {
				newItem = microsoftCommonService.createDriveItem(item, type, name, userId);
				item.addChild(newItem);
				microsoftCommonService.resetDriveItemsCache();
			}
		}
		if(newItem == null) {
			throw new AjaxException("error.adding_item");
		}
		//add new item to map
		collaborativeDocumentsSessionBean.getItemsMap().put(newItem.getId(), newItem);
		
		return loadItems(teamId, itemId, collaborativeDocumentsSessionBean.getLastSortBy(), model);
	}
	
	/**
	 * Called by AJAX - returns FRAGMENT
	 * @param teamId
	 * @param itemId
	 * @param model
	 * @return FRAGMENT
	 * @throws MicrosoftGenericException, AjaxException
	 */
	@GetMapping(value = "/deleteItem")
	public String deleteItem(
			@RequestParam String teamId,
			@RequestParam String itemId,
			Model model
	) throws MicrosoftGenericException {
		
		MicrosoftDriveItem item = collaborativeDocumentsSessionBean.getItemsMap().get(itemId);
		
		if(item == null ||
		  (item.isFolder() && !allowDeleteFolders()) ||
		  (!item.isFolder() && !allowDeleteFiles()) ||
		  !microsoftCommonService.deleteDriveItem(item)
		) {
			throw new AjaxException("error.deleting_item");
		}
		
		collaborativeDocumentsSessionBean.getItemsMap().remove(itemId);
		
		if(item.getParent() == null) {
			MicrosoftTeamWrapper teamWrapper = collaborativeDocumentsSessionBean.getItemsByTeam().get(teamId);
			teamWrapper.clearItems();
			microsoftCommonService.resetGroupDriveItemsCache(teamId);
		} else {
			item.getParent().removeChild(item);
			microsoftCommonService.resetDriveItemsCache();
		}
		
		return loadItems(teamId, (item.getParent() != null) ? item.getParent().getId() : null, collaborativeDocumentsSessionBean.getLastSortBy(), model);
	}
	
	/**
	 * Called by AJAX - returns nothing
	 * @param file
	 * @param teamId
	 * @param itemId
	 * @throws MicrosoftGenericException, AjaxException
	 */
	@PostMapping(value = "/file-upload")
	public void uploadFile(
			@RequestParam("file") MultipartFile file,
			@RequestParam(required = false) String teamId,
			@RequestParam(required = false) String itemId,
			RedirectAttributes redirectAttributes
	) throws MicrosoftGenericException {
		
		
		if(!allowUploadFiles()) {
			throw new AjaxException("error.uploading.permission");
		}
		File f = null;
		
		//check size
		long maxUploadSize = maxUploadSize();
		if(maxUploadSize > 0 && file.getSize() > (maxUploadSize * 1024 * 1024)) { //MB to Bytes
			throw new AjaxException("error.uploading.size");
		}
		
		//check content type (given by extension)
		String contentType = file.getContentType();
		
		if(!contentType.toLowerCase().startsWith(MicrosoftDriveItemFilter.MICROSOFT_DOCUMENT_TYPE.toLowerCase())) {
			throw new AjaxException("error.uploading.content_type");
		}
		
		//check TIKA content type (given by content)
		try (
				TikaInputStream buff = TikaInputStream.get(file.getInputStream());
		) {
			Metadata metadata = new Metadata();
			
			contentType = DETECTOR.detect(buff, metadata).toString();
		} catch (Exception e) {
			throw new AjaxException("error.uploading.tika_process");
		}
		if(!contentType.toLowerCase().startsWith(MicrosoftDriveItemFilter.MICROSOFT_DOCUMENT_TYPE.toLowerCase())) {
			throw new AjaxException("error.uploading.tika_content_type");
		}
		
		try {
			String filename = StringEscapeUtils.escapeHtml4(file.getOriginalFilename());
			f = File.createTempFile(filename, null);
			file.transferTo(f);
			
			String userId = (!sakaiProxy.isAdmin()) ? sakaiProxy.getCurrentUserId() : null;
			
			MicrosoftDriveItem newItem = null;
			//added to root Team
			if(itemId == null) {
				MicrosoftTeamWrapper teamWrapper = collaborativeDocumentsSessionBean.getItemsByTeam().get(teamId);
				MicrosoftDriveItem item = microsoftCommonService.getDriveItemFromTeam(teamId);
				if(item != null && teamWrapper != null) {
					newItem = microsoftCommonService.uploadDriveItem(item, f, filename, userId);
					teamWrapper.addItem(newItem);
					microsoftCommonService.resetGroupDriveItemsCache(teamId);
				}
			} else { //added to folder
				MicrosoftDriveItem item = collaborativeDocumentsSessionBean.getItemsMap().get(itemId);
				if(item != null) {
					newItem = microsoftCommonService.uploadDriveItem(item, f, filename, userId);
					item.addChild(newItem);
					microsoftCommonService.resetDriveItemsCache();
				}
			}
			if(newItem == null) {
				throw new AjaxException(MessageFormat.format(rb.getString("error.uploading_item"), filename));
			}
			//add new item to map
			collaborativeDocumentsSessionBean.getItemsMap().put(newItem.getId(), newItem);

		}  catch (IOException ioe) {
			throw new AjaxException("error.uploading.file_exception");
		} finally {
			if(f != null) {
				f.delete();
			}
		}
	}
	
	private void loadTeam(MicrosoftTeamWrapper teamWrapper, String sortBy, Model model) throws MicrosoftCredentialsException {
		List<MicrosoftDriveItem> items = teamWrapper.getItems();
		if(items == null) {
			String currentSiteId = sakaiProxy.getCurrentSiteId();
			Site currentSite = sakaiProxy.getSite(currentSiteId);
			String currentUserId = sakaiProxy.getCurrentUserId();
			//get Site-Team relationship
			SiteSynchronization ss = microsoftSynchronizationService.getSiteSynchronization(SiteSynchronization.builder().siteId(currentSiteId).teamId(teamWrapper.getTeam().getId()).build());
			if(ss != null) {
				//teacher -> get all elements (including all private channels)
				List<String> channelIds = null;
				//student -> filter elements based on group
				if(!sakaiProxy.checkPermissions(currentUserId, MicrosoftCommonService.PERM_VIEW_ALL_CHANNELS, currentSite.getReference())) {
					channelIds = new ArrayList<>();
					
					if(currentSite.hasGroups() && ss.getGroupSynchronizationsList() != null && !ss.getGroupSynchronizationsList().isEmpty()) {
						//get all groups user pertains to
						Set<String> pertainsTo = currentSite.getGroupsWithMember(currentUserId).stream().map(g -> g.getId()).collect(Collectors.toSet());
						//get Microsoft channels related to these groups
						channelIds = ss.getGroupSynchronizationsList().stream()
							.filter(gs -> pertainsTo.contains(gs.getGroupId()))
							.map(gs -> gs.getChannelId())
							.collect(Collectors.toList());
					}
				}
				
				items = microsoftCommonService.getGroupDriveItems(teamWrapper.getTeam().getId(), channelIds)
						.stream()
						.filter(i -> filter.matches(i))
						.collect(Collectors.toList());
				
				teamWrapper.setItems(items);
				
				//add to items map
				items.stream().forEach(i -> collaborativeDocumentsSessionBean.getItemsMap().put(i.getId(), i));
			}
		}
		Comparator<MicrosoftDriveItem> comparator = new ItemsComparator(sortBy);
		Collections.sort(items, comparator);
		
		collaborativeDocumentsSessionBean.setCurrentItem(teamWrapper);
		collaborativeDocumentsSessionBean.setCurrentTeam(teamWrapper.getTeam());
		model.addAttribute("item", teamWrapper);
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
				MicrosoftDriveItem aux_a = (sortBy.endsWith(SORT_DESCENDING)) ? b : a;
				MicrosoftDriveItem aux_b = (sortBy.endsWith(SORT_DESCENDING)) ? a : b;
				
				if(sortBy.startsWith(SORT_BY_NAME)) {
					if(aux_a.getName() == null) { return 1; }
					if(aux_b.getName() == null) { return -1; }
					return aux_a.getName().compareToIgnoreCase(aux_b.getName());
				}
				if(sortBy.startsWith(SORT_BY_DATE)) {
					if(aux_a.getModifiedAt() == null) { return 1; }
					if(aux_b.getModifiedAt() == null) { return -1; }
					return aux_a.getModifiedAt().compareTo(aux_b.getModifiedAt());
				}
				if(sortBy.startsWith(SORT_BY_USER)) {
					if(aux_a.getModifiedBy() == null) { return 1; }
					if(aux_b.getModifiedBy() == null) { return -1; }
					return aux_a.getModifiedBy().compareToIgnoreCase(aux_b.getModifiedBy());
				}
			}
			if(a.isFolder()) {
				return -1;
			}
			return 1;
		}
	}
}
