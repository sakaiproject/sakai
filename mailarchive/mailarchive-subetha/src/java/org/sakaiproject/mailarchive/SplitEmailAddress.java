package org.sakaiproject.mailarchive;

/**
 * Just a tuple for a split email address.
 */
public class SplitEmailAddress {

    private final String local;
    private final String domain;

    public SplitEmailAddress(String local, String domain) {
        if (local == null || domain == null)
            throw new IllegalArgumentException("No null arguments allowed");
        this.local = local;
        this.domain = domain;
    }

    public String getLocal() {
        return local;
    }

    public String getDomain() {
        return domain;
    }

    public static SplitEmailAddress parse(String address) {
        int atPos = address.indexOf('@');
        if (atPos < 1 || atPos == address.length() -1) {
            throw new IllegalArgumentException("Can't find @ or it's at the start or end.");
        }
        return new SplitEmailAddress(
                address.substring(0, atPos),
                address.substring(atPos+1).toLowerCase()
        );
    }
}
