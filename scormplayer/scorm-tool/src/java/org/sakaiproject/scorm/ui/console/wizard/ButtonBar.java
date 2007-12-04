package org.sakaiproject.scorm.ui.console.wizard;

import org.apache.wicket.extensions.wizard.CancelButton;
import org.apache.wicket.extensions.wizard.FinishButton;
import org.apache.wicket.extensions.wizard.LastButton;
import org.apache.wicket.extensions.wizard.PreviousButton;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.panel.Panel;

public class ButtonBar extends Panel {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 * 
	 * @param id
	 *            The component id
	 * @param wizard
	 *            The containing wizard
	 */
	public ButtonBar(String id, Wizard wizard)
	{
		super(id);
		add(new PreviousButton("previous", wizard));
		add(new NextActionButton("next", wizard));
		add(new LastButton("last", wizard));
		add(new CancelButton("cancel", wizard));
		FinishButton finishButton = new FinishButton("finish", wizard);
		finishButton.setVisible(false);
		add(finishButton);
	}
}
