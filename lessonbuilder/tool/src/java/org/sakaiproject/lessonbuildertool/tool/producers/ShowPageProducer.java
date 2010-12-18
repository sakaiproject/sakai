/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * This was was originally part of Simple Page Tool and was
 *
 * Copyright (c) 2007 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 *
 * The author was Joshua Ryan josh@asu.edu
 *
 * However this version is primarily new code. The new code is
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.List;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.Locale;
import java.text.DateFormat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.assignment.cover.AssignmentService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.Status;
import org.sakaiproject.lessonbuildertool.tool.view.FilePickerViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.tool.producers.PermissionsHelperProducer;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lessonbuildertool.service.SimplePageToolService;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIBoundString;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import org.springframework.core.io.Resource;


/**
 * This produces the primary view of the page. It also handles the editing of the properties of most
 * of the items (through JQuery dialogs).
 * 
 * @author Eric Jeney <jeney@rutgers.edu>
 */
public class ShowPageProducer implements ViewComponentProducer, DefaultView, NavigationCaseReporter, ViewParamsReporter {
	private static Log log = LogFactory.getLog(ShowPageProducer.class);

	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
        private SimplePageToolService simplePageToolService;
        private FormatAwareDateInputEvolver dateevolver;
	private TimeService timeService;

    // I don't much like the static, because it opens us to a possible race condition, but I don't see much option
    // see the setter. It has to be static becaue it's used in makeLink, which is static so it can be used
    // by ReorderProducer. I wonder if this whole producer could be made application scope?
        private static LessonEntity forumEntity;
	public MessageLocator messageLocator;
        private LocaleGetter localegetter;
	public static final String VIEW_ID = "ShowPage";
	private static final String DEFAULT_TYPES="mp4,mov,m2v,3gp,wmv,mp3,swf,wav";
        private static String[] multimediaTypes = null;

	public String getViewID() {
		return VIEW_ID;
	}

    // this code is written to handle the fact the CSS uses NNNpx and old code NNN. We need to be able to convert.
    // Length is intended to be a neutral representation.  getOld returns without px, getNew with px, and getOrig
    // the original version
    	public class Length {
	    String number;
	    String unit;
	    Length(String spec) {
		spec = spec.trim();
		int numlen;
		for (numlen = 0; numlen < spec.length(); numlen++) {
		    if (!Character.isDigit(spec.charAt(numlen)))
			break;
		}
		number = spec.substring(0, numlen).trim();
		unit = spec.substring(numlen).trim().toLowerCase();
	    }
	    public String getOld() {
		return number + (unit.equals("px") ? "" : unit);
	    }
	    public String getNew() {
		return number + (unit.equals("") ? "px" : unit);
	    }
	}	

        // problem is it need to work with a null argument
	public static String getOrig(Length l) {
	    if (lengthOk(l))
		return l.number + l.unit;
	    else
		return "";
	}

    // do we have a valid length?
	public static boolean lengthOk(Length l) {
	    if (l == null || l.number == null || l.number.equals("")) {
		if (l != null && l.unit.equals("auto"))
		    return true;
		return false;
	    }
	    return true;
	}

    // created style arguments. This was done at the time when i thought
    // the OBJECT tag actually paid attention to the CSS size. it doesn't.
        public String getStyle(Length w, Length h) {
	    return "width: " + w.getNew() + ", height: " + h.getNew();
	}
    
    // produce abbreviated versions of URLs, for use in constructing titles
	public String abbrevUrl (String url) {
	    if (url.startsWith("/")) {
		int suffix = url.lastIndexOf("/");
		if (suffix > 0)
		    url = url.substring(suffix+1);
		if (url.startsWith("http:__")) {
		    url = url.substring(7);
		    suffix = url.indexOf("_");
		    if (suffix > 0)
			url = messageLocator.getMessage("simplepage.fromhost").replace("{}", url.substring(0, suffix));
		} else if (url.startsWith("https:__")) {
		    url = url.substring(8);
		    suffix = url.indexOf("_");
		    if (suffix > 0)
			url = messageLocator.getMessage("simplepage.fromhost").replace("{}", url.substring(0, suffix));
		}
	    } else {
		// external, the hostname is probably best
		try {
		    URL u = new URL(url);
		    url = messageLocator.getMessage("simplepage.fromhost").replace("{}", u.getHost());
		} catch (Exception ignore) {log.error("exception " + ignore);};
	    }

	    return url;
	}

    // NOTE:
    // pages should normally be called with 3 arguments:
    // sendingPageId - the page to show
    // itemId - the item used to choose the page, because pages can occur in different places, and we need
    //    to know the context in which this was called. Note that there's an item even for top-level pages
    // path - push, next, or a number. The number is an index into the breadcrumbs if someone clicks
    //    on breadcrumbs. This iitem is used to maintain the path (the internal form of the breadcrumbs)
    //    missing is treated as next.
    // for startup, none of this will be known, so getCurrentPage will find the top level page and item if
    //    nothing is specified

	public void fillComponents(UIContainer tofill, ViewParameters params, ComponentChecker checker) {
		Locale M_locale = null;
		String langLoc[] = localegetter.get().toString().split("_");
		if ( langLoc.length >= 2 ) {
		    if ("en".equals(langLoc[0]) && "ZA".equals(langLoc[1]))
			M_locale = new Locale("en", "GB");
		    else
			M_locale = new Locale(langLoc[0], langLoc[1]);
		} else{
		    M_locale = new Locale(langLoc[0]);
		}

		String clearAttr = ((GeneralViewParameters) params).getClearAttr();

		if (clearAttr != null && !clearAttr.equals("")) {
		    Session session = SessionManager.getCurrentSession();
		    // don't let users clear random attributes
		    if (clearAttr.startsWith("LESSONBUILDER_RETURNURL"))
			session.setAttribute(clearAttr, null);
		}		    

		if (multimediaTypes == null) {
		    String mmTypes = ServerConfigurationService.getString("lessonbuilder.multimedia.types", DEFAULT_TYPES);
		    multimediaTypes = mmTypes.split(",");
		    for (int i = 0; i < multimediaTypes.length; i++) {
			multimediaTypes[i] = multimediaTypes[i].trim().toLowerCase();
		    }
		    Arrays.sort(multimediaTypes);
		}

		// security model:
		// canEditPage and canReadPage are normal Sakai privileges. They all to all
		//    pages in the site.
		// However when presented with a page, we need to make sure it's actually in
		//    this site, or users could get to pages in other sites. That's done
		//    by updatePageObject. The model is that producers always work on the
		//    current page, and updatePageObject makes sure that is in the current site
		//    at that point we can safely use canEditPage. 

		// somewhat misleading. sendingPage specifies the page we're supposed to go to
		if (((GeneralViewParameters) params).getSendingPage() != -1) {
		    // will fail if page not in this site
		    // security then depends upon making sure that we only deal with this page
		    try {
			simplePageBean.updatePageObject(((GeneralViewParameters) params).getSendingPage());
		    } catch (Exception e) {
			log.warn("ShowPage permission exception " + e);
			UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
			return;
		    }
		}

        	boolean canEditPage = simplePageBean.canEditPage();
		// if starting the tool, sendingpage isn't set. the following call
		// will give us the top page.
		SimplePage currentPage = simplePageBean.getCurrentPage();
		// now we need to find our own item, for access checks, etc.
		SimplePageItem pageItem = simplePageBean.getCurrentPageItem(((GeneralViewParameters) params).getItemId());
		// one more security check: make sure the item actually involves this page.
		// otherwise someone could pass us an item from a different page in another site
		if (Long.valueOf(pageItem.getSakaiId()) != currentPage.getPageId()) {
		    log.warn("ShowPage item not in page");
		    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
		    return;
		}

		// I believe we've now checked all the args for permissions issues. All other item and 
		// page references are generated here based on the contents of the page and items.

		if (currentPage == null || pageItem == null) {
		    UIOutput.make(tofill, "error-div");
		    if (canEditPage) 
			UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.impossible1"));
		    else
			UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
		    return;
		}

		// path is the breadcrumbs. Push, pop or reset depending upon path=
		simplePageBean.adjustPath(((GeneralViewParameters) params).getPath(), currentPage.getPageId(), pageItem.getId(), pageItem.getName());

		// potentially need time zone for setting release date
	        if (!canEditPage && currentPage.getReleaseDate() != null && currentPage.getReleaseDate().after(new Date())) {
		    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
								   DateFormat.SHORT, M_locale);
		    TimeZone tz = timeService.getLocalTimeZone();
		    df.setTimeZone(tz);
		    String releaseDate = df.format(currentPage.getReleaseDate());
		    String releaseMessage = messageLocator.getMessage("simplepage.not_yet_available_releasedate").replace("{}",releaseDate);

		    UIOutput.make(tofill, "error-div");
		    UIOutput.make(tofill, "error", releaseMessage);

		    return;
		}

