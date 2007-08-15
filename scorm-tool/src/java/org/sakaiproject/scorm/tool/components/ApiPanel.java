package org.sakaiproject.scorm.tool.components;

import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.sakaiproject.scorm.client.utils.ApiAjaxBean;
import org.sakaiproject.scorm.client.utils.ApiAjaxMethod;
import org.sakaiproject.scorm.tool.ScoBean;
import org.sakaiproject.scorm.tool.RunState;

public class ApiPanel extends Panel implements IHeaderContributor {
	public static final ResourceReference API = new CompressedResourceReference(
			ApiPanel.class, "API.js");

	public static final ResourceReference API_WRAPPER = new CompressedResourceReference(
			ApiPanel.class, "APIWrapper.js");

	
	private static final long serialVersionUID = 1L;

	private ApiAjaxBean bean = new ApiAjaxBean();
	private LaunchPanel launchPanel;
	private Form form;
	
	public ApiPanel(String id, final RunState runState, final LaunchPanel launchPanel) {
		super(id);
		this.launchPanel = launchPanel;
		
		CompoundPropertyModel beanModel = new CompoundPropertyModel(bean);
		
		// add form with markup id setter so it can be updated via ajax
		form = new Form("form", beanModel);
		add(form);
		form.setOutputMarkupId(true);

		FormComponent arg1 = new HiddenField(ApiAjaxMethod.ARG_COMPONENT_ID + "1");
		arg1.setOutputMarkupId(true);
		form.add(arg1);

		FormComponent arg2 = new HiddenField(ApiAjaxMethod.ARG_COMPONENT_ID + "2");
		arg2.setOutputMarkupId(true);
		form.add(arg2);

		FormComponent resultComponent = new HiddenField(ApiAjaxMethod.RESULT_COMPONENT_ID);
		resultComponent.setOutputMarkupId(true);
		form.add(resultComponent);
		
		FormComponent scoComponent = new HiddenField(ApiAjaxMethod.SCO_COMPONENT_ID, beanModel.bind("scoId"));
		scoComponent.setOutputMarkupId(true);
		scoComponent.setModelValue(new String[] { runState.getCurrentSco() } );
		form.add(scoComponent);
		
		final ResourceReference[] references = null; // new ResourceReference[] { API };

		form.add(new ApiAjaxMethod(form, "Commit", null, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, List<String> argumentValues, AjaxRequestTarget target) {
				String arg = getFirstArg(argumentValues);
				
				ScoBean clientBean = runState.produceScoBean(scoId);
				String result = clientBean.Commit(arg);
				
				return result;
			}
		});
		
		form.add(new ApiAjaxMethod(form, "GetDiagnostic", references, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, List<String> argumentValues, AjaxRequestTarget target) {
				String iErrorCode = getFirstArg(argumentValues);
				ScoBean clientBean = runState.produceScoBean(scoId);
				return clientBean.GetDiagnostic(iErrorCode); 
			}
		});

		form.add(new ApiAjaxMethod(form, "GetErrorString", null, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, List<String> argumentValues, AjaxRequestTarget target) {
				String arg = getFirstArg(argumentValues);
				ScoBean clientBean = runState.produceScoBean(scoId);
				return clientBean.GetErrorString(arg);
			}
		});

		form.add(new ApiAjaxMethod(form, "GetLastError", null, 0, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, List<String> argumentValues, AjaxRequestTarget target) {
				ScoBean clientBean = runState.produceScoBean(scoId);
				return clientBean.GetLastError();
			}
		});

		form.add(new ApiAjaxMethod(form, "GetValue", null, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, List<String> argumentValues, AjaxRequestTarget target) {
				String arg = getFirstArg(argumentValues);
				
				ScoBean clientBean = runState.produceScoBean(scoId);
				return clientBean.GetValue(arg);
			}
		});
		
		
		form.add(new ApiAjaxMethod(form, "Initialize", null, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, List<String> argumentValues, AjaxRequestTarget target) {
				String arg = getFirstArg(argumentValues);
				String currentSco = runState.getCurrentSco();
				
				updatePageSco(currentSco, target);
				
				ScoBean clientBean = runState.produceScoBean(currentSco);
				
				launchPanel.getTreePanel().getActivityTree().selectNode();
				launchPanel.synchronizeState(runState, target);
				
				return clientBean.Initialize(arg);
			}
		});
		
		
		form.add(new ApiAjaxMethod(form, "SetValue", null, 2, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, List<String> argumentValues, AjaxRequestTarget target) {
				String arg1 = getFirstArg(argumentValues);
				String arg2 = getSecondArg(argumentValues);
				
				ScoBean clientBean = runState.produceScoBean(scoId);
				return clientBean.SetValue(arg1, arg2);
			}
		});
		
		
		form.add(new ApiAjaxMethod(form, "Terminate", null, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, List<String> argumentValues, AjaxRequestTarget target) {
				String arg = getFirstArg(argumentValues);
				
				ScoBean clientBean = runState.produceScoBean(scoId);
				String result = clientBean.Terminate(arg, target);
				
				launchPanel.getTreePanel().getActivityTree().selectNode();
				target.addComponent(launchPanel.getTreePanel().getActivityTree());
				
				runState.discardScoBean(scoId);
				FormComponent scoComponent = (FormComponent)form.get(SCO_COMPONENT_ID);
				scoComponent.clearInput();
				
				return result;
			}
		});
		

	}
	
	public void updatePageSco(String scoId, AjaxRequestTarget target) {
		FormComponent scoComponent = (FormComponent)form.get(ApiAjaxMethod.SCO_COMPONENT_ID);
		scoComponent.setModelValue(new String[] { scoId } );
		
		if (target != null)
			target.addComponent(scoComponent);
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
		
		js.append("function APIAdapter() { };")
			.append("function apiCall(formId, url, successHandler, failureHandler, channel) {")
			.append("var call = new Wicket.Ajax.Call(url, function() {}, function() {}, channel);")
			.append("call.request.async = false;")
			.append("return call.submitFormById(formId, null); }\n")
			.append("var API_1484_11 = APIAdapter;");
		
		response.renderJavascript(js.toString(), "apiAdapterJs");
	}
}
