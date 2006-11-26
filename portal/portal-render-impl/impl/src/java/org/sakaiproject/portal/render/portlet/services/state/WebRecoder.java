package org.sakaiproject.portal.render.portlet.services.state;

/**
 * An encoder used to recode an allready encoded set of bytes
 * in order to ensure the bits can be used in a url.
 *
 * @since Sakai 2.2.4
 * @version $Rev$
 *
 */
public interface WebRecoder {

    /**
     * Recode the bits into a websafe version.
     * @param bits the original bits
     * @return websafe version
     */
    String encode(byte[] bits);

    /**
     * Decode the bits into a websafe version.
     * @param encoded websafe version
     * @return the original encoded bits
     */
    byte[] decode(String encoded);
}
