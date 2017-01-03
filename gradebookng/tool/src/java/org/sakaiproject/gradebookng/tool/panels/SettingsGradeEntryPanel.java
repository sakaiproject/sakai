package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.service.gradebook.shared.GradingType;

public class SettingsGradeEntryPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<GbSettings> model;
	private boolean expanded;

	public SettingsGradeEntryPanel(final String id, final IModel<GbSettings> model, final boolean expanded) {
		super(id, model);
		this.model = model;
		this.expanded = expanded;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final WebMarkupContainer settingsGradeEntryPanel = new WebMarkupContainer("settingsGradeEntryPanel");
		// Preserve the expand/collapse state of the panel
		settingsGradeEntryPanel.add(new AjaxEventBehavior("shown.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradeEntryPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
				SettingsGradeEntryPanel.this.expanded = true;
			}
		});
		settingsGradeEntryPanel.add(new AjaxEventBehavior("hidden.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradeEntryPanel.add(new AttributeModifier("class", "panel-collapse collapse"));
				SettingsGradeEntryPanel.this.expanded = false;
			}
		});
		if (this.expanded) {
			settingsGradeEntryPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
		}
		add(settingsGradeEntryPanel);

		// grade entry type
		final RadioGroup<Integer> gradeEntry = new RadioGroup<>("gradeEntry",
				new PropertyModel<Integer>(this.model, "gradebookInformation.gradeType"));

		gradeEntry.add(new Radio<>("points", Model.of(GradingType.POINTS.getValue())));
		gradeEntry.add(new Radio<>("percentages", Model.of(GradingType.PERCENTAGE.getValue())));
		settingsGradeEntryPanel.add(gradeEntry);

		// final grade mode (if enabled)
		final AjaxCheckBox finalGradeMode = new AjaxCheckBox("finalgrade", new PropertyModel<Boolean>(this.model, "gradebookInformation.finalGradeMode")) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return SettingsGradeEntryPanel.this.businessService.isFinalGradeModeEnabled();
			}

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// nothing required
			}
		};
		finalGradeMode.setOutputMarkupId(true);
		settingsGradeEntryPanel.add(finalGradeMode);
	}

	public boolean isExpanded() {
		return this.expanded;
	}
}
