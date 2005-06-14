/*
 * Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */



package org.sakaiproject.tool.assessment.util;


/**
 * <p>Title: NavigoProject.org</p>
 * <p>Description: OKI based implementation</p>
 * <p>Copyright: Copyright 2003 Trustees of Indiana University</p>
 * <p>Company: </p>
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id: Iso8601FormatException.java,v 1.1 2005/01/14 01:34:51 esmiley.stanford.edu Exp $
 */
public class Iso8601FormatException
  extends FormatException
{
  /**
   * Creates a new Iso8601FormatException object.
   *
   * @param message DOCUMENTATION PENDING
   */
  public Iso8601FormatException(String message)
  {
    super(message);
  }

  /**
   * Creates a new Iso8601FormatException object.
   *
   * @param message DOCUMENTATION PENDING
   * @param cause DOCUMENTATION PENDING
   */
  public Iso8601FormatException(String message, Throwable cause)
  {
    super(message, cause);
  }

  /**
   * Creates a new Iso8601FormatException object.
   *
   * @param cause DOCUMENTATION PENDING
   */
  public Iso8601FormatException(Throwable cause)
  {
    super(cause);
  }
}
