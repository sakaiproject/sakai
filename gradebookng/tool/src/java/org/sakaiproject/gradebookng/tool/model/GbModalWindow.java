package org.sakaiproject.gradebookng.tool.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

/**
 * A custom ModalWindow that adds behaviours specific to our tool
 */
public class GbModalWindow extends ModalWindow {

	private static final long serialVersionUID = 1L;

	private Component componentToReturnFocusTo;
	private List<WindowClosedCallback> closeCallbacks;

	public GbModalWindow(final String id) {
		super(id);

		this.closeCallbacks = new ArrayList<>();
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		setMaskType(MaskType.TRANSPARENT);
		setResizable(false);
		setUseInitialHeight(false);

		setDefaultWindowClosedCallback();

		setWindowClosedCallback(new WindowClosedCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClose(final AjaxRequestTarget target) {
				GbModalWindow.this.closeCallbacks.forEach((callback) -> {
					callback.onClose(target);
				});
			}
		});
	}

	@Override
	protected CharSequence getShowJavaScript() {
		// focus the first input field in the content pane
		final String focusJavascript = String.format("setTimeout(function() {$('#%s :input:first:visible').focus();});",
				getContent().getMarkupId());

		return super.getShowJavaScript().toString() + focusJavascript;
	}

	@Override
	public ModalWindow setContent(final Component component) {
		component.setOutputMarkupId(true);

		return super.setContent(component);
	}

	/**
	 * Set the component to return focus to upon closing the window. The component MUST have it's output markup id set, by calling
	 * setOutputMarkupId(true).
	 *
	 * @param component
	 */
	public void setComponentToReturnFocusTo(final Component component) {
		this.componentToReturnFocusTo = component;
	}

	public void addWindowClosedCallback(final WindowClosedCallback callback) {
		this.closeCallbacks.add(callback);
	}

	public void clearWindowClosedCallbacks() {
		this.closeCallbacks = new ArrayList<>();
		setDefaultWindowClosedCallback();
	}

	private void setDefaultWindowClosedCallback() {
		addWindowClosedCallback(new WindowClosedCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClose(final AjaxRequestTarget target) {
				// Ensure the date picker is hidden
				target.appendJavaScript("$('#ui-datepicker-div').hide();");

				// Ensure any mask is hidden
				target.appendJavaScript("GradebookGradeSummaryUtils.clearBlur();");

				// Return focus to defined component
				if (GbModalWindow.this.componentToReturnFocusTo != null) {
					target.appendJavaScript(String.format("setTimeout(function() {$('#%s').focus();});",
							GbModalWindow.this.componentToReturnFocusTo.getMarkupId()));
				}
			}
		});
	}
}