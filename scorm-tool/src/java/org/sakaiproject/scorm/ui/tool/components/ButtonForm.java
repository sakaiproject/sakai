package org.sakaiproject.scorm.ui.tool.components;

import javax.servlet.http.HttpServletRequest;

import org.adl.sequencer.SeqNavRequests;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.sakaiproject.scorm.ui.tool.RunState;
import org.sakaiproject.scorm.ui.tool.pages.View;

public class ButtonForm extends Form {
	private static final long serialVersionUID = 1L;

	private static final ResourceReference STARTBTN_INACTIVE = new CompressedResourceReference(AjaxImageButton.class, "res/startBtn_inactive.gif");
	private static final ResourceReference STARTBTN_ACTIVE = new CompressedResourceReference(AjaxImageButton.class, "res/startBtn_active.gif");
	private static final ResourceReference STARTBTN_DISABLED = new CompressedResourceReference(AjaxImageButton.class, "res/startBtn_disabled.gif");
	
	private static final ResourceReference PREVBTN_INACTIVE = new CompressedResourceReference(AjaxImageButton.class, "res/prevBtn_inactive.gif");
	private static final ResourceReference PREVBTN_ACTIVE = new CompressedResourceReference(AjaxImageButton.class, "res/prevBtn_active.gif");
	private static final ResourceReference PREVBTN_DISABLED = new CompressedResourceReference(AjaxImageButton.class, "res/prevBtn_disabled.gif");
	
	private static final ResourceReference NEXTBTN_INACTIVE = new CompressedResourceReference(AjaxImageButton.class, "res/nextBtn_inactive.gif");
	private static final ResourceReference NEXTBTN_ACTIVE = new CompressedResourceReference(AjaxImageButton.class, "res/nextBtn_active.gif");
	private static final ResourceReference NEXTBTN_DISABLED = new CompressedResourceReference(AjaxImageButton.class, "res/nextBtn_disabled.gif");
	
	private static final ResourceReference QUITBTN_INACTIVE = new CompressedResourceReference(AjaxImageButton.class, "res/quitBtn_inactive.gif");
	private static final ResourceReference QUITBTN_ACTIVE = new CompressedResourceReference(AjaxImageButton.class, "res/quitBtn_active.gif");
	private static final ResourceReference QUITBTN_DISABLED = new CompressedResourceReference(AjaxImageButton.class, "res/quitBtn_disabled.gif");
	
	private static final ResourceReference SUSPENDBTN_INACTIVE = new CompressedResourceReference(AjaxImageButton.class, "res/suspendBtn_inactive.gif");
	private static final ResourceReference SUSPENDBTN_ACTIVE = new CompressedResourceReference(AjaxImageButton.class, "res/suspendBtn_active.gif");
	private static final ResourceReference SUSPENDBTN_DISABLED = new CompressedResourceReference(AjaxImageButton.class, "res/suspendBtn_disabled.gif");
	
	
	private ActivityAjaxButton prevButton, nextButton, startButton, quitButton, suspendButton;
	private View view;
	//private LaunchPanel launchPanel;
	
	public ButtonForm(String id, final RunState runState, View view) {
		super(id);
		this.view = view;
		//this.launchPanel = launchPanel;
		
		prevButton = new ActivityAjaxButton("prevButton") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {		
				doNavigate(runState, SeqNavRequests.NAV_PREVIOUS, target);
			}
			
			protected ResourceReference getDisabledBtn() {
				return PREVBTN_DISABLED;
			}
			
			protected ResourceReference getActiveBtn() {
				return PREVBTN_ACTIVE;
			}
			
			protected ResourceReference getInactiveBtn() {
				return PREVBTN_INACTIVE;
			}
		};
		
