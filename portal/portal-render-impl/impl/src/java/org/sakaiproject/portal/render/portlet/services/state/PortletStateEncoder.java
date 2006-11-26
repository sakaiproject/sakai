package org.sakaiproject.portal.render.portlet.services.state;

import org.sakaiproject.portal.render.portlet.services.state.PortletState;

/**
 * Encodes PortletState into a serialized form.
 *
 * @since Sakai 2.2.4
 * @version $Rev$
 *
 */
public interface PortletStateEncoder {

    /**
     * Encode the PortletState into a string
     * @param portletState the current portlet state
     * @return serialized form of the state.
     */
    String encode(PortletState portletState);

    /**
     * Encode decode the PortletState from the
     * serialized form.
     * @param serialized
     * @return materialized portlet state.
     */
    PortletState decode(String serialized);
}
