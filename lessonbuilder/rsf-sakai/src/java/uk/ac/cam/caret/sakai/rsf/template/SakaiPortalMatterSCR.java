/*
 * Created on 14 Dec 2006
 */
package uk.ac.cam.caret.sakai.rsf.template;


import uk.org.ponder.rsf.renderer.ComponentRenderer;
import uk.org.ponder.rsf.renderer.RenderUtil;
import uk.org.ponder.rsf.renderer.TagRenderContext;
import uk.org.ponder.rsf.renderer.scr.BasicSCR;
import uk.org.ponder.rsf.renderer.scr.NullRewriteSCR;

public class SakaiPortalMatterSCR implements BasicSCR {
  private String headmatter;

  public String getName() {
    return "portal-matter";
  }

  public void setHeadMatter(String headmatter) {
    this.headmatter = headmatter;
  }
  
  public int render(TagRenderContext trc) {
    if (headmatter == null) {
      return NullRewriteSCR.instance.render(trc);
    }
    else {
      if (RenderUtil.isFirstSCR(trc.uselump, getName())) {
        trc.xmlw.writeRaw(headmatter);
      }
      return ComponentRenderer.LEAF_TAG;
    }
  }

}
