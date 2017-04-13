/**
 * Copyright © 2005, CARET, University of Cambridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
/*
 * Created on 20 Aug 2006
 */
package org.sakaiproject.rsf.template;

import org.sakaiproject.portal.util.PortalUtils;
import uk.org.ponder.rsf.content.ContentTypeInfoRegistry;
import uk.org.ponder.rsf.template.ContentTypedTPI;
import uk.org.ponder.rsf.template.XMLLump;

import java.util.Map;

public class SakaiBodyTPI implements ContentTypedTPI {
	public static final String SAKAI_BODY = "sakai-body";
	public static final String SCRIPT = "script";
	public static final String SRC = "src";
	public static final String LINK = "link";
	public static final String HREF = "href";
	public static final String QUESTION_MARK = "?";

	public void adjustAttributes(String tag, Map attributes) {
		if (tag.equals("body") && attributes.get(XMLLump.ID_ATTRIBUTE) == null) {
			attributes.put(XMLLump.ID_ATTRIBUTE, XMLLump.SCR_PREFIX + SAKAI_BODY);
		}
		else if (tag.equals(SCRIPT) && attributes.get(SRC)!=null && !((String)attributes.get(SRC)).contains(QUESTION_MARK)) {
			attributes.put(SRC, attributes.get(SRC) + PortalUtils.getCDNQuery());
		}
		else if (tag.equals(LINK) && attributes.get(HREF)!=null  && !((String)attributes.get(HREF)).contains(QUESTION_MARK)) {
			attributes.put(HREF, attributes.get(HREF) + PortalUtils.getCDNQuery());
		}
	}

	public String[] getInterceptedContentTypes() {
		return new String[] { ContentTypeInfoRegistry.HTML, ContentTypeInfoRegistry.HTML_FRAGMENT };
	}
}
