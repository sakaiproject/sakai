package org.sakaiproject.gradebookng.tool.model;

import java.util.List;
import java.util.ArrayList;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

/**
 * A custom ModalWindow that adds behaviours specific to our tool
 */
public class GbModalWindow extends ModalWindow {

	private Component componentToReturnFocusTo;
	private List<WindowClosedCallback> closeCallbacks;

	public GbModalWindow(String id) {
		super(id);

		closeCallbacks = new ArrayList<>();
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		setMaskType(MaskType.TRANSPARENT);
		setResizable(false);
		setUseInitialHeight(false);

		setDefaultWindowClosedCallback();

		setWindowClosedCallback(new WindowClosedCallback() {
			@Override
			public void onClose(AjaxRequestTarget target) {
				closeCallbacks.forEach((callback) -> {
					callback.onClose(target);
				});
			}
		});
	}

	@Override
	protected CharSequence getShowJavaScript() {
		// focus the first input field in the content pane
		String focusJavascript = String.format("setTimeout(function() {$('#%s :input:first:visible').focus();});", this.getContent().getMarkupId());

		return super.getShowJavaScript().toString() + focusJavascript;
	}

	@Override
	public ModalWindow setContent(Component component) {
		component.setOutputMarkupId(true);

		return super.setContent(component);
	}


	/**
	 * Set the component to return focus to upon closing the window
	 * @param component
	 */
	public void setComponentToReturnFocusTo(Component component) {
		componentToReturnFocusTo = component;
	}

	public void addWindowClosedCallback(WindowClosedCallback callback) {
		closeCallbacks.add(callback);
	}

	public void clearWindowClosedCallbacks() {
		closeCallbacks = new ArrayList<>();
		setDefaultWindowClosedCallback();
	}

	private void setDefaultWindowClosedCallback() {
		addWindowClosedCallback(new WindowClosedCallback() {
			@Override
			public void onClose(final AjaxRequestTarget target) {
				// Ensure the date picker is hidden
				target.appendJavaScript("$('#ui-datepicker-div').hide();");

				// Ensure any mask is hidden
				target.appendJavaScript("GradebookGradeSummaryUtils.clearBlur();");

				// Return focus to defined component 
				if (componentToReturnFocusTo != null) {
					target.appendJavaScript(String.format("setTimeout(function() {$('#%s').focus();});", componentToReturnFocusTo.getMarkupId()));
				}
			}
		});
	}
}