package org.sakaiproject.scorm.ui.tool.components;

import java.util.List;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.sakaiproject.scorm.ui.tool.RunState;
import org.sakaiproject.scorm.ui.tool.ScoBean;
import org.sakaiproject.scorm.ui.tool.behaviors.SjaxCall;

public class CommunicationPanel extends Panel implements IHeaderContributor {
	public static final ResourceReference API = new CompressedResourceReference(
			CommunicationPanel.class, "res/API.js");

	public static final ResourceReference API_WRAPPER = new CompressedResourceReference(
			CommunicationPanel.class, "res/APIWrapper.js");

	
	private static final long serialVersionUID = 1L;

	private LaunchPanel launchPanel;
	
	public CommunicationPanel(String id, final RunState runState, final LaunchPanel launchPanel) {
		super(id);
		this.launchPanel = launchPanel;

		add(new SjaxCall("Commit", 1) {
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
		
			
		add(new SjaxCall("GetDiagnostic", 1) {
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


		add(new SjaxCall("GetErrorString", 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				ScoBean scoBean = runState.produceScoBean(scoId);
				return scoBean.GetErrorString(arg1);
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});
	
		add(new SjaxCall("GetLastError", 0) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				ScoBean scoBean = runState.produceScoBean(scoId);
				return scoBean.GetLastError();
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});
		
		add(new SjaxCall("GetValue", 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				ScoBean scoBean = runState.produceScoBean(scoId);
				return scoBean.GetValue(arg1);
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});
		
		SjaxCall initCall = new SjaxCall("Initialize", 1) {
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
		};
		
		
		initCall.prependJavascript("tb_remove();");
		
		add(initCall);
		
		
		add(new SjaxCall("SetValue", 2) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				ScoBean scoBean = runState.produceScoBean(scoId);
				return scoBean.SetValue(arg1, arg2);
			}
			
			protected String getChannelName() {
				return "1|s";
			}
		});
		
		
		add(new SjaxCall("Terminate", 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(String scoId, String arg1, String arg2, AjaxRequestTarget target) {
				ScoBean scoBean = runState.produceScoBean(scoId);
				String result = scoBean.Terminate(arg1, target);
				
				launchPanel.getTreePanel().getActivityTree().selectNode();
				
				launchPanel.getTreePanel().getActivityTree().updateTree(target);
				
				runState.discardScoBean(scoId);
				
				updatePageSco("", target);
				
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
		
		js.append("function APIAdapter() { };")	
			.append("var API_1484_11 = APIAdapter;\n")
			.append("var api_result = new Array();\n")
			.append("var call_number = 0;\n")
			.append("var sco = undefined;\n");
		
		response.renderJavascript(js.toString(), "apiAdapterJs");
	}
}
