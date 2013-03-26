/*
 * Original Copyright Leidse Onderwijsinstellingen. All Rights Reserved.
 */

package org.sakaiproject.profile2.tool.components;

import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.calldecorator.AjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Extends a wicket TextArea to create a CKEditor instance. Each instance can be individually configured by using the
 * <b>setEditorConfig</b> and the <b>setCallbackFunction</b>. The advantage is that you can have multiple CKEditors in
 * the page each with a custom configuration. Uses the editor from sakai library. NOTE: In order to have this component
 * working on IE, the <b>WicketApplication</b> must set the javascript compressor to null. ex:
 * this.getResourceSettings().setJavascriptCompressor(null);</br></br><b>Aditional Notes:</b> The recommended jQuery version
 * is <b>1.7.2</b>, if you have an older version we recommend upgrading to a more recent jQuery version.
 * 
 * @author Bogdan Mariesan, ISDC!
 */
public class CKEditorTextArea extends TextArea<String> {

	/**
	 * The default serial version UID.
	 */
	private static final long								serialVersionUID				= 1L;

	/**
	 * Separator for property.
	 */
	private static final String								PROPERTY_SEPARATOR				= ":";

	/**
	 * The CKEDITOR js library.
	 */
	private static final String								CKEDITOR_JS						= "resources/ckeditor.js";

	/**
	 * The CKEDITOR jquery Adapter library.
	 */
	private static final String								CKEDITOR_JQUERY_ADAPTER			= "resources/adapters/jquery.js";

	/**
	 * Decorator to update all.
	 */
	// CHECKSTYLE:OFF
	public static final String								DECORATOR_FUNCTION_UPDATE_ALL	= "$.each(CKEDITOR.instances, function(index, value) {"
																									+ "if ($('#'+index).length > 0) {"
																									+ "value.updateElement(); value.postAjaxWicket();"
																									+ "" + "}" + "});";

	// CHECKSTYLE:ON

	/**
	 * The CKEDITOR config.
	 */
	private String											editorConfig					= "{}";

	/**
	 * The callback function for the CKEDITOR.
	 */
	private String											callbackFunction				= "function() {}";

	/**
	 * If the ck instance should be removed when destroying wicket component. Normally should always be true, but we saw
	 * that calling a js in onRemove function causes some loading problems in wicket. We saw that this is working if the
	 * editor is used in repeaters but not if the editor is used in a normal way. That is why default is false. TODO:
	 * Fix this to be able to set it to true by default.
	 */
	private final boolean									shouldCkInstanceRemoveInstance;

	/**
	 * Behavior.
	 */
	private final CustomAjaxFormComponentUpdatingBehavior	behavior;

	/**
	 * Right curly bracket string.
	 */
	private static final String								RIGHT_CURLY_BRACKET_STRING		= "}";

	/**
	 * New line string.
	 */
	private static final String								NEW_LINE_STRING					= "\n";

	/**
	 * Left curly bracket string.
	 */
	private static final String								LEFT_CURLY_BRACKET_STRING		= "{";

	/**
	 * Comma string.
	 */
	private static final String								COMMA_STRING					= ",";

	/**
	 * CKEDITOR height - key.
	 */
	public static final String								CONFIG_HEIGHT					= "'height'";

	/**
	 * CKEDITOR width - key.
	 */
	public static final String								CONFIG_WIDTH					= "'width'";

	/**
	 * CKEDITOR toolbar - key.
	 */
	public static final String								CONFIG_TOOLBAR					= "'toolbar'";

	/**
	 * CKEDITOR full toolbar - key.
	 */
	public static final String								TOOLBAR_FULL					= "'toolbar_Full'";

	/**
	 * CKEDITOR align - key.
	 */
	public static final String								CONFIG_ALIGN					= "'align'";

	/**
	 * CKEDITOR htmlEncodeOutput - key.
	 */
	public static final String								HTML_ENCODE_OUTPUT				= "'htmlEncodeOutput'";

	/**
	 * CKEDITOR entities - key.
	 */
	public static final String								ENTITIES						= "'entities'";

