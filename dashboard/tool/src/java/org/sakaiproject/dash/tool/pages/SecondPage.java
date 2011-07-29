package org.sakaiproject.dash.tool.pages;

import org.apache.wicket.markup.html.link.Link;

/**
 * An example page
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class SecondPage extends BasePage {

	Link<Void> toThirdPageLink;
	
	public SecondPage() {
		disableLink(secondLink);
		
		
		//link to third page
		//the i18n label for this is directly in the HTML
		toThirdPageLink = new Link<Void>("toThirdPageLink") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				setResponsePage(new ThirdPage());
			}
		};
		add(toThirdPageLink);

		
		
	}
}
