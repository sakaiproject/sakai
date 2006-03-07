/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://www.opensource.org/licenses/ecl1.php
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.service.gradebook.shared;

/**
 * An exception thrown by the gradebook application.  If a gradebook
 * client does not want to handle each of the gradebook's specific exceptions
 * individually, it can simply deal with GradebookException to handle all possible
 * exceptions.
 *
 * These were changed to runtime exceptions after the 2.1 release to make
 * it easier to throw them while using Hibernate. By default, Spring will
 * mark a transaction as rollback-only when a runtime exception is thrown
 * by a proxied method but will leave the transaction alone when a checked
 * exception is thrown. To preserve the original transaction-preserving
 * behavior, this exception class is explicitly called out in the Spring
 * TransactionProxyFactoryBean configuration file using the following syntax:
 *
 * <prop key="create*">PROPAGATION_REQUIRED,+org.sakaiproject.service.gradebook.shared.GradebookException</prop>
 */
public class GradebookException extends RuntimeException {
    protected GradebookException(String message) {
        super(message);
    }
    protected GradebookException(Throwable t) {
        super(t);
    }
}



