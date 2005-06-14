package org.sakaiproject.tool.assessment.facade;
import java.io.Serializable;

/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class GradebookFacade implements Serializable {

  //return Gradebook #10
  public static String getGradebookUId(){
    return "QA_8";
  }

  // return Gradebook #1
  public static String getDefaultGradebookUId(){
    return "QA_1";
  }

 }
