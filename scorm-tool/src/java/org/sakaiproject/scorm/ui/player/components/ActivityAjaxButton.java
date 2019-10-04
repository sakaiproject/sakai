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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;

import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.ResourceNavigator;
import org.sakaiproject.wicket.ajax.markup.html.form.AjaxRolloverImageButton;
import org.sakaiproject.wicket.util.Utils;

@Slf4j
public class ActivityAjaxButton extends AjaxRolloverImageButton
{
	private static final long serialVersionUID = 1L;

	private static final String IMAGE_EXT = ".gif";
	private static final String ACTIVE_SUFFIX = "_active";
	private static final String INACTIVE_SUFFIX = "_inactive";
	private static final String DISABLED_SUFFIX = "_disabled";

	@Getter @Setter private boolean isSyncd = true;
	private ButtonForm form;

	private int seqRequest;
	@Getter private String rootSrc;

	@SpringBean
	LearningManagementSystem lms;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormResourceService")
	ScormResourceService resourceService;

	@SpringBean(name="org.sakaiproject.scorm.service.api.ScormSequencingService")
	ScormSequencingService sequencingService;

	public ActivityAjaxButton(final ButtonForm form, SessionBean sessionBean, String id, int seqRequest, String rootSrc)
	{
		super(id, form);
		this.form = form;
		this.seqRequest = seqRequest;
		this.rootSrc = rootSrc;

		this.setModel(new Model(sessionBean));
		final boolean useRelativeUrls = lms.canUseRelativeUrls();

		add(new AjaxFormSubmitBehavior(form, "onclick")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				ActivityAjaxButton.this.onSubmit(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				ActivityAjaxButton.this.onError(target);
			}

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				attributes.setThrottlingSettings(new ThrottlingSettings(Duration.milliseconds(50)));
				if (!useRelativeUrls)
				{
					attributes.getAjaxCallListeners().add(new AjaxCallListener().onPrecondition("return false;"));
				}
			}

			@Override
			public CharSequence getCallbackUrl()
			{
				if (useRelativeUrls)
				{
					return super.getCallbackUrl();
				}

				return Utils.generateUrl(this, getComponent(), useRelativeUrls);
			}
		});
	}

	@Override
	public Form getForm()
	{
		return form != null ? form : super.getForm();
	}

	private void doNavigate(SessionBean sessionBean, int seqRequest, AjaxRequestTarget target)
	{
		sequencingService.navigate(seqRequest, sessionBean, new LocalResourceNavigator(), target);

		if (form.getLaunchPanel() != null)
		{
			form.getLaunchPanel().synchronizeState(sessionBean, target);
			form.getLaunchPanel().getPanel().updateTree(sessionBean.getActivityId(), target);
		}
	}

	@Override
	protected void onSubmit(AjaxRequestTarget target)
	{
		SessionBean sessionBean = (SessionBean)getDefaultModelObject();
		modelChanging();
		doNavigate(sessionBean, seqRequest, target);
		modelChanged();
	}

	@Override
	protected String getDisabledSrc()
	{
		return assembleSrc(getRootSrc(), DISABLED_SUFFIX, IMAGE_EXT);
	}

	@Override
	protected String getInactiveSrc()
	{
		return assembleSrc(getRootSrc(), INACTIVE_SUFFIX, IMAGE_EXT);
	}

	@Override
	protected String getActiveSrc()
	{
		return assembleSrc(getRootSrc(), ACTIVE_SUFFIX, IMAGE_EXT);
	}

	protected String assembleSrc(String rootSrc, String suffix, String ext)
	{
		return rootSrc + suffix + ext;
	}

	/**
	 * Listener method invoked on form submit with errors
	 * 
	 * @param target
	 * @param form
	 * 
	 * TODO 1.3: Make abstract to be consistent with onSubmit()
	 */
	@Override
	protected void onError(AjaxRequestTarget target) {}

	public class LocalResourceNavigator extends ResourceNavigator
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected ScormResourceService resourceService()
		{
			return ActivityAjaxButton.this.resourceService;
		}

		@Override
		public Component getFrameComponent()
		{
			if (form.getLaunchPanel() != null && form.getLaunchPanel().getContentPanel() != null)
			{
				return form.getLaunchPanel().getContentPanel();
			}

			return null;
		}
	}
}
