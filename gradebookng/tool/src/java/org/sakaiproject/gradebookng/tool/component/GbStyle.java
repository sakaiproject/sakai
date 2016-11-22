
package org.sakaiproject.gradebookng.tool.component;

/**
 * A list of styles that get turned into CSS classes
 */
public enum GbStyle {

	GB_ITEM("gb_item"),
	COMMENT("comment"),
	EXTERNAL("external"),
	NO_CHANGES("no_changes"),
	SELECTED("selected");

	private String css;

	GbStyle(final String css) {
		this.css = css;
	}

	public String getCss() {
		return this.css;
	}

}