	/**
	 * CKEDITOR basic entities - key.
	 */
	public static final String								BASIC_ENTITIES					= "'basicEntities'";

	/**
	 * CKEDITOR skin - key.
	 */
	public static final String								SKIN							= "'skin'";

	/**
	 * CKEDITOR skin - key.
	 */
	public static final String								SKIN_V2							= "'v2'";

	/**
	 * CKEDITOR extraPlugins - key.
	 */
	public static final String								EXTRA_PLUGINS					= "'extraPlugins'";

	/**
	 * CKEDITOR removePlugins - key.
	 */
	public static final String								REMOVE_PLUGINS					= "'removePlugins'";

	/**
	 * CKEDITOR elementspath plugin - key.
	 */
	public static final String								ELEMENTS_PATH					= "'elementspath'";

	/**
	 * CKEDITOR resize_enabled - key.
	 */
	public static final String								RESIZE_ENABLED					= "'resize_enabled'";

	/**
	 * CKEDITOR removeFormatTags - key.
	 */
	public static final String								REMOVE_FORMAT_TAGS				= "'removeFormatTags'";

	/**
	 * CKEDITOR shiftEnterMode - key.
	 */
	public static final String								SHIFT_ENTER_MODE				= "'shiftEnterMode'";

	/**
	 * CKEDITOR enterMode - key.
	 */
	public static final String								ENTER_MODE						= "'enterMode'";

	/**
	 * CKEDITOR CKEDITOR_ENTER_BR - key.
	 */
	public static final String								CKEDITOR_ENTER_BR				= "CKEDITOR.ENTER_BR";

	/**
	 * Used to configure language.
	 */
	public static final String								LANGUAGE_CONFIG					= "language";

	/**
	 * Used to configure lockedKeystrokes.
	 */
	public static final String								BLOCKED_KEYSTROKES				= "blockedKeystrokes";

	/**
	 * Used to configure lockedKeystrokes.
	 */
	public static final String								BLOCKED_KEYSTROKES_VALUES		= "[CKEDITOR.CTRL + 66 /*B*/, CKEDITOR.CTRL + 73 /*I*/, CKEDITOR.CTRL + 85 /*U*/ ]";

	/**
	 * Used to configure active keystrokes. A blocked keystroke overridden with an active one will be active.
	 */
	public static final String								ACTIVE_KEYSTROKES				= "keystrokes";

	/**
	 * Used to configure active keystrokes. A blocked keystroke overridden with an active one will be active.
	 */
	public static final String								ACTIVE_KEYSTROKES_VALUES		= "[[ CKEDITOR.ALT + 121 /*F10*/, 'toolbarFocus' ],"
																									+ "[ CKEDITOR.ALT + 122 /*F11*/, 'elementsPathFocus' ],"
																									+ "[ CKEDITOR.SHIFT + 121 /*F10*/, 'contextMenu' ],"
																									+ "[ CKEDITOR.CTRL + 90 /*Z*/, 'undo' ],"
																									+ "[ CKEDITOR.CTRL + 89 /*Y*/, 'redo' ],"
																									+ "[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 90 /*Z*/, 'redo' ],"
																									+ "[ CKEDITOR.CTRL + 76 /*L*/, 'link' ],"
																									+ "[ CKEDITOR.CTRL + 66 /*B*/, 'bold' ],"
																									+ "[ CKEDITOR.CTRL + 73 /*I*/, 'italic' ],"
																									+ "[ CKEDITOR.CTRL + 83 /*S*/, 'save' ],"
																									+ "[ CKEDITOR.ALT + 109 /*-*/, 'toolbarCollapse' ]]";

	/**
	 * Basic constructor. NOTE: In order to have this component working on IE, the wicket application must set the
	 * javascript compressor to null. ex: this.getResourceSettings().setJavascriptCompressor(null);
	 * 
	 * @param id
	 *            component id.
	 */
	public CKEditorTextArea(final String id) {
		this(id, new Model<String>());
	}

