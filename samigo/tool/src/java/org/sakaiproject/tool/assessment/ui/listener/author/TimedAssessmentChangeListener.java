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
