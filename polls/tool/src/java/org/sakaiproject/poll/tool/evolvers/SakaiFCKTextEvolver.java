/*
 * Created on 22 Sep 2006
 */
package org.sakaiproject.poll.tool.evolvers;

import org.sakaiproject.content.api.ContentHostingService;

import uk.org.ponder.htmlutil.HTMLUtil;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;

public class SakaiFCKTextEvolver implements TextInputEvolver {
  public static final String COMPONENT_ID = "sakai-FCKEditor:";
  private String context;
  private ContentHostingService contentHostingService;

  public void setContext(String context) {
    this.context = context;
  }

  public void setContentHostingService(ContentHostingService contentHostingService) {
    this.contentHostingService = contentHostingService;
  }
  
  public UIJointContainer evolveTextInput(UIInput toevolve) {
    toevolve.parent.remove(toevolve);
    UIJointContainer joint = new UIJointContainer(toevolve.parent,
        toevolve.ID, COMPONENT_ID);

    toevolve.ID = SEED_ID; // must change ID while unattached
    joint.addComponent(toevolve);
    String collectionID = "".equals(context)? "" :
        contentHostingService.getSiteCollection(context);
    String js = HTMLUtil.emitJavascriptCall("SakaiProject.fckeditor.initializeEditor", 
        new String[] {toevolve.getFullID(), collectionID});
    UIVerbatim.make(joint, "textarea-js", js);
    return joint;
  }


}
