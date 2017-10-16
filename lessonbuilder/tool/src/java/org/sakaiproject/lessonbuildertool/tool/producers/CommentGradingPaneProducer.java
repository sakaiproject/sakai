/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lessonbuildertool.tool.producers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.CommentsViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.CommentsGradingPaneViewParameters;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.localeutil.LocaleGetter;                                                                                          
import uk.org.ponder.rsf.builtin.UVBProducer;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;


public class CommentGradingPaneProducer implements ViewComponentProducer, ViewParamsReporter {
	public static final String VIEW_ID = "CommentGradingPane";

	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	private MessageLocator messageLocator;
        private SecurityService securityService;
        private SiteService siteService;
	public LocaleGetter localeGetter;
         
	
	public String getViewID() {
		return VIEW_ID;
	}
	
	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}
	
	public void setSimplePageToolDao(SimplePageToolDao simplePageToolDao) {
		this.simplePageToolDao = simplePageToolDao;
	}
	
	public void setSecurityService(SecurityService a) {
		this.securityService = a;
	}

	public void setSiteService(SiteService s) {
		this.siteService = s;
	}

	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	private class SimpleUser implements Comparable<SimpleUser> {
		public String displayName;
		public String userId;
		public int postCount = 0;
		public Double grade;
		public String uuid = null; // The UUID of a comment, doesn't matter which, so that we can apply grading
		public ArrayList<Long> pages = new ArrayList<Long>();
		
		public int compareTo(SimpleUser user) {
			return displayName.compareTo(user.displayName);
		}
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		CommentsGradingPaneViewParameters params = (CommentsGradingPaneViewParameters) viewparams;
		
		SimplePage currentPage = simplePageToolDao.getPage(params.pageId);
		simplePageBean.setCurrentSiteId(params.siteId);
		simplePageBean.setCurrentPage(currentPage);
		simplePageBean.setCurrentPageId(params.pageId);

		GeneralViewParameters backParams = new GeneralViewParameters(ShowPageProducer.VIEW_ID, params.pageId);
		backParams.setItemId(params.pageItemId);
		backParams.setPath("log");
		
                UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
		    .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));        

		UIInternalLink.make(tofill, "back-link", messageLocator.getMessage("simplepage.go-back"), backParams);
		
		if(simplePageBean.getEditPrivs() != 0 || !simplePageBean.itemOk(params.commentsItemId)) {
			UIOutput.make(tofill, "permissionsError");
			return;
		}
		
		String heading = null;
		if(params.studentContentItem) {
			heading = messageLocator.getMessage("simplepage.student-comments-grading");
		}else {
			heading = messageLocator.getMessage("simplepage.comments-grading");
		}
		
		SimplePageItem commentItem = simplePageToolDao.findItem(params.commentsItemId);
		SimplePage containingPage = simplePageToolDao.getPage(commentItem.getPageId());
		heading = heading.replace("{}", containingPage.getTitle());
		
		UIOutput.make(tofill, "page-header", heading);
		
		List<SimplePageComment> comments;
		
		if(!params.studentContentItem) {
			comments = simplePageToolDao.findComments(params.commentsItemId);
		}else {
			List<SimpleStudentPage> studentPages = simplePageToolDao.findStudentPages(params.commentsItemId);
			
			List<Long> commentsItemIds = new ArrayList<Long>();
			for(SimpleStudentPage p : studentPages) {
				// If the page is deleted, don't show the comments
				if(!p.isDeleted()) {
					commentsItemIds.add(p.getCommentsSection());
				}
			}
			
			comments = simplePageToolDao.findCommentsOnItems(commentsItemIds);
		}
		
		
		ArrayList<String> userIds = new ArrayList<String>();
		HashMap<String, SimpleUser> users = new HashMap<String, SimpleUser>();
		
		// initialize notsubmitted to all userids or groupids
		Set<String> notSubmitted = new HashSet<String>();
		String siteRef = simplePageBean.getCurrentSite().getReference();
		List<User> studentUsers = securityService.unlockUsers("section.role.student", siteRef);
		for (User u: studentUsers)
		    notSubmitted.add(u.getId());
		
		for(SimplePageComment comment : comments) {
			if(comment.getComment() == null || comment.getComment().equals("")) {
				continue;
			}
			
			if(!userIds.contains(comment.getAuthor())) {
				notSubmitted.remove(comment.getAuthor());
				try {
					SimpleUser user = new SimpleUser();
					user.displayName = UserDirectoryService.getUser(comment.getAuthor()).getDisplayName();
					user.postCount++;
					user.userId = comment.getAuthor();
					user.grade = comment.getPoints();
					user.uuid = comment.getUUID();
					
					if(params.studentContentItem) {
						user.pages.add(comment.getPageId());
					}
					
					users.put(comment.getAuthor(), user);
				}catch(Exception ex) {}
			}else {
				SimpleUser user = users.get(comment.getAuthor());
				if(user != null) {
					user.postCount++;
					
					if(params.studentContentItem && !user.pages.contains(comment.getPageId())) {
						user.pages.add(comment.getPageId());
					}
				}
			}
		}
		
		if (notSubmitted.size() > 0) {
		    List<String> missing = new ArrayList<String>();
		    for (String userId: notSubmitted) {
			try {
			    missing.add(UserDirectoryService.getUser(userId).getDisplayName());
			} catch (Exception e) {
			    missing.add(userId);
			}
		    }
		    Collections.sort(missing);
		    UIOutput.make(tofill, "missing-head");
		    UIOutput.make(tofill, "missing-div");
		    for (String name: missing) {
			UIBranchContainer branch = UIBranchContainer.make(tofill, "missing:");
			UIOutput.make(branch, "missing-entry", name);
		    }
		}
		    
		ArrayList<SimpleUser> simpleUsers = new ArrayList<SimpleUser>(users.values());
		Collections.sort(simpleUsers);
		
		if(params.studentContentItem) {
			UIOutput.make(tofill, "unique-header", messageLocator.getMessage("simplepage.grading-unique"));
		}
		
		if(simpleUsers.size() > 0) {
			UIOutput.make(tofill, "gradingTable");
		}else {
			UIOutput.make(tofill, "noEntriesWarning");
		}
		
		if(params.studentContentItem)
		    UIOutput.make(tofill, "clickfiller");

		UIOutput.make(tofill, "clickToSubmit", messageLocator.getMessage("simplepage.update-points")).
			    decorate(new UIFreeAttributeDecorator("title", 
								  messageLocator.getMessage("simplepage.update-points")));

		if (notSubmitted.size() > 0) 
		    UIOutput.make(tofill, "zeroMissing", messageLocator.getMessage("simplepage.zero-missing")).
			    decorate(new UIFreeAttributeDecorator("title", 
								  messageLocator.getMessage("simplepage.zero-missing")));
		
		Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");

		UIForm zeroForm = UIForm.make(tofill, "zero-form");
		if (sessionToken != null)
		    UIInput.make(zeroForm, "zero-csrf", "simplePageBean.csrfToken", sessionToken.toString());
		UIInput.make(zeroForm, "zero-item", "#{simplePageBean.itemId}", Long.toString(params.commentsItemId));
		UICommand.make(zeroForm, "zero", messageLocator.getMessage("simplepage.zero-missing"), "#{simplePageBean.missingCommentsSetZero}");

		for(SimpleUser user : simpleUsers) {
			UIBranchContainer branch = UIBranchContainer.make(tofill, "student-row:");
			
			UIOutput.make(branch, "first-row");
			UIOutput.make(branch, "details-row");
			UIOutput detailsCell = UIOutput.make(branch, "details-cell");
			
			// Set the column span based on which type of item it is.  Student content
			// items have an extra column, so we have to accommodate.
			if(params.studentContentItem) {
				detailsCell.decorate(new UIFreeAttributeDecorator("colspan", "5"));
			}else {
				detailsCell.decorate(new UIFreeAttributeDecorator("colspan", "4"));
			}
			UIOutput.make(branch, "student-name", user.displayName);
			UIOutput.make(branch, "student-total", String.valueOf(user.postCount));
			
			if(params.studentContentItem) {
				UIOutput.make(branch, "student-unique", String.valueOf(user.pages.size()));
			}
			
			// Add the link that will be fetched using Ajax
			CommentsViewParameters eParams = new CommentsViewParameters(CommentsProducer.VIEW_ID);
			eParams.placementId = ToolManager.getCurrentPlacement().getId();
			eParams.itemId = params.commentsItemId;
			eParams.author = user.userId;
			eParams.filter = true;
			eParams.pageItemId = params.pageItemId;
			eParams.studentContentItem = params.studentContentItem;
			eParams.siteId = simplePageBean.getCurrentSiteId();
			eParams.pageId = containingPage.getPageId();
			UIInternalLink.make(branch, "commentsLink", eParams);
			
			// The grading stuff
			UIOutput.make(branch, "student-grade");
			UIOutput.make(branch, "gradingSpan");
			UIOutput.make(branch, "commentsUUID", user.uuid);
			UIOutput.make(branch, "commentPoints",
					(user.grade == null? "" : String.valueOf(user.grade)));
			UIOutput.make(branch, "pointsBox").
			    decorate(new UIFreeAttributeDecorator("title", 
				    messageLocator.getMessage("simplepage.grade-for-student").replace("{}", user.displayName)));
			UIOutput.make(branch, "maxpoints", " / " + (params.studentContentItem?commentItem.getAltPoints() : commentItem.getGradebookPoints()));
			UIOutput.make(branch, "clickToExpand", messageLocator.getMessage("simplepage.click-to-expand")).
			    decorate(new UIFreeAttributeDecorator("title", 
				    messageLocator.getMessage("simplepage.expand-for-student").replace("{}", user.displayName)));

			UIOutput.make(branch, "authorUUID", user.userId);
		}
		
		UIForm gradingForm = UIForm.make(tofill, "gradingForm");

		gradingForm.viewparams = new SimpleViewParameters(UVBProducer.VIEW_ID);

		UIInput idInput = UIInput.make(gradingForm, "gradingForm-id", "gradingBean.id");
		UIInput jsIdInput = UIInput.make(gradingForm, "gradingForm-jsId", "gradingBean.jsId");
		UIInput pointsInput = UIInput.make(gradingForm, "gradingForm-points", "gradingBean.points");
		UIInput typeInput = UIInput.make(gradingForm, "gradingForm-type", "gradingBean.type");
		UIInput csrfInput = UIInput.make(gradingForm, "csrf", "gradingBean.csrfToken", (sessionToken == null ? "" : sessionToken.toString()));

		UIInitBlock.make(tofill, "gradingForm-init", "initGradingForm", new Object[] {idInput, pointsInput, jsIdInput, typeInput, csrfInput, "gradingBean.results"});
	}
	
	public ViewParameters getViewParameters() {
		return new CommentsGradingPaneViewParameters();
	}

}
