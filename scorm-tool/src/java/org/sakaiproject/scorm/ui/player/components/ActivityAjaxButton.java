package org.sakaiproject.scorm.ui.player.components;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.ResourceNavigator;
import org.sakaiproject.wicket.ajax.markup.html.form.AjaxRolloverImageButton;

public class ActivityAjaxButton extends AjaxRolloverImageButton {
	
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(ActivityAjaxButton.class);
	
	private static final String IMAGE_EXT = ".gif";
	
	private static final String ACTIVE_SUFFIX = "_active";
	private static final String INACTIVE_SUFFIX = "_inactive";
	private static final String DISABLED_SUFFIX = "_disabled";
	
	private boolean isSyncd = true;
	private ButtonForm form;
	
	private SessionBean sessionBean;
	private int seqRequest;
	private String rootSrc;
	
	@SpringBean
	ScormResourceService resourceService;
	@SpringBean
	ScormSequencingService sequencingService;
	
	public ActivityAjaxButton(final ButtonForm form, SessionBean sessionBean, String id, int seqRequest, String rootSrc) {
		super(id, form);
		this.form = form;
		this.sessionBean = sessionBean;
		this.seqRequest = seqRequest;
		this.rootSrc = rootSrc;
		
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
	
	private void doNavigate(SessionBean sessionBean, int seqRequest, AjaxRequestTarget target) {
		sequencingService.navigate(seqRequest, sessionBean, new LocalResourceNavigator(), target);
		
		if (form.getLaunchPanel() != null) {		
			form.getLaunchPanel().synchronizeState(sessionBean, target);
			form.getLaunchPanel().getTreePanel().getActivityTree().selectNode();
			form.getLaunchPanel().getCommunicationPanel().updatePageSco(sessionBean.getScoId(), target);
		}
	}
	
	public void displayContent(SessionBean sessionBean, Object target) {
		if (null == target)
			return;
		
		if (sessionBean.isEnded()) {		
			((AjaxRequestTarget)target).appendJavascript("window.location.href='" + sessionBean.getCompletionUrl() + "';");
		}
		
		String url = getCurrentUrl(sessionBean);
		if (null != url) {
			if (log.isDebugEnabled())
				log.debug("Going to " + url);
			
			((AjaxRequestTarget)target).appendJavascript("parent.scormContent.location.href='" + url + "'");
		} else {
			log.warn("Url is null!");
		}
	}
	
	public String getCurrentUrl(SessionBean sessionBean) {
		if (null != sessionBean.getLaunchData()) {
			String launchLine = sessionBean.getLaunchData().getLaunchLine();
			String baseUrl = sessionBean.getBaseUrl();
			StringBuffer fullPath = new StringBuffer().append(baseUrl);
			
			if (!baseUrl.endsWith(Entity.SEPARATOR) && !launchLine.startsWith(Entity.SEPARATOR))
				fullPath.append(Entity.SEPARATOR);

			fullPath.append(launchLine);
						
			return fullPath.toString();
		}
		return null;
	}
	
	protected void onSubmit(AjaxRequestTarget target, Form form) {
		doNavigate(sessionBean, seqRequest, target);
	}
	
	protected String getRootSrc() {
		return rootSrc;
	}
	
	protected String getDisabledSrc()
	{
		return assembleSrc(getRootSrc(), DISABLED_SUFFIX, IMAGE_EXT);
	}
	
	protected String getInactiveSrc()
	{
		return assembleSrc(getRootSrc(), INACTIVE_SUFFIX, IMAGE_EXT);
	}
	
	protected String getActiveSrc()
	{
		return assembleSrc(getRootSrc(), ACTIVE_SUFFIX, IMAGE_EXT);
	}
	
	protected String assembleSrc(String rootSrc, String suffix, String ext) {
		StringBuilder builder = new StringBuilder(rootSrc).append(suffix).append(ext);
		return builder.toString();
	}
	
	public boolean isSyncd() {
		return isSyncd;
	}

	public void setSyncd(boolean isSyncd) {
		this.isSyncd = isSyncd;
	}
	
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
	
	
	public class LocalResourceNavigator extends ResourceNavigator {

		private static final long serialVersionUID = 1L;

		@Override
		protected ScormResourceService resourceService() {
			return ActivityAjaxButton.this.resourceService;
		}
		
	}
	
}
