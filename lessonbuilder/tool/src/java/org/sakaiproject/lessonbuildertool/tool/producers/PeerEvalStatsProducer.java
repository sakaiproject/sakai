/************************************************************************************
 *
 * Author: Stephen Kane, steve.kane@rutgers.edu
 *
 * Copyright (c) 2013 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
 
package org.sakaiproject.lessonbuildertool.tool.producers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Set;

import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.PeerEvalStatsViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.SimplePagePeerEvalResult;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.authz.cover.SecurityService;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class PeerEvalStatsProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {
	public static final String VIEW_ID = "PeerEvalStats";
	
	private SimplePageBean simplePageBean;
	private ShowPageProducer showPageProducer;
	private MessageLocator messageLocator;
	private LocaleGetter localeGetter;
	private SimplePageToolDao simplePageToolDao;
	private HashMap<String, String> anonymousLookup = new HashMap<String, String>();
	private HashMap<Long, String> itemToPageowner = null;
	private String currentUserId;
	private String owner = null;
	private boolean canEditPage = false;
        Locale M_locale = null;
        DateFormat df = null;
        DateFormat dfTime = null;
        DateFormat dfDate = null;
	
	public String getViewID() {
		return VIEW_ID;
	}
	
	UIBranchContainer studentInfo;
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		PeerEvalStatsViewParameters params = (PeerEvalStatsViewParameters) viewparams;		
		try {
			simplePageBean.updatePageObject(params.getSendingPage(), false);
		} catch (Exception e) {
			System.out.println("PeerEval permission exception " + e);
			return;
		}
		canEditPage = simplePageBean.canEditPage();
		
		if(!canEditPage || params.getSendingPage() == -1)
			return;
		
		UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
	    .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));   
	    
	    Set<String> users;
		Site site = null;
		try {
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
			users = site.getUsers();
		} catch (Exception impossible) {
			System.out.println("Can't find site/users - PeerEvalStatsProducer.java");
			return;
		}
		
	    UIOutput.make(tofill, "expand-collapse");
	    UIOutput.make(tofill, "expand-all", messageLocator.getMessage("simplepage.expand-all"));
	    UIOutput.make(tofill, "collapse-all",messageLocator.getMessage("simplepage.collapse-all"));

		try {
			long studentContentBoxId = params.getItemId();
			SimplePageItem item = simplePageToolDao.findItem(studentContentBoxId);

			makePeerRubric(tofill,item);
			
			List<SimpleStudentPage> studentPages = simplePageToolDao.findStudentPages(studentContentBoxId);
		    Collections.sort(studentPages, new Comparator<SimpleStudentPage>() {
			    public int compare(SimpleStudentPage o1, SimpleStudentPage o2) {
				String title1 = o1.getTitle();
				if (title1 == null)
				    title1 = "";
				String title2 = o2.getTitle();
				if (title2 == null)
				    title2 = "";
				return title1.compareTo(title2);
			    }
			});
		    
		    for(SimpleStudentPage page : studentPages) {
				if(page.isDeleted()) continue;
				
				studentInfo = UIBranchContainer.make(tofill, "peer-eval-gradee-branch:");
				UIOutput.make(studentInfo, "user-name", ""+UserDirectoryService.getUser(page.getOwner()).getDisplayName());
				UIOutput.make(studentInfo, "user-id", ""+page.getOwner());
				//remove user from non-participant user list
				if(users != null)
					users.remove(page.getOwner());
				UIOutput.make(studentInfo, "user-pageid", ""+page.getPageId());
				ArrayList<PeerEvaluation> graders = getGraders(page.getPageId(), page.getOwner());
				makeGraders(studentInfo, graders);
		    }
		    
		    GeneralViewParameters view = new GeneralViewParameters();
		    view.viewID = ShowPageProducer.VIEW_ID;
		    view.setItemId(studentContentBoxId);  //returns to page that contains the student content box item.
		    UIInternalLink.make(tofill, "back", messageLocator.getMessage("simplepage.back"), view);
		    
		    // users is now set of users in site without page
		    // remove any who are instructor or reviewer
		    

		    String ref = "/site/" + site.getId();
		    for (String userId: users) {
			if (SecurityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref) ||
			    SecurityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_SEE_ALL, ref))
			    users.remove(userId);
		    }

		    //make inactive user list
		    if(!users.isEmpty()){
		    	UIBranchContainer inactiveMemberBranch;
				UIOutput.make(tofill, "inactive-member-collection");
				for(String userId : users){
					inactiveMemberBranch = UIBranchContainer.make(tofill, "inactive-member:");
					UIOutput.make(inactiveMemberBranch, "inactive-member-id", userId);
					UIOutput.make(inactiveMemberBranch, "inactive-member-name", UserDirectoryService.getUser(userId).getDisplayName());
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("peer eval error " + e);
		};

	}
	
	public void setShowPageProducer(ShowPageProducer showPageProducer) {
		this.showPageProducer = showPageProducer;
	}
	
	public void setSimplePageBean(SimplePageBean bean) {
		simplePageBean = bean;
	}
	
	public void setMessageLocator(MessageLocator locator) {
		messageLocator = locator;
	}
	
	public void setLocaleGetter(LocaleGetter getter) {
		localeGetter = getter;
	}

	public void setSimplePageToolDao(SimplePageToolDao simplePageToolDao) {
		this.simplePageToolDao = simplePageToolDao;
	}
	
	public ViewParameters getViewParameters() {
		return new PeerEvalStatsViewParameters();
	}
	
	private void makePeerRubric(UIContainer parent, SimplePageItem i)
	{
		class RubricRow implements Comparable{
			public int id;
			public String text;
			public RubricRow(int id, String text){ this.id=id; this.text=text;}
			public int compareTo(Object o){
				RubricRow r = (RubricRow)o;
				if(id==r.id)
					return 0;
				if(id>r.id)
					return 1;
				return -1;
			}
		}
		
		UIOutput.make(parent, "peer-eval-rubric");
		ArrayList<RubricRow> rows = new ArrayList<RubricRow>();
		List categories = (List) i.getJsonAttribute("rows");
		if(categories != null){
			for(Object o: categories){
				Map cat = (Map)o;
				rows.add(new RubricRow(Integer.parseInt(String.valueOf(cat.get("id"))), String.valueOf(cat.get("rowText"))));
			}
		}
		//else{System.out.println("This rubric has no rows.");}
		
		Collections.sort(rows);
		for(RubricRow row : rows){
			UIBranchContainer peerReviewRows = UIBranchContainer.make(parent, "peer-eval-row:");
			UIOutput.make(peerReviewRows, "peer-eval-text", row.text);
		}
	}
	
	private ArrayList<PeerEvaluation> getGraders(Long pageId, String owner){

		ArrayList<PeerEvaluation> myEvaluations = new ArrayList<PeerEvaluation>(); 
		
		List<SimplePagePeerEvalResult> evaluations = simplePageToolDao.findPeerEvalResultByOwner(pageId, owner);
		if(evaluations!=null && evaluations.size()!=0)
			for(SimplePagePeerEvalResult eval : evaluations){
				PeerEvaluation target=new PeerEvaluation(eval.getRowText(), eval.getColumnValue());
				int targetIndex=myEvaluations.indexOf(target);
				if(targetIndex!=-1){
					try{
						myEvaluations.get(targetIndex).addGrader(UserDirectoryService.getUser(eval.getGrader()).getDisplayName(), eval.getGrader());
					}catch(Exception e){
						myEvaluations.get(targetIndex).addGrader("User not found.","none");
					}
				}
				else{
					try{
						target.addGrader(UserDirectoryService.getUser(eval.getGrader()).getDisplayName(), eval.getGrader());
						myEvaluations.add(target);
					}catch(Exception e){
						target.addGrader("User not found.","none");
						myEvaluations.add(target);
					}
				}
			}
		//else
		//	System.out.println("evaluations is empty/null;");
				
		return myEvaluations;
	}
	
	public void makeGraders(UIContainer parent, ArrayList<PeerEvaluation> myEvaluations){
		if(!myEvaluations.isEmpty())
			for(PeerEvaluation eval: myEvaluations){
				UIBranchContainer evalData = UIBranchContainer.make(parent, "peer-eval-data-grade:");
				UIOutput.make(evalData, "peer-eval-row-text", eval.category);
				UIOutput.make(evalData, "peer-eval-grade", String.valueOf(eval.grade));
				for(int i = 0; i < eval.graderNames.size() ; i++){
					String graderName=eval.graderNames.get(i);
					String graderId=eval.graderIds.get(i);
					UIBranchContainer graderBranch = UIBranchContainer.make(evalData, "peer-eval-grader-branch:");
					UIOutput.make(graderBranch, "peer-eval-grader-name", graderName);
					UIOutput.make(graderBranch, "peer-eval-grader-id", graderId);
				}
				UIOutput.make(evalData, "peer-eval-count", ""+eval.graderNames.size());
			}
	}
	
	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));

		return togo;
	}
	
	class PeerEvaluation{
		String category;
		public int grade;
		public ArrayList<String> graderNames;
		public ArrayList<String> graderIds;
		public PeerEvaluation(String category, int grade){
			this.category=category;this.grade=grade;
			graderNames=new ArrayList<String>();
			graderIds=new ArrayList<String>();
		}
		public void addGrader(String graderName, String graderId){graderNames.add(graderName);graderIds.add(graderId);}
		public boolean equals(Object o){
			if ( !(o instanceof PeerEvaluation) ) return false;
			PeerEvaluation pe = (PeerEvaluation)o;
			return category.equals(pe.category) && grade==pe.grade;
		}
		public String toString(){return category + " " + grade + " [ + arraylist of graders + ]";}
	}
}
