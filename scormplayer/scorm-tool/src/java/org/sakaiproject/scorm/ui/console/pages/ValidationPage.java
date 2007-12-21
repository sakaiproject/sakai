package org.sakaiproject.scorm.ui.console.pages;

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
import org.sakaiproject.scorm.model.api.UnvalidatedResource;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;

public class ValidationPage extends ConsoleBasePage {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(ValidationPage.class);
	
	private static final ResourceReference validateIconReference = new ResourceReference(ValidationPage.class, "res/add.png");

	@SpringBean
	ScormContentService contentService;
	@SpringBean
	ScormResourceService resourceService;
	
	
	public ValidationPage(PageParameters params) {
		List<UnvalidatedResource> resources = resourceService.getUnvalidatedResources();

		ListView rows = new ListView("rows", resources) {
			private static final long serialVersionUID = 1L;
			
		 	public void populateItem(final ListItem item) {
		 		final UnvalidatedResource resource = (UnvalidatedResource)item.getModelObject();
		 		
		 		final String id = resource.getResourceId();
		 		String[] parts = id.split("/");
		 		
		 		final String fileName = parts[parts.length - 1];
		 		final PageParameters params = new PageParameters();
		 		params.add("resourceId", resource.getResourceId());
	 		
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
	
}
