/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
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

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.CommentsViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class CommentsProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {
	public static final String VIEW_ID = "Comments";

	private SimplePageBean simplePageBean;
	private MessageLocator messageLocator;
	private LocaleGetter localeGetter;
	private SimplePageToolDao simplePageToolDao;
	private HashMap<String, String> anonymousLookup = new HashMap<String, String>();
	private HashMap<Long, String> itemToPageowner = null;
	private String currentUserId;
	private String owner = null;
	private boolean filter;
	private boolean canEditPage = false;
        Locale M_locale = null;
        DateFormat df = null;
        DateFormat dfTime = null;
        DateFormat dfDate = null;
	
	public String getViewID() {
		return VIEW_ID;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		CommentsViewParameters params = (CommentsViewParameters) viewparams;
		filter = params.filter;
		
		// set up locale
		String langLoc[] = localeGetter.get().toString().split("_");
		if (langLoc.length >= 2) {
			if ("en".equals(langLoc[0]) && "ZA".equals(langLoc[1])) {
				M_locale = new Locale("en", "GB");
			} else {
				M_locale = new Locale(langLoc[0], langLoc[1]);
			}
		} else {
			M_locale = new Locale(langLoc[0]);
		}

 		df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, new ResourceLoader().getLocale());
 		df.setTimeZone(TimeService.getLocalTimeZone());
 		dfTime = DateFormat.getTimeInstance(DateFormat.SHORT, M_locale);
 		dfTime.setTimeZone(TimeService.getLocalTimeZone());
 		dfDate = DateFormat.getDateInstance(DateFormat.MEDIUM, M_locale);
 		dfDate.setTimeZone(TimeService.getLocalTimeZone());
 
		// errors redirect back to ShowPage. But if this is embedded in the page, ShowPage
		// will call us again. This is very hard for the user to recover from. So trap
		// all possible errors. It may result in an incomplete page or something invalid,
		// but better that than an infinite recursion.

		try {
			
			SimplePage currentPage = simplePageToolDao.getPage(params.pageId);
			simplePageBean.setCurrentSiteId(params.siteId);
			simplePageBean.setCurrentPage(currentPage);
			simplePageBean.setCurrentPageId(params.pageId);

			UIOutput.make(tofill, "mainlist").decorate(new UIFreeAttributeDecorator("aria-label", messageLocator.getMessage("simplepage.comments-section")));

			SimplePageItem commentsItem = simplePageToolDao.findItem(params.itemId);
			if(commentsItem != null && commentsItem.getSakaiId() != null && !commentsItem.getSakaiId().equals("")) {
				SimpleStudentPage studentPage = simplePageToolDao.findStudentPage(Long.valueOf(commentsItem.getSakaiId()));
				if(studentPage != null) {
					owner = studentPage.getOwner();
				}
			}
			
			if(params.deleteComment != null) {
				simplePageBean.deleteComment(params.deleteComment);
			}
			
			List<SimplePageComment> comments;
			
			if(!filter && !params.studentContentItem) {
				comments = (List<SimplePageComment>) simplePageToolDao.findComments(params.itemId);
			}else if(filter && !params.studentContentItem) {
				comments = (List<SimplePageComment>) simplePageToolDao.findCommentsOnItemByAuthor(params.itemId, params.author);
			}else if(filter && params.studentContentItem) {
				List<SimpleStudentPage> studentPages = simplePageToolDao.findStudentPages(params.itemId);
				Site site = SiteService.getSite(currentPage.getSiteId());
				itemToPageowner = new HashMap<Long, String>();
				List<Long> commentsItemIds = new ArrayList<Long>();
				for(SimpleStudentPage p : studentPages) {
					// If the page is deleted, don't show the comments
					if(!p.isDeleted()) {
						commentsItemIds.add(p.getCommentsSection());
						String pageOwner = p.getOwner();
						String pageGroup = p.getGroup();
						if (pageGroup != null)
						    pageOwner = pageGroup;
						try {
						    String o = null;
						    if (pageGroup != null)
							o = site.getGroup(pageGroup).getTitle();
						    else
							o = UserDirectoryService.getUser(pageOwner).getDisplayName();
						    if (o != null)
							pageOwner = o;
						} catch (Exception ignore) {};
						itemToPageowner.put(p.getCommentsSection(), pageOwner);
					}
				}
				
				comments = simplePageToolDao.findCommentsOnItemsByAuthor(commentsItemIds, params.author);
			}else {
				List<SimpleStudentPage> studentPages = simplePageToolDao.findStudentPages(params.itemId);
				
				List<Long> commentsItemIds = new ArrayList<Long>();
				for(SimpleStudentPage p : studentPages) {
					commentsItemIds.add(p.getCommentsSection());
				}
				
				comments = simplePageToolDao.findCommentsOnItems(commentsItemIds);
			}
		
			// Make sure everything is chronological
			Collections.sort(comments, new Comparator<SimplePageComment>() {
				public int compare(SimplePageComment c1, SimplePageComment c2) {
					if (itemToPageowner != null) {
					    String o1 = itemToPageowner.get(c1.getItemId());
					    String o2 = itemToPageowner.get(c2.getItemId());
					    if (o1 != o2) {
						if (o1 == null)
						    return 1;
						return o1.compareTo(o2);
					    }
					}
					return c1.getTimePosted().compareTo(c2.getTimePosted());
				}
			});
			
			currentUserId = UserDirectoryService.getCurrentUser().getId();
			
			boolean anonymous = simplePageBean.findItem(params.itemId).isAnonymous();
			if(anonymous) {
				int i = 1;
				for(SimplePageComment comment : comments) {
					if(!anonymousLookup.containsKey(comment.getAuthor())) {
						anonymousLookup.put(comment.getAuthor(), messageLocator.getMessage("simplepage.anonymous") + " " + i);
						i++;
					}
				}
			}
			
			boolean highlighted = false;
			
			// We don't want page owners to edit or grade comments on their page
			// at the moment.  Perhaps add option?
			canEditPage = simplePageBean.getEditPrivs() == 0;
			
			boolean showGradingMessage = canEditPage && commentsItem.getGradebookId() != null && !params.filter;
			
			Date lastViewed = null;  // remains null if never viewed before
			SimplePageLogEntry log = simplePageBean.getLogEntry(params.itemId);
			if (log != null)
			    lastViewed = log.getLastViewed();

			int newItems = 0;

			// Remove any "phantom" comments. So that the anonymous order stays the same,
			// comments are deleted by removing all content.  Also check to see if any comments
			// have been graded yet.  Finally, if we're filtering, it takes out comments not by
			// this author.
			for(int i = comments.size()-1; i >= 0; i--) {
				if(comments.get(i).getComment() == null || comments.get(i).getComment().equals("")) {
					comments.remove(i);
				}else if(params.filter && !comments.get(i).getAuthor().equals(params.author)) {
					comments.remove(i);
				}else{
				    if(showGradingMessage && comments.get(i).getPoints() != null)
					showGradingMessage = false;
				    if (lastViewed == null)
					newItems ++;  // all items are new if never viewed
				    else if (comments.get(i).getTimePosted().after(lastViewed))
					newItems ++;
				}
			}
			
			// update date only if we actually are going to see all the comments, and it's
			// not a weird view (i.e. filter is on)
			//   The situation with items <= 5 is actually dubious. The user has them on the
			// screen, but there's no way to know whether he's actually seen them. Some users
			// are going to be surprised either way we do it.
			if (params.showAllComments || params.showNewComments || (!params.filter && newItems <= 5)) {
			    if (log != null) {
				simplePageBean.update(log);
			    } else {
				log = simplePageToolDao.makeLogEntry(currentUserId, params.itemId, null);
				simplePageBean.saveItem(log);
			    }
			}

			// Make sure we don't show the grading message if there's nothing to grade.
			if(comments.size() == 0) {
				showGradingMessage = false;
			}
			
			boolean editable = false;
			
			if(comments.size() <= 5 || params.showAllComments || params.filter) {
				for(int i = 0; i < comments.size(); i++) {
					// We don't want them editing on the grading screen, which is why we also check filter.
					boolean canEdit = simplePageBean.canModifyComment(comments.get(i), canEditPage) && !params.filter;
					
					printComment(comments.get(i), tofill, (params.postedComment == comments.get(i).getId()), anonymous, canEdit, params, commentsItem, currentPage);
					if(!highlighted) {
						highlighted = (params.postedComment == comments.get(i).getId());
					}
					
					if(!editable) editable = canEdit;
				}
			}else {

			        UIBranchContainer container = UIBranchContainer.make(tofill, "commentList:");
				UIOutput.make(container, "commentDiv");
				// UIBranchContainer container = UIBranchContainer.make(tofill, "commentDiv:");
				CommentsViewParameters eParams = new CommentsViewParameters(VIEW_ID);
				eParams.placementId = params.placementId;
				eParams.itemId = params.itemId;
				eParams.showAllComments=true;
				eParams.showNewComments=false;
				eParams.pageId = params.pageId;
				eParams.siteId = params.siteId;
				UIInternalLink.make(container, "to-load", eParams);
				UIOutput.make(container, "load-more-link", messageLocator.getMessage("simplepage.see_all_comments").replace("{}", Integer.toString(comments.size())));

				if (!params.showNewComments && newItems > 5) {
				    container = UIBranchContainer.make(tofill, "commentList:");
				    UIOutput.make(container, "commentDiv");
				    // UIBranchContainer container = UIBranchContainer.make(tofill, "commentDiv:");
				    eParams = new CommentsViewParameters(VIEW_ID);
				    eParams.placementId = params.placementId;
				    eParams.itemId = params.itemId;
				    eParams.showAllComments=false;
				    eParams.showNewComments=true;
				    eParams.pageId = params.pageId;
				    eParams.siteId = params.siteId;
				    UIInternalLink.make(container, "to-load", eParams);
				    UIOutput.make(container, "load-more-link", messageLocator.getMessage("simplepage.see_new_comments").replace("{}", Integer.toString(newItems)));
				}
				
				int start = comments.size()-5;
				if (params.showNewComments)
				    start = 0;

				// Show 5 most recent comments
				for(int i = start; i < comments.size(); i++) {
				    if (!params.showNewComments || lastViewed == null || comments.get(i).getTimePosted().after(lastViewed)) {
					boolean canEdit = simplePageBean.canModifyComment(comments.get(i), canEditPage);
					printComment(comments.get(i), tofill, (params.postedComment == comments.get(i).getId()), anonymous, canEdit, params, commentsItem, currentPage);
					if(!highlighted) {
						highlighted = (params.postedComment == comments.get(i).getId());
					}
				
					if(!editable) editable = canEdit;
				    }
				}
			}
			
			if(highlighted) {
				// We have something to highlight
				UIOutput.make(tofill, "highlightScript");
			}
			
			if(showGradingMessage) {
				UIOutput.make(tofill, "gradingAlert");
			}
			
			if(anonymous && canEditPage && comments.size() > 0 && lastViewed == null) {
				// Tells the admin that they can see the names, but everyone else can't
				UIOutput.make(tofill, "anonymousAlert");
			}else if(editable && simplePageBean.getEditPrivs() != 0) {
				// Warns user that they only have 30 mins to edit.
				UIOutput.make(tofill, "editAlert");
			}

			

		} catch (Exception e) {
			log.info("comments error " + e);
		};

	}
	
	private Long lastTitle = -1L;

	public void printComment(SimplePageComment comment, UIContainer tofill, boolean highlight, boolean anonymous,
				 boolean showModifiers, CommentsViewParameters params, SimplePageItem commentsItem, SimplePage currentPage) {
		if (canEditPage && itemToPageowner != null && comment.getItemId() != lastTitle) {
		    UIBranchContainer commentContainer = UIBranchContainer.make(tofill, "commentList:");
		    UIOutput.make(commentContainer, "commentTitle", messageLocator.getMessage("simplepage.comments-grading").replace("{}", itemToPageowner.get(comment.getItemId())));
		    lastTitle = comment.getItemId();
		}

		// print title if this is a comment on a different page. Normally this
		// shold only happen for subpages of student pages
		String pageTitle = null;
		if (currentPage.getPageId() != comment.getPageId()) {
		    SimplePage commentPage = simplePageBean.getPage(comment.getPageId());
		    pageTitle = commentPage.getTitle();
		}

		UIBranchContainer commentContainer = UIBranchContainer.make(tofill, "commentList:");
		UIOutput.make(commentContainer, "commentDiv");
		if(highlight) commentContainer.decorate(new UIStyleDecorator("highlight-comment"));
		
		if(!filter && params.author != null && params.author.equals(comment.getAuthor())) {
			commentContainer.decorate(new UIStyleDecorator("backgroundHighlight"));
		}
		
		String author;
		
		if(!anonymous) {
			try {
				User user = UserDirectoryService.getUser(comment.getAuthor());
				author = user.getDisplayName();
			}catch(Exception ex) {
				author = messageLocator.getMessage("simplepage.comment-unknown-user");
				log.error(ex.getMessage(), ex);
			}
		}else {
			author = anonymousLookup.get(comment.getAuthor());
			
			if(comment.getAuthor().equals(owner)) {
				author = messageLocator.getMessage("simplepage.comment-author-owner");
			}
			
			if(author == null) author = "Anonymous User"; // Shouldn't ever occur
			
			if(simplePageBean.getEditPrivs() == 0) {
				try {
					User user = UserDirectoryService.getUser(comment.getAuthor());
					author += " (" + user.getDisplayName() + ")";
				}catch(Exception ex) {
					author += " (" + messageLocator.getMessage("simplepage.comment-unknown-user") + ")";
				}
			}else if(comment.getAuthor().equals(currentUserId)) {
				author += " (" + messageLocator.getMessage("simplepage.comment-you") + ")";
			}

		}
		
		UIOutput authorOutput = UIOutput.make(commentContainer, "userId", author);
		
		if(comment.getAuthor().equals(currentUserId)) {
			authorOutput.decorate(new UIStyleDecorator("specialCommenter"));
			authorOutput.decorate(new UIStyleDecorator("personalComment"));
		}else if(comment.getAuthor().equals(owner)) {
			authorOutput.decorate(new UIStyleDecorator("specialCommenter"));
			authorOutput.decorate(new UIStyleDecorator("ownerComment"));
		}
		
		if (pageTitle != null)
		    UIOutput.make(commentContainer, "pageTitle", pageTitle);

		String timeDifference = getTimeDifference(comment.getTimePosted().getTime());
		
		UIOutput.make(commentContainer, "timePosted", timeDifference);
		
		if(showModifiers) {
			UIOutput.make(commentContainer, "deleteSpan");
			
			CommentsViewParameters eParams = (CommentsViewParameters) params.copy();
			eParams.placementId = params.placementId;
			eParams.deleteComment = comment.getUUID();
			eParams.pageId = params.pageId;
			eParams.siteId = params.siteId;
			
			UIInternalLink.make(commentContainer, "deleteCommentURL", eParams);
			UIOutput.make(commentContainer, "deleteComment").
			    decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.comment-delete").replace("{}", author)));
			UIOutput.make(commentContainer, "editComment").
			    decorate(new UIFreeAttributeDecorator("onclick", "edit($(this), " + comment.getId() + ");")).
			    decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.comment-edit").replace("{}", author)));
			
			if(!filter && simplePageBean.getEditPrivs() == 0 && commentsItem.getGradebookId() != null) {
				UIOutput.make(commentContainer, "gradingSpan");
				UIOutput.make(commentContainer, "commentsUUID", comment.getUUID());
				UIOutput.make(commentContainer, "commentPoints",
						(comment.getPoints() == null? "" : String.valueOf(comment.getPoints())));
				UIOutput.make(commentContainer, "pointsBox").decorate(
					 new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.grade-for-student").replace("{}", author)));
				
				UIOutput.make(commentContainer, "maxpoints", " / " + commentsItem.getGradebookPoints());
				UIOutput.make(commentContainer, "authorUUID", comment.getAuthor());
			}
		}
		
		if(filter && simplePageBean.getEditPrivs() == 0) {
			UIOutput.make(commentContainer, "contextSpan");

			// because this is called via /faces, the full Sakai context is not set up.
			// in particular, UIInternalLink will generate the wrong thing. Thus we
			// make up a full URL ourselves.
			String pars = "/portal/tool/" + URLEncoder.encode(params.placementId) + "/ShowPage?path=none" +
			    "&author=" + URLEncoder.encode(comment.getAuthor());
			// Need to provide the item ID
			if(!params.studentContentItem && params.pageItemId != -1L) {
			    pars += "&itemId=" + URLEncoder.encode(Long.toString(params.pageItemId));
			}
			UILink contextLink = UILink.make(commentContainer, "contextLink", messageLocator.getMessage("simplepage.show-context"), pars);
			if (itemToPageowner == null)
			    contextLink.decorate( new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.context-link-title-1").
									       replace("{}", author)));
			else
			    contextLink.decorate( new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.context-link-title-2").
									       replace("{1}", author).replace("{2}", itemToPageowner.get(comment.getItemId()))));
		}
		
		String dateString = df.format(comment.getTimePosted());

		if (!filter)
		    UIOutput.make(commentContainer, "replyTo").
			decorate(new UIFreeAttributeDecorator("onclick", "replyToComment($(this),'" + 
							      messageLocator.getMessage("simplepage.in-reply-to").replace("{1}", author).replace("{2}", dateString) + "')")).
			decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.comment-reply").replace("{}",author)));

		if(!comment.getHtml()) {
			UIOutput.make(commentContainer, "comment", comment.getComment());
		}else {
			UIVerbatim.make(commentContainer, "comment", comment.getComment());
		}
	}
	
	public String getTimeDifference(long timeMillis) {
		long difference = Math.round((System.currentTimeMillis() - timeMillis) / 1000.0d); // In seconds
		
		// These constants are calculated to take rounding into effect, and try to give a fairly
		// accurate representation of the time difference using words.
		
		String descrip = "";
		
		if(difference < 45) {
			descrip = messageLocator.getMessage("simplepage.seconds");
		}else if(difference < 90) {
			descrip = messageLocator.getMessage("simplepage.one_min");
		}else if(difference < 3570) { // 2 mins --> 59 mins
			long minutes = Math.max(2, Math.round(difference / 60.0d));
			descrip = messageLocator.getMessage("simplepage.x_min").replace("{}", String.valueOf(minutes));
		}else if(difference < 7170) {
			descrip = messageLocator.getMessage("simplepage.one_hour");
		}else if(difference < 84600) { // 2 hours --> 23 hours
			long hours = Math.max(2, Math.round(difference / 3600.0d));
			descrip = messageLocator.getMessage("simplepage.x_hour").replace("{}", String.valueOf(hours));
		}else if(difference < 129600) {
			descrip = messageLocator.getMessage("simplepage.one_day");
		}else if(difference < 2548800) { // 2 days --> 29 days
			long days = Math.max(2, Math.round(difference / 86400.0d));
			descrip = messageLocator.getMessage("simplepage.x_day").replace("{}", String.valueOf(days));
		}else if(difference < 3888000) {
			descrip = messageLocator.getMessage("simplepage.one_month");
		}else if(difference < 29808000) { // 2 months --> 11 months
			long months = Math.max(2, Math.round(difference / 2592000.0d));
			descrip = messageLocator.getMessage("simplepage.x_month").replace("{}", String.valueOf(months));
		}else if(difference < 47304000) {
			descrip = messageLocator.getMessage("simplepage.one_year");
		}else { // 2+ years
			long years = Math.max(2, Math.round(difference / 31536000.0d));
			descrip = messageLocator.getMessage("simplepage.x_year").replace("{}", String.valueOf(years));
		}
		
		Date d = new Date(timeMillis);
		Date now = new Date();
		
		Calendar cpost = Calendar.getInstance(TimeService.getLocalTimeZone(), M_locale);
		Calendar cnow = Calendar.getInstance(TimeService.getLocalTimeZone(), M_locale);
		cpost.setTime(d);
		cnow.setTime(now);

		if(cpost.get(Calendar.MONTH) == cnow.get(Calendar.MONTH) &&
		   cpost.get(Calendar.DATE) == cnow.get(Calendar.DATE) &&
		   cpost.get(Calendar.YEAR) == cnow.get(Calendar.YEAR)) {
			return dfTime.format(d) + " (" + descrip + ")";
		}else if(d.getYear() == now.getYear()) {
			return dfDate.format(d) + " (" + descrip + ")";
		}else {
			return dfDate.format(d) + " (" + descrip + ")";
		}
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
		return new CommentsViewParameters();
	}
	
	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		
		GeneralViewParameters commentsParameters = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
		commentsParameters.postedComment = true;
		togo.add(new NavigationCase("added-comment", commentsParameters));
		
		return togo;
	}
	
}
