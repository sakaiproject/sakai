package org.sakaiproject.sitestats.tool.wicket.components;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.request.target.basic.EmptyRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.models.CHResourceModel;

/**
 * @author Nuno Fernandes
 */



public class FileSelectorPanel extends Panel {
	private static final long			serialVersionUID	= 1L;
	private static final String			BASE_DIR			= "/group/";

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade		facade;

	private String						siteId;
	private String						siteTitle;
	private String						selectedFiles		= "";
	private String						browsingState		= null;
	private String						currentDir			= BASE_DIR;

	private final AjaxResourcesLoader	ajaxResourcesLoader		= new AjaxResourcesLoader();

	public FileSelectorPanel(String id, String siteId) {
		this(id, siteId, null);
	}
	
	public FileSelectorPanel(String id, String siteId, IModel model) {
		super(id, model);
		this.siteId = siteId;
		try{
			this.siteTitle = facade.getSiteService().getSite(siteId).getTitle();
		}catch(IdUnusedException e){
			this.siteTitle = siteId;
		}
		
		// selected files
		HiddenField selectedFiles = new HiddenField("selectedFiles", new PropertyModel(this, "selectedFiles"));
		add(selectedFiles);
		
		// (previous) browsing state
		HiddenField browsingState = new HiddenField("browsingState", new PropertyModel(this, "browsingState"));
		add(browsingState);
		
		// hover div (for disabling control)
		WebMarkupContainer containerHover = new WebMarkupContainer("containerHover");
		if(isEnabled()) {
			containerHover.add(new AttributeModifier("style", true, new Model("display: block")));
		}else{
			containerHover.add(new AttributeModifier("style", true, new Model("display: none")));
		}
		add(containerHover);
		
		add(ajaxResourcesLoader);
	}
	
	public List<String> getSelectedFilesId() {
		List<String> filesId = new ArrayList<String>();
		if(selectedFiles != null) {
			String[] t = selectedFiles.split("\\|\\|\\|");
			for(int i=0; i<t.length; i++) {
				filesId.add(t[i]);
			}
		}
		return filesId;
	}
	
	public String getSelectedFiles() {
		return selectedFiles;
	}
	
	public void setSelectedFiles(String selectedFiles) {
		this.selectedFiles = selectedFiles;
		setModelObject(getSelectedFilesId());
	}
	
	public String getBrowsingState() {
		if(browsingState != null) {
			List<String> selectedIds = getSelectedFilesId();
			for(String id : selectedIds) {
				browsingState = browsingState.replaceAll(id, id + "\" checked=\"true\"");
			}
		}
		return browsingState;
	}
	
	public void setBrowsingState(String browsingState) {
		this.browsingState = browsingState;
	}
	
	public void setSelectedFilesId(List<String> selectedFiles) {
		this.selectedFiles = null;
		for(String s : selectedFiles) {
			if(this.selectedFiles == null) {
				this.selectedFiles = s;
			}else{
				this.selectedFiles += "|||" + s;
			}
		}
		setModelObject(getSelectedFilesId());
	}
	
	private boolean isSelected(String resourceId) {
		return getSelectedFilesId().contains(resourceId);
	}

	@Override
	public void renderHead(HtmlHeaderContainer container) {
		container.getHeaderResponse().renderJavascriptReference("/library/js/jquery.js");
		container.getHeaderResponse().renderJavascriptReference("/sakai-sitestats-tool/html/components/jqueryFileTree/jqueryFileTree.js");
		container.getHeaderResponse().renderCSSReference("/sakai-sitestats-tool/html/components/jqueryFileTree/jqueryFileTree.css");
		StringBuilder onDomReady = new StringBuilder();
		onDomReady.append("jQuery('#container').fileTree(");
		onDomReady.append("  {root: '");
		onDomReady.append(BASE_DIR);
		onDomReady.append("', script: '");
		onDomReady.append(ajaxResourcesLoader.getCallbackUrl());
		onDomReady.append("', duration: 100},");
		onDomReady.append("  function(file) {return false;}");
		onDomReady.append(");");
		onDomReady.append("jQuery('.generateReport').click( function(){saveBrowsingState('.browsingState');return true;} );");
		container.getHeaderResponse().renderOnDomReadyJavascript(onDomReady.toString());
		super.renderHead(container);
	}
	
	private List<CHResourceModel> getResources(String dir) throws IdUnusedException, TypeException, PermissionException {
		List<CHResourceModel> resourcesList = new ArrayList<CHResourceModel>();
		String siteCollectionId = facade.getContentHostingService().getSiteCollection(siteId);
		if(dir.equals("/group/")) {
			dir = siteCollectionId; 
			resourcesList.add(new CHResourceModel(siteCollectionId, siteTitle, true));
		}else{
			ContentCollection collection = facade.getContentHostingService().getCollection(dir);
			if(collection != null) {
				List<ContentEntity> members = collection.getMemberResources();
				for(ContentEntity ce : members) {
					String dispName = facade.getStatsManager().getResourceName("/content"+ce.getId());
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
				currentDir = req.getParameter("dir");
				String enc = "UTF-8";
				RequestCycle.get().setRequestTarget(EmptyRequestTarget.getInstance());
				WebResponse response = (WebResponse) getResponse();
				response.setContentType("text/html;charset="+enc);
				OutputStream out = getResponse().getOutputStream();
				try{
					out.write("<ul class=\"jqueryFileTree\" style=\"display: none;\">".getBytes(enc));
					String brsState = getBrowsingState();
					if(currentDir.equals("/group/") && brsState != null) {
						out.write(getBrowsingState().getBytes(enc));
						setBrowsingState(null);
					}else{
						List<CHResourceModel> list = getResources(currentDir);
						if(list !=  null) {
							for(CHResourceModel rm : list) {
								if(rm.isCollection()) {
									String collectionMarkup = "  <li class=\"directory collapsed\"><a href=\"#\" rel=\""+rm.getResourceId()+"\">"+rm.getResourceNameEscaped()+"</a></li>";
									out.write(collectionMarkup.getBytes(enc));
								}else{
									String markup1 = "  <li class=\"file ext_"+rm.getResourceExtension()+"\">";
									String markup2 = "    <input type=\"checkbox\" value=\""+rm.getResourceId()+"\" "+ (isSelected(rm.getResourceId()) ? "checked=\"checked\"" : "") +"onchange=\"updateFieldWithSelectedFiles('.selectedFiles')\"/>";
									String markup3 = "    <span>"+rm.getResourceNameEscaped()+"</span>";
									String markup4 = "  </li>";
									out.write(markup1.getBytes(enc));
									out.write(markup2.getBytes(enc));
									out.write(markup3.getBytes(enc));
									out.write(markup4.getBytes(enc));
								}
							}
						}
					}
					out.write("</ul>".getBytes(enc));
				}finally{
					out.close();
				}
			}catch(Exception e){
				// ignore - do nothing
			}
		}
	}

}

