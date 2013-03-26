package org.sakaiproject.profile2.tool.components;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Tania Tritean, ISDC!
 *
 */
public class CKEditorConfig {

	/**
	 * Toolbar config
	 */
	public static final String CKEDITOR_TOOLBAR_STANDARD = "[['Bold', 'Italic','Underline','Strike', '-','Subscript','Superscript', '-', 'BulletedList','NumberedList', '-','Link','Unlink', '-', 'Source']]";
	
	//multi configs are not working, the last one on a page takes precendence.
	//public static final String CKEDITOR_TOOLBAR_MINIMAL = "[['Bold', 'Italic','Underline']]";
	//public static final String CKEDITOR_TOOLBAR_NONE = "[[]]";

	private static final String TOOLBAR_DEFAULT = CKEDITOR_TOOLBAR_STANDARD;
	/**
	 * Create the CKEditor config. Uses default toolbar layout CKEDITOR_TOOLBAR_STANDARD.
	 * @return the config map
	 */
	public static Map<String, String> createCkConfig() {
		return createCkConfig(TOOLBAR_DEFAULT);
	}
	
	/**
	 * Create the CKEditor config by specifying the toolbar to use
	 * To make things simple, use CKEDITOR_TOOLBAR_STANDARD or CKEDITOR_TOOLBAR_STANDARD
	 * Defaults to CKEDITOR_TOOLBAR_STANDARD
	 * 
	 * @param toolbar
	 * @return the config map
	 */
	public static Map<String, String> createCkConfig(String toolbar) {
		
		if(StringUtils.isBlank(toolbar)) {
			toolbar = TOOLBAR_DEFAULT;
		}
		
		final Map<String, String> result = new HashMap<String, String>();
		result.put(CKEditorTextArea.CONFIG_TOOLBAR, TOOLBAR_DEFAULT);
		result.put(CKEditorTextArea.CONFIG_HEIGHT, "'300px'");
		result.put(CKEditorTextArea.CONFIG_WIDTH, "'100%'");
		result.put(CKEditorTextArea.HTML_ENCODE_OUTPUT, "false");
		result.put(CKEditorTextArea.ENTITIES, "false");
		// result.put(CKEditorTextArea.BASIC_ENTITIES,"false");
		result.put(CKEditorTextArea.REMOVE_PLUGINS, CKEditorTextArea.ELEMENTS_PATH);
		result.put(CKEditorTextArea.RESIZE_ENABLED, "false");
		result.put(CKEditorTextArea.BLOCKED_KEYSTROKES, CKEditorTextArea.BLOCKED_KEYSTROKES_VALUES);
		result.put(CKEditorTextArea.ACTIVE_KEYSTROKES, CKEditorTextArea.ACTIVE_KEYSTROKES_VALUES);
		result.put(CKEditorTextArea.SHIFT_ENTER_MODE, CKEditorTextArea.CKEDITOR_ENTER_BR);
		result.put(CKEditorTextArea.ENTER_MODE, CKEditorTextArea.CKEDITOR_ENTER_BR);
		return result;
	}
}