	/**
	 * Basic constructor. NOTE: In order to have this component working on IE, the wicket application must set the
	 * javascript compressor to null. ex: this.getResourceSettings().setJavascriptCompressor(null);
	 * 
	 * @param id
	 *            component id.
	 * @param shouldCkInstanceRemoveInstance
	 *            If the ck instance should be removed when destroying wicket component. Normally should always be true,
	 *            but we saw that calling a js in onRemove function causes some loading problems in wicket. We saw that
	 *            this is working if the editor is used in repeaters but not if the editor is used in a normal way. That
	 *            is why default is false.
	 */
	public CKEditorTextArea(final String id, final boolean shouldCkInstanceRemoveInstance) {
		this(id, new Model<String>(), shouldCkInstanceRemoveInstance);
	}

	/**
	 * Basic constructor. NOTE: In order to have this component working on IE, the wicket application must set the
	 * javascript compressor to null. ex: this.getResourceSettings().setJavascriptCompressor(null);
	 * 
	 * @param id
	 *            the wicket component id.
	 * @param model
	 *            the wicket component model.
	 * @param shouldCkInstanceRemoveInstance
	 *            If the ck instance should be removed when destroying wicket component. Normally should always be true,
	 *            but we saw that calling a js in onRemove function causes some loading problems in wicket. We saw that
	 *            this is working if the editor is used in repeaters but not if the editor is used in a normal way. That
	 *            is why default is false.
	 */
	public CKEditorTextArea(final String id, final IModel<String> model, final boolean shouldCkInstanceRemoveInstance) {
		super(id, model);
		this.shouldCkInstanceRemoveInstance = shouldCkInstanceRemoveInstance;
		// CKEditorTextArea.this.add(JavascriptPackageResource.getHeaderContribution(new JavascriptResourceReference(
		// CKEditorTextArea.class, CKEditorTextArea.CKEDITOR_JS)));
		// CKEditorTextArea.this.add(JavascriptPackageResource.getHeaderContribution(new JavascriptResourceReference(
		// CKEditorTextArea.class, CKEditorTextArea.CKEDITOR_JQUERY_ADAPTER)));

		this.add(new CKEditorBehavior());
		this.behavior = new CustomAjaxFormComponentUpdatingBehavior("onblur");
		this.add(this.behavior);
		this.setOutputMarkupId(true);
	}

	/**
	 * Basic constructor. NOTE: In order to have this component working on IE, the wicket application must set the
	 * javascript compressor to null. ex: this.getResourceSettings().setJavascriptCompressor(null);
	 * 
	 * @param id
	 *            the wicket component id.
	 * @param model
	 *            the wicket component model.
	 */
	public CKEditorTextArea(final String id, final IModel<String> model) {
		this(id, model, false);
	}

	@Override
	protected void onRemove() {
		super.onRemove();
		if (this.shouldCkInstanceRemoveInstance) {
			final String jsStr = this.createDestroyMethod();
			CKEditorTextArea.this.getResponse().write(jsStr);
		}
	}

	/**
	 * @return the destroy method
	 */
	protected String createDestroyMethod() {
		final StringBuilder js = new StringBuilder("<script type=\"text/javascript\"><!--/*--><![CDATA[/*><!--*/\n");
		js.append("if(CKEDITOR && CKEDITOR.instances && CKEDITOR.instances.")
				.append(CKEditorTextArea.this.getMarkupId()).append(") {CKEDITOR.instances.")
				.append(CKEditorTextArea.this.getMarkupId()).append(".destroy(true);}");

		js.append("/*-->]]>*/</script>");
		final String jsStr = js.toString();
		return jsStr;
	}

	@Override
	public String getInputName() {
		return this.getMarkupId();
	}

	/**
	 * Behavior.
	 * 
	 * @author Tania Tritean, ISDC!
	 */
	private class CKEditorBehavior extends AbstractBehavior {

		/**
		 * Constructor.
		 */
		public CKEditorBehavior() {

		}

		/**
		 * Serial version unique identifier.
		 */
		private static final long	serialVersionUID	= 1256506850048083283L;

