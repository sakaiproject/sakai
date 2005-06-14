package org.sakaiproject.tool.assessment.facade;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DataFacadeException extends RuntimeException{
  private String message;
  public DataFacadeException() {
  }

  public DataFacadeException(String message) {
    this.message = message;
  }
}