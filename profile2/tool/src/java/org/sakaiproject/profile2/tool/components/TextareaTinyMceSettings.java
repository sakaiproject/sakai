/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.tool.components;

import java.util.ArrayList;
import java.util.List;

import wicket.contrib.tinymce.settings.Button;
import wicket.contrib.tinymce.settings.TinyMCESettings;

/**
 * A configuration class for the TinyMCE Wicket component, used by textareas.
 * If more are required for different purposes, create a new class.
 */
public class TextareaTinyMceSettings extends TinyMCESettings {

	private static final long serialVersionUID = 1L;
	
	public TextareaTinyMceSettings(TinyMCESettings.Align toolbarAlign) {
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

		setToolbarAlign(toolbarAlign);
		setToolbarLocation(TinyMCESettings.Location.top);
		setStatusbarLocation(null);
		setResizing(true);
		setHorizontalResizing(true);
		
		//remove leading and trailing p tags, PRFL-387
		addCustomSetting("forced_root_block : false");		
	}
	
	public TextareaTinyMceSettings () {
		this(TinyMCESettings.Align.center);
	}
	
}
