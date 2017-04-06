package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 *
 * Cell panel for the student extra info
 *
 */
public class StudentExtraInfoCellPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	IModel<Map<String, Object>> model;

	public StudentExtraInfoCellPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Map<String, Object> modelData = this.model.getObject();
		final String extraStudentProperty = (String) modelData.get("extraStudentProperty");
		add(new Label("extraInfo", extraStudentProperty));
	}

}
