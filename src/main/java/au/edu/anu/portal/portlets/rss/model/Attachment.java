/**
 * Copyright 2011-2013 The Australian National University
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