		nextButton = new ActivityAjaxButton("nextButton") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				doNavigate(runState, SeqNavRequests.NAV_CONTINUE, target);
			}
			
			protected ResourceReference getDisabledBtn() {
				return NEXTBTN_DISABLED;
			}
			
			protected ResourceReference getActiveBtn() {
				return NEXTBTN_ACTIVE;
			}
			
			protected ResourceReference getInactiveBtn() {
				return NEXTBTN_INACTIVE;
			}
		};
		
		startButton = new ActivityAjaxButton("startButton") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				doNavigate(runState, SeqNavRequests.NAV_START, target);
			}
			
			protected ResourceReference getDisabledBtn() {
				return STARTBTN_DISABLED;
			}
			
			protected ResourceReference getActiveBtn() {
				return STARTBTN_ACTIVE;
			}
			
			protected ResourceReference getInactiveBtn() {
				return STARTBTN_INACTIVE;
			}
		};
		
		quitButton = new ActivityAjaxButton("quitButton") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				doNavigate(runState, SeqNavRequests.NAV_EXITALL, target);
			}
			
			protected ResourceReference getDisabledBtn() {
				return QUITBTN_DISABLED;
			}
			
			protected ResourceReference getActiveBtn() {
				return QUITBTN_ACTIVE;
			}
			
			protected ResourceReference getInactiveBtn() {
				return QUITBTN_INACTIVE;
			}
		};
		
		suspendButton = new ActivityAjaxButton("suspendButton") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				doNavigate(runState, SeqNavRequests.NAV_SUSPENDALL, target);
				// FIXME: This needs to be mapped using the new sco specific clientBeans
				//getLaunchPanel().getClientBean().suspendButtonPushed();
			}
			
			protected ResourceReference getDisabledBtn() {
				return SUSPENDBTN_DISABLED;
			}
			
			protected ResourceReference getActiveBtn() {
				return SUSPENDBTN_ACTIVE;
			}
			
			protected ResourceReference getInactiveBtn() {
				return SUSPENDBTN_INACTIVE;
			}
		};
		
		add(prevButton);
		add(nextButton);
		add(startButton);
		add(quitButton);
		add(suspendButton);
		
				
		synchronizeState(runState, null);
		/*add(new AjaxEventBehavior("onunload") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target) {
				doNavigate(runState, SeqNavRequests.NAV_EXITALL, target);
			}});*/
	}

	
	public void synchronizeState(RunState runState, AjaxRequestTarget target) {
		setNextButtonVisible(runState.isContinueEnabled() || runState.isContinueExitEnabled(), target);
		setPrevButtonVisible(runState.isPreviousEnabled(), target);
		setStartButtonVisible(runState.isStartEnabled(), target);
		setSuspendButtonVisible(runState.isSuspendEnabled(), target);
		setQuitButtonVisible(runState.isStarted() && !runState.isEnded(), target);
	}

	public void setPrevButtonVisible(boolean isVisible, AjaxRequestTarget target) {
		setButtonVisible(prevButton, isVisible, target);
	}
	
	public void setNextButtonVisible(boolean isVisible, AjaxRequestTarget target) {
		setButtonVisible(nextButton, isVisible, target);
	}
	
	public void setStartButtonVisible(boolean isVisible, AjaxRequestTarget target) {
		setButtonVisible(startButton, isVisible, target);
	}
	
	public void setQuitButtonVisible(boolean isVisible, AjaxRequestTarget target) {
		setButtonVisible(quitButton, isVisible, target);
	}
	
	public void setSuspendButtonVisible(boolean isVisible, AjaxRequestTarget target) {
		setButtonVisible(suspendButton, isVisible, target);
	}
	
	private void doNavigate(RunState runState, int seqRequest, AjaxRequestTarget target) {
		runState.navigate(seqRequest, target);
		if (getLaunchPanel() != null) {		
			getLaunchPanel().synchronizeState(runState, target);
			getLaunchPanel().getTreePanel().getActivityTree().selectNode();
			getLaunchPanel().getCommunicationPanel().updatePageSco(runState.getCurrentSco(), target);
		}
	}
	
	
	private void setButtonVisible(AjaxImageButton button, boolean isEnabled, AjaxRequestTarget target) {
		if (null != button) { 
			boolean wasEnabled = button.isEnabled();
			button.setEnabled(isEnabled);
			
			if (!button.isSyncd() || wasEnabled != isEnabled) {
				if (target != null) {
					target.addComponent(button);
					button.setSyncd(true);
				} else
					button.setSyncd(false);
			}
		}
	}
	
	
	public LaunchPanel getLaunchPanel() {
		return view.getLaunchPanel();
	}
	
	
	public abstract class ActivityAjaxButton extends AjaxImageButton {
		private Form form;
		
		public ActivityAjaxButton(String id) {
			this(id, null);
		}

		public ActivityAjaxButton(String id, final Form form) {
			super(id, form);
			this.form = form;
			
			add(new AjaxFormSubmitBehavior(form, "onclick")
			{
				private static final long serialVersionUID = 1L;

				protected void onSubmit(AjaxRequestTarget target)
				{
					ActivityAjaxButton.this.onSubmit(target, form);
				}

				protected void onError(AjaxRequestTarget target)
				{
					ActivityAjaxButton.this.onError(target, form);
				}

				protected CharSequence getEventHandler()
				{
					// TODO: May want to stick this back in: ("tb_showLoader();").append(
					return new AppendingStringBuffer(super.getEventHandler()).append("; return false;");
				}

				protected IAjaxCallDecorator getAjaxCallDecorator()
				{
					return ActivityAjaxButton.this.getAjaxCallDecorator();
				}
				
				public CharSequence getCallbackUrl()
				{
					if (getComponent() == null)
					{
						throw new IllegalArgumentException(
								"Behavior must be bound to a component to create the URL");
					}
					
					final RequestListenerInterface rli;
					
					rli = IBehaviorListener.INTERFACE;
					
					WebRequest webRequest = (WebRequest)getComponent().getRequest();
					HttpServletRequest servletRequest = webRequest.getHttpServletRequest();

					String toolUrl = servletRequest.getContextPath();
					
					AppendingStringBuffer url = new AppendingStringBuffer();
					url.append(toolUrl).append("/");
					url.append(getComponent().urlFor(this, rli));

					return url;
				}
			});
		}
		
		public Form getForm()
		{
			if (form != null)
				return form;
			else
				return super.getForm();
		}
		
		/**
		 * Listener method invoked on form submit with no errors
		 * 
		 * @param target
		 * @param form
		 */
		protected abstract void onSubmit(AjaxRequestTarget target, Form form);

		/**
		 * Listener method invoked on form submit with errors
		 * 
		 * @param target
		 * @param form
		 * 
		 * TODO 1.3: Make abstract to be consistent with onSubmit()
		 */
		protected void onError(AjaxRequestTarget target, Form form)
		{

		}
		
	}


	public ActivityAjaxButton getQuitButton() {
		return quitButton;
	}
	

}
