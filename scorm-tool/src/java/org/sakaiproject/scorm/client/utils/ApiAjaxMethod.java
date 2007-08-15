package org.sakaiproject.scorm.client.utils;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public abstract class ApiAjaxMethod extends AjaxFormSubmitBehavior {
	public static final String ARG_COMPONENT_ID = "arg";
	public static final String RESULT_COMPONENT_ID = "result";
	public static final String SCO_COMPONENT_ID = "scoId";
	
	
	private static Log log = LogFactory.getLog(ApiAjaxMethod.class);
	private static final String APIClass = "APIAdapter";
	private static final long serialVersionUID = 1L;
	
	private static final String sessionFactoryBeanName = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory";
	
	private Form form;
	private ResourceReference[] references;
	private int numArgs;
	private ApiAjaxBean bean;
	
	private FlushMode flushMode;
	
	public ApiAjaxMethod(Form form, String event, ResourceReference[] references, 
			int numArgs, ApiAjaxBean bean) {
		super(form, event);
		this.form = form;
		this.references = references;
		this.numArgs = numArgs;
		this.bean = bean;
	}
	
	protected abstract String callMethod(String scoId, List<String> argumentValues, AjaxRequestTarget target);
	
	@Override
	protected void onSubmit(AjaxRequestTarget target) {		
		try {
			List<String> argumentValues = new LinkedList<String>(); 
			for (int i=0;i<numArgs;i++) {
				String argumentName = ARG_COMPONENT_ID + (i+1);
				
				FormComponent argumentComponent = (FormComponent)form.get(argumentName);
				String argumentValue = (String)argumentComponent.getConvertedInput();
				
				argumentValues.add(argumentValue);
			}
			
			FormComponent scoComponent = (FormComponent)form.get(SCO_COMPONENT_ID);
			String scoValue = (String)scoComponent.getConvertedInput();
			
			String resultValue = callMethod(scoValue, argumentValues, target);
			
			FormComponent resultComponent = (FormComponent)form.get(RESULT_COMPONENT_ID);
			bean.setResult(resultValue);
			target.addComponent(resultComponent);
		} catch (Exception e) {
			log.error("Caught a fatal exception during scorm api communication", e);
		}
	}
	
	private SessionFactory lookupSessionFactory() {
		WebRequest webRequest = (WebRequest)(getComponent().getRequest());
		ServletContext servletContext = webRequest.getHttpServletRequest().getSession().getServletContext();
		
		WebApplicationContext wac =
			WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		return (SessionFactory) wac.getBean(sessionFactoryBeanName, SessionFactory.class);
	}
	
	private boolean isSingleSession() {
		return true;
	}
	
	protected Session getSession(SessionFactory sessionFactory) throws DataAccessResourceFailureException {
		Session session = SessionFactoryUtils.getSession(sessionFactory, true);
		FlushMode flushMode = getFlushMode();
		if (flushMode != null) {
			session.setFlushMode(flushMode);
		}
		return session;
	}
	
	private FlushMode getFlushMode() {
		return flushMode;
	}
	
	public void setFlushMode(FlushMode flushMode) {
		this.flushMode = flushMode;
	}

	
	protected void wrappedOnSubmit(AjaxRequestTarget target) {
		SessionFactory sessionFactory = lookupSessionFactory();
		boolean participate = false;

		if (isSingleSession()) {
			// single session mode
			if (TransactionSynchronizationManager.hasResource(sessionFactory)) {
				// Do not modify the Session: just set the participate flag.
				participate = true;
			}
			else {
				log.debug("Opening single Hibernate Session in ApiAjaxMethod");
				Session session = getSession(sessionFactory);
				TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
			}
		}
		else {
			// deferred close mode
			if (SessionFactoryUtils.isDeferredCloseActive(sessionFactory)) {
				// Do not modify deferred close: just set the participate flag.
				participate = true;
			}
			else {
				SessionFactoryUtils.initDeferredClose(sessionFactory);
			}
		}

		try {
			onSubmit(target);
		}

		finally {
			if (!participate) {
				if (isSingleSession()) {
					// single session mode
					SessionHolder sessionHolder =
							(SessionHolder) TransactionSynchronizationManager.unbindResource(sessionFactory);
					log.debug("Closing single Hibernate Session in ApiAjaxMethod");
					SessionFactoryUtils.closeSession(sessionHolder.getSession());
				}
				else {
					// deferred close mode
					SessionFactoryUtils.processDeferredClose(sessionFactory);
				}
			}
		}
		
		
	}
	
	
	
	/*@Override
	protected String getImplementationId() {
		return getEvent();
	}*/
	
	@Override
	protected void onComponentTag(final ComponentTag tag) {
		// Do nothing -- we don't want to add the javascript to the component.
	}
	
	@Override
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		FormComponent resultComponent = (FormComponent)form.get("result");
		return new ApiAjaxCallDecorator(resultComponent.getMarkupId());
	}
	
	@Override
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

		if (log.isDebugEnabled())
			log.debug("Callback url: " + url);

		return url;
	}
	
	
	@Override
	protected CharSequence getEventHandler()
	{
		final String formId = form.getMarkupId();
		final CharSequence url = getCallbackUrl();

		AppendingStringBuffer call = new AppendingStringBuffer("apiCall('").append(
				formId).append("', '").append(url).append("'");

		return generateCallbackScript(call) + ";";
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
	    super.renderHead(response);
	    
	    if (null != references) {
			for (int i=0;i<references.length;i++) 
				response.renderJavascriptReference(references[i]);
		}
	    
	    StringBuffer script = new StringBuffer().append(APIClass)
			.append(".")
			.append(getEvent())
			.append(" = function(");
		
		for (int i=0;i<numArgs;i++) {
			script.append("arg").append(i+1);
			if (i+1<numArgs)
				script.append(", ");
		}
			
		script.append(") { ");
		
		for (int i=0;i<numArgs;i++) {
			String argumentName = "arg" + (i+1);
			FormComponent argumentComponent = (FormComponent)form.get(argumentName);
			
			script.append("document.getElementById('")
					.append(argumentComponent.getMarkupId()).append("').value")
					.append(" = arg").append(i+1).append(";\n");
		}
		
		script.append(getEventHandler())
			.append("};");
		
		response.renderJavascript(script.toString(), getEvent());
	    
	}
	
	/*private ToolURLManager getToolURLManager()
	{
		HttpServletRequest request = ((WebRequest)getComponent().getRequest()).getHttpServletRequest();
		if (request == null)
		{
			request = (HttpServletRequest) ThreadLocalManager.get(ToolURL.HTTP_SERVLET_REQUEST);
		}
		if (request != null)
		{
			return (ToolURLManager) request.getAttribute(ToolURL.MANAGER);
		}
		return null;
	}*/
		
	
	/*@Override
	protected void onSubmit(AjaxRequestTarget target) {
		System.out.println("ApiAjaxMethod: onSubmit");
		
		List<String> argumentValues = new LinkedList<String>(); 
		for (int i=0;i<numArgs;i++) {
			String argumentName = "arg" + (i+1);
			
			FormComponent argumentComponent = (FormComponent)form.get(argumentName);
			String argumentValue = (String)argumentComponent.getConvertedInput();
			
			argumentValues.add(argumentValue);
		}
		
		String resultValue = callMethod(argumentValues);
		
		FormComponent resultComponent = (FormComponent)form.get("result");
		bean.setResult(resultValue);
		target.addComponent(resultComponent);
	}
		
	@Override
	protected String getImplementationId() {
		return getEvent();
	}
	
	@Override
	protected void onComponentTag(final ComponentTag tag) {
		// Do nothing -- we don't want to add the javascript to the component.
	}
	
	@Override
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		FormComponent resultComponent = (FormComponent)form.get("result");
		return new ApiAjaxCallDecorator(resultComponent.getMarkupId());
	}
	
	@Override
	protected void onRenderHeadInitContribution(Response response)
	{
		if (null != references) {
			for (int i=0;i<references.length;i++) 
				writeJsReference(response, references[i]);
		}

		StringBuffer script = new StringBuffer().append(APIClass)
			.append(".")
			.append(getEvent())
			.append(" = function(");
		
		for (int i=0;i<numArgs;i++) {
			script.append("arg").append(i+1);
			if (i+1<numArgs)
				script.append(", ");
		}
			
		script.append(") { ");
		
		for (int i=0;i<numArgs;i++) {
			String argumentName = "arg" + (i+1);
			FormComponent argumentComponent = (FormComponent)form.get(argumentName);
			
			script.append("document.getElementById('")
					.append(argumentComponent.getMarkupId()).append("').value")
					.append(" = arg").append(i+1).append("\n");
		}
		
		script.append(getEventHandler())
			.append("};");
		
		JavascriptUtils.writeJavascript(response, script.toString(), getEvent());
	}*/

}
