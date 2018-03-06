/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	private String assignmentIdToReturnFocusTo;
	private String studentUuidToReturnFocusTo;
	private boolean returnFocusToCourseGrade = false;
	private List<WindowClosedCallback> closeCallbacks;
	private boolean positionAtTop = false;

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
		StringBuilder extraJavascript = new StringBuilder();

		// focus the first input field in the content pane
		extraJavascript.append(String.format("setTimeout(function() {$('#%s :input:first:visible').focus();});",
				getContent().getMarkupId()));

		// position at the top of the page
		if (this.positionAtTop) {
			extraJavascript.append(
					String.format("setTimeout(function() {GbGradeTable.positionModalAtTop($('#%s').closest('.wicket-modal'));});",
							getContent().getMarkupId()));
		}

		return super.getShowJavaScript().toString() + extraJavascript.toString();
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

	/**
	 * Set the student to return focus to upon closing the window.
	 *
	 * @param component
	 */
	public void setStudentToReturnFocusTo(final String studentUuid) {
		this.studentUuidToReturnFocusTo = studentUuid;
	}

	/**
	 * Set the assignment to return focus to upon closing the window.
	 *
	 * @param component
	 */
	public void setAssignmentToReturnFocusTo(final String assignmentId) {
		this.assignmentIdToReturnFocusTo = assignmentId;
	}

	public void setReturnFocusToCourseGrade() {
		this.returnFocusToCourseGrade = true;
	}

	public void addWindowClosedCallback(final WindowClosedCallback callback) {
		this.closeCallbacks.add(callback);
	}

	public void clearWindowClosedCallbacks() {
		this.closeCallbacks = new ArrayList<>();
		setDefaultWindowClosedCallback();
	}

	public void setPositionAtTop(final boolean positionAtTop) {
		this.positionAtTop = positionAtTop;
	}

	private void setDefaultWindowClosedCallback() {
		addWindowClosedCallback(new WindowClosedCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClose(final AjaxRequestTarget target) {
				// Disable all buttons with in the modal in case it takes a moment to close
				target.appendJavaScript(
						String.format("$('#%s :input').prop('disabled', true);",
								GbModalWindow.this.getContent().getMarkupId()));

				// Ensure the date picker is hidden
				target.appendJavaScript("$('#ui-datepicker-div').hide();");

				// Ensure any mask is hidden
				target.appendJavaScript("GradebookGradeSummaryUtils.clearBlur();");

				// Return focus to defined component
				if (GbModalWindow.this.componentToReturnFocusTo != null) {
					target.appendJavaScript(String.format("setTimeout(function() {$('#%s').focus();});",
							GbModalWindow.this.componentToReturnFocusTo.getMarkupId()));
				} else if (GbModalWindow.this.assignmentIdToReturnFocusTo != null &&
						GbModalWindow.this.studentUuidToReturnFocusTo != null) {
					target.appendJavaScript(String.format("setTimeout(function() {GbGradeTable.selectCell('%s', '%s');});",
							GbModalWindow.this.assignmentIdToReturnFocusTo,
							GbModalWindow.this.studentUuidToReturnFocusTo));
				} else if (GbModalWindow.this.assignmentIdToReturnFocusTo != null) {
					target.appendJavaScript(String.format("setTimeout(function() {GbGradeTable.selectCell('%s', null);});",
							GbModalWindow.this.assignmentIdToReturnFocusTo));
				} else if (GbModalWindow.this.studentUuidToReturnFocusTo != null) {
					if (GbModalWindow.this.returnFocusToCourseGrade) {
						target.appendJavaScript(String.format("setTimeout(function() {GbGradeTable.selectCourseGradeCell('%s');});",
								GbModalWindow.this.studentUuidToReturnFocusTo));
					} else {
						target.appendJavaScript(String.format("setTimeout(function() {GbGradeTable.selectCell(null, '%s');});",
								GbModalWindow.this.studentUuidToReturnFocusTo));
					}
				} else if (GbModalWindow.this.returnFocusToCourseGrade) {
					target.appendJavaScript("setTimeout(function() {GbGradeTable.selectCourseGradeCell();});");
				}
			}
		});
	}
}