		@Override
		public void beforeRender(final Component component) {

			super.beforeRender(component);
			final String jsStr = CKEditorTextArea.this.createDestroyMethod();
			component.getResponse().write(jsStr);
		}

		@Override
		public void renderHead(final IHeaderResponse response) {
			response.renderJavascript("var CKEDITOR_BASEPATH = '/library/editor/ckeditor/';", "ckeditorpath");
			response.renderJavascriptReference("/library/editor/ckeditor/ckeditor.js", "ckeditor");
			response.renderJavascriptReference("/library/editor/ckeditor/adapters/jquery.js", "ckeditoradapter");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onRendered(final Component component) {
			if (CKEditorTextArea.this.isVisible()) {
				// CHECKSTYLE:OFF
				final StringBuilder callbackFunctionBuilder = new StringBuilder().append("function() {")
						.append("var ck_").append(CKEditorTextArea.this.getMarkupId()).append(" = ").append("$('#")
						.append(CKEditorTextArea.this.getMarkupId()).append("').ckeditorGet(); ");

				callbackFunctionBuilder.append("ck_").append(CKEditorTextArea.this.getMarkupId())
						.append(".postAjaxWicket = function() {")
						.append(CKEditorTextArea.this.behavior.getEventHandlerToCall()).append("}");

				callbackFunctionBuilder.append(";\n");

//				callbackFunctionBuilder.append("ck_").append(CKEditorTextArea.this.getMarkupId())
//						.append(".on('instanceReady', function(){ this.document.on('keyup', function(event){")
//						.append("ck_").append(CKEditorTextArea.this.getMarkupId()).append(".updateElement();}) });");

//				callbackFunctionBuilder
//						.append("ck_")
//						.append(CKEditorTextArea.this.getMarkupId())
//						.append(".on('saveSnapshot', function(event){"
//								+ "event.editor.execCommand( 'removeFormat', event.editor.selection); event.editor.updateElement();});");
//				callbackFunctionBuilder.append("ck_").append(CKEditorTextArea.this.getMarkupId())
//						.append(".on('afterUndo', function(event){event.editor.updateElement();});");
//				callbackFunctionBuilder.append("ck_").append(CKEditorTextArea.this.getMarkupId())
//						.append(".on('afterRedo', function(event){event.editor.updateElement();});");
				callbackFunctionBuilder.append("setMainFrameHeight( window.name )").append(";").append("}");

				final StringBuilder js = new StringBuilder("<script type=\"text/javascript\">");

				js.append("$('#").append(CKEditorTextArea.this.getMarkupId()).append("').ckeditor(")
						.append(callbackFunctionBuilder.toString()).append(CKEditorTextArea.COMMA_STRING)
						.append(CKEditorTextArea.this.editorConfig).append(");");

				js.append("</script>");
				// CHECKSTYLE:ON
				final String jsStr = js.toString();
				component.getResponse().write(jsStr);
			}
		}

	}

	/**
	 * Sets custom config for the CKEDITOR. The config must be passed as a key value map where the key stands for the
	 * desired configuration option and the value for the configuration value for that option.
	 * 
	 * @param config
	 *            the config map.
	 */
	public void setEditorConfig(final Map<String, String> config) {
		final StringBuilder configBuilder = new StringBuilder();
		configBuilder.append(CKEditorTextArea.LEFT_CURLY_BRACKET_STRING);
		configBuilder.append(CKEditorTextArea.NEW_LINE_STRING);
		for (final String key : config.keySet()) {
			configBuilder.append(key).append(CKEditorTextArea.PROPERTY_SEPARATOR).append(config.get(key))
					.append(CKEditorTextArea.COMMA_STRING);
			configBuilder.append(CKEditorTextArea.NEW_LINE_STRING);
		}

		if (config.get(CKEditorTextArea.SKIN) == null) {
			configBuilder.append(CKEditorTextArea.SKIN).append(CKEditorTextArea.PROPERTY_SEPARATOR)
					.append(CKEditorTextArea.SKIN_V2).append(CKEditorTextArea.COMMA_STRING);
			configBuilder.append(CKEditorTextArea.NEW_LINE_STRING);
		}

		final int lastCommaIndex = configBuilder.lastIndexOf(CKEditorTextArea.COMMA_STRING);
		configBuilder.replace(lastCommaIndex, lastCommaIndex + 1, "");
		configBuilder.append(CKEditorTextArea.NEW_LINE_STRING);
		configBuilder.append(CKEditorTextArea.RIGHT_CURLY_BRACKET_STRING);

		this.editorConfig = configBuilder.toString();
	}

