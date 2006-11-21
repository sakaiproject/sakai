package org.sakaiproject.portal.render.portlet.services.state.encode;

public interface UrlSafeEncoder {

    String encode(byte[] bits);

    byte[] decode(String string);
}
