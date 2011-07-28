/*
 * Created on 20 Aug 2006
 */
package uk.ac.cam.caret.sakai.rsf.template;

import java.util.Map;

import uk.org.ponder.rsf.content.ContentTypeInfoRegistry;
import uk.org.ponder.rsf.template.ContentTypedTPI;
import uk.org.ponder.rsf.template.XMLLump;

public class SakaiBodyTPI implements ContentTypedTPI {
  public static final String SAKAI_BODY = "sakai-body";
  public void adjustAttributes(String tag, Map attributes) {
    if (tag.equals("body") && attributes.get(XMLLump.ID_ATTRIBUTE) == null) {
      attributes.put(XMLLump.ID_ATTRIBUTE, XMLLump.SCR_PREFIX + SAKAI_BODY);
    }
  }

  public String[] getInterceptedContentTypes() {
    return new String[] {
        ContentTypeInfoRegistry.HTML, ContentTypeInfoRegistry.HTML_FRAGMENT
    };
  }
}
