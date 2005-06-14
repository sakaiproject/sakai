package test.org.sakaiproject.tool.assessment.ui.listener;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: TestActionListener.java,v 1.3 2005/05/31 19:14:29 janderse.umich.edu Exp $
 */

public class TestActionListener implements ActionListener
{
  public TestActionListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    System.out.println("debugging ActionEvent: " + ae);
    System.out.println("debug requestParams: " + requestParams);
    System.out.println("debug reqMap: " + reqMap);

  }

}
