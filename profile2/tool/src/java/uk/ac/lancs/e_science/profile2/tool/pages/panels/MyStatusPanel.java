package uk.ac.lancs.e_science.profile2.tool.pages.panels;


import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.hbm.ProfileStatus;
import uk.ac.lancs.e_science.profile2.tool.ProfileApplication;
import uk.ac.lancs.e_science.profile2.tool.models.UserProfile;

public class MyStatusPanel extends Panel {
	
	private transient Logger log = Logger.getLogger(MyStatusPanel.class);
	
    private transient SakaiProxy sakaiProxy;
    private transient Profile profile;
    private transient ProfileStatus profileStatus;
    
    //get default text that fills the textField
	String defaultStatus = new ResourceModel("text.no.status", "Say something").getObject().toString();
        
	//a number of factors can disable the status
    private boolean displayStatus = true;


	public MyStatusPanel(String id, UserProfile userProfile) {
		super(id);
		
		//get SakaiProxy API
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile API
		profile = ProfileApplication.get().getProfile();
				
		//get info
		String displayName = userProfile.getDisplayName();
		final String userId = sakaiProxy.getCurrentUserId();
		
		//setup ProfileStatus object leaded with the current values from the DB/Hibernate's session for the page to use initially.
		profileStatus = profile.getUserStatus(userId);
		
		//if no status, initialise
		if(profileStatus == null) {
			displayStatus = false;
			profileStatus = new ProfileStatus();
		}

		
		//the message and date fields get their values from the ProfileStatus object
		//when the status form is submitted, Hibernate persists the data in teh background
		//and the message and date fields get their values again, via these models from teh ProfileStatus object.
		
		//setup model for status message
		IModel statusMessageModel = new Model() {
			private String message = "";
			
			public Object getObject(){
				//profileStatus = profile.getUserStatus(userId);
				message = profileStatus.getMessage(); //get from hibernate
				if("".equals(message) || message == null){
					displayStatus = false;
					log.warn("No status message for: " + userId );
				} 
				return message;
			}
			
			
			
			
		};
		
		//setup model for status date
		Model statusDateModel = new Model() {
			
			private Date date;
			private String dateStr = "";
			
			public Object getObject(){
				date = profile.getUserStatusDate(userId);
				if(date == null) {
					displayStatus = false;
					log.warn("No status date for: " + userId );
				} else {
					//transform the date
					dateStr = profile.convertDateForStatus(date);
				}
				return dateStr;
			}
			
		};
	
				
		//create model
		CompoundPropertyModel profileStatusModel = new CompoundPropertyModel(profileStatus);
	
		//name
		Label profileName = new Label("profileName", displayName);
		add(profileName);
		
		
		//status container - this needs to reget the model when its added but not sure how.
		final WebMarkupContainer statusContainer = new WebMarkupContainer("statusContainer");
		statusContainer.setOutputMarkupId(true);
		
		
		//status
		Label statusMessageLabel = new Label("statusMessage", statusMessageModel);
		statusMessageLabel.setOutputMarkupId(true);
		statusContainer.add(statusMessageLabel);
		
		//status last updated
		Label statusDateLabel = new Label("statusDate", statusDateModel);
		statusDateLabel.setOutputMarkupId(true);
		statusContainer.add(statusDateLabel);
		
		//status update link
		AjaxFallbackLink statusClearLink = new AjaxFallbackLink("statusClearLink") {
			public void onClick(AjaxRequestTarget target) {
				System.out.println("clear clicked");
				//need to clear the latest status message with a blank string
			}
						
		};
		statusClearLink.setOutputMarkupId(true);
		statusClearLink.add(new Label("statusClearLabel",new ResourceModel("link.status.clear")));
		statusContainer.add(statusClearLink);
		
		//add status container
		add(statusContainer);
		
				
		//status form
		Form form = new Form("form", profileStatusModel);
		form.setOutputMarkupId(true);
        		
		//status field
        TextField statusField = new TextField("message", new PropertyModel(profileStatus, "message"));
        statusField.setOutputMarkupId(true);
        form.add(statusField);
        
        //link the status textfield field with the focus/blur function via this dynamic js 
		StringHeaderContributor statusJavascript = new StringHeaderContributor(
				"<script type=\"text/javascript\">" +
					"$(document).ready( function(){" +
					"autoFill($('#" + statusField.getMarkupId() + "'), '" + defaultStatus + "');" +
					"});" +
				"</script>");
		add(statusJavascript);
        
        
        //submit button
		AjaxButton submitButton = new AjaxButton("submit") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				//save() form, show message, then load display panel
				
				if(save(form)) {
					// tell wicket to repaint the statusContainer only
					target.addComponent(statusContainer);
				
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
		ProfileStatus profileStatus = (ProfileStatus) form.getModelObject();
		
		//get SakaiProxy API
		sakaiProxy = ProfileApplication.get().getSakaiProxy();
		
		//get Profile API
		profile = ProfileApplication.get().getProfile();
		
		//get userId from sakaiProxy
		String userId = sakaiProxy.getCurrentUserId();
		
		//get the status. if its the default text, do not update
		String statusMessage = profileStatus.getMessage();
		if(statusMessage.equals(defaultStatus)) {
			log.warn("Default message, not updating");
			return false;
		}
		
		System.out.println("user status on form submit: " + profileStatus.getMessage());

		//save status from userProfile
		//need to do some checking in here if its null etc, ie if they want to clear it.
		if(profile.setUserStatus(userId, profileStatus.getMessage())) {
			log.info("Saved status for: " + userId);
			return true;
		} else {
			log.info("Couldn't save status for: " + userId);
			return false;
		}
		
	}
	
	//method to get the status info when its requested
	/*
	private UserStatus getUserStatus(String userId) {
		
		//form model
		UserStatus userStatus = new UserStatus();

		//hibernate model		
		ProfileStatus profileStatus = profile.getLatestUserStatus(userId);
		if(profileStatus != null) {
			//setup values
			userStatus.setMessage(profileStatus.getMessage());
			userStatus.setDateStr(profile.convertDateForStatus(profileStatus.getDateAdded()));
			
			return userStatus;
		} 
		return null;

	}
	*/
	

	
	
	
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
