/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.scorm.ui.player.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.UISynchronizerPanel;
import org.sakaiproject.scorm.ui.player.pages.ScormPlayerPage;

public class LaunchPanel extends UISynchronizerPanel implements IHeaderContributor
{
	private static final long serialVersionUID = 1L;

	protected static final String RESIZESCRIPT = "scripts/resize.js";

	private ScormPlayerPage view;
	private ActivityTreePanel treePanel;
	private WebMarkupContainer contentPanel;

	@SpringBean
	LearningManagementSystem lms;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormResourceService")
	ScormResourceService resourceService;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormSequencingService")
	ScormSequencingService sequencingService;

	public LaunchPanel(String id, final SessionBean sessionBean, ScormPlayerPage view)
	{
		super(id, new Model(sessionBean));
		this.view = view;

		add(new SjaxContainer("sjaxContainer", sessionBean, this));

		contentPanel = new WebMarkupContainer("scormContent");
		contentPanel.setOutputMarkupId(true);
		contentPanel.setMarkupId("scormContent");
		add(contentPanel);

		// Tree is hidden if showTOC is false or there are no nodes in the tree
		treePanel = new ActivityTreePanel("tree", sessionBean, sequencingService, this);
		if (!sessionBean.getContentPackage().isShowTOC() || !new SeqActivityProvider(sessionBean, sequencingService).getRoots().hasNext())
		{
			treePanel.setVisible(false);
		}
		add(treePanel);
	}

	@Override
	public void synchronizeState(SessionBean sessionBean, AjaxRequestTarget target)
	{
		if (null != view)
		{
			view.synchronizeState(sessionBean, target);
		}
	}

	public ScormPlayerPage getView()
	{
		return view;
	}

	@Override
	public ActivityTree getTree()
	{
		return treePanel.getTree();
	}

	public ActivityTreePanel getPanel()
	{
		return treePanel;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forUrl(RESIZESCRIPT));
		response.render(OnDomReadyHeaderItem.forScript("initResizing()"));
		response.render(JavaScriptHeaderItem.forScript("window.resize = function() { onResize(); }", null));
	}

	@Override
	public WebMarkupContainer getContentPanel()
	{
		return contentPanel;
	}

	public void setContentPanel(WebMarkupContainer contentPanel)
	{
		this.contentPanel = contentPanel;
	}
}
