package au.edu.anu.portal.portlets.rss.model;

import lombok.Data;

/**
 * Replacement for SyndEnclosure, used for attachments so we can specify a bunch of display properties
 * of the item in addition to the rest of the properties of an Enclosure
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */

@Data
public class Attachment  {

	private String url;
	private String displayName;
	private String type;
	private String displayLength;
	private boolean image;
}
