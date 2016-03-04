package org.sakaiproject.gradebookng.tool.panels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.core.util.string.ComponentRenderer;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.ScoreChangedEvent;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;

/**
 * The panel for the cell of a grade item
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradeItemCellPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<Map<String, Object>> model;
	Map<String, Object> modelData;

	TextField<String> gradeCell;

	String rawGrade;
	String formattedGrade;
	String comment;
	boolean gradeable;
	boolean showMenu;

	GradeCellSaveStyle gradeSaveStyle;

	final List<GradeCellNotification> notifications = new ArrayList<GradeCellNotification>();

	public enum GradeCellNotification {
		IS_EXTERNAL("grade.notifications.isexternal"),
		OVER_LIMIT("grade.notifications.overlimit"),
		HAS_COMMENT("grade.notifications.hascomment"),
		CONCURRENT_EDIT("grade.notifications.concurrentedit"),
		ERROR("grade.notifications.haserror"),
		INVALID("grade.notifications.invalid"),
		READONLY("grade.notifications.readonly");

		private String message;

		GradeCellNotification(final String message) {
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}

	}

	public GradeItemCellPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		this.modelData = this.model.getObject();
		final Long assignmentId = (Long) this.modelData.get("assignmentId");
		final String assignmentName = (String) this.modelData.get("assignmentName");
		final Double assignmentPoints = (Double) this.modelData.get("assignmentPoints");
		final String studentUuid = (String) this.modelData.get("studentUuid");
		final String studentName = (String) this.modelData.get("studentName");
		final Long categoryId = (Long) this.modelData.get("categoryId");
		final boolean isExternal = (boolean) this.modelData.get("isExternal");
		final GbGradeInfo gradeInfo = (GbGradeInfo) this.modelData.get("gradeInfo");
		final GbRole role = (GbRole) this.modelData.get("role");

		// Note: gradeInfo may be null
		this.rawGrade = (gradeInfo != null) ? gradeInfo.getGrade() : "";
		this.comment = (gradeInfo != null) ? gradeInfo.getGradeComment() : "";
		this.gradeable = (gradeInfo != null) ? gradeInfo.isGradeable() : false; // ensure this is ALWAYS false if gradeInfo is null.

		if (role == GbRole.INSTRUCTOR) {
			this.gradeable = true;
		}

		// get grade
		this.formattedGrade = FormatHelper.formatGrade(this.rawGrade);

		// RENDER
		if (isExternal || !this.gradeable) {

			add(new Label("readonlyGrade", Model.of(this.formattedGrade)));
			add(new Label("editableGrade") {
				@Override
				public boolean isVisible() {
					return false;
				}
			});

			this.showMenu = false;

			if (isExternal) {
				getParentCellFor(this).add(new AttributeModifier("class", "gb-external-item-cell"));
				this.notifications.add(GradeCellNotification.IS_EXTERNAL);
			} else if (!this.gradeable) {
				getParentCellFor(this).add(new AttributeModifier("class", "gb-readonly-item-cell"));
				this.notifications.add(GradeCellNotification.READONLY);
			} else {
				getParentCellFor(this).add(new AttributeModifier("class", "gb-grade-item-cell"));
			}

		} else {
			add(new Label("readonlyGrade") {
				@Override
				public boolean isVisible() {
					return false;
				}
			});
			this.gradeCell = new TextField<String>("editableGrade", Model.of(this.formattedGrade)) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void onInitialize() {
					// set original grade, once only
					super.onInitialize();

					final Component parentCell = getParentCellFor(this);
					parentCell.add(new AttributeModifier("data-assignmentid", assignmentId));
					parentCell.add(new AttributeModifier("data-studentuuid", studentUuid));
					parentCell.add(new AttributeModifier("class", "gb-grade-item-cell"));
					parentCell.setOutputMarkupId(true);

					// check if grade is over limit and mark the cell with the warning class
					if (NumberUtils.toDouble(GradeItemCellPanel.this.formattedGrade) > assignmentPoints.doubleValue()) {
						markOverLimit(this);
						GradeItemCellPanel.this.notifications.add(GradeCellNotification.OVER_LIMIT);
					}

					// check if we have a comment and mark the cell with the comment icon
					if (StringUtils.isNotBlank(GradeItemCellPanel.this.comment)) {
						markHasComment(this);
					}

					GradeItemCellPanel.this.showMenu = true;
				}
			};

			this.gradeCell.add(new AjaxFormComponentUpdatingBehavior("scorechange.sakai") {
				private static final long serialVersionUID = 1L;
				private String originalGrade;

				@Override
				public void onBind() {
					super.onBind();
					this.originalGrade = GradeItemCellPanel.this.gradeCell.getDefaultModelObjectAsString();
				}

				@Override
				protected void onUpdate(final AjaxRequestTarget target) {
					final String rawGrade = GradeItemCellPanel.this.gradeCell.getValue();

					clearNotifications();

					// perform validation here so we can bypass the backend
					final DoubleValidator validator = new DoubleValidator();

					if (StringUtils.isNotBlank(rawGrade) && (!validator.isValid(rawGrade) || Double.parseDouble(rawGrade) < 0)) {
						markWarning(getComponent());
						GradeItemCellPanel.this.gradeCell.setDefaultModelObject(this.originalGrade);
					} else {
						final String newGrade = FormatHelper.formatGrade(rawGrade);

						// for concurrency, get the original grade we have in the UI and pass it into the service as a check
						final GradeSaveResponse result = GradeItemCellPanel.this.businessService.saveGrade(assignmentId, studentUuid,
								this.originalGrade, newGrade, GradeItemCellPanel.this.comment);

						// TODO here, add the message
						switch (result) {
							case OK:
								markSuccessful(GradeItemCellPanel.this.gradeCell);
								this.originalGrade = newGrade;
								refreshCourseGradeAndCategoryAverages(target);
								break;
							case ERROR:
								markError(getComponent());
								// TODO fix this message
								error("oh dear");
								break;
							case OVER_LIMIT:
								markOverLimit(GradeItemCellPanel.this.gradeCell);
								refreshCourseGradeAndCategoryAverages(target);
								this.originalGrade = newGrade;
								break;
							case NO_CHANGE:
								handleNoChange(GradeItemCellPanel.this.gradeCell);
								break;
							case CONCURRENT_EDIT:
								markError(GradeItemCellPanel.this.gradeCell);
								error(getString("error.concurrentedit"));
								GradeItemCellPanel.this.notifications.add(GradeCellNotification.CONCURRENT_EDIT);
								break;
							default:
								throw new UnsupportedOperationException("The response for saving the grade is unknown.");
						}
					}

					refreshNotifications();

					// refresh the components we need
					target.addChildren(getPage(), FeedbackPanel.class);
					target.add(getParentCellFor(getComponent()));
				}

				private void refreshCourseGradeAndCategoryAverages(final AjaxRequestTarget target) {
					// trigger async event that score has been updated and now displayed
					target.appendJavaScript(
							String.format("$('#%s').trigger('scoreupdated.sakai')", GradeItemCellPanel.this.gradeCell.getMarkupId()));
				}

				@Override
				protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);

					final Map<String, Object> extraParameters = attributes.getExtraParameters();
					extraParameters.put("assignmentId", assignmentId);
					extraParameters.put("studentUuid", studentUuid);

					final AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
						@Override
						public CharSequence getPrecondition(final Component component) {
							return "return GradebookWicketEventProxy.updateGradeItem.handlePrecondition('"
									+ getParentCellFor(component).getMarkupId() + "', attrs);";
						}

						@Override
						public CharSequence getBeforeSendHandler(final Component component) {
							return "GradebookWicketEventProxy.updateGradeItem.handleBeforeSend('"
									+ getParentCellFor(component).getMarkupId() + "', attrs, jqXHR, settings);";
						}

						@Override
						public CharSequence getSuccessHandler(final Component component) {
							return "GradebookWicketEventProxy.updateGradeItem.handleSuccess('" + getParentCellFor(component).getMarkupId()
									+ "', attrs, jqXHR, data, textStatus);";
						}

						@Override
						public CharSequence getFailureHandler(final Component component) {
							return "GradebookWicketEventProxy.updateGradeItem.handleFailure('" + getParentCellFor(component).getMarkupId()
									+ "', attrs, jqXHR, errorMessage, textStatus);";
						}

						@Override
						public CharSequence getCompleteHandler(final Component component) {
							return "GradebookWicketEventProxy.updateGradeItem.handleComplete('" + getParentCellFor(component).getMarkupId()
									+ "', attrs, jqXHR, textStatus);";
						}
					};
					attributes.getAjaxCallListeners().add(myAjaxCallListener);
				}
			});

			this.gradeCell.setType(String.class);
			this.gradeCell.add(new AjaxEventBehavior("scoreupdated.sakai") {
				@Override
				protected void onEvent(final AjaxRequestTarget target) {
					send(getPage(), Broadcast.BREADTH, new ScoreChangedEvent(studentUuid, categoryId, target));
					// ensure the fixed course grade column has been updated
					target.appendJavaScript(String.format("sakai.gradebookng.spreadsheet.refreshCourseGradeForStudent('%s')", studentUuid));
				}
			});


			this.gradeCell.add(new AjaxEventBehavior("viewlog.sakai") {
				@Override
				protected void onEvent(final AjaxRequestTarget target) {
					final GradebookPage gradebookPage = (GradebookPage) getPage();
					final GbModalWindow window = gradebookPage.getGradeLogWindow();

					window.setComponentToReturnFocusTo(getParentCellFor(GradeItemCellPanel.this.gradeCell));
					window.setContent(new GradeLogPanel(window.getContentId(), GradeItemCellPanel.this.model, window));
					window.show(target);
				}
			});
			this.gradeCell.add(new AjaxEventBehavior("editcomment.sakai") {
				@Override
				protected void onEvent(final AjaxRequestTarget target) {
					final GradebookPage gradebookPage = (GradebookPage) getPage();
					final GbModalWindow window = gradebookPage.getGradeCommentWindow();

					final EditGradeCommentPanel panel = new EditGradeCommentPanel(window.getContentId(), GradeItemCellPanel.this.model, window);
					window.setContent(panel);
					window.showUnloadConfirmation(false);
					window.clearWindowClosedCallbacks();
					window.setComponentToReturnFocusTo(getParentCellFor(GradeItemCellPanel.this.gradeCell));
					window.addWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
						private static final long serialVersionUID = 1L;

						@Override
						public void onClose(final AjaxRequestTarget target) {
							GradeItemCellPanel.this.comment = panel.getComment();

							if (StringUtils.isNotBlank(GradeItemCellPanel.this.comment)) {
								markHasComment(GradeItemCellPanel.this.gradeCell);
							}

							target.add(getParentCellFor(GradeItemCellPanel.this.gradeCell));
							target.appendJavaScript("sakai.gradebookng.spreadsheet.setupCell('"
									+ getParentCellFor(GradeItemCellPanel.this.gradeCell).getMarkupId() + "','" + assignmentId + "', '"
									+ studentUuid + "');");
							refreshNotifications();
						}
					});
					window.show(target);
				}
			});

			this.gradeCell.setOutputMarkupId(true);
			add(this.gradeCell);
		}

		// always add these
		getParent().add(new AttributeModifier("role", "gridcell"));
		getParent().add(new AttributeModifier("aria-readonly", Boolean.toString(isExternal || !this.gradeable)));

		refreshNotifications();
	}

	/**
	 * Set the enum value so we can use it when we style. TODO collapse these into one
	 *
	 */
	private void markSuccessful(final Component gradeCell) {
		this.gradeSaveStyle = GradeCellSaveStyle.SUCCESS;
		styleGradeCell(gradeCell);
	}

	private void markError(final Component gradeCell) {
		this.gradeSaveStyle = GradeCellSaveStyle.ERROR;
		styleGradeCell(gradeCell);
		this.notifications.add(GradeCellNotification.ERROR);
	}

	private void markWarning(final Component gradeCell) {
		this.gradeSaveStyle = GradeCellSaveStyle.WARNING;
		styleGradeCell(gradeCell);
		this.notifications.add(GradeCellNotification.INVALID);
	}

	private void markOverLimit(final Component gradeCell) {
		this.gradeSaveStyle = GradeCellSaveStyle.OVER_LIMIT;
		styleGradeCell(gradeCell);
		this.notifications.add(GradeCellNotification.OVER_LIMIT);
	}

	private void markHasComment(final Component gradeCell) {
		styleGradeCell(gradeCell); // maintains existing save style
		this.notifications.add(GradeCellNotification.HAS_COMMENT);
	}

	private void handleNoChange(final Component gradeCell) {
		// clear any previous styles
		this.gradeSaveStyle = null;
		styleGradeCell(gradeCell);
	}

	private void clearNotifications() {
		this.notifications.clear();
		if (StringUtils.isNotBlank(this.comment)) {
			markHasComment(this.gradeCell);
		}
	}

	/**
	 * Builder for styling the cell. Aware of the cell 'save style' as well as if it has comments and adds styles accordingly
	 *
	 * @param gradeCell the cell to style
	 */
	private void styleGradeCell(final Component gradeCell) {

		final ArrayList<String> cssClasses = new ArrayList<>();
		cssClasses.add("gb-grade-item-cell"); // always
		if (this.gradeSaveStyle != null) {
			cssClasses.add(this.gradeSaveStyle.getCss()); // the particular style for this cell that has been computed previously
		}

		// replace the cell styles with the new set
		getParentCellFor(gradeCell).add(AttributeModifier.replace("class", StringUtils.join(cssClasses, " ")));
	}

	/**
	 * Get the parent container for the grade cell so we can style it
	 *
	 * @param gradeCell
	 * @return
	 */
	private Component getParentCellFor(final Component component) {
		if (StringUtils.equals(component.getMarkupAttributes().getString("wicket:id"), "cells")) {
			return component;
		} else {
			return getParentCellFor(component.getParent());
		}
	}

	/**
	 * Enum for encapsulating the grade cell save css class that is to be applied
	 *
	 */
	enum GradeCellSaveStyle {

		SUCCESS("grade-save-success"),
		ERROR("grade-save-error"),
		WARNING("grade-save-warning"),
		OVER_LIMIT("grade-save-over-limit");

		private String css;

		GradeCellSaveStyle(final String css) {
			this.css = css;
		}

		public String getCss() {
			return this.css;
		}
	}

	private void refreshNotifications() {
		final WebMarkupContainer commentNotification = new WebMarkupContainer("commentNotification");
		final WebMarkupContainer warningNotification = new WebMarkupContainer("warningNotification");
		final WebMarkupContainer errorNotification = new WebMarkupContainer("errorNotification");
		final WebMarkupContainer overLimitNotification = new WebMarkupContainer("overLimitNotification");

		warningNotification.setVisible(false);
		errorNotification.setVisible(false);
		overLimitNotification.setVisible(false);

		if (StringUtils.isNotBlank(this.comment)) {
			this.modelData.put("comment", this.comment);
			commentNotification.setVisible(true);
			addPopover(commentNotification, Arrays.asList(GradeCellNotification.HAS_COMMENT));
		} else {
			commentNotification.setVisible(false);
		}
		this.notifications.remove(GradeCellNotification.HAS_COMMENT);

		if (!this.notifications.isEmpty()) {
			if (this.notifications.contains(GradeCellNotification.ERROR)) {
				errorNotification.setVisible(true);
				addPopover(errorNotification, this.notifications);
			} else if (this.notifications.contains(GradeCellNotification.INVALID)
					|| this.notifications.contains(GradeCellNotification.CONCURRENT_EDIT)
					|| this.notifications.contains(GradeCellNotification.IS_EXTERNAL)
					|| this.notifications.contains(GradeCellNotification.READONLY)) {
				warningNotification.setVisible(true);
				addPopover(warningNotification, this.notifications);
			} else if (this.notifications.contains(GradeCellNotification.OVER_LIMIT)) {
				overLimitNotification.setVisible(true);
				addPopover(overLimitNotification, this.notifications);
			}
		}

		addOrReplace(commentNotification);
		addOrReplace(warningNotification);
		addOrReplace(errorNotification);
		addOrReplace(overLimitNotification);
	}

	private void addPopover(final Component component, final List<GradeCellNotification> notifications) {
		this.modelData.put("gradeable", this.gradeable);
		final GradeItemCellPopoverPanel popover = new GradeItemCellPopoverPanel("popover", Model.ofMap(this.modelData), notifications);
		final String popoverString = ComponentRenderer.renderComponent(popover).toString();

		component.add(new AttributeModifier("data-toggle", "popover"));
		component.add(new AttributeModifier("data-trigger", "manual"));
		component.add(new AttributeModifier("data-placement", "bottom"));
		component.add(new AttributeModifier("data-html", "true"));
		component.add(new AttributeModifier("data-container", "#gradebookGrades"));
		component.add(new AttributeModifier("data-content", popoverString));
		component.add(new AttributeModifier("tabindex", "0"));
	}
}