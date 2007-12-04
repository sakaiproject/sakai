package org.sakaiproject.scorm.ui.console.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormClientFacade;

public class DataManagerListPage extends ConsoleBasePage {

	
	@SpringBean
	ScormClientFacade clientFacade;
	
	public DataManagerListPage(PageParameters params) {
		
		// TODO: Build a simple data grid abstraction that displays lists
		
		/*
		
		
		List<ContentEntity> contentPackages = clientFacade.getContentPackages();
		List<ContentEntityWrapper> contentPackageWrappers = new LinkedList<ContentEntityWrapper>();
		
		try {
		for (ContentEntity ce : contentPackages) {
			contentPackageWrappers.add(new ContentEntityWrapper(ce));
		}
		} catch (Exception e) {
			log.error("Caught an exception retrieving Content Packages from content service", e);
		}
		
		add(new Label("page-title", "Content Packages"));
		
		ListView rows = new ListView("rows", contentPackageWrappers) {
			private static final long serialVersionUID = 1L;
			
		 	public void populateItem(final ListItem item) {
		 		final ContentEntityWrapper resource = (ContentEntityWrapper)item.getModelObject();
		 		
		 		String id = resource.getId();
		 		String[] parts = id.split("/");
		 		
		 		if (id.endsWith("/"))
		 			id = id.substring(0, id.length() - 1);
		 		
		 		final String resourceId = id;
		 		final String fileName = parts[parts.length - 1];
		 		final PageParameters params = new PageParameters();
		 		params.add("contentPackage", resourceId);
	 		
		 		if (null != parts && parts.length > 0) {
		 			String title = resource.getTitle();
		 			
		 			if (title == null)
		 				title = fileName;
		 			
		 			item.add(new Label("packageName", title));
		 			
		 					 			
		 			BookmarkablePageLink launchLink = new BookmarkablePageLink("launch", View.class, params);
	 				launchLink.setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE | 
	 						PopupSettings.SCROLLBARS).setWindowName("SCORMPlayer"));
		 			
		 			item.add(launchLink);
		 			
		 			Image launchIcon = new Image("launchIcon")
		 			{
		 				private static final long serialVersionUID = 1L;

		 				protected ResourceReference getImageResourceReference()
		 				{
		 					return launchIconReference;
		 				}
		 			};
		 			
		 			launchLink.add(launchIcon);
		 			
		 			
		 			BookmarkablePageLink summaryPageLink = new BookmarkablePageLink("summary", SummaryPage.class, params);

		 			item.add(summaryPageLink);
		 			
		 			
		 			Image reportIcon = new Image("reportIcon")
		 			{
		 				private static final long serialVersionUID = 1L;

		 				protected ResourceReference getImageResourceReference()
		 				{
		 					return reportIconReference;
		 				}
		 			};
		 			
		 			summaryPageLink.add(reportIcon);
		 			
		 			
		 			AjaxFallbackLink deleteLink = new AjaxFallbackLink("delete") {

						private static final long serialVersionUID = 1L;

						@Override
						public void onClick(AjaxRequestTarget target) {
							String isConfirmed = PackageListPage.this.getRequest().getParameter("isConfirmed");
							
							if (isConfirmed == null || isConfirmed.length() == 0) {
								List<IBehavior> behaviors = this.getBehaviors(AjaxEventBehavior.class);
								
								String url = "";
								
								if (behaviors != null && behaviors.size() > 0)
									url = ((AjaxEventBehavior)behaviors.get(0)).getCallbackUrl(false).toString();
								
								target.appendJavascript("ScormManageFunctions.confirmDelete('" + url + "&isConfirmed=true')");
								
							} else {
								clientFacade.removeContentPackage(resourceId); 
								//target.addComponent(rows);
							}
						}
		 				
		 			};
		 			
		 			item.add(deleteLink);
		 			
		 			Image deleteIcon = new Image("deleteIcon")
		 			{
		 				private static final long serialVersionUID = 1L;

		 				protected ResourceReference getImageResourceReference()
		 				{
		 					return deleteIconReference;
		 				}
		 			};
		 			
		 			deleteLink.add(deleteIcon);
		 		}
		 	}
		};
		
		add(rows);
		*/
	}
	
	
}
