package org.sakaiproject.profile2.tool.components;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.IAutoCompleteRenderer;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.StringAutoCompleteRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auto-Complete text field that allows capture of choice selections (rather than just strings). Replacement for
 * {@link org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField}.
 * 
 * @author whoover
 * @param <CHOICE> the choice model object type
 */
public abstract class AbstractAutoCompleteTextField<CHOICE> extends TextField {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractAutoCompleteTextField.class);
	private static final long serialVersionUID = 1L;
	private final AutoCompleteChoiceBehavior autoCompleteBehavior;
	private transient List<CHOICE> choiceList;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param type
	 *            the type to set
	 */
	public AbstractAutoCompleteTextField(final String id, final Class<?> type) {
		this(id, (IModel) null, type, false);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param model
	 *            the model to set
	 * @param type
	 *            the type to set
	 * @param preselect
	 *            the preselect to set
	 */
	public AbstractAutoCompleteTextField(final String id, final IModel model, final Class<?> type, final boolean preselect) {
		this(id, model, type, StringAutoCompleteRenderer.INSTANCE, preselect);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param model
	 *            the model to set
	 * @param type
	 *            the type to set
	 * @param settings
	 *            the settings to set
	 */
	public AbstractAutoCompleteTextField(final String id, final IModel model, final Class<?> type, final AutoCompleteSettings settings) {
		this(id, model, type, StringAutoCompleteRenderer.INSTANCE, settings);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param model
	 *            the model to set
	 * @param preselect
	 *            the preselect to set
	 */
	public AbstractAutoCompleteTextField(final String id, final IModel model, final boolean preselect) {
		this(id, model, (Class<?>) null, preselect);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param model
	 *            the model to set
	 * @param settings
	 *            the settings to set
	 */
	public AbstractAutoCompleteTextField(final String id, final IModel model, final AutoCompleteSettings settings) {
		this(id, model, (Class<?>) null, settings);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param model
	 *            the model to set
	 */
	public AbstractAutoCompleteTextField(final String id, final IModel model) {
		this(id, model, (Class<?>) null, false);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param preselect
	 *            the preselect to set
	 */
	public AbstractAutoCompleteTextField(final String id, final boolean preselect) {
		this(id, (IModel) null, preselect);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param settings
	 *            the settings to set
	 */
	public AbstractAutoCompleteTextField(final String id, final AutoCompleteSettings settings) {
		this(id, (IModel) null, settings);

	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 */
	public AbstractAutoCompleteTextField(final String id) {
		this(id, (IModel) null, false);

	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param renderer
	 *            the renderer to set
	 */
	public AbstractAutoCompleteTextField(final String id, final IAutoCompleteRenderer renderer) {
		this(id, (IModel) null, renderer);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param type
	 *            the type to set
	 * @param renderer
	 *            the renderer to set
	 */
	public AbstractAutoCompleteTextField(final String id, final Class<?> type, final IAutoCompleteRenderer renderer) {
		this(id, null, type, renderer, false);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param model
	 *            the model to set
	 * @param renderer
	 *            the renderer to set
	 */
	public AbstractAutoCompleteTextField(final String id, final IModel model, final IAutoCompleteRenderer renderer) {
		this(id, model, (Class<?>) null, renderer, false);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID to set
	 * @param model
	 *            the model to set
	 * @param type
	 *            the type to set
	 * @param renderer
	 *            the renderer to set
	 * @param preselect
	 *            the preselect to set
	 */
	public AbstractAutoCompleteTextField(final String id, final IModel model, final Class<?> type, final IAutoCompleteRenderer renderer, final boolean preselect) {
		this(id, model, type, renderer, new AutoCompleteSettings().setPreselect(preselect));
	}

	/**
	 * Constructor that
	 * 
	 * @param id
	 *            the ID to set
	 * @param model
	 *            the model to set
	 * @param type
	 *            the type to set
	 * @param renderer
	 *            the renderer to set
	 * @param settings
	 *            the settings to set
	 */
	public AbstractAutoCompleteTextField(final String id, final IModel model, final Class<?> type, final IAutoCompleteRenderer renderer, final AutoCompleteSettings settings) {
		super(id, model);
		setType(type);

		add(new SimpleAttributeModifier("autocomplete", "off"));

		autoCompleteBehavior = createAutoCompleteBehavior(renderer, settings);
		if (autoCompleteBehavior == null) {
			throw new NullPointerException("Auto complete behavior cannot be null");
		}
		add(autoCompleteBehavior);
	}

	/**
	 * Gets the auto-complete behavior
	 * 
	 * @param renderer
	 *            the renderer to set
	 * @param settings
	 *            the settings to set
	 * @return the auto-complete behavior
	 */
	protected AutoCompleteChoiceBehavior createAutoCompleteBehavior(final IAutoCompleteRenderer renderer, final AutoCompleteSettings settings) {
		return new AutoCompleteChoiceBehavior(renderer, settings);
	}

	/**
	 * @return the autoCompleteBehavior
	 */
	public final AutoCompleteChoiceBehavior getAutoCompleteBehavior() {
		return autoCompleteBehavior;
	}

	/**
	 * Call-back method that should return an iterator over all possible assist choice objects. These objects will be passed to the renderer to generate output. Usually it is enough to return an
	 * iterator over strings.
	 * 
	 * @see org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteBehavior#getChoices(String)
	 * @param searchTextInput
	 *            current input
	 * @return iterator over all possible choice objects
	 */
	private final Iterator<CHOICE> getChoices(final String searchTextInput) {
		// find list of items to display in auto-complete (we need to cache the list because the auto-complete only deals with strings)
		choiceList = getChoiceList(searchTextInput);
		return choiceList.iterator();
	}

	/**
	 * Call-back method that should return a list of all possible assist choice objects. These objects will be passed to the renderer to generate output.
	 * 
	 * @param searchTextInput
	 *            current input text
	 * @return iterator over all possible choice objects
	 */
	protected abstract List<CHOICE> getChoiceList(final String searchTextInput);

	/**
	 * Gets the string value from the specified choice
	 * 
	 * @param choice
	 *            the choice that needs value extraction
	 * @return the unique string value of the choice
	 * @throws Throwable
	 *             any error that may occur during choice extraction
	 */
	protected abstract String getChoiceValue(final CHOICE choice) throws Throwable;

	/**
	 * Finds the selection by cycling through the current choices and matching the choices value. If the selected choice is found the choices will be reset and the choice will be returned.
	 * <p>
	 * <b>NOTE:</b> Assumes that the selection choice values are unique
	 * </p>
	 * 
	 * @return the found selection model by name (null if it cannot be found)
	 */
	public final CHOICE findChoice() {
		try {
			for (final CHOICE choice : choiceList) {
				if (getConvertedInput().equals(getChoiceValue(choice))) {
					return choice;
				}
			}
			LOG.info("Unable to find choice selection for \"{}\"", getModelObject());
		} catch (final Throwable e) {
			LOG.error("Unable to find choice selection", e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onAfterRender() {
		super.onAfterRender();
		// clear choices cache
		choiceList = null;
	}

	/**
	 * Auto-complete behavior that implements the choice iteration
	 * 
	 * @author whoover
	 */
	public class AutoCompleteChoiceBehavior extends AutoCompleteBehavior {

		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * 
		 * @param renderer
		 *            renderer that will be used to generate output
		 * @param settings
		 *            settings for the auto-complete list
		 */
		public AutoCompleteChoiceBehavior(final IAutoCompleteRenderer renderer, final AutoCompleteSettings settings) {
			super(renderer, settings);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected final Iterator<?> getChoices(final String input) {
			return AbstractAutoCompleteTextField.this.getChoices(input);
		}
	}
}
