/**
 * Copyright (c) 2007-2019 The Apereo Foundation
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

import java.util.Optional;
import java.util.Set;

import lombok.Getter;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.UISynchronizerPanel;

/**
 *
 * @author bjones86
 */
public class ActivityTreePanel extends Panel
{
	private static final long serialVersionUID = 1L;

	@Getter private ActivityTree tree;
	private SessionBean sessionBean;
	private ScormSequencingService sequencingService;
	private UISynchronizerPanel synchronizer;

	private SeqActivityProvider provider;

	public ActivityTreePanel(String id, SessionBean sessionBean, ScormSequencingService sequencingService, UISynchronizerPanel synchronizer)
	{
		super(id);
		this.sessionBean = sessionBean;
		this.sequencingService = sequencingService;
		this.synchronizer = synchronizer;
		provider = new SeqActivityProvider(sessionBean, sequencingService);
		tree = new ActivityTree("activityTree", provider, new SeqActivityNodeExpansionModel(), sessionBean, synchronizer, this);
		add(tree);
		ActivityTreeExpansion.get().expandAll();
		setOutputMarkupId( true );
	}

	private class SeqActivityNodeExpansionModel implements IModel<Set<SeqActivityNode>>
	{
		@Override
		public Set<SeqActivityNode> getObject()
		{
			return ActivityTreeExpansion.get();
		}
	}

	public Optional<String> getSelectedID()
	{
		return Optional.ofNullable(StringUtils.trimToNull(ActivityTreeExpansion.get().getSelectedID()));
	}

	public void updateTree(String selectedNodeID, AjaxRequestTarget target)
	{
		ActivityTreeExpansion.get().setSelectedID(selectedNodeID);
		tree = new ActivityTree("activityTree", provider, new SeqActivityNodeExpansionModel(), sessionBean, synchronizer, this);
		target.add(this);
	}
}
