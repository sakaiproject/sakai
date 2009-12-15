package org.sakaiproject.profile2.tool.components;

import java.util.ArrayList;
import java.util.List;

import wicket.contrib.tinymce.settings.Button;
import wicket.contrib.tinymce.settings.TinyMCESettings;

public class TextareaTinyMceSettings extends TinyMCESettings {

	private static final long serialVersionUID = 1L;
	
	public TextareaTinyMceSettings () {
		super(TinyMCESettings.Theme.advanced);
		
		/*
		add(Button.bullist, TinyMCESettings.Toolbar.first, TinyMCESettings.Position.after);
		add(Button.numlist, TinyMCESettings.Toolbar.first, TinyMCESettings.Position.after);

		disableButton(Button.styleselect);
		disableButton(Button.sub);
		disableButton(Button.sup);
		disableButton(Button.charmap);
		disableButton(Button.image);
		disableButton(Button.anchor);
		disableButton(Button.help);
		disableButton(Button.code);
		disableButton(Button.link);
		disableButton(Button.unlink);
		disableButton(Button.formatselect);
		disableButton(Button.indent);
		disableButton(Button.outdent);
		disableButton(Button.undo);
		disableButton(Button.redo);
		disableButton(Button.cleanup);
		disableButton(Button.hr);
		disableButton(Button.visualaid);
		disableButton(Button.separator);
		disableButton(Button.formatselect);
		disableButton(Button.removeformat);
		*/
		
		List<Button> firstRowButtons = new ArrayList<Button>();
		firstRowButtons.add(Button.bold);
		firstRowButtons.add(Button.italic);
		firstRowButtons.add(Button.underline);
		firstRowButtons.add(Button.strikethrough);
		firstRowButtons.add(Button.separator);
		firstRowButtons.add(Button.sub);
		firstRowButtons.add(Button.sup);
		firstRowButtons.add(Button.separator);
		firstRowButtons.add(Button.link);
		firstRowButtons.add(Button.unlink);
		firstRowButtons.add(Button.separator);
		firstRowButtons.add(Button.bullist);
		firstRowButtons.add(Button.numlist);
		firstRowButtons.add(Button.separator);
		firstRowButtons.add(Button.code);

		//set first toolbar
		setToolbarButtons(TinyMCESettings.Toolbar.first, firstRowButtons);

		//remove the second and third toolbars
		setToolbarButtons(TinyMCESettings.Toolbar.second, new ArrayList<Button>());
		setToolbarButtons(TinyMCESettings.Toolbar.third, new ArrayList<Button>());
		setToolbarButtons(TinyMCESettings.Toolbar.fourth, new ArrayList<Button>());

		setToolbarAlign(TinyMCESettings.Align.center);
		setToolbarLocation(TinyMCESettings.Location.top);
		setStatusbarLocation(null);
		setResizing(true);
		setHorizontalResizing(true);

	}
	
}
