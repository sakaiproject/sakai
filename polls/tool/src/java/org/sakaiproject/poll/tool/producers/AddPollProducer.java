package org.sakaiproject.poll.tool.producers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.PollImpl;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.tool.params.VoteBean;

import uk.org.ponder.beanutil.entity.EntityID;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.EntityCentredViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;



import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.entity.api.Entity;

public class AddPollProducer implements ViewComponentProducer,NavigationCaseReporter,ViewParamsReporter {
	 public static final String VIEW_ID = "voteAdd";
	  private UserDirectoryService userDirectoryService;
	  private PollListManager pollListManager;
	  private ToolManager toolManager;
	  private MessageLocator messageLocator;
	  private LocaleGetter localegetter;

	  
	  private static Log m_log = LogFactory.getLog(AddPollProducer.class);
	  
	  public String getViewID() {
	    return VIEW_ID;
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
	  
	  private VoteBean voteBean;
	  public void setVoteBean(VoteBean vb){
		  this.voteBean = vb;
	  }
	  public void fillComponents(UIContainer tofill, ViewParameters viewparams,
		      ComponentChecker checker) {
		  
		
		  
	    User currentuser = userDirectoryService.getCurrentUser();
	    String currentuserid = currentuser.getId();
		   
	    EntityCentredViewParameters ecvp = (EntityCentredViewParameters) viewparams;
	    Poll poll = null;
	    boolean isNew = true;
	    
	    UIForm newPoll = UIForm.make(tofill, "add-poll-form");
	    if (voteBean.getPoll() != null) { 
	    	poll = voteBean.getPoll();
	    	UIOutput.make(tofill,"new_poll_title",messageLocator.getMessage("new_poll_title"));
	    	isNew = false;
	    	newPoll.parameters.add(new UIELBinding("#{pollToolBean.newPoll.pollId}",
			           poll.getPollId()));
	    	
	    } else if (ecvp.mode.equals(EntityCentredViewParameters.MODE_NEW)) {
			UIOutput.make(tofill,"new_poll_title",messageLocator.getMessage("new_poll_title"));
			//build an empty poll 
			poll = new PollImpl();
	    } else { 
			UIOutput.make(tofill,"new_poll_title",messageLocator.getMessage("new_poll_title_edit"));  
			//	hack but this needs to work
			String strId = ecvp.getELPath().substring(ecvp.getELPath().indexOf(".") + 1);
			m_log.debug("got id of " + strId);
			poll = pollListManager.getPollById(new Long(strId));
			voteBean.setPoll(poll);
			newPoll.parameters.add(new UIELBinding("#{pollToolBean.newPoll.pollId}",
			           poll.getPollId()));

			isNew = false;
		}
	    
	    
	    //only display for exisiting polls
	    if (!isNew) {
			//fill the options list
			UIOutput.make(tofill,"options-title",messageLocator.getMessage("new_poll_option_title"));
			UIInternalLink.make(tofill,"option-add",messageLocator.getMessage("new_poll_option_add"),
					new EntityCentredViewParameters(PollOptionProducer.VIEW_ID, 
		                      new EntityID("Poll", "Poll_" + poll.getPollId().toString()),EntityCentredViewParameters.MODE_NEW));
					
					//new SimpleViewParameters(PollOptionProducer.VIEW_ID));
			List options = poll.getPollOptions();
			for (int i = 0; i <options.size();i++){
				Option o = (Option)options.get(i);
				UIBranchContainer oRow = UIBranchContainer.make(newPoll,"options-row:");
				UIOutput.make(oRow,"options-name",o.getOptionText());
				UIInternalLink.make(oRow,"option-edit",messageLocator.getMessage("new_poll_option_edit"),
						new EntityCentredViewParameters(PollOptionProducer.VIEW_ID, 
			                      new EntityID("Option", "Option_" + o.getId().toString()), EntityCentredViewParameters.MODE_EDIT));
				UIInternalLink.make(oRow,"option-delete",messageLocator.getMessage("new_poll_option_delete"),
						new EntityCentredViewParameters(PollOptionDeleteProducer.VIEW_ID, 
								new EntityID("Option", "Option_" + o.getId().toString())));
			}
	    }
	    
		  UIOutput.make(tofill, "new-poll-descr", messageLocator.getMessage("new_poll_title"));
		  UIOutput.make(tofill, "new-poll-question-label", messageLocator.getMessage("new_poll_question_label"));
		  UIOutput.make(tofill, "new-poll-descr-label", messageLocator.getMessage("new_poll_descr_label")); 
		  UIOutput.make(tofill, "new-poll-descr-label2", messageLocator.getMessage("new_poll_descr_label2"));
		  UIOutput.make(tofill, "new-poll-open-label", messageLocator.getMessage("new_poll_open_label"));
		  UIOutput.make(tofill, "new-poll-close-label", messageLocator.getMessage("new_poll_close_label"));
		  UIOutput.make(tofill, "new-poll-limits", messageLocator.getMessage("new_poll_limits"));
		  UIOutput.make(tofill, "new-poll-min-limits", messageLocator.getMessage("new_poll_min_limits"));
		  UIOutput.make(tofill, "new-poll-max-limits", messageLocator.getMessage("new_poll_max_limits"));
		  
		  
		  //the form fields
		  
		  UIInput.make(newPoll, "new-poll-text", "#{pollToolBean.newPoll.text}",poll.getText());
		  UIInput.make(newPoll, "new-poll-descr", "#{pollToolBean.newPoll.details}", poll.getDetails());
		  
		  
		  //we need a date fomater
		    SimpleDateFormat dayf = new SimpleDateFormat("d");
		  
//		  build a days array
		  String[] days = new String[] {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};
		  UISelect oDay = UISelect.make(newPoll,"new-poll-opend", days, "#{pollToolBean.openDay}",dayf.format(poll.getVoteOpen()));
		  UISelect cDay = UISelect.make(newPoll,"new-poll-closed", days,"#{pollToolBean.closeDay}",dayf.format(poll.getVoteClose()));
		   
		  oDay.optionlist = UIOutputMany.make(days);
		  oDay.optionnames = UIOutputMany.make(days);
		  cDay.optionlist = UIOutputMany.make(days);
		  cDay.optionnames = UIOutputMany.make(days);
		  
		  //build the month array
		  
		  String[] months = new String[12];
		  String[] monthsI = new String[12];
		  SimpleDateFormat monthIn = new SimpleDateFormat("M");
		  SimpleDateFormat monthf = new SimpleDateFormat("MMM",localegetter.get());
		  for (int i = 0; i < 12; i++) {
			  
			  try {
				  Date d = monthIn.parse(Integer.toString(i+1));
				  months[i]= monthf.format(d);
				  monthsI[i] = Integer.toString(i+1);
			  } 
			  catch (ParseException e) {
				  e.printStackTrace();
			  }
			  
		  }
		  
		  m_log.debug("this poll opens in month " +monthIn.format(poll.getVoteOpen()) + " on day " + dayf.format(poll.getVoteOpen()) + " and closes in month: " + monthIn.format(poll.getVoteClose()));
		  UISelect oMonth = UISelect.make(newPoll,"new-poll-openm",monthsI, "#{pollToolBean.openMonth}",monthIn.format(poll.getVoteOpen()));
		  UISelect cMonth = UISelect.make(newPoll,"new-poll-closem",monthsI,"#{pollToolBean.closeMonth}",monthIn.format(poll.getVoteClose()));
		  //oMonth.optionlist = UIOutputMany.make(months);
		  oMonth.optionnames = UIOutputMany.make(months);
		  //cMonth.optionlist = UIOutputMany.make(months);
		  cMonth.optionnames = UIOutputMany.make(months);
		  
		  
		  String[] years = new String[] {"2006","2007","2008","2009","2010"};
		  SimpleDateFormat yearf = new SimpleDateFormat("yyyy");
		  
		  UISelect oYear = UISelect.make(newPoll,"new-poll-openy",years,"#{pollToolBean.openYear}", yearf.format(poll.getVoteOpen()));
		  UISelect cYear = UISelect.make(newPoll,"new-poll-closey",years, "#{pollToolBean.closeYear}", yearf.format(poll.getVoteClose()));
		  oYear.optionnames = UIOutputMany.make(years);
		  cYear.optionnames = UIOutputMany.make(years);
		  
		  String[] hours = new String[] {"1","2","3","4","5","6","7","8","9","10","11","12"};
		  SimpleDateFormat hoursf = new SimpleDateFormat("h");
		  UISelect oHours = UISelect.make(newPoll,"new-poll-openh",hours,"#{pollToolBean.openHour}", hoursf.format(poll.getVoteOpen()));
		  UISelect cHours = UISelect.make(newPoll,"new-poll-closeh",hours,"#{pollToolBean.closeHour}",hoursf.format(poll.getVoteClose()));
		  cHours.optionnames = UIOutputMany.make(hours);
		  oHours.optionnames = UIOutputMany.make(hours);
		  
		  SimpleDateFormat minf = new SimpleDateFormat("m");
		  String[] minutes = new String[]{"00","15","30","45"};
		  String openM = null;
		  String closeM = null;
		  if (poll == null) {
			  openM ="00";
			  closeM ="00";
		  } else {
			  openM=minf.format(poll.getVoteOpen());
			  closeM=minf.format(poll.getVoteClose());
		  }
		  
		  UISelect.make(newPoll,"new-poll-openms",minutes,"#{pollToolBean.openMinutes}",openM);
		  UISelect.make(newPoll,"new-poll-closems",minutes,"#{pollToolBean.closeMinutes}", closeM);
		  
		  SimpleDateFormat amf = new SimpleDateFormat("a");
		  String[] ampm = new String[]{"AM","PM"};
		  UISelect.make(newPoll,"new-poll-openampm",ampm,"#{pollToolBean.openAmPm}",amf.format(poll.getVoteOpen()));
		  UISelect.make(newPoll,"new-poll-closeampm",ampm,"#{pollToolBean.closeAmPm}", amf.format(poll.getVoteClose()));
		  
		  String[] minVotes = new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"};
		  String[] maxVotes = new String[]{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"};
		  UISelect.make(newPoll,"min-votes",minVotes,"#{pollToolBean.newPoll.minOptions}",Integer.toString(poll.getMinOptions()));
		  UISelect.make(newPoll,"max-votes",maxVotes,"#{pollToolBean.newPoll.maxOptions}",Integer.toString(poll.getMaxOptions()));
		  /*
			 * 	open - can be viewd at any time
			 * 	never - not diplayed
			 * 	afterVoting - after user has voted
			 * 	afterClosing
			 * 
			 */
		    String[] values = new String[] { "open", "never", "afterVoting", "afterClosing" };
		    String[] labels = new String[] {
		    		messageLocator.getMessage("new_poll_open"), 
		    		messageLocator.getMessage("new_poll_never"),
		    		messageLocator.getMessage("new_poll_aftervoting"),
		    		messageLocator.getMessage("new_poll_afterClosing")};
		    
		    

		    UISelect radioselect = UISelect.make(newPoll, "release-select", values,
		        "#{pollToolBean.newPoll.displayResult}", poll.getDisplayResult());
		    
		    radioselect.optionnames = UIOutputMany.make(labels);
		    
		    
		    String selectID = radioselect.getFullID();
		    //StringList optList = new StringList();
		    UIOutput.make(newPoll,"add_results_label",messageLocator.getMessage("new_poll_results_label"));
		    for (int i = 0; i < values.length; ++i) {
		    	
		      UIBranchContainer radiobranch = UIBranchContainer.make(newPoll,
		          "releaserow:", Integer.toString(i));
		      UISelectChoice.make(radiobranch, "release", selectID, i);
		      UISelectLabel.make(radiobranch, "releaseLabel", selectID, i);

		    }
		    
		    
		    

		  newPoll.parameters.add(new UIELBinding("#{pollToolBean.newPoll.owner}",
		           currentuserid));
		  String siteId = toolManager.getCurrentPlacement().getContext();
		  newPoll.parameters.add(new UIELBinding("#{pollToolBean.siteID}",siteId));
		  
		  if (ecvp.mode!= null && ecvp.mode.equals(EntityCentredViewParameters.MODE_NEW))	 {
			  UICommand.make(newPoll, "submit-new-poll", messageLocator.getMessage("new_poll_saveoption"),
			  "#{pollToolBean.processActionAdd}");
		  } else {
			  UICommand.make(newPoll, "submit-new-poll", messageLocator.getMessage("new_poll_submit"),
			  "#{pollToolBean.processActionAdd}");		  
		  }
		  
		  UICommand cancel = UICommand.make(newPoll, "cancel",messageLocator.getMessage("new_poll_cancel"),"#{pollToolBean.cancel}");
		   cancel.parameters.add(new UIELBinding("#{voteCollection.submissionStatus}", "cancel"));
	  }
	  

	  public List reportNavigationCases() {
		    List togo = new ArrayList(); // Always navigate back to this view.
		    togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		    togo.add(new NavigationCase("added", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		    togo.add(new NavigationCase("option", new EntityCentredViewParameters(PollOptionProducer.VIEW_ID, new EntityID("Option", "new 1"),
	                EntityCentredViewParameters.MODE_NEW)));
		    togo.add(new NavigationCase("cancel", new SimpleViewParameters(PollToolProducer.VIEW_ID)));
		    return togo;
		  }
	  
	  public ViewParameters getViewParameters() {
		    return new EntityCentredViewParameters(VIEW_ID, new EntityID("Poll", null));

	  }
}
	  
	  
