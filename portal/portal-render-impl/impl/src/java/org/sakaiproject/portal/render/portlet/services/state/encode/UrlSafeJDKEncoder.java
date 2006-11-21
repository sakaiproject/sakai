package org.sakaiproject.portal.render.portlet.services.state.encode;

import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

public class UrlSafeJDKEncoder implements UrlSafeEncoder {

    private static final String UTF8 = "UTF-8";

    public String encode(byte[] bits) {
        try {
            return URLEncoder.encode(new String(bits), UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UFT-8 is not supported? Should never happen.");
        }
    }

    public byte[] decode(String string) {
        try {
            return URLDecoder.decode(string, UTF8).getBytes();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UFT-8 is not supported? Should never happen.");
        }
    }
}
