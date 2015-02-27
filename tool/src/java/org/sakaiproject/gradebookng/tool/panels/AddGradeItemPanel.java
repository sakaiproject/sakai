package org.sakaiproject.gradebookng.tool.panels;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * The panel for the add grade item window
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AddGradeItemPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public AddGradeItemPanel(String id) {
		super(id);

        Assignment assignment = new Assignment();
        Form<?> form = new Form("form", new CompoundPropertyModel<Assignment>(assignment));

		add(new AddGradeItemPanelContent("subComponents"));


	}


}
