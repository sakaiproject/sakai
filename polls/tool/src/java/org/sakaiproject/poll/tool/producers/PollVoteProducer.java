package org.sakaiproject.poll.tool.producers;

import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.logic.PollVoteManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.rsf.viewstate.EntityCentredViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.beanutil.BeanGetter;
import uk.org.ponder.beanutil.entity.EntityID;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.messageutil.TargettedMessageList;
import java.text.MessageFormat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class PollVoteProducer implements ViewComponentProducer,ViewParamsReporter,NavigationCaseReporter {

	public static final String VIEW_ID = "voteQuestion";
	private BeanGetter rbg;
	private UserDirectoryService userDirectoryService;
	private PollListManager pollListManager;
	private ToolManager toolManager;
	private MessageLocator messageLocator;
	private LocaleGetter localegetter;
	private TargettedMessageList tml;
	private PollVoteManager pollVoteManager;
	
	
	private static Log m_log = LogFactory.getLog(PollVoteProducer.class);
	  
	public String getViewID() {
		// TODO Auto-generated method stub
		return VIEW_ID;
	}
	
	public void setPollVoteManager(PollVoteManager pvm){
		this.pollVoteManager = pvm;
	}
	
	public void setMessageLocator(MessageLocator messageLocator) {
		  
	    this.messageLocator = messageLocator;
	  }

	  public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
	    this.userDirectoryService = userDirectoryService;
	  }

	  public void setPollListManager(PollListManager pollListManager) {
	    this.pollListManager = pollListManager;
	  }

	  public void setToolManager(ToolManager toolManager) {
	    this.toolManager = toolManager;
	  }

	  public void setLocaleGetter(LocaleGetter localegetter) {
	    this.localegetter = localegetter;
	  }
	  
	  public void setTargettedMessageList(TargettedMessageList tml) {
		    this.tml = tml;
	  }
	  
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		
		
			
		    if (tml.size() > 0) {
		    	for (int i = 0; i < tml.size(); i ++ ) {
		    		UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:");
		    		String output;
		    		if (tml.messageAt(i).args != null ) {	    		
		    			output = messageLocator.getMessage(tml.messageAt(i).acquireMessageCode(),tml.messageAt(i).args[0]);
		    		} else {
		    			output = messageLocator.getMessage(tml.messageAt(i).acquireMessageCode());
		    		}
		    		UIOutput.make(errorRow,"error", output);
		    	}
		    }
		      
		

		
		
		
		UIOutput.make(tofill, "poll-vote-title", messageLocator.getMessage("poll_vote_title"));
		
		
		
		
		try {
			
		 EntityCentredViewParameters ecvp = (EntityCentredViewParameters) viewparams;
		 
		 //m_log.info(ecvp.toPathInfo());
		 
		 //m_log.info(ecvp.getELPath());
		 
		 //hack but this needs to work
		 String strId = ecvp.getELPath().substring(ecvp.getELPath().indexOf(".") + 1);
		 m_log.debug("got id of " + strId);
		 Poll poll = pollListManager.getPollById(new Long(strId));
		    
		 m_log.debug("got poll " + poll.getText());
		 
		 
		 //check if they can vote
		 if (poll.getLimitVoting() && pollVoteManager.userHasVoted(poll.getPollId())) {
			 m_log.warn("This user has already voted!");
			 UIOutput.make(tofill, "hasErrors",messageLocator.getMessage("vote_hasvoted"));
			 return;
		 }
		 
		 UIOutput.make(tofill,"poll-text",poll.getText());
		 if (poll.getDetails() != null)
		 {
			 UIOutput.make(tofill,"poll-description",poll.getDetails());
		 }
		 
		 m_log.debug("this poll has " + poll.getPollOptions().size()+ " options");
		 
		 UIForm voteForm = UIForm.make(tofill,"options-form",""); 
		 
		 List pollOptions = poll.getPollOptions();
		 //build the options + label lists
		    String[] values= new String[pollOptions.size()];
		    for (int i = 0;i <pollOptions.size(); i++ ) {
		    	Option po = (Option)pollOptions.get(i);
		    	values[i]= po.getId().toString();
		    }
		                                 
		    
		    String[] labels = new String[pollOptions.size()];
		    for (int i = 0;i<  pollOptions.size(); i++ ) {
		    	Option po = (Option)pollOptions.get(i);
		    	if (po.getOptionText() != null ) {
		    		labels[i]= po.getOptionText();
		    	} else {
		    		m_log.warn("Option text is null!");
		    		labels[i]="null option!";
		    	}
		    }
		    
			 //we need to deside is this a single or multiple?
			 //poll.getMaxOptions()
		 	 boolean isMultiple = false;
			 if (poll.getMaxOptions()>1)
				 isMultiple = true;
	 
			 
			 UISelect radio;
			 if (isMultiple)
			 	radio = UISelect.makeMultiple(voteForm,"optionform",values,"#{voteCollection.optionsSelected}",new String[] {});
			 else
				 radio = UISelect.make(voteForm,"optionform",values,"#{voteCollection.option}",new String());
			 
			 radio.optionnames = UIOutputMany.make(labels);
			 String selectID = radio.getFullID();
			 

			 
			 for (int i = 0;i < pollOptions.size(); i++ ) {
				 Option po = (Option)pollOptions.get(i);
				 m_log.debug("got option " + po.getOptionText() + " with id of  " + po.getId());
				 			 
				 UIBranchContainer radioRow = UIBranchContainer.make(voteForm,
						 isMultiple ? "option:select"
					              : "option:radio"						 
						 ,Integer.toString(i));
				 UISelectChoice.make(radioRow,"option-radio",selectID,i);
				 UISelectLabel.make(radioRow,"option-label",selectID,i);
				 //UIOutput.make(radioRow,"option-label",labels[i]);
			 }
			 //bind some parameters
			 voteForm.parameters.add(new UIELBinding("#{voteCollection.pollId}", poll.getPollId()));
			 
			 UICommand sub = UICommand.make(voteForm, "submit-new-vote",messageLocator.getMessage("vote_vote"),
		        "#{pollToolBean.processActionVote}");
			   sub.parameters.add(new UIELBinding("#{voteCollection.submissionStatus}", "sub"));
			   UICommand cancel = UICommand.make(voteForm, "cancel",messageLocator.getMessage("vote_cancel"),"#{pollToolBean.cancel}");
			   cancel.parameters.add(new UIELBinding("#{voteCollection.submissionStatus}", "cancel"));
					   
		} 
		catch (Exception e)
		{
			m_log.error("Error: " + e);
			e.printStackTrace();
		}
	}
	  public void setELEvaluator(BeanGetter rbg) {
		    this.rbg = rbg;
		  }
	  public ViewParameters getViewParameters() {
		    return new EntityCentredViewParameters(VIEW_ID, new EntityID("Poll", null));

		  }
		 
	  
	  public List reportNavigationCases() {
		    List togo = new ArrayList(); // Always navigate back to this view.
		    //togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		    togo.add(new NavigationCase("Error", new SimpleViewParameters(VIEW_ID)));
		    togo.add(new NavigationCase("Success", new SimpleViewParameters(
		            ConfirmProducer.VIEW_ID)));
		    togo.add(new NavigationCase("cancel", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		    return togo;
		  }
}
