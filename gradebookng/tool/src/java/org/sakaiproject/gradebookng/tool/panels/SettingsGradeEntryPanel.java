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
package org.sakaiproject.gradebookng.tool.panels;

import lombok.Getter;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingConstants;

public class SettingsGradeEntryPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	IModel<GbSettings> model;
	@Getter
    private boolean expanded;

	public SettingsGradeEntryPanel(final String id, final IModel<GbSettings> model, final boolean expanded) {
		super(id, model);
		this.model = model;
		this.expanded = expanded;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final WebMarkupContainer settingsGradeEntryAccordionButton = new WebMarkupContainer("settingsGradeEntryAccordionButton");
		final WebMarkupContainer settingsGradeEntryPanel = new WebMarkupContainer("settingsGradeEntryPanel");
		
		// Set up accordion behavior
		setupAccordionBehavior(settingsGradeEntryAccordionButton, settingsGradeEntryPanel, this.expanded, 
			new AccordionStateUpdater() {
				@Override
				public void updateState(boolean newState) {
					SettingsGradeEntryPanel.this.expanded = newState;
				}
				
				@Override
				public boolean getState() {
					return SettingsGradeEntryPanel.this.expanded;
				}
			});
		
		add(settingsGradeEntryPanel);
		add(settingsGradeEntryAccordionButton);

		// grade entry type
		RadioGroup<GradeType> gradeEntry = new RadioGroup<>("gradeEntry",
                new PropertyModel<>(this.model, "gradebookInformation.gradeType"));

		gradeEntry.add(new Radio<GradeType>("points", Model.of(GradeType.POINTS)));
		gradeEntry.add(new Radio<GradeType>("percentages", Model.of(GradeType.PERCENTAGE)));
		gradeEntry.add(new Radio<GradeType>("letter", Model.of(GradeType.LETTER)));
		settingsGradeEntryPanel.add(gradeEntry);
	}

}
