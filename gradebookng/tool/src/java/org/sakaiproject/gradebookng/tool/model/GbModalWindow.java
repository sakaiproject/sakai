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
import lombok.extern.slf4j.Slf4j;

/**
 * A custom ModalWindow that adds behaviours specific to our tool
 */
@Slf4j
public class GbModalWindow extends ModalWindow {

	private static final long serialVersionUID = 1L;

	private Component componentToReturnFocusTo;
	private String assignmentIdToReturnFocusTo;
	private String studentUuidToReturnFocusTo;
	private boolean returnFocusToCourseGrade = false;
	private List<WindowClosedCallback> closeCallbacks;
	private Component initialFocusComponent;

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
		if (getContent() == null) {
			log.warn("ModalWindow content is null, cannot generate show JavaScript reliably.");
			return super.getShowJavaScript();
		}
		getContent().setOutputMarkupId(true);

		StringBuilder js = new StringBuilder(super.getShowJavaScript().toString());

		js.append(String.format("$('#%s').attr('tabindex', '-1');", getContent().getMarkupId()));

		js.append("setTimeout(function() {");
		if (this.initialFocusComponent != null && this.initialFocusComponent.getOutputMarkupId()) {
			js.append(String.format("try { $('#%s').focus(); } catch(e) { console.error('Failed to focus initial component:', e); }",
					this.initialFocusComponent.getMarkupId()));
		} else {
			js.append(String.format("try { $('#%s').focus(); } catch(e) { console.error('Failed to focus modal content:', e); }",
					getContent().getMarkupId()));
		}
		js.append("}, 500);");

		js.append("function GbModalWindow_trapFocus(event, modalContentId) {");
		js.append("  if (!modalContentId || event.key !== 'Tab' && event.keyCode !== 9) return;");
		js.append("  const modalContent = document.getElementById(modalContentId);");
		js.append("  if (!modalContent) return;");
		js.append("  const modalWindowEl = modalContent.closest('.wicket-modal');");
		js.append("  if (!modalWindowEl) return;");

		js.append("  const focusableElements = modalWindowEl.querySelectorAll(");
		js.append("    'a[href]:not([disabled]), button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex=\"-1\"])'");
		js.append("  );");
		js.append("  if (focusableElements.length === 0) return;");
		js.append("  const firstFocusableElement = focusableElements[0];");
		js.append("  const lastFocusableElement = focusableElements[focusableElements.length - 1];");

		js.append("  if (event.shiftKey) {");
		js.append("    if (document.activeElement === firstFocusableElement) {");
		js.append("      lastFocusableElement.focus();");
		js.append("      event.preventDefault();");
		js.append("    }");
		js.append("  } else {");
		js.append("    if (document.activeElement === lastFocusableElement) {");
		js.append("      firstFocusableElement.focus();");
		js.append("      event.preventDefault();");
		js.append("    }");
		js.append("  }");
		js.append("}");

		// Attach the event listener to the document, namespaced per modal instance
		this.setOutputMarkupId(true); // Ensure modal window itself has an ID for namespacing
		js.append(String.format(
			"$(document).on('keydown.gbTrapFocus_%s', function(e) { " +
			"  if (e.key !== 'Tab' && e.keyCode !== 9) return; " + // Early exit
			"  const modalContent = document.getElementById('%s'); " +
			"  if (!modalContent) { $(document).off('keydown.gbTrapFocus_%s'); return; } " + // Cleanup if content disappears
			"  const modalWindow = $(modalContent).closest('.wicket-modal'); " +
			"  if (!modalWindow || modalWindow.is(':hidden')) return; " + // Check if *this* modal is visible
			"  if (modalContent.contains(document.activeElement)) { " + // Check if focus is currently inside
			"    GbModalWindow_trapFocus(e, '%s'); " +
			"  } " +
			"});",
			this.getMarkupId(),      // Namespace for document listener
			getContent().getMarkupId(),
			this.getMarkupId(),      // Namespace for cleanup inside listener
			getContent().getMarkupId()
		));

