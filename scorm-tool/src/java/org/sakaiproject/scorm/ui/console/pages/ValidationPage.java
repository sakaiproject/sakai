package org.sakaiproject.scorm.ui.console.pages;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.scorm.service.api.ScormContentService;

public class ValidationPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(ValidationPage.class);
	
	private static final ResourceReference validateIconReference = new ResourceReference(ValidationPage.class, "res/add.png");

	
	@SpringBean
	ScormContentService contentService;
	
	public ValidationPage(PageParameters params) {
		List<ContentResource> zipFiles = contentService.getZipArchives();
		List<ZipFileWrapper> contentPackageWrappers = new LinkedList<ZipFileWrapper>();
		
		try {
		for (ContentResource resource : zipFiles) {
			contentPackageWrappers.add(new ZipFileWrapper(resource));
		}
		} catch (Exception e) {
			log.error("Caught an exception retrieving Content Packages from content service", e);
		}
		

		ListView rows = new ListView("rows", contentPackageWrappers) {
			private static final long serialVersionUID = 1L;
			
		 	public void populateItem(final ListItem item) {
		 		final ZipFileWrapper resource = (ZipFileWrapper)item.getModelObject();
		 		
		 		final String id = resource.getId();
		 		String[] parts = id.split("/");
		 		
		 		final String fileName = parts[parts.length - 1];
		 		final PageParameters params = new PageParameters();
		 		params.add("resourceId", resource.getId());
	 		
		 		if (null != parts && parts.length > 0) {
		 			String title = resource.getTitle();
		 			
		 			if (title == null)
		 				title = fileName;
		 			
		 			item.add(new Label("fileName", title));
		 			
		 					 			
		 			AjaxLink validateLink = new AjaxLink("validate") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							try {
								contentService.validate(id, false, true);
							} catch (Exception e) {
								log.error("Caught an exception validating ", e);
							}
						}
		 				
		 			};

		 			item.add(validateLink);
		 			
		 			Image validateIcon = new Image("validateIcon")
		 			{
		 				private static final long serialVersionUID = 1L;

		 				protected ResourceReference getImageResourceReference()
		 				{
		 					return validateIconReference;
		 				}
		 			};
		 			
		 			validateLink.add(validateIcon);
		 			
		 			
		 		}
		 	}
		};
		
		add(rows);
	}
	
	
	public class ZipFileWrapper implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private String id;
		private String url;
		private String title;
		
		public ZipFileWrapper(ContentResource resource) {
			this.id = resource.getId();
			this.url = resource.getUrl();
			
			ResourceProperties props = resource.getProperties();
			
			if (props != null && props.getProperty(ResourceProperties.PROP_DISPLAY_NAME) != null) 
				this.title = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		}
		
		public String getId() {
			return id;
		}
		
		public String getUrl() {
			return url;
		}
		
		public String getTitle() {
			return title;
		}
		
	}
}
