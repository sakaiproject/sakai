package org.sakaiproject.scorm.ui.player.components;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.user.api.User;

public class AdminPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(AdminPanel.class);
	
	protected String selectedLearner;
	protected String binding;
	protected String dataValue;
	
	public AdminPanel(String id, String contentPackageId, final SessionBean sessionBean) {
		super(id);
		
		
		/*List<IDataManager> dataManagers = clientFacade.getContentPackageDataManagers(contentPackageId);
		
		List<UserWrapper> options = new LinkedList<UserWrapper>();
		for (IDataManager manager : dataManagers) {
			String userId = manager.getUserId();
			User user;
			try {
				user = clientFacade.getUser(userId);
				options.add(new UserWrapper(user));
			} catch (UserNotDefinedException e) {
				log.error("Could not find a user for " + userId, e);
			}
			
		}
		
		ChoiceRenderer choiceRenderer = new ChoiceRenderer("displayName", "id");
		PropertyModel selectionModel = new PropertyModel(this, "selectedLearner");
		DropDownChoice learnerChoice = new DropDownChoice("learnerChoice", selectionModel, options, choiceRenderer);

		final TextField bindingTextField = new TextField("binding", new PropertyModel(this, "binding"));
		bindingTextField.setOutputMarkupId(true);
		
		final Label dataValueLabel = new Label("dataValue", new PropertyModel(this, "dataValue"));
		dataValueLabel.setOutputMarkupId(true);
		
		Form form = new Form("adminForm");
		add(form);
		form.add(learnerChoice);
		form.add(bindingTextField);
		form.add(dataValueLabel);
		form.add(new AjaxButton("submitButton") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				
				ScoBean scoBean = clientFacade.applicationProgrammingInterface().produceScoBean(sessionBean.getScoId(), sessionBean);
				String bindingValue = getBinding();
				String result = scoBean.GetValue(bindingValue);
				
				log.warn("Binding: " + bindingValue + " Value: " + result);
				
				setDataValue(result);
				target.addComponent(dataValueLabel);
			}
			
		});*/
	}
	
	
	public class UserWrapper implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private String id;
		private String displayName;
		
		public UserWrapper(User user) {
			this.id = user.getId();
			this.displayName = user.getDisplayName();
		}

		public String getId() {
			return id;
		}

		public String getDisplayName() {
			return displayName;
		}
	}
	

	public String getSelectedLearner() {
		return selectedLearner;
	}


	public void setSelectedLearner(String selectedLearner) {
		this.selectedLearner = selectedLearner;
	}


	public String getDataValue() {
		return dataValue;
	}


	public void setDataValue(String dataValue) {
		this.dataValue = dataValue;
	}


	public String getBinding() {
		return binding;
	}


	public void setBinding(String binding) {
		this.binding = binding;
	}

}