		if (canEditPage) {
			// show tool bar
			createToolBar(tofill, currentPage);
			UIOutput.make(tofill, "edit-title").
			    decorate(new UIFreeAttributeDecorator("title", 
			        messageLocator.getMessage("simplepage.editTitle")));
			UIOutput.make(tofill, "dialogDiv");
		} else if (!simplePageBean.canReadPage())
		        return;
		// at this point we know we can read or edit the page.
		else {
		    List<String> needed = simplePageBean.pagesNeeded(pageItem);
		    if (needed.size() > 0) {
			// this is normal for a top level page, but for one deeper it can only happen if someone
			// is gaming us. Otherwise the level above will never present a link to get to the page.
			UIOutput.make(tofill, "pagetitle", currentPage.getTitle());
			UIOutput.make(tofill, "error-div");
			UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.has_prerequistes"));
			UIBranchContainer errorList = UIBranchContainer.make(tofill, "error-list:");
			for (String errorItem: needed) {
			    UIBranchContainer errorListItem = UIBranchContainer.make(errorList, "error-item:");
			    UIOutput.make(errorListItem, "error-item-text", errorItem);
			}
			return;
		    }
		}

		// note page accessed. the code checks to see whether all the required items on it
		// have been finished, and if so marks it complete, else just updates acces date
		simplePageBean.track(pageItem.getId(), true);

		UIOutput.make(tofill, "pagetitle", currentPage.getTitle());

		if (pageItem.getPageId() != 0) {
			// Not top-level, so we have to show breadcrumbs

			List<SimplePageBean.PathEntry> breadcrumbs = simplePageBean.getHierarchy();

			int index = 0;
			if (breadcrumbs.size() > 1)
			    UIOutput.make(tofill, "crumbdiv");
			    for (SimplePageBean.PathEntry e : breadcrumbs) {
			    // don't show current page. We already have a title. This was too much
				UIBranchContainer crumb = UIBranchContainer.make(tofill, "crumb:");
				GeneralViewParameters view = new GeneralViewParameters(VIEW_ID);
				view.setSendingPage(e.pageId);
				view.setItemId(e.pageItemId);
				view.setPath(Integer.toString(index));
				if (index < breadcrumbs.size() - 1) {
					// Not the last item
				    UIInternalLink.make(crumb, "crumb-link", e.title, view);
				    UIOutput.make(crumb, "crumb-follow", " > ");
				} else {
				    UIOutput.make(crumb, "crumb-follow", e.title).decorate(new UIStyleDecorator("bold"));
				}

				index++;
			    }

			// see if there's a next item in sequence. Note that if this is a page that
			// has the nextPage property, we supposedly branched to it rather than calling
			// it as a subpage. So next doesn't apply.
			if (!pageItem.getNextPage()) {
			    SimplePageItem item = simplePageToolDao.findNextPageItemOnPage(pageItem.getPageId(), pageItem.getSequence());
			    if (item != null && simplePageBean.isItemAvailable(item, pageItem.getPageId())) {
				GeneralViewParameters view = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
				view.setSendingPage(Long.valueOf(item.getSakaiId()));
				view.setItemId(item.getId());
				view.setPath("next");

				// must check availability against parent page
				//	UIForm form = UIForm.make(tofill, "next-form");
				UIInternalLink.make(tofill, "next", "Next",  view);
			    }
			}

		}

		// swfObject is not currently used
		boolean shownSwfObject = false;

		// produce the main table
		UIBranchContainer container = UIBranchContainer.make(tofill, "itemContainer:");

		boolean showRefresh = false;
		int textboxcount = 1;

		// items to show
		List<SimplePageItem> itemList = (List<SimplePageItem>) simplePageBean.getItemsOnPage(currentPage.getPageId());

		UIBranchContainer tableContainer = UIBranchContainer.make(container, "itemTable:");
		// the table header is for accessibility tools only, so it's positioned off screen
		if (canEditPage)
		    UIOutput.make(tableContainer, "header-edits");
		UIOutput.make(tableContainer, "header-status");
		UIOutput.make(tableContainer, "header-items");