		return js;
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

	/**
	 * Get the assignment to return focus to upon closing the window.
	 */
	public String getAssignmentToReturnFocusTo() {
		return this.assignmentIdToReturnFocusTo;
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

	/**
	 * Set the component to focus when the modal is first opened.
	 * The component MUST have its output markup ID set via setOutputMarkupId(true).
	 * If null or the component doesn't have an output markup id, the modal content panel will be focused.
	 *
	 * @param component The component to focus initially, or null to focus the content panel.
	 */
	public void setInitialFocusComponent(final Component component) {
		this.initialFocusComponent = component;
		if (this.initialFocusComponent != null) {
			this.initialFocusComponent.setOutputMarkupId(true);
		}
	}

	private void setDefaultWindowClosedCallback() {
		addWindowClosedCallback(new WindowClosedCallback() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClose(final AjaxRequestTarget target) {
				target.appendJavaScript(
						String.format("try { $('#%s :input').prop('disabled', true); } catch(e) { console.error('Failed to disable inputs on close:', e); }",
								GbModalWindow.this.getContent().getMarkupId()));

				target.appendJavaScript("try { $('#ui-datepicker-div').hide(); } catch(e) { console.error('Failed to hide datepicker:', e); }");

				// Check if GradebookGradeSummaryUtils and clearBlur exist before calling
				target.appendJavaScript(
					"if (typeof GradebookGradeSummaryUtils !== 'undefined' && GradebookGradeSummaryUtils.clearBlur) { " +
					"  try { GradebookGradeSummaryUtils.clearBlur(); } catch(e) { console.error('Failed to clear blur:', e); } " +
					"} else { console.debug('GradebookGradeSummaryUtils or clearBlur function not found.'); }"
				);

				// Remove the focus trap listener from the document using the correct namespace
				target.appendJavaScript(String.format("try { $(document).off('keydown.gbTrapFocus_%s'); } catch(e) { console.error('Failed to remove focus trap listener:', e); }", GbModalWindow.this.getMarkupId()));

				String focusScript = "setTimeout(function() { try { ";
				boolean focusSet = false;

				if (GbModalWindow.this.componentToReturnFocusTo != null) {
					focusScript += String.format("$('#%s').focus();",
							GbModalWindow.this.componentToReturnFocusTo.getMarkupId());
					focusSet = true;
				} else if (GbModalWindow.this.assignmentIdToReturnFocusTo != null &&
						GbModalWindow.this.studentUuidToReturnFocusTo != null) {
					focusScript += String.format("GbGradeTable.selectCell('%s', '%s');",
							GbModalWindow.this.assignmentIdToReturnFocusTo,
							GbModalWindow.this.studentUuidToReturnFocusTo);
					focusSet = true;
				} else if (GbModalWindow.this.assignmentIdToReturnFocusTo != null) {
					focusScript += String.format("GbGradeTable.selectCell('%s', null);",
							GbModalWindow.this.assignmentIdToReturnFocusTo);
					focusSet = true;
				} else if (GbModalWindow.this.studentUuidToReturnFocusTo != null) {
					if (GbModalWindow.this.returnFocusToCourseGrade) {
						focusScript += String.format("GbGradeTable.selectCourseGradeCell('%s');",
								GbModalWindow.this.studentUuidToReturnFocusTo);
					} else {
						focusScript += String.format("GbGradeTable.selectCell(null, '%s');",
								GbModalWindow.this.studentUuidToReturnFocusTo);
					}
					focusSet = true;
				} else if (GbModalWindow.this.returnFocusToCourseGrade) {
					focusScript += "GbGradeTable.selectCourseGradeCell();";
					focusSet = true;
				}

				if (focusSet) {
					focusScript += " } catch(e) { console.error('Error returning focus:', e); } }, 50);";
					target.appendJavaScript(focusScript);
				} else {
					// Fallback focus if nothing specific is set? Maybe focus body or a known static element?
					// For now, do nothing if no specific return focus is set.
				}
			}
		});
	}
}
