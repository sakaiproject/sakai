package org.sakaiproject.scorm.tool.components;

import javax.servlet.http.HttpServletRequest;

import org.adl.sequencer.SeqNavRequests;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.sakaiproject.scorm.tool.RunState;

public class ButtonForm extends Form {
	private static final long serialVersionUID = 1L;

	private AjaxButton prevButton, nextButton, startButton, quitButton, suspendButton;
	private LaunchPanel launchPanel;
	
	
	public ButtonForm(String id, final RunState runState, LaunchPanel launchPanel) {
		super(id);
		this.launchPanel = launchPanel;
		
		prevButton = new ActivityAjaxButton("prevButton") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {		
				doNavigate(runState, SeqNavRequests.NAV_PREVIOUS, target);
			}
		};
		
		nextButton = new ActivityAjaxButton("nextButton") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				doNavigate(runState, SeqNavRequests.NAV_CONTINUE, target);
			}
		};
		
		startButton = new ActivityAjaxButton("startButton") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				doNavigate(runState, SeqNavRequests.NAV_START, target);
			}
		};
		
		quitButton = new ActivityAjaxButton("quitButton") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				doNavigate(runState, SeqNavRequests.NAV_EXIT, target);
			}
		};
		
		suspendButton = new ActivityAjaxButton("suspendButton") {
			protected void onSubmit(AjaxRequestTarget target, Form form) {
				doNavigate(runState, SeqNavRequests.NAV_SUSPENDALL, target);
				// FIXME: This needs to be mapped using the new sco specific clientBeans
				//getLaunchPanel().getClientBean().suspendButtonPushed();
			}
		};
		
		add(prevButton);
		add(nextButton);
		add(startButton);
		add(quitButton);
		add(suspendButton);
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
		getLaunchPanel().synchronizeState(runState, target);
		getLaunchPanel().getTreePanel().getActivityTree().selectNode();
		getLaunchPanel().getApiPanel().updatePageSco(runState.getCurrentSco(), target);
	}
	
	
	private void setButtonVisible(AjaxButton button, boolean isVisible, AjaxRequestTarget target) {
		if (null != button && button.isEnabled() != isVisible) {
			button.setEnabled(isVisible);

			if (target != null)
				target.addComponent(this);
		}
	}
	
	
	public LaunchPanel getLaunchPanel() {
		return launchPanel;
	}
	
	
	public abstract class ActivityAjaxButton extends AjaxButton {
		private Form form;
		
		public ActivityAjaxButton(String id) {
			this(id, null);
		}

		public ActivityAjaxButton(String id, final Form form) {
			super(id);
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
	

}