		for (SimplePageItem i : itemList) {

		    // listitem is mostly historical. it uses some shared HTML, but if I were
		    // doing it from scratch I wouldn't make this distinction. At the moment it's
		    // everything that isn't inline.

			boolean listItem = !(i.getType() == SimplePageItem.TEXT ||
					     i.getType() == SimplePageItem.MULTIMEDIA);
					     // (i.getType() == SimplePageItem.PAGE && "button".equals(i.getFormat())))

			UIBranchContainer tableRow = UIBranchContainer.make(tableContainer, "item:");

			// you really need the HTML file open at the same time to make sense of the following code
			if (listItem) { // Not an HTML Text, Element or Multimedia Element

				if (canEditPage) {
					UIOutput.make(tableRow, "current-item-id2", String.valueOf(i.getId()));
				}

				// users can declare a page item to be navigational. If so we display
				// it to the left of the normal list items, and use a button.  This is
				// used for pages that are "next" pages, i.e. they replace this page
				// rather than creating a new level in the breadcrumbs. Since they can't
				// be required, they don't need the status image, which is good because
				// they're displayed with colspan=2, so there's no space for the image.

				boolean navButton = "button".equals(i.getFormat()) && !i.isRequired();
				boolean notDone = false;
				if (!navButton) {
				    notDone = handleStatusImage(tableRow, i);
				}

				UIOutput linktd = UIOutput.make(tableRow, "link-td");
				if (navButton)
				    linktd.decorate(new UIFreeAttributeDecorator("colspan", "2"));
				UIOutput linkdiv = UIOutput.make(tableRow, "link-div");
				if (i.isRequired()) {
				    if (simplePageBean.isItemComplete(i))
					UIOutput.make(tableRow, "link-status", messageLocator.getMessage("simplepage.status.completed").replace("{}",""));
				    else
					UIOutput.make(tableRow, "link-status", messageLocator.getMessage("simplepage.status.required").replace("{}",""));
				}

				UIOutput descriptiondiv = null;

				// refresh isn't actually used anymore. We've changed the way things are
				// done so the user never has to request a refresh.
				showRefresh = !makeLink(tableRow, "link", i, canEditPage, currentPage, notDone) || showRefresh;

				// dummy is used when an assignment, quiz, or forum item is copied
				// from another site. The way the copy code works, our import code 
				// doesn't have access to the necessary info to use the item from the
				// new site. So we add a dummy, which generates an explanation that the
				// author is going to have to choose the item from the current site
				if (i.getSakaiId().equals(SimplePageItem.DUMMY)) {
				    String code = null;
                                    switch (i.getType()) {
				    case SimplePageItem.ASSIGNMENT:
					code = "simplepage.copied.assignment"; break;
				    case SimplePageItem.ASSESSMENT:
					code = "simplepage.copied.assessment"; break;
				    case SimplePageItem.FORUM:
					code = "simplepage.copied.forum"; break;
				    }
				    descriptiondiv = UIOutput.make(tableRow, "description", messageLocator.getMessage(code));
				} else
				    descriptiondiv = UIOutput.make(tableRow, "description", i.getDescription());

				// nav button gets float left so any description goes to its right. Otherwise the
				// description block will display underneath
				if ("button".equals(i.getFormat()))
				    linkdiv.decorate(new UIFreeAttributeDecorator("style", "float:left"));
				// for accessibility
				if (navButton) {
				    linkdiv.decorate(new UIFreeAttributeDecorator("role", "navigation"));
				}

				// note that a lot of the info here is used by the javascript that prepares
				// the jQuery dialogs
				if (canEditPage) {
					UIOutput.make(tableRow, "edit-td");
					UILink.make(tableRow, "edit-link", messageLocator.getMessage("simplepage.editItem"), "").
					    decorate(new UIFreeAttributeDecorator("title", 
						  messageLocator.getMessage("simplepage.edit-title.generic").replace("{}", i.getName())));

					UIOutput.make(tableRow, "prerequisite-info", String.valueOf(i.isPrerequisite()));

					if (i.getType() == SimplePageItem.ASSIGNMENT) {
					    // the type indicates whether scoring is letter grade, number, etc.
					    // the javascript needs this to present the right choices to the user
					    // types 6 and 8 aren't legal scoring types, so they are used as 
					    // markers for quiz or forum. I ran out of numbers and started using
					    // text for things that aren't scoring types. That's better anyway
						int type = 4;
						if (!i.getSakaiId().equals(SimplePageItem.DUMMY))
						    type = simplePageBean.getAssignmentTypeOfGrade(simplePageBean.assignmentRef(i.getSakaiId()));
						UIOutput.make(tableRow, "type", String.valueOf(type));
						String requirement = String.valueOf(i.getSubrequirement());
						if ((type == SimplePageItem.PAGE || type == SimplePageItem.ASSIGNMENT) && i.getSubrequirement()) {
						    requirement = i.getRequirementText();
						}
						UIOutput.make(tableRow, "requirement-text", requirement);
					} else if (i.getType() == SimplePageItem.ASSESSMENT) {
						UIOutput.make(tableRow, "type", "6"); // Not used by assignments, so it is
										      // safe to dedicate to assessments
						UIOutput.make(tableRow, "requirement-text", (i.getSubrequirement() ? i.getRequirementText() : "false"));
					} else if (i.getType() == SimplePageItem.FORUM) {
						UIOutput.make(tableRow, "extra-info");
						UIOutput.make(tableRow, "type", "8"); 
					} else if (i.getType() == SimplePageItem.PAGE) {
						UIOutput.make(tableRow, "type", "page"); 
						UIOutput.make(tableRow, "page-next", Boolean.toString(i.getNextPage()));
						UIOutput.make(tableRow, "page-button", Boolean.toString("button".equals(i.getFormat())));
					}
				}

		// the following are for the inline item types. Multimedia is the most complex because
		// it can be IMG, IFRAME, or OBJECT, and Youtube is treated separately
				
			} else if (i.getType() == SimplePageItem.MULTIMEDIA) {

				StringTokenizer token = new StringTokenizer(i.getSakaiId(), ".");

				String extension = "";

				while (token.hasMoreTokens()) {
				    extension = token.nextToken().toLowerCase();
				}

				// the extension is almost never used. Normally we have
				// the MIME type and use it. Extension is used only if
				// for some reason we don't have the MIME type
				UIComponent item;
				String youtubeKey;

				Length width=null;
				if (i.getWidth() != null)
				    width = new Length(i.getWidth());
				Length height=null;
				if (i.getHeight() != null)
				    height = new Length(i.getHeight());
				
				    
				// Get the MIME type. For multimedia types is should be in the html field. 
				// The old code saved the URL there. So if it looks like a URL ignore it.
				String mimeType = i.getHtml();
				if (mimeType != null && (mimeType.startsWith("http") || mimeType.equals("")))
				    mimeType = null;

				// here goes. dispatch on the type and produce the right tag type
				if (simplePageBean.isImageType(i)) {

					UIOutput.make(tableRow, "imageSpan");

					String imageName = i.getAlt();
					if (imageName == null || imageName.equals(""))
					    imageName = abbrevUrl(i.getURL());

					item = UIOutput.make(tableRow, "image").
					    decorate(new UIFreeAttributeDecorator("src", i.getURL())).
					    decorate(new UIFreeAttributeDecorator("alt", imageName));
					if (lengthOk(height) && lengthOk(width))
					    item.decorate(new UIFreeAttributeDecorator("width", width.getOld())).
						decorate(new UIFreeAttributeDecorator("height", height.getOld()));

					// stuff for the jquery dialog
					if (canEditPage) {
					    UIOutput.make(tableRow, "imageHeight", getOrig(height));
					    UIOutput.make(tableRow, "imageWidth", getOrig(width));
					    UIOutput.make(tableRow, "description2", i.getDescription());
					    UIOutput.make(tableRow, "mimetype2", mimeType);
					    UIOutput.make(tableRow, "current-item-id4", Long.toString(i.getId()));
					    UIOutput.make(tableRow, "editmm-td");
					    UILink.make(tableRow, "iframe-edit", messageLocator.getMessage("simplepage.editItem"), "").
						decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
					}

				} else if ((youtubeKey = simplePageBean.getYoutubeKey(i)) != null) {
					String youtubeUrl = "http://www.youtube.com/v/" + youtubeKey + "?version=3";
					// this is very odd. The official youtube embedding uses <OBJECT> with
					// a stylesheet to specify size. But the only values that actually
					// work are px and percent. I.e. it works just like the old 
					// HTML length types. A real stylesheet length understands other units.
					// I'm generating a style sheet, so that our HTML embedding is as close
					// to theirs as possible, even the lengths are actually interpreted as old style

					UIOutput.make(tableRow, "youtubeSpan");

					// if width is blank or 100% scale the height
					if (width != null && height != null && !height.number.equals("")) {
					    if (width.number.equals("") && width.unit.equals("") ||
						width.number.equals("100") && width.unit.equals("%")) {

						int h = Integer.parseInt(height.number);
						if (h > 0) {
						    width.number = Integer.toString((int)Math.round(h * 1.641025641));
						    width.unit = height.unit;
						}
					    }
					}
					    
// <object style="height: 390px; width: 640px"><param name="movie" value="http://www.youtube.com/v/AKIC7OQqBrA?version=3"><param name="allowFullScreen" value="true"><param name="allowScriptAccess" value="always"><embed src="http://www.youtube.com/v/AKIC7OQqBrA?version=3" type="application/x-shockwave-flash" allowfullscreen="true" allowScriptAccess="always" width="640" height="390"></object>

					item = UIOutput.make(tableRow, "youtubeObject");
					// youtube seems ok with length and width
					if (lengthOk(height) && lengthOk(width))
					    item.decorate(new UIFreeAttributeDecorator("style", getStyle(width, height)));
					item.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.youtube_player")));

					UIOutput.make(tableRow, "youtubeURLInject").decorate(new UIFreeAttributeDecorator("value", youtubeUrl));

					item = UIOutput.make(tableRow, "youtubeEmbed").
					    decorate(new UIFreeAttributeDecorator("type", "application/x-shockwave-flash")).
					    decorate(new UIFreeAttributeDecorator("src", youtubeUrl));
					if (lengthOk(height) && lengthOk(width)) {
					    item.decorate(new UIFreeAttributeDecorator("height", height.getOld())).
						decorate(new UIFreeAttributeDecorator("width", width.getOld()));
					}

					if (canEditPage) {
					    UIOutput.make(tableRow, "youtubeId", String.valueOf(i.getId()));
					    UIOutput.make(tableRow, "currentYoutubeURL", youtubeUrl);
					    UIOutput.make(tableRow, "currentYoutubeHeight", getOrig(height));
					    UIOutput.make(tableRow, "currentYoutubeWidth", getOrig(width));
					    UIOutput.make(tableRow, "description4", i.getDescription());
					    UIOutput.make(tableRow, "current-item-id5", Long.toString(i.getId()));

					    UIOutput.make(tableRow, "youtube-td");
					    UILink.make(tableRow, "youtube-edit", messageLocator.getMessage("simplepage.editItem"), "").
						decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.youtube")));
					}

					// as of Oct 28, 2010, we store the mime type. mimeType null is an old entry.
					// For that use the old approach of checking the extension.
					// Otherwise we want to use iframes for HTML and OBJECT for everything else
					// We need the iframes because IE up through 8 doesn't reliably display
					// HTML with OBJECT. Experiments show that everything else works with OBJECT
					// starting with IE 6. Object has the advantage of better error handling.
					// application/xhtml+xml is XHTML.

				} else if ((mimeType != null && !mimeType.equals("text/html") && !mimeType.equals("application/xhtml+xml")) ||
					   (mimeType == null && Arrays.binarySearch(multimediaTypes, extension) >= 0)) {
				    // Traditionally people nested OBJECT and EMBED. My tests with IE6-8, Safari and Firefox
				    // show that OBJECT works, and does not require any specification of the MIME type.
				    // this code is used for everything that isn't HTML. HTML is done with an IFRAME

				        UIComponent item2;
					UIOutput.make(tableRow, "movieSpan");

					String movieUrl = i.getURL();
					String oMimeType = mimeType; // in case we change it for FLV or others
					// FLV is special. There's no player for flash video in the browser
					// it shows with a special flash program, which I supply
					if (mimeType != null && mimeType.equals("video/x-flv")) {
					    mimeType = "application/x-shockwave-flash";
					    movieUrl = "/sakai-lessonbuildertool-tool/templates/OSplayer.swf?movie=" + movieUrl + "&autoload=on&autoplay=off";
					}
					item2 = UIOutput.make(tableRow, "movieObject").
					    decorate(new UIFreeAttributeDecorator("data", movieUrl)).
					    decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.mm_player").replace("{}",abbrevUrl(i.getURL()))));
					if (mimeType != null)
					    item2.decorate(new UIFreeAttributeDecorator("type", mimeType));
					if (canEditPage)
					    item2.decorate(new UIFreeAttributeDecorator("style", "border: 1px solid black"));

					// some object types seem to need a specification
					if (lengthOk(height) && lengthOk(width))
					    item2.decorate(new UIFreeAttributeDecorator("height", height.getOld())).
						decorate(new UIFreeAttributeDecorator("width", width.getOld()));

					UIOutput.make(tableRow, "movieURLInject").
					    decorate(new UIFreeAttributeDecorator("value", movieUrl));
					UILink.make(tableRow, "noplugin", i.getName(), movieUrl);

					// item = UIOutput.make(tableRow, "movieSrcURLInject").
					//    decorate(new UIFreeAttributeDecorator("src", movieUrl));
					//if (height != null && width != null)
					//    item.decorate(new UIFreeAttributeDecorator("height", height.getOld())).
					//	decorate(new UIFreeAttributeDecorator("width", width.getOld()));

					if (canEditPage) {
					    UIOutput.make(tableRow, "movieId", String.valueOf(i.getId()));
					    UIOutput.make(tableRow, "movieHeight", getOrig(height));
					    UIOutput.make(tableRow, "movieWidth", getOrig(width));
					    UIOutput.make(tableRow, "description3", i.getDescription());
					    UIOutput.make(tableRow, "mimetype5", oMimeType);
					    UIOutput.make(tableRow, "current-item-id6", Long.toString(i.getId()));
					    
					    UIOutput.make(tableRow, "movie-td");
					    UILink.make(tableRow, "edit-movie", messageLocator.getMessage("simplepage.editItem"), "").
						    decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
					}
				} else {
				    // finally, HTML. Use an iframe
				    // definition of resizeiframe, at top of page
					if (getOrig(height) .equals("auto")) {
					    UIOutput.make(tofill, "iframeJavascript");
					}

					UIOutput.make(tableRow, "iframeSpan");

					item = UIOutput.make(tableRow, "iframe").decorate(new UIFreeAttributeDecorator("src", i.getURL()));
					// if user specifies auto, use Javascript to resize the iframe when the
					// content changes. This only works for URLs with the same origin, i.e.
					// URLs in this sakai system
					if (getOrig(height).equals("auto")) {
					    item.decorate(new UIFreeAttributeDecorator("onload", "resizeiframe('" + item.getFullID() + "')"));
					    if (lengthOk(width))
						item.decorate(new UIFreeAttributeDecorator("width", width.getOld()));
					    item.decorate(new UIFreeAttributeDecorator("height", "300"));
					} else {
					// we seem OK without a spec
					    if (lengthOk(height) && lengthOk(width)) {
						item.decorate(new UIFreeAttributeDecorator("height", height.getOld())).
						    decorate(new UIFreeAttributeDecorator("width", width.getOld()));
					    }
					}
					item.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.web_content").replace("{}",abbrevUrl(i.getURL()))));

					if (canEditPage) {
					    UIOutput.make(tableRow, "iframeHeight", getOrig(height));
					    UIOutput.make(tableRow, "iframeWidth", getOrig(width));
					    UIOutput.make(tableRow, "description5", i.getDescription());
					    UIOutput.make(tableRow, "mimetype3", mimeType);
					    UIOutput.make(tableRow, "current-item-id3", Long.toString(i.getId()));
					    UIOutput.make(tableRow, "editmm-td");
					    UILink.make(tableRow, "iframe-edit", messageLocator.getMessage("simplepage.editItem"), "").
						decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
					}
				}

			} else {
			    // remaining type must be a block of HTML
				UIOutput.make(tableRow, "itemSpan");
				UIVerbatim.make(tableRow, "content", (i.getHtml() == null ? "" : "<br />" + i.getHtml() + "\t"));

				// editing is done using a special producer that calls FCK. 
				if (canEditPage) {
					GeneralViewParameters eParams = new GeneralViewParameters();
					eParams.setSendingPage(currentPage.getPageId());
					eParams.setItemId(i.getId());
					eParams.viewID = EditPageProducer.VIEW_ID;
					UIOutput.make(tableRow, "edittext-td");
					UIInternalLink.make(tableRow, "edit-link", messageLocator.getMessage("simplepage.editItem"), eParams).
					    decorate(new UIFreeAttributeDecorator("title", 
						  messageLocator.getMessage("simplepage.edit-title.textbox").replace("{}", Integer.toString(textboxcount))));

					
					textboxcount++;
				}
			}

		}

