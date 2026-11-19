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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.portal.util.PortalUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * A custom ModalDialog (Wicket 10's replacement for the removed ModalWindow) that adds
 * behaviours specific to our tool: a title bar, a close button, focus trapping/restoration,
 * and a WindowClosedCallback mechanism fired on every close path (explicit close or escape key).
 * Deliberately does not close on backdrop click, matching the original ModalWindow's behavior.
 */
@Slf4j
public class GbModalWindow extends ModalDialog {

	private static final long serialVersionUID = 1L;

	public interface WindowClosedCallback extends Serializable {
		void onClose(AjaxRequestTarget target);
	}

	private Component componentToReturnFocusTo;
	private String assignmentIdToReturnFocusTo;
	private String studentUuidToReturnFocusTo;
	private boolean returnFocusToCourseGrade = false;
	private List<WindowClosedCallback> closeCallbacks;
	private Component initialFocusComponent;
	private Component content;

	private IModel<String> title = Model.of("");
	private String cssClassName;
	private int initialWidth = -1;
	private int initialHeight = -1;
	private String widthUnit = "px";

	public GbModalWindow(final String id) {
		super(id);

		this.closeCallbacks = new ArrayList<>();
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forUrl("/gradebookng-tool/scripts/gradebook-modal.js" + PortalUtils.getCDNQuery()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		closeOnEscape();
		trapFocus();

		final WebMarkupContainer dialogContainer = (WebMarkupContainer) get("overlay:dialog");
		dialogContainer.setOutputMarkupId(true);

		final Label titleLabel = new Label("title", new IModel<String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getObject() {
				return GbModalWindow.this.title == null ? null : GbModalWindow.this.title.getObject();
			}
		});
		titleLabel.setOutputMarkupId(true);
		dialogContainer.add(titleLabel);
		dialogContainer.add(AttributeModifier.replace("aria-labelledby", titleLabel.getMarkupId()));

		dialogContainer.add(new AjaxLink<Void>("close") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				GbModalWindow.this.close(target);
			}
		});

		dialogContainer.add(AttributeModifier.replace("style", (IModel<String>) this::buildDialogStyle));
		dialogContainer.add(AttributeModifier.append("class", (IModel<String>) () -> this.cssClassName == null ? "" : this.cssClassName));

		setDefaultWindowClosedCallback();
	}

	private String buildDialogStyle() {
		final StringBuilder style = new StringBuilder();
		if (this.initialWidth > 0) {
			style.append("width:").append(this.initialWidth).append(this.widthUnit).append(";");
		}
		if (this.initialHeight > 0) {
			style.append("height:").append(this.initialHeight).append("px;");
		}
		return style.toString();
	}

	@Override
	public GbModalWindow open(final AjaxRequestTarget target) {
		super.open(target);

		if (getContent() != null) {
			if (this.initialFocusComponent != null && this.initialFocusComponent.getOutputMarkupId()) {
				target.appendJavaScript(String.format(
						"setTimeout(function() { try { $('#%s').focus(); } catch(e) { console.error('Failed to focus initial component:', e); } }, 500);",
						this.initialFocusComponent.getMarkupId()));
			} else {
				target.appendJavaScript(String.format(
						"setTimeout(function() { try { "
						+ "$('#%s').find('input, select, textarea, button, a[href]').not(':disabled').filter(':visible').first().focus(); "
						+ "} catch(e) { console.error('Failed to focus first input in modal content:', e); } }, 500);",
						getContent().getMarkupId()));
			}
		}

		return this;
	}

	public void show(final AjaxRequestTarget target) {
		open(target);
	}

	public boolean isShown() {
		return isOpen();
	}

	public GbModalWindow showUnloadConfirmation(final boolean show) {
		// no-op: ModalDialog has no built-in unload confirmation support
		return this;
	}

	public GbModalWindow setTitle(final String title) {
		this.title = Model.of(title);
		return this;
	}

	public GbModalWindow setTitle(final IModel<String> title) {
		this.title = title;
		return this;
	}

	public GbModalWindow setCssClassName(final String cssClassName) {
		this.cssClassName = cssClassName;
		return this;
	}

	public GbModalWindow setInitialWidth(final int initialWidth) {
		this.initialWidth = initialWidth;
		return this;
	}

	public GbModalWindow setInitialHeight(final int initialHeight) {
		this.initialHeight = initialHeight;
		return this;
	}

	public GbModalWindow setWidthUnit(final String widthUnit) {
		this.widthUnit = widthUnit;
		return this;
	}

	public GbModalWindow setResizable(final boolean resizable) {
		// no-op: ModalDialog is never resizable
		return this;
	}

	public GbModalWindow setUseInitialHeight(final boolean useInitialHeight) {
		// no-op: sizing is CSS-driven in ModalDialog
		return this;
	}

	public String getContentId() {
		return CONTENT_ID;
	}

	protected Component getContent() {
		return this.content;
	}

	@Override
	public void setContent(final Component component) {
		component.setOutputMarkupId(true);

		this.content = component;

		super.setContent(component);
	}

	@Override
	public GbModalWindow close(final AjaxRequestTarget target) {
		super.close(target);

		this.closeCallbacks.forEach(callback -> callback.onClose(target));

		return this;
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
				if (GbModalWindow.this.getContent() != null) {
					target.appendJavaScript(
							String.format("try { $('#%s :input').prop('disabled', true); } catch(e) { console.error('Failed to disable inputs on close:', e); }",
									GbModalWindow.this.getContent().getMarkupId()));
				}

				target.appendJavaScript("try { $('#ui-datepicker-div').hide(); } catch(e) { console.error('Failed to hide datepicker:', e); }");

				// Check if GradebookGradeSummaryUtils and clearBlur exist before calling
				target.appendJavaScript(
					"if (typeof GradebookGradeSummaryUtils !== 'undefined' && GradebookGradeSummaryUtils.clearBlur) { " +
					"  try { GradebookGradeSummaryUtils.clearBlur(); } catch(e) { console.error('Failed to clear blur:', e); } " +
					"} else { console.debug('GradebookGradeSummaryUtils or clearBlur function not found.'); }"
				);

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
				}
			}
		});
	}
}
