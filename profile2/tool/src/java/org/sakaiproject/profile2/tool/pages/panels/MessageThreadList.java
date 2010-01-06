package org.sakaiproject.profile2.tool.pages.panels;

import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.log4j.Logger;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.Message;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.dataproviders.MessageThreadHeadersDataProvider;
import org.sakaiproject.profile2.util.ProfileConstants;

public class MessageThreadList extends Panel {
	
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger(ConfirmedFriends.class);
    private transient SakaiProxy sakaiProxy;
    private transient ProfileLogic profileLogic;
	
	public MessageThreadList(final String id) {
		super(id);
		
		log.debug("MessageList()");
		
		//get API's
		sakaiProxy = getSakaiProxy();
		profileLogic = getProfileLogic();
		
		//get current user
		final String userUuid = sakaiProxy.getCurrentUserId();
		
		//container which wraps list
		final WebMarkupContainer messageThreadListContainer = new WebMarkupContainer("messageThreadListContainer");
		messageThreadListContainer.setOutputMarkupId(true);
		
		//get our list of confirmed friends as an IDataProvider
		final MessageThreadHeadersDataProvider provider = new MessageThreadHeadersDataProvider(userUuid);
		
		//init number of threads
		int numMessages = provider.size();
		
		//messageList
		DataView<Message> messageThreadList = new DataView<Message>("messageThreadList", provider) {
			private static final long serialVersionUID = 1L;

			protected void populateItem(final Item<Message> item) {
		        
				final Message message = (Message)item.getDefaultModelObject();
				
				System.out.println(message.getId());
				
				item.setOutputMarkupId(true);
		    }
			
		};
		messageThreadList.setOutputMarkupId(true);
		//messageThreadList.setItemsPerPage(ProfileConstants.MAX_MESSAGES_PER_PAGE);
		messageThreadList.setItemsPerPage(ProfileConstants.MAX_MESSAGES_PER_PAGE);

		messageThreadListContainer.add(messageThreadList);

		//add results container
		add(messageThreadListContainer);
		
		//add pager
		AjaxPagingNavigator pager = new AjaxPagingNavigator("navigator", messageThreadList);
		add(pager);

	
		//initially, if no message thread to show, hide container and pager
		if(numMessages == 0) {
			messageThreadListContainer.setVisible(false);
			pager.setVisible(false);
		}
		
		//also, if num less than num required for pager, hide it
		if(numMessages <= ProfileConstants.MAX_MESSAGES_PER_PAGE) {
			pager.setVisible(false);
		}
		
		
	}
	
	/* reinit for deserialisation (ie back button) */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		log.debug("MessageList has been deserialized.");
		//re-init our transient objects
		profileLogic = getProfileLogic();
		sakaiProxy = getSakaiProxy();
	}
	
	
	private SakaiProxy getSakaiProxy() {
		return Locator.getSakaiProxy();
	}

	private ProfileLogic getProfileLogic() {
		return Locator.getProfileLogic();
	}
}
