package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import java.lang.String;

import uk.org.ponder.rsf.renderer.MessageRenderer;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.stringutil.StringList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;

public class GradebookMessageRenderer extends MessageRenderer{
	private String infoStyleClass;
	public void setInfoStyleClass(String styleClass) {
		this.infoStyleClass = styleClass;
	}
	private String errorStyleClass;
	public void setErrorStyleClass(String styleClass) {
		this.errorStyleClass = styleClass;
	}
	
  private MessageLocator messagelocator;
  public void setMessageLocator(MessageLocator messagelocator) {
    this.messagelocator = messagelocator;
  }
  
  public UIMessage renderMessage(String key) {
    UIMessage togo = UIMessage.make(key);
    togo.setValue(messagelocator.getMessage(togo.messagekeys,
        togo.arguments));
    return togo;
  }
  
  public UIBranchContainer renderMessageList(TargettedMessageList messagelist) {
    UIBranchContainer togo = new UIBranchContainer();
    StringList renderered = messagelist == null? new StringList() : 
      messagelist.render(messagelocator);
    for (int i = 0; i < renderered.size(); ++ i) {
      UIOutput output = UIOutput.make(togo, "message:", renderered.stringAt(i));
      if (messagelist.messageAt(i) != null){
    	  TargettedMessage message = messagelist.messageAt(i);
    	  if (message.severity == TargettedMessage.SEVERITY_INFO){
    		  output.decorate(new UIStyleDecorator(infoStyleClass));
    	  } else {
    		  output.decorate(new UIStyleDecorator(errorStyleClass));
    	  }
      }
    }
    return togo;
  }
}
