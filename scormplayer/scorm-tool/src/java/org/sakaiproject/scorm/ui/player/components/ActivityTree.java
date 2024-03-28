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

import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.model.IModel;

import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.ui.UISynchronizerPanel;

/**
 *
 * @author bjones86
 */
public class ActivityTree extends NestedTree<SeqActivityNode>
{
	private SessionBean sessionBean;
	private UISynchronizerPanel synchronizer;
	private ActivityTreePanel treePanel;

	public ActivityTree( String id, SeqActivityProvider provider, IModel<Set<SeqActivityNode>> state, SessionBean sessionBean, UISynchronizerPanel synchronizer, ActivityTreePanel treePanel )
	{
		super( id, provider, state );
		this.sessionBean = sessionBean;
		this.synchronizer = synchronizer;
		this.treePanel = treePanel;
		add( new WindowsTheme() );
	}

	@Override
	protected Component newContentComponent( String id, IModel<SeqActivityNode> imodel )
	{
		return new ActivityLinkPanel( id, imodel, sessionBean, synchronizer, treePanel);
	}
}
