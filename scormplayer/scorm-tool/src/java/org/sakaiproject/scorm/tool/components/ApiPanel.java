package org.sakaiproject.scorm.tool.components;

import java.util.List;

import org.adl.api.ecmascript.APIErrorCodes;
import org.adl.api.ecmascript.IErrorManager;
import org.adl.datamodels.DMErrorCodes;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.CompoundPropertyModel;
import org.sakaiproject.scorm.client.utils.ApiAjaxBean;
import org.sakaiproject.scorm.client.utils.ApiAjaxMethod;
import org.sakaiproject.scorm.tool.ScormTool;

public class ApiPanel extends Panel {
	public static final ResourceReference API = new CompressedResourceReference(
			ApiPanel.class, "API.js");

	public static final ResourceReference API_WRAPPER = new CompressedResourceReference(
			ApiPanel.class, "APIWrapper.js");

	private static final long serialVersionUID = 1L;

	private ApiAjaxBean bean = new ApiAjaxBean();

	public ApiPanel(String id) {
		super(id);

		// add form with markup id setter so it can be updated via ajax
		final Form form = new Form("form", new CompoundPropertyModel(bean));
		add(form);
		form.setOutputMarkupId(true);

		// add form components to the form as usual
		// FormComponent value = new TextField("value");
		// value.setOutputMarkupId(true);
		// form.add(value);

		FormComponent arg1 = new HiddenField("arg1");
		arg1.setOutputMarkupId(true);
		form.add(arg1);

		FormComponent arg2 = new HiddenField("arg2");
		arg2.setOutputMarkupId(true);
		form.add(arg2);

		FormComponent resultComponent = new HiddenField("result");
		resultComponent.setOutputMarkupId(true);
		form.add(resultComponent);

		final ResourceReference[] references = new ResourceReference[] { API };

		form.add(new ApiAjaxMethod(form, "GetDiagnostic", references, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(List<String> argumentValues) {
				IErrorManager errorManager = ((ScormTool) getApplication())
						.getErrorManager();
				String arg = getFirstArg(argumentValues);

				return errorManager.getErrorDiagnostic(arg);
			}
		});

		form.add(new ApiAjaxMethod(form, "GetErrorString", null, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(List<String> argumentValues) {
				IErrorManager errorManager = ((ScormTool) getApplication()).getErrorManager();
				String arg = getFirstArg(argumentValues);

				return errorManager.getErrorDescription(arg);
			}
		});

		form.add(new ApiAjaxMethod(form, "GetLastError", null, 0, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(List<String> argumentValues) {
				IErrorManager errorManager = ((ScormTool) getApplication()).getErrorManager();
				String arg = getFirstArg(argumentValues);

				return errorManager.getCurrentErrorCode();
			}
		});

		form.add(new ApiAjaxMethod(form, "GetValue", null, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(List<String> argumentValues) {
				IErrorManager errorManager = ((ScormTool) getApplication()).getErrorManager();
				String arg = getFirstArg(argumentValues);
				String result = "";

				// already terminated
				/*if (mTerminatedState) {
					errorManager.setCurrentErrorCode(APIErrorCodes.GET_AFTER_TERMINATE);
					return result;
				}
				if (arg.length() == 0) {
					errorManager.setCurrentErrorCode(DMErrorCodes.GEN_GET_FAILURE);
					return result;
				}

				if (isInitialized()) {

					// Clear current error codes
					errorManager.clearCurrentErrorCode();

					// Process 'GET'
					DMProcessingInfo dmInfo = new DMProcessingInfo();
					int dmErrorCode = 0;
					dmErrorCode = DMInterface.processGetValue(
							iDataModelElement, false, mSCOData, dmInfo);

					// Set the LMS Error Manager from the Data Model Error
					// Manager
					errorManager.setCurrentErrorCode(dmErrorCode);

					if (dmErrorCode == APIErrorCodes.NO_ERROR) {
						result = dmInfo.mValue;
					} else {
						result = new String("");
					}
				}
				// not initialized
				else {
					errorManager
							.setCurrentErrorCode(APIErrorCodes.GET_BEFORE_INIT);
				}*/

				return result;
			}
		});
		
		
		form.add(new ApiAjaxMethod(form, "Initialize", null, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(List<String> argumentValues) {


				return "";
			}
		});
		
		
		form.add(new ApiAjaxMethod(form, "SetValue", null, 2, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(List<String> argumentValues) {


				return "";
			}
		});
		
		
		form.add(new ApiAjaxMethod(form, "Terminate", null, 1, bean) {
			private static final long serialVersionUID = 1L;

			protected String callMethod(List<String> argumentValues) {


				return "";
			}
		});
		

	}

	private String getFirstArg(List<String> argumentValues) {
		if (null == argumentValues || argumentValues.size() <= 0)
			return "";

		return argumentValues.get(0);
	}
}