		boolean showBreak = false;

		// I believe refresh is now done automatically in all cases
		//		if (showRefresh) {
		//			UIOutput.make(tofill, "refreshAlert");
		//
		//			// Should simply refresh
		//			GeneralViewParameters p = new GeneralViewParameters(VIEW_ID);
		//			p.setSendingPage(currentPage.getPageId());
		//			UIInternalLink.make(tofill, "refreshLink", p);
		//			showBreak = true;
		//		}

		// stuff goes on the page in the order in the HTML file. So the fact that it's here doesn't mean it shows
		// up at the end. This code produces errors and other odd stuff.

		if (canEditPage) {
	        // if the page is hidden, warn the faculty [students get stopped at the top]
		    if (currentPage.isHidden()) {
			UIOutput.make(tofill, "hiddenAlert").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.pagehidden")));
			UIVerbatim.make(tofill, "hidden-text", messageLocator.getMessage("simplepage.pagehidden.text"));
			
			showBreak = true;
		// similarly warn them in it isn't released yet
		    } else  if (currentPage.getReleaseDate() != null && currentPage.getReleaseDate().after(new Date())) {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
								       DateFormat.SHORT, M_locale);
			TimeZone tz = timeService.getLocalTimeZone();
			df.setTimeZone(tz);
			String releaseDate = df.format(currentPage.getReleaseDate());
			UIOutput.make(tofill, "hiddenAlert").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.notreleased")));
			UIVerbatim.make(tofill, "hidden-text", messageLocator.getMessage("simplepage.notreleased.text").replace("{}",releaseDate));
			showBreak = true;
		    }
		}

		if (showBreak) {
			UIOutput.make(tofill, "breakAfterWarnings");
		}

		// more warnings: if no item on the page, give faculty instructions, students an error
		if (itemList.size() == 0) {
			if (canEditPage) {
				Resource resource = simplePageToolService.getResource(messageLocator.getMessage("simplepage.startup_help"));
				StringBuffer instructions = new StringBuffer();
				try {
				    InputStream is = resource.getInputStream();
				    BufferedReader br = new BufferedReader(new InputStreamReader(is));
 
				    String line;
				    while ((line = br.readLine()) != null) {
					instructions.append(line);
					instructions.append("\n");
				    } 
				    br.close();
				} catch (Exception e) {
				    log.error("Can't read startup help " + e);
				}
			    
				UIVerbatim.make(tofill, "startupHelp", instructions.toString());
			} else {
				UIOutput.make(tofill, "error-div");
				UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.noitems_error_user"));
			}
		}

		// now output the dialogs. but only for faculty (to avoid making the file bigger)
		if (canEditPage)
		    createSubpageDialog(tofill, currentPage);
		createEditItemDialog(tofill, currentPage);
		createAddMultimediaDialog(tofill, currentPage);
		createEditMultimediaDialog(tofill, currentPage);
		createEditTitleDialog(tofill, currentPage, pageItem);
		createYoutubeDialog(tofill);
		createMovieDialog(tofill, currentPage);
	}

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

    	private boolean makeLink(UIContainer container, String ID, SimplePageItem i, boolean canEditPage, SimplePage currentPage, boolean notDone) {
	    return makeLink(container, ID, i, simplePageBean, simplePageToolDao, messageLocator, canEditPage, currentPage, notDone);
	}

	/**
	 * 
	 * @param container
	 * @param ID
	 * @param i
	 * @param simplePageBean
	 * @param simplePageToolDao
	 * @return Whether or not this item is available.
	 */
    	protected static boolean makeLink(UIContainer container, String ID, SimplePageItem i, SimplePageBean simplePageBean, SimplePageToolDao simplePageToolDao, MessageLocator messageLocator, boolean canEditPage, SimplePage currentPage, boolean notDone) {
		String URL = "";
		boolean available = simplePageBean.isItemAvailable(i);

		if (i.getType() == SimplePageItem.RESOURCE || i.getType() == SimplePageItem.URL) {
			if (available) {
				URL = i.getURL();
			}

			UIInternalLink link = LinkTrackerProducer.make(container, ID, i.getName(), URL, i.getId(), notDone);

			if (available) {
				link.decorate(new UIFreeAttributeDecorator("target", "_blank"));
			} else {
				disableLink(link, messageLocator);
			}
		} else if (i.getType() == SimplePageItem.PAGE) {
			SimplePage p = simplePageToolDao.getPage(Long.valueOf(i.getSakaiId()));

			GeneralViewParameters eParams = new GeneralViewParameters(ShowPageProducer.VIEW_ID, p.getPageId());
			eParams.setItemId(i.getId());
			// nextpage indicates whether it should be pushed onto breadcrumbs or replace the top item
			if (i.getNextPage())
			    eParams.setPath("next");   
			else
			    eParams.setPath("push");
			boolean isbutton = false;
			// button says to display the link as a button. use navIntrTool, which is standard
			// Sakai CSS that generates the type of button used in toolbars. We have to override
			// with background:transparent or we get remnants of the gray
			if ("button".equals(i.getFormat())) {
			    isbutton = true;
			    UIOutput span = UIOutput.make(container, ID + "-button-span");
			    ID = ID + "-button";
			    if (!i.isRequired())
				span.decorate(new UIFreeAttributeDecorator("class", "navIntraTool buttonItem"));;
			}
			UILink link;
			if (available) {
				link = UIInternalLink.make(container, ID, p.getTitle(), eParams);
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, true);
				}
			// at this point we know the page isn't available, i.e. user hasn't
			// met all the prerequistes. Normally we give them a nonworking
			// grayed out link. But if they are the author, we want to
			// give them a real link. Otherwise if it's a subpage they have no
			// way to get to it (currently -- we'll fix that)
			// but we make it look like it's disabled so they can see what
			// students see
			} else if (canEditPage) {
				link = UIInternalLink.make(container, ID, p.getTitle(), eParams);
				fakeDisableLink(link, messageLocator);				
			} else {
				link = UILink.make(container, ID, p.getTitle(), "");
				disableLink(link, messageLocator);
			}
			// not sure what screen readers will do with this bizarre HTML/CSS, so 
			// give it a label to be safe
			if (isbutton)
			    link.decorate(new UIFreeAttributeDecorator("title", i.getName()));

		} else if (i.getType() == SimplePageItem.ASSIGNMENT) {
			if (available) {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, true);
				}

				GeneralViewParameters params = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
				params.setSendingPage(currentPage.getPageId());
				params.setSource("/direct/assignment/" + i.getSakaiId());
				params.setItemId(i.getId());
				UIInternalLink.make(container, "link", i.getName(), params);

			} else {
				if (i.isPrerequisite()) {
				    simplePageBean.checkItemPermissions(i, false);
				}
				UILink link = UILink.make(container, ID, i.getName(), "");
				disableLink(link, messageLocator);
			}
		} else if (i.getType() == SimplePageItem.ASSESSMENT) {
			if (available) {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, true);
				}

				// we've hacked Samigo to look at a special lesson builder session
				// attribute. otherwise at the end of the test, Samigo replaces the
				// whole screen, exiting form our iframe. The other tools don't do this.
				GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
				view.setSendingPage(currentPage.getPageId());
				view.setClearAttr("LESSONBUILDER_RETURNURL_SAMIGO");
				view.setSource("/samigo-app/servlet/Login?id=" + simplePageBean.getAssessmentAlias(i.getSakaiId()));
				view.setItemId(i.getId());
				UIInternalLink.make(container, "link", i.getName(), view);
			} else {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, false);
				}
				UILink link = UILink.make(container, ID, i.getName(), "");
				disableLink(link, messageLocator);
			}
		} else if (i.getType() == SimplePageItem.FORUM) {
			if (available) {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, true);
				}
				GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
				view.setSendingPage(currentPage.getPageId());
				view.setSource("/samigo-app/servlet/Login?id=" + simplePageBean.getAssessmentAlias(i.getSakaiId()));
				view.setItemId(i.getId());
				LessonEntity lessonEntity = forumEntity.getEntity(i.getSakaiId());
				view.setSource((lessonEntity==null)?"dummy":lessonEntity.getUrl());
				UIInternalLink.make(container, "link", i.getName(), view);

			} else {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, false);
				}
				UILink link = UILink.make(container, ID, i.getName(), "");
				disableLink(link, messageLocator);
			}
		}


		return available;
	}

        private static void disableLink(UILink link, MessageLocator messageLocator) {
		link.decorate(new UIFreeAttributeDecorator("onclick", "return false"));
		link.decorate(new UIDisabledDecorator());
		link.decorate(new UIStyleDecorator("disabled"));
		link.decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.complete_required")));
	}

        // show is if it was disabled but don't actually
        private static void fakeDisableLink(UILink link, MessageLocator messageLocator) {
		link.decorate(new UIDisabledDecorator());
		link.decorate(new UIStyleDecorator("disabled"));
		link.decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.complete_required")));
	}

	public void setSimplePageToolDao(SimplePageToolDao s) {
		simplePageToolDao = s;
	}

	public void setSimplePageToolService(SimplePageToolService s) {
		simplePageToolService = s;
	}

	public void setDateEvolver(FormatAwareDateInputEvolver dateevolver) {
		this.dateevolver = dateevolver;
	}

	public void setTimeService(TimeService ts) {
		timeService = ts;
	}

	public void setLocaleGetter(LocaleGetter localegetter) {
		this.localegetter = localegetter;
	}

	public void setForumEntity(LessonEntity e) {
	    // forumEntity is static, so it may already have been set
	    // there is a possible race condition, but since the bean is
	    // a singleton both people in the race will be trying to set
	    // the same value. So it shouldn't matter
	    	if (forumEntity == null)
			forumEntity = e;
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	private void createToolBar(UIContainer tofill, SimplePage currentPage) {
		UIBranchContainer toolBar = UIBranchContainer.make(tofill, "tool-bar:");

		createToolBarLink(ReorderProducer.VIEW_ID, toolBar, "reorder", "simplepage.reorder", currentPage, "simplepage.reorder.tooltip");
		createToolBarLink(EditPageProducer.VIEW_ID, toolBar, "add-text", "simplepage.text", currentPage, "simplepage.text.tooltip").setItemId(null);

		createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, toolBar, "add-resource", "simplepage.resource", false, currentPage, "simplepage.resource.tooltip");

		UILink subpagelink = UIInternalLink.makeURL(toolBar, "subpage-link", "");
		subpagelink.decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.subpage.tooltip")));
		subpagelink.linktext = new UIBoundString(messageLocator.getMessage("simplepage.subpage"));

		createToolBarLink(AssignmentPickerProducer.VIEW_ID, toolBar, "add-assignment", "simplepage.assignment", currentPage, "simplepage.assignment.tooltip");
		createToolBarLink(QuizPickerProducer.VIEW_ID, toolBar, "add-quiz", "simplepage.quiz", currentPage, "simplepage.quiz.tooltip");
		createToolBarLink(ForumPickerProducer.VIEW_ID, toolBar, "add-forum", "simplepage.forum", currentPage, "simplepage.forum.tooltip");

		createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, toolBar, "add-multimedia", "simplepage.multimedia", true, currentPage, "simplepage.multimedia.tooltip");
		createToolBarLink(PermissionsHelperProducer.VIEW_ID, toolBar, "permissions", "simplepage.permissions", currentPage, "simplepage.permissions.tooltip");
		UILink.make(toolBar, "help", messageLocator.getMessage("simplepage.help"), messageLocator.getMessage("simplepage.general-instructions"));

	}

	private GeneralViewParameters createToolBarLink(String viewID, UIContainer tofill, String ID, String message, SimplePage currentPage, String tooltip) {
		GeneralViewParameters params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		createStandardToolBarLink(viewID, tofill, ID, message, params, tooltip);
		return params;
	}

        private FilePickerViewParameters createFilePickerToolBarLink(String viewID, UIContainer tofill, String ID, String message, boolean resourceType, SimplePage currentPage, String tooltip) {
		FilePickerViewParameters fileparams = new FilePickerViewParameters();
		fileparams.setSender(currentPage.getPageId());
		fileparams.setResourceType(resourceType);
		createStandardToolBarLink(viewID, tofill, ID, message, fileparams, tooltip);
		return fileparams;
	}

        private void createStandardToolBarLink(String viewID, UIContainer tofill, String ID, String message, SimpleViewParameters params, String tooltip) {
		params.viewID = viewID;
		UILink link = UIInternalLink.make(tofill, ID, messageLocator.getMessage(message), params);
		link.decorate(new UITooltipDecorator(messageLocator.getMessage(tooltip)));
	}

	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("failure", new SimpleViewParameters(ReloadPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("reload", new SimpleViewParameters(ReloadPageProducer.VIEW_ID)));
		return togo;
	}

    private void createSubpageDialog(UIContainer tofill, SimplePage currentPage) {
	    UIOutput.make(tofill, "subpage-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.subpage")));
	    UIForm form = UIForm.make(tofill, "subpage-form");

		UIOutput.make(form, "subpage-label", messageLocator.getMessage("simplepage.pageTitle_label"));
		UIInput.make(form, "subpage-title", "#{simplePageBean.subpageTitle}");


		GeneralViewParameters view = new GeneralViewParameters(PagePickerProducer.VIEW_ID);
		view.setSendingPage(currentPage.getPageId());

		UIInternalLink.make(form, "subpage-choose", messageLocator.getMessage("simplepage.choose_existing_page"), view);

		UIBoundBoolean.make(form, "subpage-next", "#{simplePageBean.subpageNext}", false);
		UIBoundBoolean.make(form, "subpage-button", "#{simplePageBean.subpageButton}", false);

		UICommand.make(form, "create-subpage", messageLocator.getMessage("simplepage.create"), "#{simplePageBean.createSubpage}");
		UICommand.make(form, "cancel-subpage", messageLocator.getMessage("simplepage.cancel"), null);

	}

	private void createEditItemDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "edit-item-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edititem_header")));

		UIForm form = UIForm.make(tofill, "edit-form");

		UIOutput.make(form, "name-label", messageLocator.getMessage("simplepage.name_label"));
		UIInput.make(form, "name", "#{simplePageBean.name}");

		UIOutput.make(form, "description-label", messageLocator.getMessage("simplepage.description_label"));
		UIInput.make(form, "description", "#{simplePageBean.description}");

		GeneralViewParameters params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.viewID = AssignmentPickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-assignment", messageLocator.getMessage("simplepage.change_assignment"), params);

		params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.viewID = QuizPickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-quiz", messageLocator.getMessage("simplepage.change_quiz"), params);

		params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.viewID = ForumPickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-forum", messageLocator.getMessage("simplepage.change_forum"), params);

		FilePickerViewParameters fileparams = new FilePickerViewParameters();
		fileparams.setSender(currentPage.getPageId());
		fileparams.setResourceType(false);
		fileparams.viewID = ResourcePickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-resource", messageLocator.getMessage("simplepage.change_resource"), fileparams);

		params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.viewID = PagePickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-page", messageLocator.getMessage("simplepage.change_page"), params);

		UIBoundBoolean.make(form, "item-next", "#{simplePageBean.subpageNext}", false);
		UIBoundBoolean.make(form, "item-button", "#{simplePageBean.subpageButton}", false);

		UIInput.make(form, "item-id", "#{simplePageBean.itemId}");

		UIBoundBoolean.make(form, "item-required2", "#{simplePageBean.subrequirement}", false);

		UIBoundBoolean.make(form, "item-required", "#{simplePageBean.required}", false);
		UIBoundBoolean.make(form, "item-prerequisites", "#{simplePageBean.prerequisite}", false);

		UISelect.make(form, "assignment-dropdown", SimplePageBean.GRADES, "#{simplePageBean.dropDown}", SimplePageBean.GRADES[0]);
		UIInput.make(form, "assignment-points", "#{simplePageBean.points}");

		UICommand.make(form, "edit-item", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.editItem}");

		UICommand.make(form, "delete-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "edit-item-cancel", messageLocator.getMessage("simplepage.cancel"), null);
	}

    // for both add multimedia and add resource, as well as updating resources in the edit dialogs
	private void createAddMultimediaDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "add-multimedia-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.resource")));
		UILink.make(tofill, "mm-additional-instructions", messageLocator.getMessage("simplepage.additional-instructions-label"), messageLocator.getMessage("simplepage.additional-instructions"));

		UIForm form = UIForm.make(tofill, "add-multimedia-form");

		UIOutput.make(form, "mm-file-label", messageLocator.getMessage("simplepage.upload_label"));

		UIOutput.make(form, "mm-url-label", messageLocator.getMessage("simplepage.addLink_label"));
		UIInput.make(form, "mm-url", "#{simplePageBean.mmUrl}");

		FilePickerViewParameters fileparams = new FilePickerViewParameters();
		fileparams.setSender(currentPage.getPageId());
		fileparams.setResourceType(true);
		fileparams.viewID = ResourcePickerProducer.VIEW_ID;
		UILink link = UIInternalLink.make(form, "mm-choose", messageLocator.getMessage("simplepage.choose_existing"), fileparams);

		// createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, toolBar, "add-resource", "simplepage.resource", false, currentPage, "simplepage.resource.tooltip");

		UICommand.make(form, "mm-add-item", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.addMultimedia}");
		UIInput.make(form, "mm-item-id", "#{simplePageBean.itemId}");
		UIInput.make(form, "mm-is-mm", "#{simplePageBean.isMultimedia}");
		UICommand.make(form, "mm-cancel", messageLocator.getMessage("simplepage.cancel"), null);
	}

	private void createEditMultimediaDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "edit-multimedia-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.editMultimedia")));

		UIOutput.make(tofill, "instructions");

		UIForm form = UIForm.make(tofill, "edit-multimedia-form");

		UIOutput.make(form, "height-label", messageLocator.getMessage("simplepage.height_label"));
		UIInput.make(form, "height", "#{simplePageBean.height}");

		UIOutput.make(form, "width-label", messageLocator.getMessage("simplepage.width_label"));
		UIInput.make(form, "width", "#{simplePageBean.width}");

		UIOutput.make(form, "description2-label", messageLocator.getMessage("simplepage.description_label"));
		UIInput.make(form, "description2", "#{simplePageBean.description}");

		FilePickerViewParameters fileparams = new FilePickerViewParameters();
		fileparams.setSender(currentPage.getPageId());
		fileparams.setResourceType(true);
		fileparams.viewID = ResourcePickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-resource-mm", messageLocator.getMessage("simplepage.change_resource"), fileparams);

		UIOutput.make(form, "alt-label", messageLocator.getMessage("simplepage.alt_label"));
		UIInput.make(form, "alt", "#{simplePageBean.alt}");

		UIInput.make(form, "mimetype", "#{simplePageBean.mimetype}");

		UICommand.make(form, "edit-multimedia-item", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.editMultimedia}");

		UIInput.make(form, "multimedia-item-id", "#{simplePageBean.itemId}");

		UICommand.make(form, "delete-multimedia-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "edit-multimedia-cancel", messageLocator.getMessage("simplepage.cancel"), null);
	}

	private void createYoutubeDialog(UIContainer tofill) {
		UIOutput.make(tofill, "youtube-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit_youtubelink")));

		UIForm form = UIForm.make(tofill, "youtube-form");
		UIInput.make(form, "youtubeURL", "#{simplePageBean.youtubeURL}");
		UIInput.make(form, "youtubeEditId", "#{simplePageBean.youtubeId}");
		UIInput.make(form, "youtubeHeight", "#{simplePageBean.height}");
		UIInput.make(form, "youtubeWidth", "#{simplePageBean.width}");
		UIOutput.make(form, "description4-label", messageLocator.getMessage("simplepage.description_label"));
		UIInput.make(form, "description4", "#{simplePageBean.description}");
		UICommand.make(form, "delete-youtube-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteYoutubeItem}");
		UICommand.make(form, "update-youtube", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.updateYoutube}");
		UICommand.make(form, "cancel-youtube", messageLocator.getMessage("simplepage.cancel"), null);
	}

	private void createMovieDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "movie-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edititem_header")));

		UIForm form = UIForm.make(tofill, "movie-form");

		UIInput.make(form, "movie-height", "#{simplePageBean.height}");
		UIInput.make(form, "movie-width", "#{simplePageBean.width}");
		UIInput.make(form, "movieEditId", "#{simplePageBean.itemId}");
		UIOutput.make(form, "description3-label", messageLocator.getMessage("simplepage.description_label"));
		UIInput.make(form, "description3", "#{simplePageBean.description}");
		UIInput.make(form, "mimetype4", "#{simplePageBean.mimetype}");

		FilePickerViewParameters fileparams = new FilePickerViewParameters();
		fileparams.setSender(currentPage.getPageId());
		fileparams.setResourceType(true);
		fileparams.viewID = ResourcePickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-resource-movie", messageLocator.getMessage("simplepage.change_resource"), fileparams);

		UICommand.make(form, "delete-movie-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "update-movie", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.updateMovie}");
		UICommand.make(form, "movie-cancel", messageLocator.getMessage("simplepage.cancel"), null);
	}

	private void createEditTitleDialog(UIContainer tofill, SimplePage page, SimplePageItem pageItem) {
		UIOutput.make(tofill, "edit-title-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.editTitle")));

		UIForm form = UIForm.make(tofill, "title-form");
		UIOutput.make(form, "pageTitleLabel", messageLocator.getMessage("simplepage.pageTitle_label"));
		UIInput.make(form, "pageTitle", "#{simplePageBean.pageTitle}");

		if (pageItem.getPageId() == 0) {
			UIOutput.make(tofill, "hideContainer");
			UIBoundBoolean.make(form, "hide", "#{simplePageBean.hidePage}", (page.isHidden()));

			Date releaseDate = page.getReleaseDate();

			UIBoundBoolean.make(form, "page-releasedate", "#{simplePageBean.hasReleaseDate}", (releaseDate != null));

			if (releaseDate == null)
			    releaseDate = new Date();
			simplePageBean.setReleaseDate(releaseDate);
			UIInput releaseForm = UIInput.make(form, "releaseDate:", "simplePageBean.releaseDate");
			dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
			dateevolver.evolveDateInput(releaseForm, page.getReleaseDate());

			UIBoundBoolean.make(form, "page-required", "#{simplePageBean.required}", (pageItem.isRequired()));
			UIBoundBoolean.make(form, "page-prerequisites", "#{simplePageBean.prerequisite}", (pageItem.isPrerequisite()));
		}

		UICommand.make(form, "create-title", messageLocator.getMessage("simplepage.save"), "#{simplePageBean.editTitle}");
		UICommand.make(form, "cancel-title", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
	}

    /* 
       return true if the item is required and not completed, i.e. if we need to update the status
       after the user views the item
    */

	private boolean handleStatusImage(UIContainer container, SimplePageItem i) {
		if (i.getType() != SimplePageItem.TEXT && i.getType() != SimplePageItem.MULTIMEDIA) {
			if (!i.isRequired()) {
				addStatusImage(Status.NOT_REQUIRED, container, "status", i.getName());
				return false;
			} else if (simplePageBean.isItemComplete(i)) {
				addStatusImage(Status.COMPLETED, container, "status", i.getName());
				return false;
			} else {
				addStatusImage(Status.REQUIRED, container, "status", i.getName());
				return true;
			}
		}
		return false;
	}

    // add the checkmark or asterisk. This code supports a couple of other statuses that we
    // never ended up using
	private void addStatusImage(Status status, UIContainer container, String imageId, String name) {
		String imagePath = "/sakai-lessonbuildertool-tool/images/";
		String imageAlt = "";

		// include names because a screen reader going down the column won't see what they are
		// associated with
		if (status == Status.COMPLETED) {
			imagePath += "checkmark.png";
			imageAlt = messageLocator.getMessage("simplepage.status.completed").replace("{}", name);
		} else if (status == Status.DISABLED) {
			imagePath += "unavailable.png";
			imageAlt = messageLocator.getMessage("simplepage.status.notavailable").replace("{}", name);
		} else if (status == Status.FAILED) {
			imagePath += "failed.png";
			imageAlt = messageLocator.getMessage("simplepage.status.failed").replace("{}", name);
		} else if (status == Status.REQUIRED) {
			imagePath += "available.png";
			imageAlt = messageLocator.getMessage("simplepage.status.required").replace("{}", name);
		} else if (status == Status.NOT_REQUIRED) {
			imagePath += "not-required.png";
			imageAlt = messageLocator.getMessage("simplepage.status.notrequired").replace("{}", name);
		}

		UIOutput.make(container, "status-td");
		UIOutput.make(container, imageId).decorate(new UIFreeAttributeDecorator("src", imagePath)).decorate(new UIFreeAttributeDecorator("alt", imageAlt)).decorate(new UITooltipDecorator(imageAlt));
	}
}
