package uk.ac.lancs.e_science.profile2.tool.pages.panels;


import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.hbm.ProfileStatus;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class MyStatusPanel extends Panel {
	
	private transient Logger log = Logger.getLogger(MyStatusPanel.class);
	
    private transient SakaiProxy sakaiProxy;
    private transient Profile profile;


	public MyStatusPanel(String id, UserProfile userProfile) {
		super(id);
		
		//create model
		CompoundPropertyModel userProfileModel = new CompoundPropertyModel(userProfile);
		
		 //get the list of items to display from provider (database, etc)
        //in the form of a LoadableDetachableModel
		/*
        IModel userStatusModel =  new LoadableDetachableModel() {
            protected Object load() {
                return getHateList();
            }
        };
		*/
		
		
		//get SakaiProxy API
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile API
		profile = ProfileApplication.get().getProfile();
		
		//a number of factors can disable the status
		boolean displayStatus = true;
		
		//get info
		String displayName = userProfile.getDisplayName();
		String userId = sakaiProxy.getCurrentUserId();
		
		//get this from a local method so we can recall this method each time.
		ProfileStatus profileStatus = profile.getLatestUserStatus(userId);
		
		String statusMessage = "";
		String statusDateStr = "";
		
		if(profileStatus == null) {
			displayStatus = false;
		} else {
			statusMessage = profileStatus.getMessage();
			statusDateStr = profile.convertDateForStatus(profileStatus.getDateAdded());
			userProfile.setStatus(statusMessage);
		}
		
					
		//name
		Label profileName = new Label("profileName", displayName);
		add(profileName);
		
		
		//status container
		final WebMarkupContainer statusContainer = new WebMarkupContainer("statusContainer");
		statusContainer.setOutputMarkupId(true);
		
		//status
		Label statusMessageLabel = new Label("statusMessage", statusMessage);
		statusContainer.add(statusMessageLabel);
		
		//status last updated
		Label statusDateLabel = new Label("statusDate", statusDateStr);
		statusContainer.add(statusDateLabel);
		
		//status update link
		AjaxFallbackLink statusClearLink = new AjaxFallbackLink("statusClearLink") {
			public void onClick(AjaxRequestTarget target) {
				
			}
						
		};
		statusClearLink.setOutputMarkupId(true);
		statusClearLink.add(new Label("statusClearLabel",new ResourceModel("link.status.clear")));
		statusContainer.add(statusClearLink);
		
		//add status container
		add(statusContainer);
		
		//hide status if none or its too old
		//if(!displayStatus) {
		//	statusContainer.setVisible(false);
		//}
		
		
		//status form
		Form form = new Form("form", userProfileModel);
		form.setOutputMarkupId(true);
        
		//status field (with customised onclick)
        TextField statusField = new TextField("statusField", new PropertyModel(userProfile, "status"));
        statusField.setOutputMarkupId(true);
        statusField.add(new AttributeAppender("onfocus", new Model("autoFill('" + statusField.getMarkupId() + "', '" + displayName + "');"), " "));
        
        form.add(statusField);
        
        //submit button
		AjaxButton submitButton = new AjaxButton("submit") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show message, then load display panel
				
				if(save(form)) {
					String js = "alert('ok!');";
					target.prependJavascript(js);
					// tell wicket to repaint the statusContainer only 
				     target.addComponent(statusContainer);
				     //this isn't refreshing. we will need to get the latest status from the db each time so we need to reorder where
				     //we get the info. perhaps to an anonymous inner calss.
				
				} else {
					String js = "alert('crap!');";
					target.prependJavascript(js);
				}
				
            }
		};
		form.add(submitButton);
		
        //add form
		add(form);
        
	}
	

	private boolean save(Form form) {
		
		//System.out.println("model-" + form.getModelObject());

		//get the backing model
		UserProfile userProfile = (UserProfile) form.getModelObject();
		
		//get SakaiProxy API
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile API
		profile = ProfileApplication.get().getProfile();
		
		//get userId from Sakaiproxy
		String userId = sakaiProxy.getCurrentUserId();

		//save status from userProfile
		//need to do some checking in here if its null etc, ie if they want to clear it.
		if(profile.setUserStatus(userId, userProfile.getStatus())) {
			log.info("Saved status for: " + userId );
			return true;
		} else {
			log.info("Couldn't save status for: " + userId);
			return false;
		}
		
	}
	
	
	private ProfileStatus getLatestUserStatus(String userId) {
		return (profile.getLatestUserStatus(userId));
	}
	
	
	/*
	 * public class AjaxListPage
	 * 
	 * 
	 * 
	 * 
	 * <?xml version="1.0" encoding="UTF-8"?>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:wicket="http://wicket.apache.org/">
  <body>
      <table wicket:id="theContainer">
        <tr><th>index</th><th>name</th><th>date</th></tr>
        <tr wicket:id="hateList">
            <td><span wicket:id="hateIndex">index</span></td>
            <td><span wicket:id="hateName">name</span></td>
            <td><span wicket:id="hateDate">date</span></td>
        </tr>
      </table>
  </body>
</html>
	 * 
	 * 
    extends Page
{

    public AjaxListPage()
    {

        //get the list of items to display from provider (database, etc)
        //in the form of a LoadableDetachableModel
        IModel hateList =  new LoadableDetachableModel()
        {
            protected Object load() {
                return getHateList();
            }
        };

        ListView hateView = new ListView("hateList", hateList)
        {
            protected void populateItem(final ListItem item) {
                MyListItem mli = (MyListItem)item.getModelObject();
                item.add(new Label("hateName", mli.name));
                item.add(new Label("hateDate", mli.date.toString()));
                item.add(new Label("hateIndex", mli.index.toString()));
            }
        };

        //encapsulate the ListView in a WebMarkupContainer in order for it to update
        WebMarkupContainer listContainer = new WebMarkupContainer("theContainer");
        //generate a markup-id so the contents can be updated through an AJAX call
        listContainer.setOutputMarkupId(true);
        listContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)));
        // add the list view to the container
        listContainer.add(hateView);
        // finally add the container to the page
        add(listContainer);
    }

    // just keep randomizing the values, for display, so you can see it's updating.
    private List getHateList() {
        List ret = new ArrayList();
        for (int i = 0; i < 5; i++) {
            MyListItem x =  new MyListItem();
            x.name = RandomStringUtils.randomAlphabetic(10);
            x.date = new Date();
            x.index = x.date.getSeconds();
            ret.add(x);
        }
        return ret;

    }

    // a very simple model object just to have something concrete for an example
    private class MyListItem {
        public String name;
        public Date date;
        public Integer index;
    }
}
	 */
	
	
	
	
}
