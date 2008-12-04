package org.sakaiproject.site.tool.helper.managegroup.rsf;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.helper.managegroup.impl.SiteManageGroupHandler;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteComparator;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.SortedIterator;

import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * 
 * @author Dr. WHO?
 *
 */
public class GroupEditProducer implements ViewComponentProducer, ActionResultInterceptor, ViewParamsReporter {

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(GroupEditProducer.class);
	
    public SiteManageGroupHandler handler;
    public static final String VIEW_ID = "GroupEdit";
    public MessageLocator messageLocator;
    public FrameAdjustingProducer frameAdjustingProducer;
    public SiteService siteService = null;
    
	private TextInputEvolver richTextEvolver;
	public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
				this.richTextEvolver = richTextEvolver;
	}

    public String getViewID() {
        return VIEW_ID;
    }
    
    private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}
	
	public UserDirectoryService userDirectoryService;
	public void setUserDiretoryService(UserDirectoryService userDirectoryService)
	{
		this.userDirectoryService = userDirectoryService;
	}

    public void fillComponents(UIContainer arg0, ViewParameters arg1, ComponentChecker arg2) {
    	
    	String state="";
    	
    	// group
    	Group g = null;
    	// id for group
    	String groupId = null;
    	// title for group
    	String groupTitle = null;
    	// description for group
    	String groupDescription = null;
    	// member list for group
    	Collection<Member> groupMembers = new Vector<Member>();
    	
    	UIForm groupForm = UIForm.make(arg0, "groups-form");

    	 String id = ((GroupEditViewParameters) arg1).id;
    	 if (id != null)
    	 {
    		 try
    		 {
    			 g = siteService.findGroup(id);
    			 groupId = g.getId();
    			 groupTitle = g.getTitle();
    			 groupDescription = g.getDescription();
    			 groupMembers = g.getMembers();
    		 }
    		 catch (Exception e)
    		 {
    			 M_log.debug(this + "fillComponents: cannot get group id=" + id);
    		 }
    	 }
    	 else
    	 {
    		 handler.resetParams();
    	 }
    	 

         UIOutput.make(groupForm, "prompt", messageLocator.getMessage("group.newgroup"));
         UIOutput.make(groupForm, "instructions", messageLocator.getMessage("editgroup.instruction"));
         
         UIOutput.make(groupForm, "group_title_label", messageLocator.getMessage("group.title"));
         UIInput titleTextIn = UIInput.make(groupForm, "group_title", "#{SiteManageGroupHandler.title}",groupTitle);
		 
		
		 UIMessage groupDescrLabel = UIMessage.make(arg0, "group_description_label", "group.description"); 
		 UIInput groupDescr = UIInput.make(groupForm, "group_description:", "#{SiteManageGroupHandler.description}", groupDescription); 
		 richTextEvolver.evolveTextInput(groupDescr);
		 UILabelTargetDecorator.targetLabel(groupDescrLabel, groupDescr);
		 
		 UIOutput.make(groupForm, "membership_label", messageLocator.getMessage("editgroup.membership"));
		 UIOutput.make(groupForm, "membership_site_label", messageLocator.getMessage("editgroup.generallist"));
		 UIOutput.make(groupForm, "membership_group_label", messageLocator.getMessage("editgroup.grouplist"));
		 
		 // for the site members list
		 Collection siteMembers= handler.getSiteParticipant(g);
		 String[] siteMemberLabels = new String[siteMembers.size()];
		 String[] siteMemberValues = new String[siteMembers.size()];
		 UISelect siteMember = UISelect.makeMultiple(groupForm,"siteMembers",siteMemberValues,siteMemberLabels,"#{SiteManageGroupHandler.selectedSiteMembers}", new String[] {});
		 
		 int i =0;
		 Iterator<Participant> sIterator = new SortedIterator(siteMembers.iterator(), new SiteComparator(SiteConstants.SORTED_BY_PARTICIPANT_NAME, Boolean.TRUE.toString()));
	     for (; sIterator.hasNext();i++){
	        	Participant p = (Participant) sIterator.next();
	        	// not in the group yet
	        	if (g == null || g.getMember(p.getUniqname()) == null)
	        	{
					siteMemberLabels[i] = p.getName() + "(" + p.getDisplayId() + ")";
					siteMemberValues[i] = p.getUniqname();
	        	}
	        }
	     
	     // for the group members list
		 String[] groupMemberLabels = new String[groupMembers.size()];
		 String[] groupMemberValues = new String[groupMembers.size()];
		 UISelect groupMember = UISelect.make(groupForm,"groupMembers",groupMemberValues,groupMemberLabels,null);
		 i =0;
		 Iterator<Member> gIterator = new SortedIterator(groupMembers.iterator(), new SiteComparator(SiteConstants.SORTED_BY_PARTICIPANT_NAME, Boolean.TRUE.toString()));
	     for (; gIterator.hasNext();i++){
	        	Member p = (Member) gIterator.next();
	        	String userId = p.getUserId();
	        	try
	        	{
	        		User u = userDirectoryService.getUser(userId);
	        		groupMemberLabels[i] = u.getSortName() + "(" + u.getDisplayId() + ")";
	        	}
	        	catch (Exception e)
	        	{
	        		M_log.warn(this + ":fillComponents: cannot find user " + userId);
	        	}
				groupMemberValues[i] = userId;
	        }
	        
    	 UICommand.make(groupForm, "save", id != null?messageLocator.getMessage("editgroup.update"):messageLocator.getMessage("editgroup.new"), "#{SiteManageGroupHandler.processAddGroup}");

         UICommand.make(groupForm, "cancel", messageLocator.getMessage("editgroup.cancel"), "#{SiteManageGroupHandler.processBack}");
         
         UIInput.make(groupForm, "newRight", "#{SiteManageGroupHandler.memberList}", state);
         
         // hidden field for group id
         UIInput.make(groupForm, "groupId", "#{SiteManageGroupHandler.id}", groupId);
         
         //process any messages
         if (tml.size() > 0) {
 			for (i = 0; i < tml.size(); i ++ ) {
 				UIBranchContainer errorRow = UIBranchContainer.make(arg0,"error-row:", new Integer(i).toString());
 				TargettedMessage msg = tml.messageAt(i);
		    	if (msg.args != null ) 
		    	{
		    		UIMessage.make(errorRow,"error", msg.acquireMessageCode(), (Object[]) msg.args);
		    	} 
		    	else 
		    	{
		    		UIMessage.make(errorRow,"error", msg.acquireMessageCode());
		    	}	
 			}
         }
         
         frameAdjustingProducer.fillComponents(arg0, "resize", "resetFrame");
    }
    
    public ViewParameters getViewParameters() {
        GroupEditViewParameters params = new GroupEditViewParameters();

        params.id = null;
        return params;
    }

    // old and busted
//    public List reportNavigationCases() {
//        List togo = new ArrayList();
//        togo.add(new NavigationCase("success", new SimpleViewParameters(GroupListProducer.VIEW_ID)));
//    	togo.add(new NavigationCase("cancel", new SimpleViewParameters(GroupListProducer.VIEW_ID)));
//        return togo;
//    }

    // new hotness
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        if ("success".equals(actionReturn) || "cancel".equals(actionReturn)) {
            result.resultingView = new SimpleViewParameters(GroupListProducer.VIEW_ID);
        }
    }

}
