package org.sakaiproject.scorm.tool.components;

import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.CompoundPropertyModel;
import org.sakaiproject.scorm.client.utils.ApiAjaxBean;
import org.sakaiproject.scorm.client.utils.BlankSjaxCall;
import org.sakaiproject.scorm.client.utils.SJAXCall;
import org.sakaiproject.scorm.tool.RunState;
import org.sakaiproject.scorm.tool.ScoBean;

public class CommunicationPanel extends Panel implements IHeaderContributor {
	public static final ResourceReference API = new CompressedResourceReference(
			CommunicationPanel.class, "API.js");

	public static final ResourceReference API_WRAPPER = new CompressedResourceReference(
			CommunicationPanel.class, "APIWrapper.js");

	
	private static final long serialVersionUID = 1L;

	private ApiAjaxBean bean = new ApiAjaxBean();
	private LaunchPanel launchPanel;
	
	public CommunicationPanel(String id, final RunState runState, final LaunchPanel launchPanel) {
		super(id);
		this.launchPanel = launchPanel;
		
		CompoundPropertyModel beanModel = new CompoundPropertyModel(bean);
				
		final ResourceReference[] references = null; // new ResourceReference[] { API };

		
		add(new SJAXCall("Commit", bean, 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				
				ScoBean scoBean = runState.produceScoBean(scoId);
				String result = scoBean.Commit(arg1);
				
				return result;
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});
		
			
		add(new SJAXCall("GetDiagnostic", bean, 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				String iErrorCode = arg1;
				ScoBean scoBean = runState.produceScoBean(scoId);
				return scoBean.GetDiagnostic(iErrorCode); 
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});


		add(new SJAXCall("GetErrorString", bean, 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				ScoBean scoBean = runState.produceScoBean(scoId);
				return scoBean.GetErrorString(arg1);
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});
	
		add(new SJAXCall("GetLastError", bean, 0) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				ScoBean scoBean = runState.produceScoBean(scoId);
				return scoBean.GetLastError();
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});
		
		add(new SJAXCall("GetValue", bean, 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				ScoBean scoBean = runState.produceScoBean(scoId);
				return scoBean.GetValue(arg1);
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});
		
		add(new SJAXCall("Initialize", bean, 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				String currentSco = runState.getCurrentSco();
				
				updatePageSco(currentSco, target);
				
				ScoBean scoBean = runState.produceScoBean(currentSco);
				
				launchPanel.getTreePanel().getActivityTree().selectNode();
				launchPanel.synchronizeState(runState, target);
				
				return scoBean.Initialize(arg1);
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});
		
		
		add(new SJAXCall("SetValue", bean, 2) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				ScoBean scoBean = runState.produceScoBean(scoId);
				return scoBean.SetValue(arg1, arg2);
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});
		
		
		add(new SJAXCall("Terminate", bean, 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				ScoBean scoBean = runState.produceScoBean(scoId);
				String result = scoBean.Terminate(arg1, target);
				
				launchPanel.getTreePanel().getActivityTree().selectNode();
				target.addComponent(launchPanel.getTreePanel().getActivityTree());
				
				runState.discardScoBean(scoId);
				
				updatePageSco("", target);
				//FormComponent scoComponent = (FormComponent)form.get(SCO_COMPONENT_ID);
				//scoComponent.clearInput();
				//target.addComponent(scoComponent);
				
				return result;
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});
		

	}
	
	public void updatePageSco(String scoId, AjaxRequestTarget target) {
		if (target != null)
			target.appendJavascript("sco = '" + scoId + "';");
	}

	protected String getFirstArg(List<String> argumentValues) {
		if (null == argumentValues || argumentValues.size() <= 0)
			return "";

		return argumentValues.get(0);
	}
	
	protected String getSecondArg(List<String> argumentValues) {
		if (null == argumentValues || argumentValues.size() <= 1)
			return "";
		
		return argumentValues.get(1);
	}
	
	
	
	public void renderHead(IHeaderResponse response) {
		StringBuffer js = new StringBuffer();
		
		/*js.append("function APIAdapter() { };")
			.append("function sjaxCall(scoId, url, arg1, arg2, successHandler, failureHandler, precondition, channel) {\n")
			.append("var call = new Wicket.Sjax.Call(url + '&scoId=' + scoId + '&arg1=' + arg1 + '&arg2=' + arg2 + '&callNumber=' + call_number, function() {}, function() {}, channel);\n")
			//.append("call.request.async = false;\n")
			//.append("call.request.suppressDone = true;")
			//.append("if (typeof(precondition) != \"undefined\" && precondition != null) {\n")
			//.append("call.request.precondition = precondition;\n")
			//.append("}\n")
			.append("var resultCode = call.call();\n")
			.append("var resultValue = api_result[call_number];\n")
			.append("call_number++;\n")
			.append("return resultValue;\n }\n")*/
		js.append("function APIAdapter() { };")	
			.append("var API_1484_11 = APIAdapter;\n")
			.append("var api_result = new Array();\n")
			.append("var call_number = 0;\n")
			.append("var sco = undefined;\n");
		
		response.renderJavascript(js.toString(), "apiAdapterJs");
	}
}
