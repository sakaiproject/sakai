/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.components;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.handler.EmptyRequestHandler;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.models.CHResourceModel;
import org.sakaiproject.sitestats.tool.wicket.pages.BasePage;

/**
 * @author Nuno Fernandes
 */



public class FileSelectorPanel extends Panel {
	private static final long			serialVersionUID	= 1L;
	private static final String			BASE_DIR			= "/";

	private String						siteId;
	private boolean						showDefaultBaseFoldersOnly;
	private String						currentDir			= BASE_DIR;

	private final AjaxResourcesLoader	ajaxResourcesLoader		= new AjaxResourcesLoader();

	public FileSelectorPanel(String id, String siteId, boolean showDefaultBaseFoldersOnly) {
		this(id, siteId, null, showDefaultBaseFoldersOnly);
	}
	
	public FileSelectorPanel(String id, String siteId, IModel model, boolean showDefaultBaseFoldersOnly) {
		super(id, model);
		this.siteId = siteId;
		this.showDefaultBaseFoldersOnly = showDefaultBaseFoldersOnly;
		
		// selected files
		HiddenField selectedFiles = new HiddenField("selectedFiles", new PropertyModel(this, "selectedFiles"));
		add(selectedFiles);
		
		// hover div (for disabling control)
		WebMarkupContainer containerHover = new WebMarkupContainer("containerHover");
		if(isEnabled()) {
			containerHover.add(new AttributeModifier("style", new Model("display: block")));
		}else{
			containerHover.add(new AttributeModifier("style", new Model("display: none")));
		}
		add(containerHover);
		
		add(ajaxResourcesLoader);
	}
	
	public List<String> getSelectedFilesId() {
		List<String> files = (List<String>) getDefaultModelObject();
		if(!showDefaultBaseFoldersOnly) {
			List<String> files2 = new ArrayList<String>();
			for(String f : files) {
				if(StatsManager.RESOURCES_DIR.equals(f) || StatsManager.DROPBOX_DIR.equals(f) || StatsManager.ATTACHMENTS_DIR.equals(f)){
					files2.add(f + siteId + "/");
				}else{
					files2.add(f);
				}
			}
			return files2;
		}
		return files;
	}
	
	public void setSelectedFilesId(List<String> files) {
		setDefaultModelObject(files);
	}
	
	public String getSelectedFiles() {
		List<String> files = (List<String>) getDefaultModelObject();
		StringBuilder filesEncoded = new StringBuilder();
		for(String s : files) {
			if(filesEncoded.length() != 0) {
				filesEncoded.append("|||");
			}
			if(!showDefaultBaseFoldersOnly && (StatsManager.RESOURCES_DIR.equals(s) || StatsManager.DROPBOX_DIR.equals(s) || StatsManager.ATTACHMENTS_DIR.equals(s))){
				filesEncoded.append(s).append(siteId).append("/");
			}else{
				filesEncoded.append(s);
			}
		}
		return filesEncoded.toString();
	}
	
	public void setSelectedFiles(String filesEncoded) {
		List<String> files = new ArrayList<String>();
		if(filesEncoded != null) {
			String[] t = filesEncoded.split("\\|\\|\\|");
			for(int i=0; i<t.length; i++) {
				files.add(t[i]);
			}
		}
		setDefaultModelObject(files);
	}
	
	private boolean isSelected(String resourceId) {
		return getSelectedFilesId().contains(resourceId);
	}
	
