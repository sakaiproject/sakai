package org.sakaiproject.mailarchive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Just a tuple for a split email address, but it also remove any BATV checking from local part.
 */
public class SplitEmailAddress {

    /**
     * Pattern to get the local part out of a BATV modified from address.
     * @see <a href="https://tools.ietf.org/html/draft-levine-smtp-batv-01">BATV RFC</a>
     */
    private static final Pattern BATV_RFC = Pattern.compile("prvs=\\d\\d{3}[0-9A-F]{6}=(?<localpart>.+)",
            Pattern.CASE_INSENSITIVE);
    /**
     * Pattern for Sub-Address syntax.
     * @see <a href="https://www.agwa.name/projects/batv-tools/">Sub Addressing BATV</a>
     */
    private static final Pattern BATV_SUB_ADDRESS = Pattern.compile("(?<localpart>.+)\\+prvs=\\d\\d{3}[0-9A-F]{6}",
            Pattern.CASE_INSENSITIVE);

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
        String local = address.substring(0, atPos);
        Matcher rfcMatcher = BATV_RFC.matcher(local);
        if (rfcMatcher.matches()) {
            local = rfcMatcher.group("localpart");
        } else {
            Matcher subMatcher = BATV_SUB_ADDRESS.matcher(local);
            if (subMatcher.matches()) {
                local = subMatcher.group("localpart");
            }
        }
        return new SplitEmailAddress(
                local,
                address.substring(atPos+1).toLowerCase()
        );
    }
}
