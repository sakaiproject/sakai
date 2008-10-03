package uk.ac.lancs.e_science.profile2.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.tool.models.TestModel;


public class TestData extends BasePage {

	private transient Logger log = Logger.getLogger(TestData.class);

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
		
		
		
		TestModel testModel = new TestModel();
		testModel.setName("hello");
		
		testModel = new TestModel(); //need a getter to ge thte model, can't call new each time, it kills it
		add(new Label("testLabel7",testModel.getName()));


	}
}
