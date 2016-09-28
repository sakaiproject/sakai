package org.sakaiproject.gradebookng.tool.component;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * Wrapper for a WebMarkupContainer that can have CSS styles added to it which then get output when the {@link #style()} method is called.
 */
public class GbStyleableWebMarkupContainer extends WebMarkupContainer {

	private static final long serialVersionUID = 1L;

	private final Set<GbStyle> styles;

	public GbStyleableWebMarkupContainer(final String id) {
		super(id);
		this.styles = new TreeSet<>();
		setOutputMarkupId(true);
	}

	/**
	 * Add a {@link GbStyle} to the list (only if not already present)
	 * @param style
	 */
	public void addStyle(final GbStyle style) {
		this.styles.add(style);
	}

	/**
	 * Remove a {@link GbStyle} from the list. Does nothing if it didn't exist.
	 * @param style
	 */
	public void removeStyle(final GbStyle style) {
		this.styles.remove(style);
	}

	/**
	 * Output the styles for the component
	 */
	public void style() {

		final ArrayList<String> cssClasses = new ArrayList<>();
		if (!this.styles.isEmpty()) {
			this.styles.forEach(s -> {
				cssClasses.add(s.getCss());
			});
		}

		// replace the cell styles with the new set
		this.add(AttributeModifier.replace("class", StringUtils.join(cssClasses, " ")));
	}
}
