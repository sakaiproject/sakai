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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.PeerEvalStatsViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.SimplePagePeerEvalResult;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;

@Slf4j
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
			log.info("PeerEval permission exception " + e);
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
			log.info("Can't find site/users - PeerEvalStatsProducer.java");
			return;
		}
		
	    UIOutput.make(tofill, "expand-collapse");
	    UIOutput.make(tofill, "expand-all", messageLocator.getMessage("simplepage.expand-all"));
	    UIOutput.make(tofill, "collapse-all",messageLocator.getMessage("simplepage.collapse-all"));

		try {

		    // outer loop on pages
		    // inner loop on targets of evaluation
		    // normally there's one evalution per page, but when individuals in groups
		    //    are being evaluated there's one evaluation per person in the page's group

		        // top level item, under which the student pages lie
			long studentContentBoxId = params.getItemId();
			SimplePageItem item = simplePageToolDao.findItem(studentContentBoxId);

			// need map from row id to text, since new format entries use the ID
			Map<Long, String> rowMap = new HashMap<Long, String>();
			List<Map> categories = (List<Map>) item.getJsonAttribute("rows");
			if (categories == null)   // not valid to do update on item without rubic
			    return;
			for (Map cat: categories) {
			    String rowText = String.valueOf(cat.get("rowText"));
			    String rowId = String.valueOf(cat.get("id"));
			    rowMap.put(new Long(rowId), rowText);
			}

			makePeerRubric(tofill,item);
			// if rubric is for a group, we need list of all allowed groups so we can
			// see which haven't created a page
			Set<String> groups = null;
			boolean grouped = (item.isGroupOwned() && !"true".equals(item.getAttribute("group-eval-individual")));
			boolean individual = (item.isGroupOwned() && "true".equals(item.getAttribute("group-eval-individual")));

			if (grouped || individual) {
			    String allowedString = item.getOwnerGroups();
			    if (allowedString != null)
				groups = new HashSet<String>(Arrays.asList(allowedString.split(",")));
			}
			
			// map each page to an object that we can use for sorting and printing title
			class Target {
			    String id;
			    String name;
			    String sort;
			}

			// page pageid to target
			Map<Long, Target> targetMap = new HashMap<Long, Target>();
			List<SimpleStudentPage> studentPages = simplePageToolDao.findStudentPages(studentContentBoxId);
			for (SimpleStudentPage page: studentPages) {
			    Target target = new Target();
			    if (grouped || individual) {
				String group = page.getGroup();
				target.name = simplePageBean.getCurrentSite().getGroup(group).getTitle();
				target.sort = target.name;
			    } else {
				try {
				    User u = UserDirectoryService.getUser(page.getOwner());
				    target.name = u.getDisplayName();
				    target.sort = u.getSortName();
				} catch (Exception e) {
				    target.name = page.getOwner();
				    target.sort = page.getOwner();
				}
			    }
			    targetMap.put(page.getId(), target);
			}				    

		    Collections.sort(studentPages, new Comparator<SimpleStudentPage>() {
			    public int compare(SimpleStudentPage o1, SimpleStudentPage o2) {
				return targetMap.get(o1.getId()).sort.compareTo(targetMap.get(o2.getId()).sort);
			    }
			});
		    
		    for(SimpleStudentPage page : studentPages) {
		    	   if(page.isDeleted()) continue;
				
			   String pageId = Long.toString(page.getPageId());
			   
			   // normally just person or group being evaluated,
			   // but for eval individuals in group, it's all the individuals in the group
			   List<Target> evalList = new ArrayList<Target>();

			   if (individual) {
			       List<String>groupMembers = simplePageBean.studentPageGroupMembers(item, page.getGroup());
			       for (String userId: groupMembers) {
				   Target t = new Target();
				   t.id = userId;
				   User u = null;
				   try {
				       u = UserDirectoryService.getUser(userId);
				   } catch (Exception e) {
				       continue; // user no longer exists?
				   }
				   if (u == null)
				       continue;
				   t.name = simplePageBean.getCurrentSite().getGroup(page.getGroup()).getTitle() + ": " + u.getDisplayName();
				   t.sort = u.getSortName();
				   evalList.add(t);
			       }
			   } else {
			       Target t = new Target();
			       t.name = targetMap.get(page.getId()).name;
			       t.id = page.getOwner();
			       if (grouped)
				   t.id = page.getGroup();
			       evalList.add(t);
			   }

			   Collections.sort(evalList, new Comparator<Target>() {
				public int compare(Target o1, Target o2) {
				    return o1.sort.compareTo(o2.sort);
				}
			    });

			   for (Target target: evalList) {
				studentInfo = UIBranchContainer.make(tofill, "peer-eval-gradee-branch:");
				UIOutput.make(studentInfo, "user-name", target.name);
				UIOutput.make(studentInfo, "user-id", target.id);
				UIOutput.make(studentInfo, "user-pageid", pageId);
				// for grouped, we need list of members in the group

				if (grouped) {
				    UIOutput.make(studentInfo, "peer-eval-gradee-members");
				    List<String>groupMembers = simplePageBean.studentPageGroupMembers(item, page.getGroup());
				    for (String member: groupMembers) {
					User u = null;
					try {
					    u = UserDirectoryService.getUser(member);
					} catch (Exception e) {
					    continue; // user no longer exists?
					}
					if (u == null)
					    continue;
					UIContainer memberInfo = UIBranchContainer.make(studentInfo, "gradee-member:");
					UIOutput.make(memberInfo, "gradee-member-id", member);
					UIOutput.make(memberInfo, "gradee-member-name", u.getDisplayName());
					users.remove(member);
				   }
				} else
				    users.remove(target.id);
				ArrayList<PeerEvaluation> graders = null;
				if (grouped)
				    graders = getGraders(page.getPageId(), page.getOwner(), page.getGroup(), rowMap);
				else
				    graders = getGraders(page.getPageId(), target.id, null, rowMap);
				makeGraders(studentInfo, graders);
			   }

		    }

		    GeneralViewParameters view = new GeneralViewParameters();
		    view.viewID = ShowPageProducer.VIEW_ID;
		    view.setItemId(studentContentBoxId);  //returns to page that contains the student content box item.
		    UIInternalLink.make(tofill, "back", messageLocator.getMessage("simplepage.back"), view);
		    
		    // users is now set of users in site without page
		    // or for grouped pages, groups is
		    // remove any who are instructor or reviewer

		    String ref = "/site/" + site.getId();
			
		    Iterator<String> userIt = users.iterator();
		    if (!(grouped || individual)) {
			while (userIt.hasNext()) {
			    String userId = userIt.next();
			    if (SecurityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref) ||
				SecurityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_SEE_ALL, ref))
				userIt.remove();
			}
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
			log.info("peer eval error " + e);
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
		//else{log.info("This rubric has no rows.");}
		
		Collections.sort(rows);
		for(RubricRow row : rows){
			UIBranchContainer peerReviewRows = UIBranchContainer.make(parent, "peer-eval-row:");
			UIOutput.make(peerReviewRows, "peer-eval-text", row.text);
		}
	}
	
	private ArrayList<PeerEvaluation> getGraders(Long pageId, String owner, String ownerGroup, Map<Long, String> rowMap){

		ArrayList<PeerEvaluation> myEvaluations = new ArrayList<PeerEvaluation>(); 
		
		List<SimplePagePeerEvalResult> evaluations = simplePageToolDao.findPeerEvalResultByOwner(pageId, owner, ownerGroup);
		if(evaluations!=null && evaluations.size()!=0)
			for(SimplePagePeerEvalResult eval : evaluations){
				String rowText = eval.getRowText();
				if (eval.getRowId() != 0L)
				    rowText = rowMap.get(eval.getRowId());
				if (rowText == null)
				    continue;  // should be impossible
				PeerEvaluation target=new PeerEvaluation(rowText, eval.getColumnValue());
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
		//	log.info("evaluations is empty/null;");
				
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
