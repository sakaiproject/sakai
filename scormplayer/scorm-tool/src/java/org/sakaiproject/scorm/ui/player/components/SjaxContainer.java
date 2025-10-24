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

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.ResourceNavigator;
import org.sakaiproject.scorm.ui.UISynchronizerPanel;
import org.sakaiproject.scorm.ui.player.behaviors.SjaxCall;

@Slf4j
public class SjaxContainer extends WebMarkupContainer implements IHeaderContributor
{
	private static final long serialVersionUID = 1L;

	private static final ResourceReference SJAX = new PackageResourceReference(SjaxContainer.class, "res/scorm-sjax.js");

	@SpringBean
	LearningManagementSystem lms;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormApplicationService")
	ScormApplicationService applicationService;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormResourceService")
	ScormResourceService resourceService;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormSequencingService")
	ScormSequencingService sequencingService;
	
	private UISynchronizerPanel synchronizerPanel;
	private SjaxCall[] calls = new SjaxCall[8]; 
	private HiddenField[] components = new HiddenField[8];

	public SjaxContainer(String id, final SessionBean sessionBean, final UISynchronizerPanel synchronizerPanel)
	{
		super(id, new Model(sessionBean));
		this.synchronizerPanel = synchronizerPanel;
		setOutputMarkupId(true);
		setMarkupId("sjaxContainer");

		calls[0] = new ScormSjaxCall("Commit", 1);
		calls[1] = new ScormSjaxCall("GetDiagnostic", 1);
		calls[2] = new ScormSjaxCall("GetErrorString", 1);
		calls[3] = new ScormSjaxCall("GetLastError", 0);
		calls[4] = new ScormSjaxCall("GetValue", 1);
		calls[5] = new ScormSjaxCall("Initialize", 1)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String callMethod(ScoBean blankScoBean, AjaxRequestTarget target, Object... args)
			{
				ScoBean scoBean = applicationService().produceScoBean("undefined", getSessionBean());
				String result = super.callMethod(scoBean, target, args);
				synchronizerPanel.synchronizeState(sessionBean, target);
				return result;
			}
		};

		calls[6] = new ScormSjaxCall("SetValue", 2);
		calls[7] = new ScormSjaxCall("Terminate", 1)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String callMethod(ScoBean scoBean, AjaxRequestTarget target, Object... args)
			{
				String result = super.callMethod(scoBean, target, args);
				if (scoBean != null)
				{
					applicationService.discardScoBean(scoBean.getScoId(), sessionBean, new LocalResourceNavigator());
				}

				return result;
			}
		};

		Form form = new Form("sjaxForm");
		add(form);

		components[0] = addSjaxComponent("commitcall", calls[0], form);
		components[1] = addSjaxComponent("getdiagnosticcall", calls[1], form);
		components[2] = addSjaxComponent("geterrorstringcall", calls[2], form);
		components[3] = addSjaxComponent("getlasterrorcall", calls[3], form);
		components[4] = addSjaxComponent("getvaluecall", calls[4], form);
		components[5] = addSjaxComponent("initializecall", calls[5], form);
		components[6] = addSjaxComponent("setvaluecall", calls[6], form);
		components[7] = addSjaxComponent("terminatecall", calls[7], form);
	}

	@Override
	public void onBeforeRender()
	{
		super.onBeforeRender();

		for (int i = 0; i < 8; i++)
		{
			components[i].setModel(new Model(calls[i].getCallUrl().toString()));
		}
	}

	private HiddenField addSjaxComponent(String callname, SjaxCall call, Form form)
	{
		HiddenField cc = new HiddenField(callname); 
		form.add(cc);
		cc.setMarkupId(callname);
		cc.setVersioned(false);
		cc.add(call);
		return cc;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		response.render(JavaScriptHeaderItem.forReference(SJAX));

		StringBuffer js = new StringBuffer()
			.append("function APIAdapter() { };\n")
			.append("var API_1484_11 = APIAdapter;\n")
			.append("var api_result = new Array();\n")
			.append("var call_number = 0;\n");

		for( SjaxCall call : calls )
		{
			js.append( call.getJavascriptCode() ).append( "\n" );
		}

		response.render(JavaScriptHeaderItem.forScript(js.toString(), "SCORM_API"));
	}

	public class ScormSjaxCall extends SjaxCall
	{
		private static final long serialVersionUID = 1L;

		public ScormSjaxCall(String event, int numArgs)
		{
			super(event, numArgs);
		}

		@Override
		protected String callMethod(ScoBean scoBean, AjaxRequestTarget target, Object... args)
		{
			String result = super.callMethod(scoBean, target, args);
			if (target != null)
			{
				target.add(SjaxContainer.this);
			}
			if (log.isDebugEnabled())
			{
				String methodName = getEvent();
				StringBuilder argDisplay = new StringBuilder();
				for (int i = 0; i < args.length; i++)
				{
					argDisplay.append("'").append(args[i]).append("'");
					if (i + 1 < args.length)
					{
						argDisplay.append(", ");
					}
				}
				String display = new StringBuilder().append(methodName)
					.append("(")
					.append(argDisplay).append(")").append(" returns ")
					.append("'").append(result).append("'").toString();

				log.debug(display);
			}

			return result;
		}

		@Override
		protected void onEvent(final AjaxRequestTarget target)
		{
			modelChanging();
			super.onEvent(target);
			modelChanged();
		}

		@Override
		protected SessionBean getSessionBean()
		{
			return (SessionBean)getDefaultModelObject();
		}

		@Override
		protected LearningManagementSystem lms()
		{
			return lms;
		}

		@Override
		protected ScormApplicationService applicationService()
		{
			return applicationService;
		}

		@Override
		protected ScormResourceService resourceService()
		{
			return resourceService;
		}

		@Override
		protected ScormSequencingService sequencingService()
		{
			return sequencingService;
		}

		protected String getChannelName()
		{
			return "1|s";
		}

		@Override
		protected INavigable getNavigationAgent()
		{
			return new LocalResourceNavigator();
		}
	}

	public class LocalResourceNavigator extends ResourceNavigator
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected ScormResourceService resourceService()
		{
			return SjaxContainer.this.resourceService;
		}

		@Override
		public Component getFrameComponent()
		{
			if (synchronizerPanel != null && synchronizerPanel.getContentPanel() != null) 
			{
				return synchronizerPanel.getContentPanel();
			}

			return null;
		}

		@Override
		public boolean useLocationRedirect()
		{
			return false;
		}
	}
}
