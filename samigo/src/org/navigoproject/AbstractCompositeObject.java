/*
 *                       Navigo Software License
 *
 * Copyright 2003, Trustees of Indiana University, The Regents of the University
 * of Michigan, and Stanford University, all rights reserved.
 *
 * This work, including software, documents, or other related items (the
 * "Software"), is being provided by the copyright holder(s) subject to the
 * terms of the Navigo Software License. By obtaining, using and/or copying this
 * Software, you agree that you have read, understand, and will comply with the
 * following terms and conditions of the Navigo Software License:
 *
 * Permission to use, copy, modify, and distribute this Software and its
 * documentation, with or without modification, for any purpose and without fee
 * or royalty is hereby granted, provided that you include the following on ALL
 * copies of the Software or portions thereof, including modifications or
 * derivatives, that you make:
 *
 *    The full text of the Navigo Software License in a location viewable to
 *    users of the redistributed or derivative work.
 *
 *    Any pre-existing intellectual property disclaimers, notices, or terms and
 *    conditions. If none exist, a short notice similar to the following should
 *    be used within the body of any redistributed or derivative Software:
 *    "Copyright 2003, Trustees of Indiana University, The Regents of the
 *    University of Michigan and Stanford University, all rights reserved."
 *
 *    Notice of any changes or modifications to the Navigo Software, including
 *    the date the changes were made.
 *
 *    Any modified software must be distributed in such as manner as to avoid
 *    any confusion with the original Navigo Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * The name and trademarks of copyright holder(s) and/or Indiana University,
 * The University of Michigan, Stanford University, or Navigo may NOT be used
 * in advertising or publicity pertaining to the Software without specific,
 * written prior permission. Title to copyright in the Software and any
 * associated documentation will at all times remain with the copyright holders.
 * The export of software employing encryption technology may require a specific
 * license from the United States Government. It is the responsibility of any
 * person or organization contemplating export to obtain such a license before
 * exporting this Software.
 */

package org.navigoproject;


/**
 * <p>
 * This abstract class is meant to be used in cases where a meaningful
 * <code>toString()</code> method is desired, but the author does not want to
 * code the method.  Please be aware that this implementation does use
 * <code>Refelection</code> to obtain field names and values.
 * </p>
 *
 * <p>
 * Authors should override the <code>printData(StringBuffer
 * mutableBuffer)</code> method to alter the default behavior. Format is
 * <pre>fieldName=fieldValue, fieldName=fieldValue, fieldName=fieldValue, etc.</pre>
 * </p>
 *
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id: AbstractCompositeObject.java,v 1.1.1.1 2004/07/28 21:32:06 rgollub.stanford.edu Exp $
 */
public abstract class AbstractCompositeObject
  implements CompositeObject
{
  private static final org.apache.log4j.Logger LOG =
    org.apache.log4j.Logger.getLogger(AbstractCompositeObject.class);

  /**
   * DOCUMENTATION PENDING
   *
   * @return DOCUMENTATION PENDING
   */
  public String toString()
  {
    LOG.debug("toString()");

    return CompositeObjectUtil.printToString(this);
  }

  //  public void printData(StringBuffer mutableBuffer)
  //  {
  //    if(LOG.isDebugEnabled())
  //    {
  //      LOG.debug("printData(StringBuffer " + mutableBuffer + ")");
  //    }
  //    CompositeObjectUtil.printData(mutableBuffer, this);
  //  }
}
