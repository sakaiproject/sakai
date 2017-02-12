package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.gradebookng.business.GbGradingType;
import org.sakaiproject.gradebookng.tool.model.GbSettings;

public class SettingsGradeEntryPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

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

		// points/percentage entry
		final RadioGroup<Integer> gradeEntry = new RadioGroup<>("gradeEntry",
				new PropertyModel<Integer>(this.model, "gradebookInformation.gradeType"));
		gradeEntry.add(new Radio<>("points", Model.of(GbGradingType.POINTS.getValue())));
		gradeEntry.add(new Radio<>("percentages", Model.of(GbGradingType.PERCENTAGE.getValue())));
		settingsGradeEntryPanel.add(gradeEntry);

	}

	public boolean isExpanded() {
		return this.expanded;
	}
}