	/**
	 * Sets the callback function for the CKEDITOR.
	 * 
	 * @param callbackFunctionParam
	 *            the callbackFunction to set
	 */
	public void setCallbackFunction(final String callbackFunctionParam) {
		final StringBuilder callbackFunctionBuilder = new StringBuilder();
		callbackFunctionBuilder.append("function() " + CKEditorTextArea.LEFT_CURLY_BRACKET_STRING);
		callbackFunctionBuilder.append(CKEditorTextArea.NEW_LINE_STRING);
		callbackFunctionBuilder.append(callbackFunctionParam);
		callbackFunctionBuilder.append(CKEditorTextArea.NEW_LINE_STRING);
		callbackFunctionBuilder.append(CKEditorTextArea.RIGHT_CURLY_BRACKET_STRING);

		this.callbackFunction = callbackFunctionBuilder.toString();
	}

	/**
	 * Gets the ajax decorator that can be used to update the all ckeditors on page.
	 * 
	 * @return decorator that updates all elements
	 */
	public static final AjaxCallDecorator getAjaxCallDecoratedToUpdateElementForAllEditorsOnPage() {

		return new AjaxCallDecorator() {

			/** Serial version id. */
			private static final long	serialVersionUID	= 1L;

			@Override
			public CharSequence decorateScript(final CharSequence script) {
				final StringBuilder js = new StringBuilder();

				js.append(CKEditorTextArea.DECORATOR_FUNCTION_UPDATE_ALL);

				return js.toString() + script;
			}
		};
	}

	/**
	 * Gets the ajax decorator that can be used to update the current element.
	 * 
	 * @return decorator that updates the element
	 */
	public AjaxCallDecorator getAjaxCallDecoratedToUpdateElement() {

		return new AjaxCallDecorator() {

			/** Serial version id. */
			private static final long	serialVersionUID	= 1L;

			@Override
			public CharSequence decorateScript(final CharSequence script) {
				final StringBuilder js = new StringBuilder();

				js.append("CKEDITOR.instances." + CKEditorTextArea.this.getMarkupId() + ".updateElement();");

				return js.toString() + script;
			}
		};
	}

	/**
	 * Behavior used to access the wicket port function.
	 * 
	 * @author Tania Tritean, ISDC!
	 */
	private class CustomAjaxFormComponentUpdatingBehavior extends AjaxFormComponentUpdatingBehavior {

		/**
		 * Constructor.
		 * 
		 * @param event
		 *            event
		 */
		public CustomAjaxFormComponentUpdatingBehavior(final String event) {
			super(event);
		}

		/** */
		private static final long	serialVersionUID	= 1L;

		@Override
		protected void onUpdate(final AjaxRequestTarget target) {

		}

		/**
		 * Used to return the ajax post function. To be called on update.
		 * 
		 * @return ajax post function.
		 */
		public CharSequence getEventHandlerToCall() {
			return this.getEventHandler();
		}

	};
	
	/**
	 * Behavior for updating all editors from page.
	 * 
	 * @author Tania Tritean, ISDC!
	 */
	public static class CKEditorAjaxSubmitModifier extends AttributeModifier {

		/**
		 */
		private static final long	serialVersionUID	= 1L;

		/**
		 * 
		 * Constructor.
		 */
		public CKEditorAjaxSubmitModifier() {
			super("onclick", true, new Model<String>(CKEditorTextArea.DECORATOR_FUNCTION_UPDATE_ALL));
		}
	};
}
