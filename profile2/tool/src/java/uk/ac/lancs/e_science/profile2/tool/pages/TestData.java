package uk.ac.lancs.e_science.profile2.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.tool.pages.panels.views.TestPanel;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.views.TestPanel2;
import uk.ac.lancs.e_science.profile2.tool.pages.panels.views.TestPanelFullReplace1;


public class TestData extends BasePage {

	private transient Logger log = Logger.getLogger(TestData.class);
	private String text;
	FileUploadField uploadField;
	
	public TestData() {
		
		if(log.isDebugEnabled()) log.debug("TestData()");
		
		//heading
		add(new Label("testDataHeading", new ResourceModel("heading.test")));
		
		//print sample data
		String userId = sakaiProxy.getCurrentUserId();
		boolean isAdmin = sakaiProxy.isUserAdmin(userId);
		
		add(new Label("testLabel1",sakaiProxy.getCurrentSiteId()));
		add(new Label("testLabel2",userId));
		add(new Label("testLabel3",sakaiProxy.getUserEid(userId)));
		add(new Label("testLabel4",sakaiProxy.getUserDisplayName(userId)));
		add(new Label("testLabel5",sakaiProxy.getUserEmail(userId)));
		String isAdminStr = "false";
		if(isAdmin) {
			isAdminStr = "true";
		}
		add(new Label("testLabel6",isAdminStr));
		
		//normal replace
		final TestPanel replace = new TestPanel("replace", "this is the default message");
		add(replace);
		add(new Link("replaceLinkOld") {
			public void onClick() {
				replace.replaceWith(new TestPanel("replace", "different message"));
			}
		});
		
		//ajax replace
		final TestPanel replaceAjax = new TestPanel("replaceAjax", "default message");
		replaceAjax.setOutputMarkupId(true);
		add(replaceAjax);
		add(new AjaxFallbackLink("replaceLink") {
			public void onClick(AjaxRequestTarget target) {
				Component testPanel2 = new TestPanel2("replaceAjax", "replaced message");
				testPanel2.setOutputMarkupId(true);
				replaceAjax.replaceWith(testPanel2);
				if(target != null) {
					target.addComponent(testPanel2);
				}
			}
		});
		
		//full replace - just adds the default panel in
		final TestPanelFullReplace1 testPanelFullReplace1 = new TestPanelFullReplace1("fullReplace");
		testPanelFullReplace1.setOutputMarkupId(true);
		add(testPanelFullReplace1);
		
		
		//form test
		Form form = new Form("form") {
			public void onSubmit() {
				
				System.out.println("submitted: " + getText());

				FileUpload upload = uploadField.getFileUpload();
				if (upload == null) {
				     // No image was provided
				     System.out.println("Please upload an image.");
				     return;
				 } else if (upload.getSize() == 0) {
					 System.out.println("The image you attempted to upload is empty.");
				     return;
				 } else {
					 System.out.println("content type: " + upload.getContentType());
					 System.out.println("filename: " + upload.getClientFileName());
				 }
				
				
			}
		};
		form.setMultiPart(true);
		
		//text
		TextField textfield = new TextField("text", new PropertyModel(this,"text"));
		form.add(textfield);

		//file
		FileUploadField uploadField = new FileUploadField("file");
		form.add(uploadField);
		
		
		add(form);

	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