	private boolean isFolderPartOfSelectedFiles(String collectionId) {
		for(String id : getSelectedFilesId()) {
			if(id.startsWith(collectionId)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void renderHead(HtmlHeaderContainer container) {
		container.getHeaderResponse().render(JavaScriptHeaderItem.forUrl(BasePage.JQUERYSCRIPT));
		container.getHeaderResponse().render(JavaScriptHeaderItem.forUrl(StatsManager.SITESTATS_WEBAPP+"/html/components/jqueryFileTree/jqueryFileTree.js"));
		container.getHeaderResponse().render(CssHeaderItem.forUrl(StatsManager.SITESTATS_WEBAPP+"/html/components/jqueryFileTree/jqueryFileTree.css"));
		StringBuilder onDomReady = new StringBuilder();
		onDomReady.append("jQuery('#sitestats-containerInner').fileTree(");
		onDomReady.append("  {root: '");
		onDomReady.append(BASE_DIR);
		onDomReady.append("', script: '");
		onDomReady.append(ajaxResourcesLoader.getCallbackUrl());
		onDomReady.append("', duration: 100},");
		onDomReady.append("  function(file) {return false;}");
		onDomReady.append(");");
		container.getHeaderResponse().render(OnDomReadyHeaderItem.forScript(onDomReady.toString()));
		super.renderHead(container);
	}
	
	private List<CHResourceModel> getResources(String dir) throws IdUnusedException, TypeException, PermissionException {
		List<CHResourceModel> resourcesList = new ArrayList<CHResourceModel>();
		String resourcesCollectionId = null;
		String dropboxCollectionId = null;
		String attachmentsCollectionId = null;
		if(!showDefaultBaseFoldersOnly) {
			resourcesCollectionId = Locator.getFacade().getContentHostingService().getSiteCollection(siteId);
			dropboxCollectionId = Locator.getFacade().getContentHostingService().getDropboxCollection(siteId);
			attachmentsCollectionId = resourcesCollectionId.replaceFirst(StatsManager.RESOURCES_DIR, StatsManager.ATTACHMENTS_DIR);
		}else{
			resourcesCollectionId = StatsManager.RESOURCES_DIR;
			dropboxCollectionId = StatsManager.DROPBOX_DIR;
			attachmentsCollectionId = StatsManager.ATTACHMENTS_DIR;
		}
		if(dir.equals(BASE_DIR)) {
			resourcesList.add(new CHResourceModel(resourcesCollectionId, Locator.getFacade().getToolManager().getTool(StatsManager.RESOURCES_TOOLID).getTitle()/*(String) new ResourceModel("report_content_resources").getObject()*/, true));
			resourcesList.add(new CHResourceModel(dropboxCollectionId, Locator.getFacade().getToolManager().getTool(StatsManager.DROPBOX_TOOLID).getTitle()/*(String) new ResourceModel("report_content_dropbox").getObject()*/, true));
			resourcesList.add(new CHResourceModel(attachmentsCollectionId, (String) new ResourceModel("report_content_attachments").getObject(), true));
		}else if(!showDefaultBaseFoldersOnly) {
			ContentCollection collection = Locator.getFacade().getContentHostingService().getCollection(dir);
			if(collection != null) {
				List<ContentEntity> members = collection.getMemberResources();
				for(ContentEntity ce : members) {
					String dispName = Locator.getFacade().getStatsManager().getResourceName("/content"+ce.getId(), false);
					resourcesList.add(new CHResourceModel(ce.getId(), dispName, ce.isCollection()));
				}
			}
		}
		return resourcesList;
	}

	/**
	 * Ajax behavior for lazy loading CHS resources
	 * @author Nuno Fernandes
	 */
	private class AjaxResourcesLoader extends AbstractDefaultAjaxBehavior {
		private static final long	serialVersionUID	= 1L;

		@Override
		protected void respond(AjaxRequestTarget target) {
			// get dir
	    	Request req = RequestCycle.get().getRequest();
			try{
				currentDir = req.getRequestParameters().getParameterValue("dir").toString();
				String enc = "UTF-8";
				RequestCycle.get().scheduleRequestHandlerAfterCurrent(new EmptyRequestHandler());
				WebResponse response = (WebResponse) getResponse();
				response.setContentType("text/html;charset="+enc);
				OutputStream out = getResponse().getOutputStream();
				try{
					out.write("<ul class=\"jqueryFileTree\" style=\"display: none;\">".getBytes(enc));
					boolean expandToSelection = currentDir.equals(BASE_DIR) && getSelectedFilesId() != null && getSelectedFilesId().size() > 0;
					getResourcesMarkup(currentDir, out, expandToSelection, enc);					
					out.write("</ul>".getBytes(enc));
				}finally{
					out.close();
				}
			}catch(RuntimeException e){
				// ignore - do nothing
			}catch(Exception e){
				// ignore - do nothing
			}
		}

		private void getResourcesMarkup(String folder, OutputStream out, boolean expandToSelection, String encoding) throws IdUnusedException, TypeException, PermissionException, IOException, UnsupportedEncodingException {
			List<CHResourceModel> list = getResources(folder);
			if(list !=  null) {
				for(CHResourceModel rm : list) {
					if(rm.isCollection()) {
						if(!expandToSelection 
							|| (expandToSelection && !isFolderPartOfSelectedFiles(rm.getResourceId()))								
						) {
							StringBuilder collectionMarkup = new StringBuilder();
							collectionMarkup.append("  <li class=\"directory collapsed\">");
							collectionMarkup.append("    <input type=\"checkbox\" value=\"").append(rm.getResourceId()).append("\" ").append(isSelected(rm.getResourceId()) ? "checked=\"checked\"" : "").append("onchange=\"updateFieldWithSelectedFiles('.selectedFiles')\"/>");
							collectionMarkup.append("    <a href=\"#\" rel=\"").append(rm.getResourceId()).append("\">").append(rm.getResourceNameEscaped()).append("</a>");
							collectionMarkup.append("  </li>");
							out.write(collectionMarkup.toString().getBytes(encoding));
						}else{
							StringBuilder collectionMarkup = new StringBuilder();
							collectionMarkup.append("  <li class=\"directory expanded\"  style=\"position: static;\">");
							collectionMarkup.append("    <input type=\"checkbox\" value=\"").append(rm.getResourceId()).append("\" ").append(isSelected(rm.getResourceId()) ? "checked=\"checked\"" : "").append("onchange=\"updateFieldWithSelectedFiles('.selectedFiles')\"/>");
							collectionMarkup.append("    <a href=\"#\" rel=\"").append(rm.getResourceId()).append("\">").append(rm.getResourceNameEscaped()).append("</a>");
							collectionMarkup.append("    <ul style=\"display: block;\" class=\"jqueryFileTree\">");
							out.write(collectionMarkup.toString().getBytes(encoding));
							
							// get contents recursively
							collectionMarkup = new StringBuilder();
							
							getResourcesMarkup(rm.getResourceId(), out, expandToSelection, encoding);
							collectionMarkup.append("    </ul>");
							collectionMarkup.append("  </li>");
							out.write(collectionMarkup.toString().getBytes(encoding));
						}
					}else{
						StringBuilder markup = new StringBuilder();
						markup.append("  <li class=\"file ext_").append(rm.getResourceExtension()).append("\">");
						markup.append("    <input type=\"checkbox\" value=\"").append(rm.getResourceId()).append("\" ").append(isSelected(rm.getResourceId()) ? "checked=\"checked\"" : "").append("onchange=\"updateFieldWithSelectedFiles('.selectedFiles')\"/>");
						markup.append("    <span>").append(rm.getResourceNameEscaped()).append("</span>");
						markup.append("  </li>");
						out.write(markup.toString().getBytes(encoding));
					}
				}
			}
		}
	}

}

