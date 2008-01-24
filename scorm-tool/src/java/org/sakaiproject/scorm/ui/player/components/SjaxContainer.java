package org.sakaiproject.scorm.ui.player.components;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.model.Model;
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

public class SjaxContainer extends WebMarkupContainer implements IHeaderContributor {

	private static final long serialVersionUID = 1L;

	private static final ResourceReference SJAX = new JavascriptResourceReference(SjaxCall.class, "res/scorm-sjax.js");
	
	@SpringBean
	transient LearningManagementSystem lms;
	
	@SpringBean
	transient ScormApplicationService applicationService;
	@SpringBean
	transient ScormResourceService resourceService;
	@SpringBean
	transient ScormSequencingService sequencingService;
	
	private SessionBean sessionBean;
	private UISynchronizerPanel synchronizerPanel;
	private SjaxCall[] calls = new SjaxCall[8]; 
	private HiddenField[] components = new HiddenField[8];
	
	public SjaxContainer(String id, final SessionBean sessionBean, final UISynchronizerPanel synchronizerPanel) {
		
		super(id);
		
		this.sessionBean = sessionBean;
		this.synchronizerPanel = synchronizerPanel;
		
		this.setOutputMarkupId(true);
		this.setMarkupId("sjaxContainer");
		
		
		calls[0] = new ScormSjaxCall("Commit", 1);
		
		calls[1] = new ScormSjaxCall("GetDiagnostic", 1);
	
		calls[2] = new ScormSjaxCall("GetErrorString", 1);
	
		calls[3] = new ScormSjaxCall("GetLastError", 0);
		
		calls[4] = new ScormSjaxCall("GetValue", 1);

		calls[5] = new ScormSjaxCall("Initialize", 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(ScoBean scoBean, AjaxRequestTarget target, Object... args) {
				
				
				ActivityTree tree = synchronizerPanel.getTree();
				if (tree != null && !tree.isEmpty()) {
					tree.selectNode();
					tree.updateTree(target);
				}
				
				synchronizerPanel.synchronizeState(sessionBean, target);

				updatePageSco(scoBean.getScoId(), target);
				
				return super.callMethod(scoBean, target, args);
			}
		};

		calls[6] = new ScormSjaxCall("SetValue", 2);

		calls[7] = new ScormSjaxCall("Terminate", 1) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(ScoBean scoBean, AjaxRequestTarget target, Object... args) {
				String result = super.callMethod(scoBean, target, args);
						
				ActivityTree tree = synchronizerPanel.getTree();
				if (tree != null && !tree.isEmpty()) {
					tree.selectNode();
					tree.updateTree(target);
				}
				
				applicationService.discardScoBean(scoBean.getScoId(), sessionBean, new LocalResourceNavigator());
				
				updatePageSco("undefined", target);
				
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

	public void onBeforeRender() {
		super.onBeforeRender();
		
		for (int i=0;i<8;i++) {
			components[i].setModel(new Model(calls[i].getCallUrl().toString()));
		}
	}
	
	
	private HiddenField addSjaxComponent(String callname, SjaxCall call, Form form) {
		HiddenField cc = new HiddenField(callname); 
		form.add(cc);
		cc.setMarkupId(callname);
		cc.add(call);
		
		return cc;
	}
	
	
	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference(SJAX);
		
		StringBuffer js = new StringBuffer();
		
		js.append("function APIAdapter() { };\n")	
			.append("var API_1484_11 = APIAdapter;\n")
			.append("var api_result = new Array();\n")
			.append("var call_number = 0;\n")
			.append("var sco = undefined;\n");
		
		for (int i=0;i<calls.length;i++) {
			js.append(calls[i].getJavascriptCode()).append("\n");
		}
		
		response.renderJavascript(js.toString(), "SCORM_API");
	}
	
	public class ScormSjaxCall extends SjaxCall {
		
		private static final long serialVersionUID = 1L;
		
		public ScormSjaxCall(String event, int numArgs) {
			super(event, numArgs);
		}
		
		@Override
		protected SessionBean getSessionBean() {
			return sessionBean;
		}
		
		@Override
		protected LearningManagementSystem lms() {
			return lms;
		}
		
		@Override
		protected ScormApplicationService applicationService() {
			return applicationService;
		}

		@Override
		protected ScormResourceService resourceService() {
			return resourceService;
		}
		
		@Override
		protected ScormSequencingService sequencingService() {
			return sequencingService;
		}
		
		@Override
		protected String getChannelName() {
			return "1|s";
		}
		
		public void updatePageSco(String scoId, AjaxRequestTarget target) {
			if (target != null)
				target.appendJavascript("sco = '" + scoId + "';");
		}

		@Override
		protected INavigable getNavigationAgent() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	
	public class LocalResourceNavigator extends ResourceNavigator {

		private static final long serialVersionUID = 1L;

		@Override
		public Object getApplication() {
			return SjaxContainer.this.getApplication();
		}
		
		@Override
		protected ScormResourceService resourceService() {
			return SjaxContainer.this.resourceService;
		}
		
		public Component getFrameComponent() {
			if (synchronizerPanel != null && synchronizerPanel.getContentPanel() != null) 
				return synchronizerPanel.getContentPanel();
			return null;
		}
		
	}
}
