package org.sakaiproject.site.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Helper for managing groups in a site.
 *
 * @author Matthew Buckett
 */
public class SiteGroupHelper {

	// The separator when packing ids into a string.
	static final char SEPARATOR = ',';
	// The character we use to escape strings.
	static final char ESCAPE = '\\';

	// String versions to simplify code.
	static final String ESCAPE_STR = Character.toString(ESCAPE);
	static final String SEPARATOR_STR = Character.toString(SEPARATOR);

	// Force this class to be a helper.
	private SiteGroupHelper() {
	}

	/**
	 * This unpacks IDs from a string. It supports empty IDs but not <code>null</code>.
	 *
	 * @param ids The packed IDs in a string, can be <code>null</code>.
	 * @return A collection of IDs unpacked from the string.
	 * @see #pack(java.util.Collection)
	 */
	public static Collection<String> unpack(String ids) {
		Collection<String> unpacked = new ArrayList<String>();
		if (ids != null) {
			StringBuilder id = new StringBuilder();
			boolean inEscape = false;
			for (int i = 0; i < ids.length(); i++) {
				if (inEscape) {
					// We could check that it's a ESCAPE or SEPERATOR here, but we can just be lax.
					id.append(ids.charAt(i));
					inEscape = false;
				} else {
					switch (ids.charAt(i)) {
						case ESCAPE:
							inEscape = true;
							break;
						case SEPARATOR:
							unpacked.add(id.toString());
							id = new StringBuilder();
							break;
						default:
							id.append(ids.charAt(i));
					}
				}
			}
			unpacked.add(id.toString());
		}
		return unpacked;
	}

	/**
	 * This packs IDs into a string. It supports empty IDs but not <code>null</code>.
	 *
	 * @param ids A Collection of IDs to be packed together.
	 * @return The packed string containing all the IDs or <code>null</code> if the original collection was
	 * <code>null</code>.
	 * @see #unpack(String)
	 */
	public static String pack(Collection<String> ids) {
		String packed = null;
		if (ids != null) {
			StringBuilder builder = new StringBuilder();
			String separator = "";
			for (String id : ids) {
				builder.append(separator);
				separator = SEPARATOR_STR; // Actually set it up correctly.
				builder.append(id
						.replace(ESCAPE_STR, ESCAPE_STR + ESCAPE_STR)
						.replace(SEPARATOR_STR, ESCAPE_STR + SEPARATOR_STR));
			}
			packed = builder.toString();
		}
		return packed;
	}
}
