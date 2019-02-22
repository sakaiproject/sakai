/**
 * Copyright (c) 2019 The Apereo Foundation
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
package org.sakaiproject.wicket.markup.html.navigation;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

/**
 * The purpose of this class (and associated markup and properties) is to encapsulate the nav bar 'tabs' into
 * a single, reusable component.
 *
 * Prior to Wicket 7, we were relying on the fact that {@link org.apache.wicket.markup.html.link.AbstractLink}
 * changed it's own markup when being disabled, such that the anchor tag would be replaced with a span. This
 * functionality was removed in Wicket 7, and backwards compatibility was provided through the
 * {@link org.apache.wicket.markup.html.link.DisabledLinkBehavior} convenience class. However, it was subsequently
 * deprecated in a more recent version of Wicket 7, and was removed entirely in Wicket 8 (who's docs mention this
 * markup replacement behaviour is no longer a good practice, and recommends another approach be adopted/developed
 * as needed).
 *
 * As such, this class was created to encapsulate both the 'active' link, and an 'inactive' span. On selection
 * of the link, the visibility and screen reader classes on each are swapped, creating the same visual appearance
 * of selected and no selected tabs we had before, while preserving the onClick functionality and the appropriate
 * screen-reader attributes, etc.
 *
 * @author bjones86
 */
public class NavLink extends Panel
{

    private static final long serialVersionUID = 1L;

    Link<Void> link;
    WebMarkupContainer disabledLink;

    /**
     * Constructor.
     *
     * @param id           the wicket id specific in HTML markup
     * @param responsePage the page to forward to when the tab is clicked
     * @param isVisible    if the tab is visible to the user
     * @param linkText     the text shown on the tab (model)
     * @param toolTipText  the text shown in the tool-tip on hover of the tab (model)
     */
    public NavLink( String id, Class responsePage, boolean isVisible, IModel linkText, IModel toolTipText )
    {
        super( id );

        link = new Link<Void>( "link" )
        {
            @Override
            public void onClick()
            {
                setResponsePage( responsePage );
            }
        };
        link.add( new Label( "linkText", linkText ) );
        link.add( new AttributeAppender( "title", toolTipText ) );
        link.add( new Label( "screenReaderLabel", new ResourceModel( "link.screenreader.tabnotselected" ) ) );
        link.setVisible( isVisible );
        add( link );

        disabledLink = new WebMarkupContainer( "disabledLink" );
        disabledLink.add( new Label( "linkText", linkText ) );
        disabledLink.add( new AttributeAppender( "title", toolTipText ) );
        disabledLink.add( new Label( "screenReaderLabel", new ResourceModel( "link.screenreader.tabselected" ) ) );
        disabledLink.setVisible( false );
        add( disabledLink );
    }

    /**
     * Convenience method to 'disable' the active tab (called by the page instantiated by the link).
     * Performs the following:
     *
     * 1) Set's the anchor's visibility to false
     * 2) Apply the "skip" class to the anchor so screen-readers don't see it
     * 3) Set the "disabled link" span's visibility to true
     * 4) Replace all the "skip" class on the disabled link span with the "current" class to apply the "current tab" styling
     */
    public final void disable()
    {
        link.setVisible( false );
        link.add( new AttributeAppender( "class", "skip" ) );

        disabledLink.setVisible( true );
        disabledLink.add( new AttributeModifier( "class", "current" ) );
    }
}
