/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.wicket.markup.html.fckeditor;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.collections.MiniMap;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.apache.wicket.util.template.TextTemplate;

/**
 * A wicket component which renders an FCKeditor textarea
 * 
 * @author Adrian Fish
 */
public class FCKEditorPanel extends Panel
{
	private transient Logger logger = Logger.getLogger(FCKEditorPanel.class);

	private boolean stripContainingParagraphTags = false;

	public static final String BASIC = "Basic";
	public static final String DEFAULT = "Default";

	private static final String ESCAPED_OPENING_P = "&lt;p&gt;";
	private static final String ESCAPED_CLOSING_P = "&lt;/p&gt;";

	/**
	 * 
	 * @param id The wicket:id
	 * @param model The data model
	 * @param width The width of the rendered textarea
	 * @param height The height of the rendered text area
	 * @param toolbarSet The set of toolbars you want rendering, either basic of default
	 * @param collectionId The Sakai collection id for the FCKeditor to use
	 * @param strip Set to true if you want the component to strip off the surrounding paragraph tags that FCKeditor produces
	 */
	public FCKEditorPanel(String id,IModel model,String width,String height,String toolbarSet, String collectionId,boolean stripContainingParagraphTags)
	{
		this(id,model,width,height,toolbarSet,collectionId);
		this.stripContainingParagraphTags = stripContainingParagraphTags;
	}

	/**
	 * 
	 * @param id The wicket:id
	 * @param model The data model
	 * @param width The width of the rendered textarea
	 * @param height The height of the rendered text area
	 * @param toolbarSet The set of toolbars you want rendering, either basic of default
	 * @param collectionId The Sakai collection id for the FCKeditor to use
	 */
	public FCKEditorPanel(String id,IModel model,String width,String height,String toolbarSet, String collectionId)
	{
		super(id);

		TextArea textArea = new TextArea("editor",model)
		{
			@Override
			protected void onModelChanged()
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("onModelChanged()");
				}

				if(stripContainingParagraphTags)
				{
					String value = getModelValue();

					if(logger.isDebugEnabled())
					{
						logger.debug("Value Before:" + value);
					}

					if(value.length() >= (ESCAPED_OPENING_P.length() + ESCAPED_CLOSING_P.length()))
					{
						if(value.startsWith(ESCAPED_OPENING_P))
						{
							value = value.substring(ESCAPED_OPENING_P.length());
						}

						if(value.endsWith(ESCAPED_CLOSING_P))
						{
							value = value.substring(0,value.length() - ESCAPED_CLOSING_P.length());
						}
					}

					if(logger.isDebugEnabled())
					{
						logger.debug("Value After:" + value);
					}

					getModel().setObject(value);

					super.onModelChanged();
				}
			}
		};

		String textareaId = id + ((int)(Math.random() * 100));
		textArea.setMarkupId(textareaId);
		textArea.setOutputMarkupId(true);
		add(textArea);
		TextTemplate scriptTemplate = new PackageTextTemplate(FCKEditorPanel.class,"FCKEditorScript.js");
		Map map = new MiniMap(5);
		map.put("width", width);
		map.put("height", height);
		map.put("toolbarSet", toolbarSet);
		map.put("textareaId", textareaId);
		map.put("collectionId", collectionId);

		String script = scriptTemplate.asString(map);
		Label scriptLabel = new Label("script",script);
		scriptLabel.setEscapeModelStrings(false);
		add(scriptLabel);
	}
}
