package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.component.UIComponent;

import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * <p>Organization: Sakai Project</p>
 * @author Chen Wen
 */

public class TimedAssessmentChangeListener implements ValueChangeListener 
{
  public TimedAssessmentChangeListener() 
  {
  }

  public void processValueChange(ValueChangeEvent ae)
	{
	    
	    //fix bug SAK-4439
            if(ae.getOldValue()==null && ae.getNewValue()!=null && ae.getNewValue().equals("false"))
		return;
	    if(ae.getOldValue()!=null && ae.getNewValue()!=null && ae.getOldValue().equals(ae.getNewValue()))
		return;

	    //  if(((Boolean)ae.getNewValue()).booleanValue() == false && ae.getOldValue() == null)
	    //  return;
	    //else if(((Boolean)ae.getNewValue()).booleanValue() == ((Boolean)ae.getOldValue()).booleanValue())
	    //return;
    
  	UIComponent sourceComp = (UIComponent)ae.getSource();
  	UIComponent hideDivComp = null;
  	while(sourceComp.getParent() != null)
  	{
  		hideDivComp = sourceComp.getParent();
  		if(hideDivComp.getRendererType().equalsIgnoreCase("HideDivision"))
  		{
  			break;
  		}
  		else
  		{
  			sourceComp = sourceComp.getParent();
  		}
  	}
  	if(hideDivComp != null)
  	{
  		String hideDivId = hideDivComp.getId(); 
      ToolSession session = SessionManager.getCurrentToolSession();
      session.setAttribute("sam_expande_hide_div_id", hideDivId);
  	}
  	
  	return;
  }
}
