/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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

import java.net.URLEncoder;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringEscapeUtils;

import uk.org.ponder.localeutil.LocaleGetter;  
import uk.org.ponder.messageutil.MessageLocator;                                                                                        
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;                                                               
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.TrackerViewParameters;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonBuilderAccessService;

@Slf4j
public class LinkTrackerProducer implements ViewComponentProducer, ViewParamsReporter {
	public static final String VIEW_ID = "LinkTracker";

	public String getViewID() {
		return VIEW_ID;
	}

	private SimplePageBean simplePageBean;

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	private SimplePageToolDao simplePageToolDao;

	public void setSimplePageToolDao(SimplePageToolDao s) {
		simplePageToolDao = s;
	}

	private static LessonBuilderAccessService lessonBuilderAccessService;

	public void setLessonBuilderAccessService (LessonBuilderAccessService a) {
	    if (lessonBuilderAccessService == null)
		lessonBuilderAccessService = a;
	}

	public MessageLocator messageLocator;

	public void setMessageLocator(MessageLocator s) {
		messageLocator = s;
	}

	public LocaleGetter localeGetter;                                                                                             

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		TrackerViewParameters params = (TrackerViewParameters) viewparams;

                if (!simplePageBean.canReadPage())
		    return;

                UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
		    .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));        

		Long itemId = params.getItemId();

		SimplePageItem i = simplePageToolDao.findItem(itemId);

		SimplePage page = simplePageBean.getCurrentPage();

		if (i.getPageId() != page.getPageId()) {
		    log.info("LinkTracker asked to track item not in current page");
		    return;
		}

		if (i != null && simplePageBean.isItemAvailable(i)) {

		    simplePageBean.track(itemId, null);

		    String URL = params.getURL();
		    if (lessonBuilderAccessService.needsCopyright(i.getSakaiId()))
			URL = "/access/require?ref=" + URLEncoder.encode("/content" + i.getSakaiId()) + "&url=" + URLEncoder.encode(URL.substring(7));

		    String js = "window.location = \"" + StringEscapeUtils.escapeJavaScript(URL) + "\"";
		    if (params.getRefresh())
			js = "window.top.opener.location.reload(true);" + js;
		    UIVerbatim.make(tofill, "redirect", js);

		} else {

		    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.error"));
		    UIOutput.make(tofill, "errormsg", messageLocator.getMessage("simplepage.complete_required"));
		}
	}

    	public static UIInternalLink make(UIContainer container, String ID, String name, String URL, long itemId, boolean notDone) {
		TrackerViewParameters params = new TrackerViewParameters(URL, itemId, notDone);
		return UIInternalLink.make(container, ID, params);
	}

	public ViewParameters getViewParameters() {
		return new TrackerViewParameters();
	}
}
