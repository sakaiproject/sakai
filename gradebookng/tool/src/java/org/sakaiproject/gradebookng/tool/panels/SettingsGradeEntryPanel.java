package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
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

public class SettingsGradeEntryPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<GbSettings> model;

	public SettingsGradeEntryPanel(final String id, final IModel<GbSettings> model) {
		super(id, model);
		this.model = model;
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
			}
		});
		settingsGradeEntryPanel.add(new AjaxEventBehavior("hidden.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradeEntryPanel.add(new AttributeModifier("class", "panel-collapse collapse"));
			}
		});
		add(settingsGradeEntryPanel);

		// points/percentage entry
		final RadioGroup<Integer> gradeEntry = new RadioGroup<>("gradeEntry",
				new PropertyModel<Integer>(this.model, "gradebookInformation.gradeType"));
		gradeEntry.add(new Radio<>("points", new Model<>(1)));
		gradeEntry.add(new Radio<>("percentages", new Model<>(2)));
		settingsGradeEntryPanel.add(gradeEntry);

	}
}
